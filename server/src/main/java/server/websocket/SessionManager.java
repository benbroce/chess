package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class SessionManager {
    // Map<gameID, Map<authToken, session>> (each game has several sessions, each linked to an authToken)
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Session>> sessionMap;

    public SessionManager() {
        this.sessionMap = new ConcurrentHashMap<>();
    }

    public void addSessionToGame(int gameID, String authToken, Session session) {
        if (!this.sessionMap.containsKey(gameID)) {
            this.sessionMap.put(gameID, new ConcurrentHashMap<>());
        }
        this.sessionMap.get(gameID).put(authToken, session);
    }

    public void removeSessionFromGame(int gameID, String authToken, Session session) {
        Session sessionToRemove = sessionMap.get(gameID).remove(authToken);
        sessionToRemove.close(); // TODO: Is this ok to be here?
    }

    public void removeSession(Session session) {
        for (ConcurrentHashMap<String, Session> gameSessions : sessionMap.values()) {
            for (Map.Entry<String, Session> entry : gameSessions.entrySet()) {
                if (entry.getValue() == session) {
                    Session sessionToRemove = gameSessions.remove(entry.getKey());
                    sessionToRemove.close();
                    return;
                }
            }
        }
    }

    public Map<String, Session> getSessionsForGame(int gameID) {
        return sessionMap.get(gameID);
    }
}