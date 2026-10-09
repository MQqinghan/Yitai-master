# -*- coding: utf-8 -*-
"""
gen_schema.py —— 从 Java 实体类反向生成 Yitai 全量建库 SQL（db: yitai_mall）
用法: python gen_schema.py
输入: backend/model/src/main/java/com/yitai/model/entity/**/*.java
      + 各服务 Mapper XML（提取真实表名做交叉验证）
输出: backend/docs/sql/yitai_mall_schema.sql
说明: 字段类型由 Java 类型映射，varchar 长度按语义规则推断；
      生成脚本仅供本地一键建库使用，若与原始库有出入以原始为准。
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]      # backend/
ENTITY_DIR = ROOT / "model" / "src" / "main" / "java" / "com" / "yitai" / "model" / "entity"
OUT_FILE = Path(__file__).resolve().parent / "yitai_mall_schema.sql"
DB_NAME = "yitai_mall"

# 实体类名 -> 表名特例（以 Mapper XML 实际表名为准）
TABLE_NAME_OVERRIDES = {
    "SysRoleUser": "sys_user_role",
}

# 语义化 varchar 长度特例：字段名 -> SQL 类型
FIELD_TYPE_OVERRIDES = {
    "content": "text",
    "remark": "varchar(500)",
    "memo": "varchar(500)",
    "cancelReason": "varchar(500)",
    "avatar": "varchar(500)",
    "password": "varchar(100)",
    "phone": "varchar(20)",
    "receiverPhone": "varchar(20)",
    "lastLoginIp": "varchar(50)",
    "openId": "varchar(64)",
    "unionId": "varchar(64)",
    "receiverAddress": "varchar(200)",
    "receiverTagName": "varchar(50)",
    "nickName": "varchar(100)",
    "username": "varchar(64)",
    "title": "varchar(200)",
    "dedupKey": "varchar(100)",
    "orderNo": "varchar(64)",
}

JAVA_TO_SQL = [
    (r"^BigDecimal$", "decimal(10,2)"),
    (r"^Long$", "bigint(20)"),
    (r"^Integer$", "int(11)"),
    (r"^Date$", "datetime"),
    (r"^String$", None),  # 走语义规则
]


def camel_to_snake(name: str) -> str:
    s1 = re.sub(r"(.)([A-Z][a-z]+)", r"\1_\2", name)
    return re.sub(r"([a-z0-9])([A-Z])", r"\1_\2", s1).lower()


def sql_type_for(java_type: str, field: str) -> str:
    for key, typ in FIELD_TYPE_OVERRIDES.items():
        if field == key:
            return typ
    for pat, typ in JAVA_TO_SQL:
        if re.match(pat, java_type):
            if typ is None:  # String 默认
                return "varchar(255)"
            return typ
    return "varchar(255)"


def parse_entity(path: Path):
    text = path.read_text(encoding="utf-8")
    cls_m = re.search(r"public\s+class\s+(\w+)(?:\s+extends\s+BaseEntity)?", text)
    if not cls_m:
        return None
    cls_name = cls_m.group(1)
    fields = []
    lines = text.splitlines()
    pending_desc = None
    for line in lines:
        d = re.search(r'@Schema\(\s*description\s*=\s*"([^"]*)"', line)
        if d:
            pending_desc = d.group(1)
            continue
        f = re.match(r"\s+private\s+(static\s+)?([\w<>.]+)\s+(\w+)\s*;", line)
        if f:
            is_static, jtype, fname = f.group(1), f.group(2), f.group(3)
            if is_static or fname == "serialVersionUID":
                pending_desc = None
                continue
            if jtype.startswith("List<") or jtype.startswith("Map<"):
                pending_desc = None
                continue  # 关联对象，非列
            fields.append({"name": fname, "type": jtype, "desc": pending_desc or fname})
        pending_desc = None if line.strip() and not line.strip().startswith("@") and not f else pending_desc
    return cls_name, fields


def collect_xml_tables():
    tables = set()
    for xml in ROOT.rglob("src/main/resources/mapper/**/*.xml"):
        try:
            t = xml.read_text(encoding="utf-8", errors="ignore")
        except Exception:
            continue
        for m in re.finditer(r"(?:from|FROM|into|INTO|update|UPDATE)\s+`?([a-z_][a-z0-9_]*)`?", t):
            name = m.group(1)
            if name not in ("id",):
                tables.add(name)
    return tables


def gen_table_ddl(table: str, fields, cls: str):
    cols = []
    cols.append("  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID'")
    idx_cols = []
    for f in fields:
        if f["name"] == "id":
            continue
        typ = sql_type_for(f["type"], f["name"])
        desc = f["desc"].replace("'", "''")
        cols.append(f"  `{camel_to_snake(f['name'])}` {typ} DEFAULT NULL COMMENT '{desc}'")
        if f["name"].endswith("Id") and f["name"] != "id":
            idx_cols.append(camel_to_snake(f["name"]))
    cols.append("  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'")
    cols.append("  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间'")
    cols.append("  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除 1-已删除'")
    idx_lines = []
    for c in dict.fromkeys(idx_cols):
        idx_lines.append(f",\n  KEY `idx_{c}` (`{c}`)")
    body = ",\n".join(cols) + "".join(idx_lines)
    return f"-- ========== {cls} -> {table} ==========\nDROP TABLE IF EXISTS `{table}`;\nCREATE TABLE `{table}` (\n{body},\n  PRIMARY KEY (`id`)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='{table}';\n"


def main():
    xml_tables = collect_xml_tables()
    entities = []
    warn = []
    for path in sorted(ENTITY_DIR.rglob("*.java")):
        r = parse_entity(path)
        if not r:
            continue
        cls, fields = r
        if cls == "BaseEntity":
            continue
        table = TABLE_NAME_OVERRIDES.get(cls, camel_to_snake(cls))
        entities.append((cls, table, fields))
        if xml_tables and table not in xml_tables:
            warn.append(f"  [warn] {cls} -> {table} 不在 Mapper XML 表名集合中（Region/ProductUnit 类基础表可忽略）")

    parts = [
        f"-- ============================================================",
        f"-- Yitai 商城 全量建库脚本（库名 {DB_NAME}，2026-09-28 反向生成）",
        f"-- 由 gen_schema.py 从实体类生成，风格对齐 message_info.sql",
        f"-- 执行顺序：本文件 -> generate_test_data.sql -> add_analysis_menu_complete.sql",
        f"-- ============================================================",
        f"",
        f"SET NAMES utf8mb4;",
        f"SET FOREIGN_KEY_CHECKS = 0;",
        f"",
        f"CREATE DATABASE IF NOT EXISTS `{DB_NAME}` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;",
        f"USE `{DB_NAME}`;",
        f"",
    ]
    for cls, table, fields in entities:
        parts.append(gen_table_ddl(table, fields, cls))
    parts.append(f"SET FOREIGN_KEY_CHECKS = 1;")
    OUT_FILE.write_text("\n".join(parts), encoding="utf-8")

    print(f"实体数: {len(entities)}  输出: {OUT_FILE}")
    print("表清单:")
    for cls, table, fields in entities:
        print(f"  {table:<24} ({len(fields)} 列)  <- {cls}")
    if warn:
        print("警告:")
        print("\n".join(warn))


if __name__ == "__main__":
    sys.exit(main())
