package com.festena.manager;

import com.festena.Session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserSessionManager {
    private Map<Long, UserSession> sessions = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(UserSessionManager.class);

    public UserSessionManager(){
        log.info("Менеджер сессий создан!");
    }

    public void addSession(Long chatID, Long userId){
        UserSession session = new UserSession(chatID, userId);
        sessions.put(chatID, session);
    }

    public boolean isSessionExist(Long chatId){
        return sessions.containsKey(chatId);
    }

    public Map<Long, UserSession> getAllSessions(){
        return sessions;
    }

    public UserSession getUserSession(Long chatId){
        return sessions.get(chatId);
    }

    public void removeSession(Long chatId) {
        sessions.remove(chatId);
    }
}
