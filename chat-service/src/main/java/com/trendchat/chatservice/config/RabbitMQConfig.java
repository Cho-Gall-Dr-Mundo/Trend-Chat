package com.trendchat.chatservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // Exchange / Queue / Routing Key 이름 상수로 선언
    public static final String CHAT_EXCHANGE = "chat-exchange";
    public static final String CHAT_QUEUE = "chat-queue";
    public static final String CHAT_ROUTING_KEY = "chat-message";

    //메시지 라우팅용 TopicExchange 생성 (메시지 라우팅 규칙 지정 )
    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(CHAT_EXCHANGE);
    }

    //메시지 보관용 Queue 생성
    @Bean
    public Queue chatQueue() {
        return new Queue(CHAT_QUEUE, true); //	Queue가 디스크에 저장되어 서버 재시작 후에도 유지됨
    }

    //메시지 라우팅(Exchange)& Queue를 Routing Key로 연결(Exchange ↔ Queue 연결, Routing Key로 필터링)
    @Bean
    public Binding chatBinding() {
        return BindingBuilder.bind(chatQueue()).to(chatExchange()).with(CHAT_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
