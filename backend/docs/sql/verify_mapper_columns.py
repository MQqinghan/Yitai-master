# -*- coding: utf-8 -*-
"""
verify_mapper_columns.py —— 全量校验 mapper XML 的 SQL 与 yitai_mall 库实际列结构是否匹配。

原理：用 ElementTree 解析每个 mapper XML，把 <select>/<insert>/<update>/<delete> 的内容
结构化还原成"全条件展开"的 SQL（<if>/<choose> 剥壳留内容，<set>/<where>/<trim>/foreach
的 open/close 还原关键字与括号，<include> 就地展开，#{} 占位替换为 1），
然后送进 MySQL EXPLAIN（只做语法/列解析，不执行）。Unknown column 即表结构不匹配。

用法：python verify_mapper_columns.py
依赖：docker 容器 yitai-mysql（mysql:8.0），库 yitai_mall，root/root。
"""
import re
import glob
import subprocess
import sys
import xml.etree.ElementTree as ET

BASE = r"D:\xiangmu\Yitai\Yitai-master\backend"
DB = "yitai_mall"
MYSQL_CMD = ["docker", "exec", "yitai-mysql", "mysql", "-uroot", "-proot", "-N", "-D", DB]


def db_schema():
    out = subprocess.run(
        MYSQL_CMD + ["-e",
        f"SELECT table_name, column_name FROM information_schema.columns "
        f"WHERE table_schema='{DB}'"],
        capture_output=True, text=True).stdout
    schema = {}
    for line in out.splitlines():
        parts = line.split("\t")
        if len(parts) == 2:
            schema.setdefault(parts[0].lower(), set()).add(parts[1].lower())
    return schema


def render(elem, fragments):
    """递归把 MyBatis XML 元素还原为 SQL 文本"""
    out = [elem.text or ""]
    for child in elem:
        tag = child.tag.lower()
        if tag == "include":
            refid = child.get("refid")
            frag_root = fragments.get(refid)
            if frag_root is not None:
                out.append(render(frag_root, fragments))
        elif tag == "set":
            inner = render(child, fragments).strip().rstrip(",")
            out.append(" SET " + inner)
        elif tag == "where":
            inner = re.sub(r"^(and|or)\s+", "", render(child, fragments).strip(), flags=re.I)
            out.append(" WHERE " + inner)
        elif tag == "trim":
            prefix = child.get("prefix", "")
            suffix = child.get("suffix", "")
            out.append(f" {prefix} ")
            out.append(render(child, fragments))
            out.append(f" {suffix} ")
        elif tag == "foreach":
            out.append(f" {child.get('open', '')} ")
            out.append(render(child, fragments))
            out.append(f" {child.get('close', '')} ")
        elif tag == "choose":
            out.append(render_choose(child, fragments))
        else:  # if / bind 等：剥壳留内容
            out.append(render(child, fragments))
        out.append(child.tail or "")
    return "".join(out)


def render_choose(elem, fragments):
    """<choose> 只渲染第一个命中的 <when>（EXPLAIN 无法判 test，取第一个），无则 <otherwise>"""
    for child in elem:
        t = child.tag.lower()
        if t == "when":
            return render(child, fragments)
        if t == "otherwise":
            return render(child, fragments)
    return ""


def extract_statements(xml_path):
    tree = ET.parse(xml_path)
    root = tree.getroot()
    fragments = {}
    for sq in root.iter("sql"):
        if sq.get("id"):
            fragments[sq.get("id")] = sq
    stmts = []
    for tag in ("select", "insert", "update", "delete"):
        for st in root.iter(tag):
            sid = st.get("id")
            if sid:
                stmts.append((sid, render(st, fragments)))
    return stmts


def clean_sql(s):
    s = re.sub(r"--[^\n\r]*", " ", s)          # 删 SQL 行注释（必须在压平换行之前，防吞后续子句）
    s = re.sub(r"#\{[^}]*\}", "1", s)
    s = re.sub(r"\$\{[^}]*\}", "1", s)
    return re.sub(r"\s+", " ", s).strip()


def tables_of(sql):
    hits = set()
    for m in re.finditer(r"\b(?:from|into|join)\s+`?([a-zA-Z_][\w]*)`?", sql, flags=re.I):
        hits.add(m.group(1).lower())
    for m in re.finditer(r"\bupdate\s+`?([a-zA-Z_][\w]*)`?\s+set\b", sql, flags=re.I):
        hits.add(m.group(1).lower())
    return hits


def explain(sql):
    r = subprocess.run(MYSQL_CMD + ["-e", f"EXPLAIN {sql}"], capture_output=True, text=True)
    if r.returncode != 0:
        err = [l for l in (r.stderr or "").splitlines() if "insecure" not in l.lower()]
        return err[0] if err else "unknown error"
    return None


def main():
    schema = db_schema()
    xmls = sorted(glob.glob(f"{BASE}\\**\\src\\main\\resources\\mapper\\**\\*.xml", recursive=True))
    print(f"db tables: {len(schema)}, mapper xml: {len(xmls)}\n")
    fail = 0
    for path in xmls:
        short = path.split("backend")[-1]
        try:
            stmts = extract_statements(path)
        except ET.ParseError as e:
            print(f"[XML-ERROR] {short}: {e}")
            fail += 1
            continue
        for sid, raw in stmts:
            sql = clean_sql(raw)
            tabs = tables_of(sql)
            missing_tables = [t for t in tabs if t not in schema]
            if missing_tables:
                print(f"[TABLE-MISS] {short} :: {sid} -> {missing_tables}\n        SQL: {sql[:160]}")
                fail += 1
                continue
            if not tabs:
                continue
            err = explain(sql)
            if err:
                print(f"[FAIL] {short} :: {sid}\n        {err}\n        SQL: {sql[:160]}")
                fail += 1
    print(f"\n===== done: {fail} problem(s) =====")
    sys.exit(0 if fail == 0 else 1)


if __name__ == "__main__":
    main()
