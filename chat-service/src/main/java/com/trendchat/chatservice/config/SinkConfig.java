package com.trendchat.chatservice.config;

import com.trendchat.chatservice.entity.ChatMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

// WebFlux의 Flux Sink 객체를 Bean으로 등록
@Configuration
public class SinkConfig {

    // 다수의 구독자에게 실시간 메시지를 전파 (Multicast)
    @Bean
    public Sinks.Many<ChatMessage> chatMessageSink(){
        return Sinks.many().multicast().onBackpressureBuffer();
    }
}
