package com.festena.manager;

import com.festena.Session.Resources;
import com.festena.Session.UserSession;
import com.festena.databases.IDataBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserSessionManager {
    private final Map<Long, UserSession> sessions = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(UserSessionManager.class);
    private final IDataBase playersTable;

    public UserSessionManager(IDataBase playersTable){
        log.info("Менеджер сессий создан!");
        this.playersTable = playersTable;
    }

    public void addSession(Long chatID, Long userId){
        UserSession session = new UserSession(chatID, userId);
        sessions.put(chatID, session);
        if (playersTable.isPlayerExists(chatID)){
            Map<String, Integer> dbRes = playersTable.getPlayerData(chatID);
            session.updateRes(dbRes);
        } else{
            insertNewPlayerInDB(chatID, session);
        }
    }

    public void updatePlayerInDB(Long chatId){
        UserSession session = sessions.get(chatId);
        Resources res = session.getResources();
        playersTable.updatePlayer(chatId, res.getGold(), res.getPeople(), res.getReputation(),
                res.getFood(), res.getArmy(), res.getTechnology());
    }
    private void insertNewPlayerInDB(Long chatId, UserSession session){
        playersTable.addPlayer(chatId);
        Resources res = session.getResources();
        playersTable.updatePlayer(chatId, res.getGold(), res.getPeople(), res.getReputation(),
                res.getFood(), res.getArmy(), res.getTechnology());

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

    public Map<Long, Integer> getTopPlayers(int limit){
        return playersTable.getTopPlayers(limit);
    }
}
