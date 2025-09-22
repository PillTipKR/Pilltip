package com.oauth2.AgenticAI.Util;

public final class SessionUtils {
    private SessionUtils() {}

    private static final ThreadLocal<String> SESS = new ThreadLocal<>();
    private static final ThreadLocal<Long> USER = new ThreadLocal<>();
    private static final ThreadLocal<String> NICK = new ThreadLocal<>();
    public static void set(String session, Long userId, String nick){ SESS.set(session); USER.set(userId); NICK.set(nick);}
    public static Long userId(){ return USER.get(); }
    public static void clear(){ SESS.remove(); USER.remove(); }
}