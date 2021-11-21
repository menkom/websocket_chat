package info.mastera.websocketchat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Метод для настройки брокера сообщений,
     * который будет использоваться для направления сообщений от одного клиента к другому.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Адреса с префиксом /app предназначены для сообщений, обрабатываемых методами с аннотацией @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        // сообщения, чей адрес начинается с  "/topic", должны быть направлены в брокер сообщений.
        // Брокер перенаправляет сообщения всем клиентам, подписанным на тему.
        // конфигурирует простой брокер сообщений в памяти с одним адресом с префиксом
        // Enables a simple in-memory broker
        config.enableSimpleBroker("/user","/topic");

        //   Use this for enabling a Full featured broker like RabbitMQ
        /*
        registry.enableStompBrokerRelay("/topic")
                .setRelayHost("localhost")
                .setRelayPort(61613)
                .setClientLogin("guest")
                .setClientPasscode("guest");
        */

        config.setUserDestinationPrefix("/user");
    }

    /**
     * Конечная точка для использования, чтобы подключиться к нашему Websocket-серверу.
     * SockJS — для браузеров, которые могут не поддерживать Websocket.
     * STOMP(Simple Text Oriented Messaging Protocol) - протокол обмена сообщениями, задающий формат и правила обмена.
     * STOMP это надстройка на WebSocket, чтобы можно было отправлять сообщения пользователям,
     * подписанным на тему, или отправка сообщений конкретному пользователю.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/user-control")
                // Ограничения для CORS подключений
                .setAllowedOriginPatterns("*")
                // Использование SockJS для данного подключения
                .withSockJS()
                // this property can be used to disable the WebSocket transport if the load balancer does not support WebSocket
                .setWebSocketEnabled(true)
                // ping-pong frames для проверки состояния
                .setHeartbeatTime(25000);
    }
}
