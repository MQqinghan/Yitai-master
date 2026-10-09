package com.yitai.ai.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 客服 RAG-lite：内置结构化帮助文档，按「标题命中 + 关键词/同义词重叠」检索 Top-K 片段注入提示词。
 *
 * 设计要点：
 *  - 零外部依赖（纯内存），保证 clone 即跑；生产可替换为 Milvus / ES 向量检索，接口不变。
 *  - 每条文档带人工梳理的「同义词/口语变体」关键词，解决中文短语漏召回（如「退钱」→退款、「寄回」→退货）。
 *  - 召回采用双向子串重叠打分：标题强命中 +4，文档关键词被问句包含 +2，取 Top-4 拼接。
 */
@Component
public class HelpDocRetriever {

    /** 结构化帮助文档（标题 + 正文 + 人工同义词关键词）。可按业务持续扩充。 */
    private static final List<HelpDoc> DOCS = List.of(
            new HelpDoc("退换货政策",
                    "支持自收货起 7 天无理由退货，15 天内出现质量问题包换。退回商品需保持吊牌、包装完好且不影响二次销售；定制类、贴身衣物、生鲜不支持无理由退货。退款将原路返回。",
                    List.of("退货", "退换", "退换货", "七天无理由", "7天无理由", "无理由退货", "换货", "质量问题",
                            "不喜欢", "不想要", "退回来", "寄回", "退掉")),
            new HelpDoc("退款时效与方式",
                    "退款在仓库签收并验收通过后 1-3 个工作日原路退回（微信/支付宝/银行卡）。若超时未到账，可在“我的-退款售后”查看进度或联系人工客服。",
                    List.of("退款", "退钱", "钱退回", "到账", "原路返回", "退款进度", "多久退款", "没收到退款", "退的款")),
            new HelpDoc("运费与配送",
                    "单笔订单满 99 元包邮，不满收 10 元运费。默认圆通或京东发货，大部分地区 1-3 天送达，偏远地区 3-5 天。可在物流中心实时查看快递轨迹。",
                    List.of("运费", "邮费", "包邮", "快递费", "配送", "发货", "送达", "几天到", "多久到", "偏远",
                            "圆通", "京东快递", "送货", "快递")),
            new HelpDoc("物流查询",
                    "支付后可在“我的-订单-查看物流”查看快递单号与实时轨迹；若下单超过 48 小时仍无物流更新，可联系人工客服核查。",
                    List.of("查物流", "物流", "快递单号", "快递轨迹", "到哪了", "发货了没", "物流更新", "没发货",
                            "运单号", "快递到哪")),
            new HelpDoc("支付方式",
                    "支持微信支付、支付宝、银行卡（储蓄卡/信用卡）。线上支付由支付宝通道完成，支付成功后订单状态自动更新为已支付。",
                    List.of("支付", "付款", "怎么付", "微信支付", "支付宝", "银行卡", "信用卡", "储蓄卡", "支付方式", "付钱")),
            new HelpDoc("支付失败处理",
                    "支付失败多为网络或银行卡限额导致。请更换网络或支付方式重试；如已扣款但订单未生成，款项会在 1-3 个工作日自动原路退回，无需重复支付。",
                    List.of("支付失败", "付不了", "付款失败", "扣款", "重复扣款", "多扣", "支付异常", "限额", "付错")),
            new HelpDoc("发票",
                    "下单时可选择开具电子发票，抬头支持个人或企业，开具后发送至预留邮箱，也可在“我的-发票”自助申请。电子发票与纸质发票具有同等报销效力。",
                    List.of("发票", "开票", "电子发票", "抬头", "报销", "企业发票", "发票邮箱", "普票", "专票", "要票")),
            new HelpDoc("账号与安全",
                    "账号采用 token 登录，请妥善保管密码。忘记密码可通过注册手机号验证码找回；如需注销账号，请在“设置-账号与安全”提交申请，注销后数据不可恢复。",
                    List.of("账号", "登录", "密码", "找回密码", "忘记密码", "注册", "注销", "token", "手机号",
                            "验证码", "安全", "登不上")),
            new HelpDoc("优惠券与活动",
                    "优惠券在“个人中心-卡券包”查看与使用；满减/秒杀等活动详见首页活动专区。优惠券一般不支持叠加，具体以商品页规则为准。",
                    List.of("优惠券", "券", "满减", "秒杀", "活动", "促销", "折扣", "领券", "卡券", "叠加", "红包")),
            new HelpDoc("价格与保价",
                    "商品以页面实时售价为准。若购买后短期内出现降价，可在“价格保护”入口申请差额返还（部分品类/活动商品不参与）。保价期通常为下单后 7 天。",
                    List.of("价格", "降价", "保价", "差价", "贵了", "便宜了", "价格保护", "退差价", "买贵")),
            new HelpDoc("缺货与预售",
                    "部分热销商品可能临时缺货或转预售。缺货商品可点击“到货通知”订阅；预售商品以详情页标注的发货时间为准。",
                    List.of("缺货", "没货", "无货", "预售", "补货", "到货通知", "有货吗", "什么时候有", "断货")),
            new HelpDoc("售后与人工客服",
                    "智能客服无法解决时，可转人工客服。人工客服在线时间 9:00-21:00，可通过“我的-客服中心-在线人工”或拨打官方客服热线 400-000-0000。",
                    List.of("人工客服", "人工", "客服", "投诉", "联系客服", "热线", "电话客服", "在线客服", "解决不了", "转人工")),
            new HelpDoc("会员与积分",
                    "注册即会员，购物累积积分，积分可在结算时抵扣现金（100 积分=1 元，以页面为准）。会员等级越高享受的专属券与包邮权益越多。",
                    List.of("会员", "积分", "等级", "成长值", "抵扣", "vip", "权益", "升级")),
            new HelpDoc("商品与下单咨询",
                    "商品库存、尺码、颜色、规格以详情页实时显示为准；下单前可加入购物车并选择规格，库存为 0 时无法下单。大件商品支持预约配送。",
                    List.of("下单", "库存", "尺码", "颜色", "规格", "有货吗", "能买吗", "加购物车", "大件", "尺寸")),
            new HelpDoc("隐私与信息安全",
                    "我们严格保护您的个人信息，仅在履行义务所必需范围内处理数据，不会向无关第三方泄露。授权范围可在“设置-隐私授权”中查看与撤回。",
                    List.of("隐私", "信息", "泄露", "授权", "个人信息", "数据安全", "第三方", "保密"))
    );

    /** 召回条数：客服需要较充足上下文，取 Top-4。 */
    private static final int TOP_K = 4;

    public String retrieve(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }
        String q = query.toLowerCase();
        List<Scored> scored = new ArrayList<>();
        for (HelpDoc d : DOCS) {
            int s = score(d, q);
            if (s > 0) {
                scored.add(new Scored(d, s));
            }
        }
        if (scored.isEmpty()) {
            return "";
        }
        scored.sort((a, b) -> Integer.compare(b.score, a.score));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(TOP_K, scored.size()); i++) {
            sb.append("- ").append(scored.get(i).doc.title)
                    .append("：").append(scored.get(i).doc.content).append("\n");
        }
        return sb.toString();
    }

    private int score(HelpDoc d, String q) {
        int s = 0;
        // 标题双向子串命中（问句含标题，或标题含问句核心词）
        if (d.title.toLowerCase().contains(q) || q.contains(d.title.toLowerCase())) {
            s += 4;
        }
        // 同义词/口语变体关键词命中
        for (String kw : d.keywords) {
            if (!kw.isBlank() && q.contains(kw.toLowerCase())) {
                s += 2;
            }
        }
        return s;
    }

    static class HelpDoc {
        final String title;
        final String content;
        final List<String> keywords;

        HelpDoc(String title, String content, List<String> keywords) {
            this.title = title;
            this.content = content;
            this.keywords = keywords;
        }
    }

    static class Scored {
        final HelpDoc doc;
        final int score;

        Scored(HelpDoc doc, int score) {
            this.doc = doc;
            this.score = score;
        }
    }
}
