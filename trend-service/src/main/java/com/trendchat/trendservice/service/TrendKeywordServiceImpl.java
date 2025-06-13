package com.trendchat.trendservice.service;

import com.trendchat.trendservice.entity.TrendKeyword;
import com.trendchat.trendservice.repository.TrendKeywordRepository;
import com.trendchat.trendservice.util.HotKeywordDetector;
import com.trendchat.trendservice.util.KeywordUtil;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 트렌드 키워드 저장 및 급상승 키워드(HOT 키워드) 감지를 담당하는 도메인 서비스 구현체입니다.
 * <p>
 * 이 서비스는 Google Trends에서 수집된 키워드 데이터를 다음과 같이 처리합니다:
 * <ul>
 *     <li>DB(JPA)를 통해 영속 저장</li>
 *     <li>Redis ZSet에 시간 단위로 검색량 점수 저장</li>
 *     <li>Redis 기반 집계 데이터를 분석하여 급상승 키워드를 탐지</li>
 * </ul>
 * </p>
 *
 * <h3>Redis 저장 구조</h3>
 * <pre>{@code
 * Key: trend:yyyyMMddHHmm
 * Type: ZSET
 * Value: 키워드 문자열 → 검색량 점수 (score)
 * TTL: 15분
 * }</pre>
 *
 * @see com.trendchat.trendservice.entity.TrendKeyword
 * @see com.trendchat.trendservice.util.HotKeywordDetector
 * @see com.trendchat.trendservice.repository.TrendKeywordRepository
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrendKeywordServiceImpl implements TrendKeywordService {

    private final TrendKeywordRepository trendKeywordRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final HotKeywordDetector hotKeywordDetector;
    private final KeywordUtil keywordUtil;

    /**
     * 전달받은 키워드 정보로 트렌드 키워드 엔티티를 생성하고 DB 및 Redis에 저장합니다.
     * <p>
     * Redis에는 'trend:yyyyMMddHHmm' 형식의 ZSet 키로 저장되며, 각 키워드는 검색량(approxTraffic)만큼 score로 집계됩니다. TTL은
     * 15분으로 설정되어 Redis 메모리를 효율적으로 관리합니다.
     * </p>
     *
     * @param keyword       수집된 트렌드 키워드 문자열
     * @param approxTraffic 해당 키워드의 추정 검색량 (예: 10000)
     */
    @Override
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
     * 최근 5분 간의 트렌드 키워드를 집계하여 Top10을 반환합니다. 부족할 경우 이전 시간대 데이터를 보완하여 항상 10개를 반환합니다.
     *
     * @return 실시간 트렌드 Top 10 키워드 리스트 (Map<String, Integer>)
     */
    @Override
    public List<Map<String, Object>> getTop10Keywords() {
        Instant now = Instant.now();

        // 최근 5분 간 키워드 점수 수집
        List<String> recentKeys = keywordUtil.getMinuteKeys(now, 0, 5);
        Map<String, Double> scoreMap = new HashMap<>();

        for (String key : recentKeys) {
            Set<ZSetOperations.TypedTuple<String>> zset = redisTemplate.opsForZSet()
                    .rangeWithScores(key, 0, -1);
            if (zset == null) {
                continue;
            }
            for (ZSetOperations.TypedTuple<String> tuple : zset) {
                scoreMap.merge(tuple.getValue(), Objects.requireNonNull(tuple.getScore()),
                        Double::sum);
            }
        }

        // 점수 기준 내림차순 정렬 후 Top10 추출
        List<Map<String, Object>> top10 = keywordUtil.sortAndLimit(scoreMap, 10);

        // 만약 10개 미만이면, 5~10분 전 데이터로 보완
        if (top10.size() < 10) {
            Set<String> exists = keywordUtil.extractKeywords(top10);
            List<String> backupKeys = keywordUtil.getMinuteKeys(now, 5, 10);

            for (String key : backupKeys) {
                Set<ZSetOperations.TypedTuple<String>> backup = redisTemplate.opsForZSet()
                        .reverseRangeWithScores(key, 0, -1);
                if (backup == null) {
                    continue;
                }
                for (ZSetOperations.TypedTuple<String> tuple : backup) {
                    if (!exists.contains(tuple.getValue())) {
                        Map<String, Object> newItem = new HashMap<>();
                        newItem.put("keyword", tuple.getValue());
                        newItem.put("traffic", Objects.requireNonNull(tuple.getScore()).intValue());
                        top10.add(newItem);
                        exists.add(tuple.getValue());
                    }
                    if (top10.size() >= 10) {
                        break;
                    }
                }
                if (top10.size() >= 10) {
                    break;
                }
            }
        }

        return top10;
    }

    /**
     * 최근 5분간과 과거 5분간의 Redis ZSet 데이터를 비교하여 급상승 키워드를 감지합니다.
     * <p>
     * {@link HotKeywordDetector}를 통해 시간대별 키워드 점수를 수집하고, 두 기간 사이의 변화량을 기준으로 급상승 키워드(HOT)를 판단합니다.
     * </p>
     *
     * @return 급상승 키워드(Set 형태)
     */
    @Override
    public Set<String> detectHotKeywords() {
        Instant now = Instant.now();
        List<String> recentKeys = keywordUtil.getMinuteKeys(now, 0, 5);
        List<String> pastKeys = keywordUtil.getMinuteKeys(now, 5, 10);

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

