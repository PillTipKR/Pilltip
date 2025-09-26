package com.oauth2.AgenticAI.Agent;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class MemoryStore {

    private static final int MAX_TURNS = 60; // 최근 60 메시지만 유지(원하면 조정)
    private static int cnt = 0;
    private final Map<String, Deque<Message>> history = new ConcurrentHashMap<>();
    private final Map<String, String> summary = new ConcurrentHashMap<>();

    public String summary(String session) {
        return summary.getOrDefault(session, "");
    }

    public List<Message> recentTurns(String session, int n) {
        var q = history.getOrDefault(session, new ConcurrentLinkedDeque<>());
        int size = q.size();
        int keep = Math.max(0, n);
        int skip = Math.max(0, size - keep);
        return q.stream().skip(skip).toList();
    }

    public void appendUser(String session, String text) { addMessage(session, new UserMessage(text)); }
    public void appendAssistant(String session, String text) { addMessage(session, new AssistantMessage(text)); }

    public void appendToolResult(String session, String userText, String output) {

        String summary = "["+cnt+"번째 대화]\n"
                +session+"\n"
                +userText+"\n"
                +output+"\n"
                +"-----------------------------------------------------\n";
        Message summaryMessage = new AssistantMessage(summary);
        addMessage(session, summaryMessage);
    }

    private void addMessage(String session, Message m) {
        var q = history.computeIfAbsent(session, k -> new ConcurrentLinkedDeque<>());
        q.add(m);
        // 롤링 컷: 오래된 것 제거
        while (q.size() > MAX_TURNS) q.pollFirst();
    }

    public void updateSummary(String session, String s) { summary.put(session, s); }

    // 선택: 전체 히스토리가 필요할 때
    public List<Message> all(String session) {
        return new ArrayList<>(history.getOrDefault(session, new ConcurrentLinkedDeque<>()));
    }
}

