package com.yitai.ai.store;

import com.yitai.ai.model.ConversationRecord;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 会话存储：
 *  - records：全量日志（最近 500 条），供管理后台查看。
 *  - history：按 conversationId 隔离的多轮对话历史（仅用户/助手文本轮，不含工具中间结果），
 *    用于把上下文回灌给大模型，实现多轮对话。
 * 生产环境可替换为 MySQL / Elasticsearch 持久化。
 */
@Repository
public class ConversationStore {

    private final Deque<ConversationRecord> records = new ConcurrentLinkedDeque<>();
    private final int MAX = 500;

    private final Map<String, Deque<HistoryItem>> history = new ConcurrentHashMap<>();
    private final int MAX_TURNS = 12; // 每会话保留最近 12 轮

    public void append(ConversationRecord record) {
        records.addLast(record);
        while (records.size() > MAX) {
            records.pollFirst();
        }
    }

    public List<ConversationRecord> recent(int limit, String mode) {
        List<ConversationRecord> all = new ArrayList<>(records);
        Collections.reverse(all);
        if (mode != null && !mode.isBlank()) {
            all = all.stream().filter(r -> mode.equals(r.getMode())).toList();
        }
        return all.stream().limit(limit).toList();
    }

    /** 存一轮对话（用户消息 + 助手回复），按 conversationId 隔离 */
    public void saveTurn(String conversationId, String userMsg, String botMsg) {
        if (conversationId == null || conversationId.isBlank()) return;
        Deque<HistoryItem> q = history.computeIfAbsent(conversationId, k -> new ConcurrentLinkedDeque<>());
        q.addLast(new HistoryItem("user", userMsg));
        q.addLast(new HistoryItem("assistant", botMsg));
        while (q.size() > MAX_TURNS * 2) q.pollFirst();
    }

    /** 取最近 N 轮历史（不含当前消息），用于拼装给大模型 */
    public List<HistoryItem> getHistory(String conversationId, int turns) {
        if (conversationId == null || conversationId.isBlank()) return List.of();
        Deque<HistoryItem> q = history.get(conversationId);
        if (q == null || q.isEmpty()) return List.of();
        List<HistoryItem> all = new ArrayList<>(q);
        int keep = Math.min(turns * 2, all.size());
        return all.subList(all.size() - keep, all.size());
    }

    /** 删除某会话的全部历史（用户在前端点“清空对话”时调用，等价于断开上下文） */
    public void delete(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) return;
        history.remove(conversationId);
    }

    /** 多轮历史条目 */
    public static class HistoryItem {
        public final String role;
        public final String content;

        public HistoryItem(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
