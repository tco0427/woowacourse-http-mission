package org.apache.coyote.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    private SessionManager() {
    }

    public static void add(final Session session) {
        SESSIONS.put(session.getId(), session);
    }

    public static Session findSession(final String id) {
        if (SESSIONS.containsKey(id)) {
            return SESSIONS.get(id);
        }
        throw new IllegalArgumentException("올바르지 않은 세션 ID 입니다.");
    }
}
