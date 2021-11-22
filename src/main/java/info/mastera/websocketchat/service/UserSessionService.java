package info.mastera.websocketchat.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class UserSessionService {

    private static final String USER_SESSION_MAP = "UserSessionMap";

    @Autowired
    private HazelcastInstance hazelcastInstance;

    private IMap<String, Set<String>> getMap() {
        return hazelcastInstance.getMap(UserSessionService.USER_SESSION_MAP);
    }

    public void add(String username, String sessionId) {
        Optional.ofNullable(getMap().get(username))
                .ifPresentOrElse(
                        mapValue -> {
                            mapValue.add(sessionId);
                            getMap().put(username, mapValue);
                            log.info("Added to user '" + username + "' connection sessionId '" + sessionId + "'");
                        },
                        () -> {
                            getMap().put(username, new HashSet<>(Set.of(sessionId)));
                            log.info("Created for user '" + username + "' connection sessionId '" + sessionId + "'");
                        }
                );
        log.info("Users connected: " + getMap().size());
    }
//
//    public void removeUser(String username) {
//        getMap().delete(username);
//        log.info("Users connected: " + getMap().size());
//    }

    public Set<String> getSessions(@NonNull String username) {
        return Optional.ofNullable(getMap().get(username))
                .orElse(Collections.emptySet());
    }

    public void removeSessionId(String username, String sessionId) {
        Optional.ofNullable(getMap().get(username))
                .ifPresent(
                        mapValue -> {
                            mapValue.remove(sessionId);
                            if (mapValue.isEmpty()) {
                                getMap().delete(username);
                            }
                        });
        log.info("Users connected: " + getMap().size());
    }
}
