package com.trendchat.trendservice.dto;

import com.trendchat.trendservice.entity.Trend;
import java.util.List;

/**
 * {@code TrendResponse}는 트렌드 데이터를 클라이언트에 전달하기 위한 응답 DTO의 상위 인터페이스입니다.
 *
 * <p>{@code sealed interface}로 선언되어 있으며, {@link Simple}, {@link Get} 두 개의 응답 형태만 허용됩니다.</p>
 *
 * @author TrendChat
 * @see Simple
 * @see Get
 */
public sealed interface TrendResponse permits TrendResponse.Simple, TrendResponse.Get {

    /**
     * {@code Simple} 응답은 트렌드 키워드에 대한 요약 정보를 포함하는 경량 응답 형태입니다.
     *
     * <p>리스트 페이지, 검색 결과 등에서 간단한 미리보기 용도로 사용됩니다.</p>
     *
     * @param keyword         트렌드 키워드
     * @param summary         요약 텍스트
     * @param majorCategories 대분류 카테고리 목록
     * @param subCategories   소분류 카테고리 목록
     */
    record Simple(
            String keyword,
            String summary,
            List<String> majorCategories,
            List<String> subCategories
    ) implements TrendResponse {

        /**
         * {@link Trend} 엔티티로부터 Simple 응답 DTO를 생성합니다.
         *
         * @param trend 트렌드 엔티티
         */
        public Simple(Trend trend) {
            this(
                    trend.getKeyword(),
                    trend.getSummary(),
                    extractMajorCategories(trend),
                    extractSubCategories(trend)
            );
        }
    }

    /**
     * {@code Get} 응답은 단일 트렌드 키워드의 상세 정보를 담는 응답 형태입니다.
     *
     * <p>상세 페이지 등에서 사용되며, 블로그 요약 내용을 포함합니다.</p>
     *
     * @param keyword         트렌드 키워드
     * @param summary         요약 텍스트
     * @param blogPost        블로그 형식 본문 내용
     * @param majorCategories 대분류 카테고리 목록
     * @param subCategories   소분류 카테고리 목록
     */
    record Get(
            String keyword,
            String summary,
            String blogPost,
            List<String> majorCategories,
            List<String> subCategories
    ) implements TrendResponse {

        /**
         * {@link Trend} 엔티티로부터 Get 응답 DTO를 생성합니다.
         *
         * @param trend 트렌드 엔티티
         */
        public Get(Trend trend) {
            this(
                    trend.getKeyword(),
                    trend.getSummary(),
                    trend.getBlogPost(),
                    extractMajorCategories(trend),
                    extractSubCategories(trend)
            );
        }
    }

    /**
     * 트렌드 엔티티에서 대분류 이름 목록을 추출합니다.
     *
     * @param trend 트렌드 엔티티
     * @return 중복 제거된 대분류 이름 목록
     */
    private static List<String> extractMajorCategories(Trend trend) {
        return trend.getSubCategoryLinks().stream()
                .map(link -> link.getSubCategory().getMajorCategory().getName())
                .distinct()
                .toList();
    }

    /**
     * 트렌드 엔티티에서 소분류 이름 목록을 추출합니다.
     *
     * @param trend 트렌드 엔티티
     * @return 중복 제거된 소분류 이름 목록
     */
    private static List<String> extractSubCategories(Trend trend) {
        return trend.getSubCategoryLinks().stream()
                .map(link -> link.getSubCategory().getName())
                .distinct()
                .toList();
    }
}
