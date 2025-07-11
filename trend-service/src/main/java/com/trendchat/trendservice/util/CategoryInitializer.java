package com.trendchat.trendservice.util;

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

@Component
@RequiredArgsConstructor
public class CategoryInitializer implements ApplicationRunner {

    private final MajorCategoryRepository majorCategoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    @Override
    public void run(ApplicationArguments args) {
        // 1차 대분류
        List<String> majorCategories = List.of(
                "정치", "경제", "사회", "생활/문화", "세계", "IT/과학",
                "연예", "스포츠", "오피니언/칼럼", "날씨", "지역", "기타/특집"
        );

        // 2차 세부분류 (대분류-세부분류 쌍)
        Map<String, List<String>> subCategoryMap = Map.of(
                "경제", List.of("증권/금융", "부동산", "산업/기업", "생활경제/소비자"),
                "사회", List.of("사건/사고", "노동/복지", "교육", "환경", "법원/검찰"),
                "생활/문화", List.of("음식/맛집", "여행/레저", "패션/뷰티", "건강/의학", "종교", "결혼/육아", "공연/전시"),
                "연예", List.of("방송/TV", "영화", "음악/가요", "스타/인물"),
                "IT/과학", List.of("인터넷/통신", "모바일/가전", "게임/콘텐츠", "미래기술/AI", "과학일반"),
                "스포츠", List.of("축구", "야구", "농구/배구", "골프", "스포츠일반"),
                "세계", List.of("아시아/중동", "미주/유럽/아프리카", "국제기구")
        );

        // 1. MajorCategory 모두 저장
        for (String major : majorCategories) {
            if (!majorCategoryRepository.existsByName(major)) {
                majorCategoryRepository.save(
                        new MajorCategory(major)
                );
            }
        }

        // 2. SubCategory 모두 저장 (MajorCategory와 연결)
        for (Map.Entry<String, List<String>> entry : subCategoryMap.entrySet()) {
            String majorName = entry.getKey();
            MajorCategory major = majorCategoryRepository.findByName(majorName)
                    .orElseThrow(
                            () -> new IllegalStateException("MajorCategory 미존재: " + majorName));
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