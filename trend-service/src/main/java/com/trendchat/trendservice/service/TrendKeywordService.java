package com.trendchat.trendservice.service;

import com.trendchat.trendservice.entity.TrendKeyword;
import com.trendchat.trendservice.repository.TrendKeywordRepository;
import com.trendchat.trendservice.util.HotKeywordDetector;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 트렌드 키워드 저장 및 급상승 키워드 감지를 담당하는 도메인 서비스입니다.
 * <p>
 * Google Trends에서 수집된 키워드를 DB와 Redis에 저장하고, Redis 기반 집계 데이터를 바탕으로 급상승 키워드(HOT 키워드)를 탐지합니다.
 * </p>
 *
 * <ul>
 *     <li>{@link #createTrendKeyword(String, int)}: 키워드 저장 및 Redis 집계</li>
 *     <li>{@link #detectHotKeywords()}: 최근 5분/이전 5분 데이터를 비교해 HOT 키워드 감지</li>
 * </ul>
 *
 * @see com.trendchat.trendservice.entity.TrendKeyword
 * @see com.trendchat.trendservice.util.HotKeywordDetector
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrendKeywordService {

    private final TrendKeywordRepository trendKeywordRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final HotKeywordDetector hotKeywordDetector;

    /**
     * 전달받은 키워드 정보로 트렌드 키워드 엔티티를 생성하고 DB 및 Redis에 저장합니다.
     * <p>
     * Redis에는 'trend:yyyyMMddHHmm' 형식의 ZSet 키로 저장되며, 각 키워드는 검색량(approxTraffic)만큼 score로 집계됩니다.
     * </p>
     *
     * @param keyword       수집된 트렌드 키워드 문자열
     * @param approxTraffic 해당 키워드의 추정 검색량 (예: 10000)
     */
    @Transactional
    public void createTrendKeyword(String keyword, int approxTraffic) {
        trendKeywordRepository.save(TrendKeyword.of(keyword, approxTraffic));

        String key = "trend:" + DateTimeFormatter.ofPattern("yyyyMMddHHmm")
                .withZone(ZoneId.of("Asia/Seoul"))
                .format(Instant.now());

        redisTemplate.opsForZSet().incrementScore(key, keyword, approxTraffic);
        redisTemplate.expire(key, Duration.ofMinutes(15));

        log.info("Keyword saved: {}", keyword);
    }

    /**
     * 최근 5분간과 과거 5분간의 Redis ZSet 데이터를 비교하여 급상승 키워드를 감지합니다.
     * <p>
     * {@link HotKeywordDetector}를 통해 시간대별 키워드 점수를 수집하고, 두 기간 사이의 변화량을 기준으로 HOT 키워드를 선별합니다.
     * </p>
     *
     * @return 급상승 키워드(Set 형태)
     */
    public Set<String> detectHotKeywords() {
        Instant now = Instant.now();
        List<String> recentKeys = hotKeywordDetector.getMinuteKeys(now, 0, 5);
        List<String> pastKeys = hotKeywordDetector.getMinuteKeys(now, 5, 10);

        Map<String, Integer> recentCounts = loadKeywordScores(recentKeys);
        Map<String, Integer> pastCounts = loadKeywordScores(pastKeys);

        Set<String> hotKeywordSet = hotKeywordDetector.detect(recentCounts, pastCounts);

        log.info("HOT keyword notification: {}", hotKeywordSet);

        return hotKeywordSet;
    }

    /**
     * Redis ZSet에서 키워드별 점수를 로드하여 통합 집계 맵으로 반환합니다.
     *
     * @param keys Redis ZSet 키 목록 (예: trend:202406051259)
     * @return 키워드별 점수 합계 Map
     */
    private Map<String, Integer> loadKeywordScores(List<String> keys) {
        Map<String, Integer> result = new HashMap<>();
        for (String key : keys) {
            Set<String> members = redisTemplate.opsForZSet().range(key, 0, -1);
            if (members != null) {
                for (String member : members) {
                    Double score = redisTemplate.opsForZSet().score(key, member);
                    result.merge(member, score != null ? score.intValue() : 0, Integer::sum);
                }
            }
        }
        return result;
    }
}

