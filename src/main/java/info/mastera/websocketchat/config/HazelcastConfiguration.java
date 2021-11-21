package info.mastera.websocketchat.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.MapConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class HazelcastConfiguration {

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired(required = false)
    private List<MapConfig> hazelcastMapConfigs;

    @Value("${mastera.hazelcast.group-name}")
    private String hazelcastGroupName;

    @Bean
    public Config hazelcastConfig() {
        var config = new Config("websocket-hazelcast-instance");
        config.setProperty("hazelcast.logging.type", "slf4j");
        config.setGroupConfig(new GroupConfig(hazelcastGroupName));
        config.getMapConfig("default").setAsyncBackupCount(0).setBackupCount(0);
        var joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig()
                .setEnabled(true)
                .addMember("localhost");
        if (hazelcastMapConfigs != null && !hazelcastMapConfigs.isEmpty()) {
            hazelcastMapConfigs.forEach(config::addMapConfig);
        }
        return config;
    }
}
