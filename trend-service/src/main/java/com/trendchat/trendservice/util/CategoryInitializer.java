package com.trendchat.trendservice.util;

import static java.util.Map.entry;

import com.trendchat.trendservice.entity.MajorCategory;
import com.trendchat.trendservice.entity.SubCategory;
import com.trendchat.trendservice.repository.MajorCategoryRepository;
import com.trendchat.trendservice.repository.SubCategoryRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * {@code CategoryInitializer}는 애플리케이션 시작 시 실행되어 미리 정의된 대분류(MajorCategory) 및 소분류(SubCategory) 데이터를
 * 데이터베이스에 초기화하는 역할을 담당합니다.
 *
 * <p>Spring Boot의 {@link ApplicationRunner}를 구현하여
 * 애플리케이션 실행 직후 자동으로 초기화 작업을 수행합니다.</p>
 *
 * <p>초기화 대상은 정치, 경제, 사회, 문화 등 주요 뉴스 카테고리와
 * 그에 대응되는 세부 카테고리들입니다.</p>
 *
 * @author TrendChat
 */
@Component
@RequiredArgsConstructor
public class CategoryInitializer implements ApplicationRunner {

    private final MajorCategoryRepository majorCategoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    /**
     * 애플리케이션 실행 시 호출되어 대분류 및 소분류 카테고리를 초기화합니다.
     *
     * <ul>
     *   <li>대분류 카테고리는 존재 여부 확인 후 존재하지 않으면 저장합니다.</li>
     *   <li>소분류 카테고리는 매핑된 대분류를 기준으로 저장합니다.</li>
     * </ul>
     *
     * @param args 애플리케이션 실행 인자 (사용하지 않음)
     * @throws IllegalStateException 대분류 조회 실패 시 발생
     */
    @Override
    public void run(ApplicationArguments args) {
        // 1차 대분류
        List<String> majorCategories = List.of(
                "정치", "경제", "사회", "생활/문화", "세계", "IT/과학",
                "연예", "스포츠", "오피니언/칼럼", "날씨", "지역", "기타/특집"
        );

        // 2차 세부분류 (대분류-세부분류 쌍)
        Map<String, List<String>> subCategoryMap = Map.ofEntries(
                entry("정치", List.of("대통령실", "국회/정당", "행정/지자체", "외교/국방", "북한", "선거")),
                entry("경제", List.of("증권/금융", "부동산", "산업/기업", "생활경제/소비자")),
                entry("사회", List.of("사건/사고", "노동/복지", "교육", "환경", "법원/검찰")),
                entry("생활/문화", List.of("음식/맛집", "여행/레저", "패션/뷰티", "건강/의학", "종교", "결혼/육아", "공연/전시")),
                entry("세계", List.of("아시아/중동", "미주/유럽/아프리카", "국제기구")),
                entry("IT/과학", List.of("인터넷/통신", "모바일/가전", "게임/콘텐츠", "미래기술/AI", "과학일반")),
                entry("연예", List.of("방송/TV", "영화", "음악/가요", "스타/인물")),
                entry("스포츠", List.of("축구", "야구", "농구/배구", "골프", "스포츠일반")),
                entry("오피니언/칼럼", List.of("사설", "칼럼", "독자마당")),
                entry("날씨", List.of("오늘/내일", "주간/주말", "태풍/기상특보")),
                entry("지역", List.of("수도권", "영남", "호남", "충청", "강원", "제주")),
                entry("기타/특집", List.of("인사/부고", "사진/포토", "만평", "특집기획"))
        );

        // MajorCategory 모두 저장
        for (String major : majorCategories) {
            if (!majorCategoryRepository.existsByName(major)) {
                majorCategoryRepository.save(
                        new MajorCategory(major)
                );
            }
        }

        // SubCategory 모두 저장 (MajorCategory와 연결)
        for (Map.Entry<String, List<String>> entry : subCategoryMap.entrySet()) {
            String majorName = entry.getKey();
            MajorCategory major = majorCategoryRepository.findByName(majorName).orElseThrow(
                    () -> new IllegalStateException("Not found majorCategory: " + majorName));
            for (String sub : entry.getValue()) {
                if (!subCategoryRepository.existsByName(sub)) {
                    subCategoryRepository.save(
                            new SubCategory(sub, major)
                    );
                }
            }
        }
    }
}
