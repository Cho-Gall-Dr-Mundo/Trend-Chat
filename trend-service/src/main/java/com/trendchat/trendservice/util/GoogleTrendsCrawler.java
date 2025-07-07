package com.trendchat.trendservice.util;

import com.trendchat.trendservice.dto.NewsItem;
import com.trendchat.trendservice.dto.TrendKeywordItem;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

/**
 * Google Trends 실시간 급상승 키워드 및 관련 뉴스 기사 정보를 크롤링하는 유틸리티 컴포넌트입니다.
 * <p>
 * Selenium WebDriver를 이용해 동적 렌더링 및 Shadow DOM이 적용된 Google Trends 페이지를 자동화 브라우저로 탐색하며, 키워드, 검색량, 상태,
 * 시간 및 관련 뉴스(제목, URL, 썸네일, 메타정보)를 {@link TrendKeywordItem} 형태로 수집합니다.
 * <ul>
 *     <li>대상 URL: {@code https://trends.google.co.kr/trending?geo=KR&hours=4}</li>
 *     <li>Headless Chrome 기반 Selenium WebDriver 활용</li>
 *     <li>키워드별로 다수의 뉴스 기사 정보를 포함하여 반환</li>
 * </ul>
 *
 * @see TrendKeywordItem
 * @see NewsItem
 */
@Slf4j
@Component
public class GoogleTrendsCrawler {

    /**
     * Google Trends 실시간 트렌드 키워드와 각 키워드별 뉴스 기사 정보를 크롤링합니다.
     * <p>
     * - 크롬 headless 모드로 페이지 접속 후 키워드 카드를 순회하며 정보를 추출합니다.<br> - 각 카드 클릭 시 오버레이/광고/팝업 등 방해 요소를 제거하여
     * 뉴스 영역을 활성화합니다.<br> - 키워드별로 뉴스 기사({@link NewsItem})들을 추출, {@link TrendKeywordItem}에 함께
     * 저장합니다.<br> - 크롤링 과정 및 결과를 상세 로그로 남깁니다.
     * </p>
     *
     * @return 키워드를 key로 하고, 해당 키워드의 검색량, 상태, 시간, 뉴스 기사 목록을 value로 갖는 Map 객체
     */
    public Map<String, TrendKeywordItem> crawl() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--start-maximized");
        options.addArguments("user-agent=Mozilla/5.0");

        WebDriver driver = new ChromeDriver(options);
        Map<String, TrendKeywordItem> resultMap = new HashMap<>();

        long started = System.currentTimeMillis();
        try {
            driver.get("https://trends.google.co.kr/trending?geo=KR&hours=4");
            Thread.sleep(4000); // 최초 렌더링 대기

            JavascriptExecutor js = (JavascriptExecutor) driver;
            List<WebElement> allCards = driver.findElements(
                    By.cssSelector("td.enOdEe-wZVHld-aOtOmf"));
            List<WebElement> keywordCards = new ArrayList<>();

            for (WebElement card : allCards) {
                try {
                    String keyword = card.findElement(By.cssSelector(".mZ3RIc")).getText();
                    if (!keyword.isBlank()) {
                        keywordCards.add(card);
                    }
                } catch (NoSuchElementException e) {
                    // 키워드 없는 td는 skip
                }
            }

            log.info("실제 크롤링 대상 키워드 카드 수: {}", keywordCards.size());

            for (int i = 0; i < keywordCards.size(); i++) {
                WebElement card = keywordCards.get(i);
                log.info("[{}] 카드 진입", i);

                try {
                    // 오버레이 제거
                    js.executeScript(
                            "document.querySelectorAll('.pYTkkf-Bz112c-RLmnJb, .pYTkkf-Bz112c, .gb_Lc, .gb_Ad, [role=\"presentation\"]').forEach(e => e.remove());"
                    );

                    js.executeScript("arguments[0].scrollIntoView({block:'center'});", card);
                    js.executeScript("arguments[0].click();", card);

                    // 뉴스 컨테이너 등장 대기 (2.5초 이내)
                    try {
                        new WebDriverWait(driver, Duration.ofMillis(2500))
                                .until(ExpectedConditions.visibilityOfElementLocated(
                                        By.cssSelector(".jDtQ5 a.xZCHj")));
                    } catch (TimeoutException e) {
                        log.warn("[{}] 뉴스 등장 대기 timeout", i);
                    }

                    // 중복 뉴스 방지
                    List<WebElement> newsContainers = driver.findElements(By.cssSelector(".jDtQ5"));
                    List<NewsItem> newsList = new ArrayList<>();
                    if (!newsContainers.isEmpty()) {
                        WebElement latestNews = newsContainers.get(newsContainers.size() - 1);
                        List<WebElement> newsLinks = latestNews.findElements(
                                By.cssSelector("a.xZCHj"));
                        log.info("[{}] 추출된 뉴스 개수: {}", i, newsLinks.size());
                        for (WebElement a : newsLinks) {
                            String url = a.getAttribute("href");
                            String title = "";
                            String meta = "";
                            String thumbnail = "";
                            try {
                                title = a.findElement(By.cssSelector(".QbLC8c")).getText();
                            } catch (Exception ignore) {
                            }
                            try {
                                meta = a.findElement(By.cssSelector(".pojp0c")).getText();
                            } catch (Exception ignore) {
                            }
                            try {
                                thumbnail = a.findElement(By.cssSelector(".QtVIpe"))
                                        .getAttribute("src");
                            } catch (Exception ignore) {
                            }
                            newsList.add(new NewsItem(title, url, meta, thumbnail));
                        }
                    }

                    // 카드 정보 파싱(JS)
                    String info = (String) js.executeScript(
                            "var card=arguments[0];const keyword=card.querySelector('.mZ3RIc')?.innerText||'';const volume=card.querySelector('.qNpYPd')?.innerText||'';const status=card.querySelector('.QxIiwc')?.innerText||'';const time=card.querySelector('.A7jE4')?.innerText||'';return `${keyword}|${volume}|${status}|${time}`;",
                            card
                    );
                    log.info("[{}] 카드 정보: {}", i, info);

                    String[] parts = info.split("\\|", -1);
                    if (parts.length >= 1 && !parts[0].isBlank()) {
                        String keyword = parts[0];
                        String volume = parts.length > 1 ? parts[1] : "";
                        int numericVolume = parseVolume(volume);
                        String status = parts.length > 2 ? parts[2] : "";
                        String time = parts.length > 3 ? parts[3] : "";
                        TrendKeywordItem item = new TrendKeywordItem(numericVolume, status, time,
                                newsList);
                        log.info("[{}] TrendItem 저장: {} (뉴스수: {})", i, keyword, newsList.size());
                        resultMap.put(keyword, item);
                    }
                } catch (Exception e) {
                    log.error("[{}] 카드 크롤링 중 예외", i, e);
                }
            }

        } catch (Exception e) {
            log.error("Exception occurred while crawling trends", e);
        } finally {
            driver.quit();
        }

        long elapsed = System.currentTimeMillis() - started;
        log.info("크롤링 전체 소요 시간(ms): {}", elapsed);

        return resultMap;
    }

    /**
     * "검색 2천+회" 등과 같은 문자열에서 정량화된 검색량 값을 추출합니다.
     *
     * @param volumeText 예: "검색 2천+회", "3만+회" 등
     * @return 숫자로 변환된 검색량 (예: 2000, 30000)
     */
    private int parseVolume(String volumeText) {
        if (volumeText == null || volumeText.isBlank()) {
            return 0;
        }
        volumeText = volumeText
                .replace("검색", "")
                .replace("회", "")
                .replace("+", "")
                .replaceAll("\\s", "");

        try {
            if (volumeText.contains("만")) {
                return Integer.parseInt(volumeText.replace("만", "")) * 10_000;
            } else if (volumeText.contains("천")) {
                return Integer.parseInt(volumeText.replace("천", "")) * 1_000;
            } else {
                return Integer.parseInt(volumeText);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
