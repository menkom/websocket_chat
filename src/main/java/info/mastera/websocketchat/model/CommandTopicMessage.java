package info.mastera.websocketchat.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Accessors(chain = true)
@Setter
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommandTopicMessage implements Serializable {

    CommandType type;
    String username;
    String sessionId;
}
