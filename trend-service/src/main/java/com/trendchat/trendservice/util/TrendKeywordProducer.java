package com.trendchat.trendservice.util;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrendKeywordProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 지정된 Kafka 토픽("trend-keywords")에 메시지를 전송합니다.
     * <p>
     * 전송되는 메시지는 주어진 키와 값으로 구성되며, 키는 파티셔닝 기준으로 사용될 수 있습니다. 값은 Kafka Serializer 설정에 따라 직렬화되어 전송됩니다.
     * </p>
     *
     * @param key   Kafka 메시지의 키. 주로 파티션을 결정하거나 메시지 그룹핑에 사용됩니다.
     * @param value Kafka 메시지의 값. 직렬화 가능한 객체여야 하며, 트렌드 데이터 등 임의의 정보가 포함될 수 있습니다.
     */
    public void send(String key, Object value) {
        kafkaTemplate.send("trend-keywords", key, value);
    }
}