package com.trendchat.trendservice.service;

import com.trendchat.trendservice.entity.TrendKeyword;
import com.trendchat.trendservice.repository.TrendKeywordRepository;
import com.trendchat.trendservice.util.HotKeywordDetector;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 외부에서 전달된 트렌드 키워드 데이터를 저장하는 도메인 서비스입니다.
 * <p>
 * 크롤링/수신된 키워드와 추정 검색량을 기반으로 {@link com.trendchat.trendservice.entity.TrendKeyword} 엔티티를 생성하고,
 * {@link com.trendchat.trendservice.repository.TrendKeywordRepository}를 통해 DB에 영속화합니다.
 * </p>
 *
 * @see com.trendchat.trendservice.entity.TrendKeyword
 * @see com.trendchat.trendservice.repository.TrendKeywordRepository
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrendKeywordService {

    private final TrendKeywordRepository trendKeywordRepository;
    private final StringRedisTemplate redisTemplate;
    private final HotKeywordDetector hotKeywordDetector;

    /**
     * 전달받은 키워드 정보로 트렌드 키워드 엔티티를 생성하고 DB에 저장합니다.
     *
     * @param keyword       수집된 트렌드 키워드 문자열
     * @param approxTraffic 해당 키워드의 추정 검색량 (예: 10000)
     */
    @Transactional
    public void createTrendKeyword(String keyword, int approxTraffic) {
        trendKeywordRepository.save(TrendKeyword.of(keyword, approxTraffic));
        log.info("Keyword saved: {}", keyword);
    }

    /**
     * Redis에서 최근/과거 데이터 수집 후 급상승 키워드 감지
     */
    public Set<String> detectHotKeywords() {
        Instant now = Instant.now();
        List<String> recentKeys = hotKeywordDetector.getMinuteKeys(now, 0, 5);
        List<String> pastKeys = hotKeywordDetector.getMinuteKeys(now, 5, 10);

        Map<String, Integer> recentCounts = loadKeywordScores(recentKeys);
        Map<String, Integer> pastCounts = loadKeywordScores(pastKeys);

        return hotKeywordDetector.detect(recentCounts, pastCounts);
    }

    /**
     * Redis ZSet에서 키워드 점수 수집
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

