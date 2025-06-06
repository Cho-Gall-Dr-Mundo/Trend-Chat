package com.trendchat.trendservice.util;

import com.trendchat.trendservice.dto.TrendItem;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

/**
 * Google Trends 웹사이트에서 실시간 트렌드 키워드 정보를 크롤링하는 유틸리티 컴포넌트입니다.
 * <p>
 * Shadow DOM 기반의 동적 웹 페이지를 Selenium WebDriver와 JavaScript 실행을 통해 분석하며, 키워드, 추정 검색량, 상태, 시간 등의 정보를
 * {@link com.trendchat.trendservice.dto.TrendItem} 형태로 반환합니다.
 * </p>
 *
 * <ul>
 *     <li>대상 URL: {@code https://trends.google.co.kr/trending?geo=KR&hours=4}</li>
 *     <li>헤드리스 Chrome 브라우저로 실행</li>
 *     <li>수집된 데이터는 {@code Map<String, TrendItem>} 형태로 반환됨</li>
 * </ul>
 *
 * @see com.trendchat.trendservice.dto.TrendItem
 */
@Slf4j
@Component
public class GoogleTrendsCrawler {

    /**
     * Google Trends 웹사이트에서 실시간 트렌드 키워드를 크롤링하여 정제된 형태로 반환합니다.
     * <p>
     * 이 메서드는 Selenium WebDriver를 사용하여 Shadow DOM 기반의 동적 페이지를 탐색하고, JavaScript 실행을 통해 키워드, 검색량, 상태,
     * 시간 정보를 추출한 뒤, {@link TrendItem} 객체로 변환하여 키워드 이름을 key로 하는 Map에 담아 반환합니다.
     * </p>
     *
     * <ul>
     *     <li>접속 URL: {@code https://trends.google.co.kr/trending?geo=KR&hours=4}</li>
     *     <li>Headless Chrome 환경에서 실행되며, 렌더링 대기를 위해 5초간 sleep됩니다.</li>
     *     <li>각 카드에서 추출한 데이터 형식은 {@code "키워드|검색량|상태|시간"}입니다.</li>
     * </ul>
     *
     * @return 키워드를 key로, {@link TrendItem} 정보를 value로 가지는 Map 객체
     */
    public Map<String, TrendItem> crawl() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        // Chrome 브라우저 옵션 설정
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--start-maximized");
        options.addArguments("user-agent=Mozilla/5.0");

        WebDriver driver = new ChromeDriver(options);
        Map<String, TrendItem> resultMap = new HashMap<>();

        try {
            driver.get("https://trends.google.co.kr/trending?geo=KR&hours=4");
            Thread.sleep(5000); // JS 렌더링 시간 대기 (안정적인 수집을 위해)

            // Shadow DOM 포함된 카드 정보 추출 (JavaScript로 직접 DOM 탐색)
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String script = """
                        const cards = document.querySelectorAll('td.enOdEe-wZVHld-aOtOmf');
                        return Array.from(cards).map(card => {
                            const keyword = card.querySelector('.mZ3RIc')?.innerText || "";
                            const volume = card.querySelector('.qNpYPd')?.innerText || "";
                            const status = card.querySelector('.QxIiwc')?.innerText || "";
                            const time = card.querySelector('.A7jE4')?.innerText || "";
                            return `${keyword}|${volume}|${status}|${time}`;
                        });
                    """;

            @SuppressWarnings("unchecked")
            List<String> rawResults = (List<String>) js.executeScript(script);

            for (String line : Objects.requireNonNull(rawResults)) {
                String[] parts = line.split("\\|", -1); // 빈 값도 포함

                if (parts.length >= 1 && !parts[0].isBlank()) {
                    String keyword = parts[0];
                    String volume = parts.length > 1 ? parts[1] : "";
                    int numericVolume = parseVolume(volume);
                    String status = parts.length > 2 ? parts[2] : "";
                    String time = parts.length > 3 ? parts[3] : "";

                    TrendItem item = new TrendItem(numericVolume, status, time);
                    resultMap.put(keyword, item);
                }
            }

        } catch (Exception e) {
            log.error("Exception occurred while crawling trends", e);
        } finally {
            driver.quit();
        }

        return resultMap;
    }

    /**
     * 검색량 정량화 메서드
     *
     * @param volumeText "검색 2천+회" 같은 형식의 문자열
     * @return 정량화된 정수값 (예: 2000)
     */
    private int parseVolume(String volumeText) {
        if (volumeText == null || volumeText.isBlank()) {
            return 0;
        }

        // 전처리: "검색", "회", "+", 공백 제거
        volumeText = volumeText
                .replace("검색", "")
                .replace("회", "")
                .replace("+", "")
                .replaceAll("\\s", ""); // 공백 제거

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