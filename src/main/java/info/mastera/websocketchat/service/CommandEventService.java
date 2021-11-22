package info.mastera.websocketchat.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import info.mastera.websocketchat.listener.CommandEventListener;
import info.mastera.websocketchat.model.CommandTopicMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CommandEventService {

    private static final String USER_COMMAND_TOPIC_NAME = "user-command-topic";
    private ITopic<CommandTopicMessage> topicInstance;

    @Autowired
    private HazelcastInstance hazelcastInstance;
    @Autowired
    private CommandEventListener commandEventListener;

    @PostConstruct
    private void init() {
        topicInstance = hazelcastInstance.getTopic(CommandEventService.USER_COMMAND_TOPIC_NAME);
        topicInstance.addMessageListener(commandEventListener);
    }

    public void publish(CommandTopicMessage message) {
        topicInstance.publish(message);
    }
}
