package com.playhive.batch.crawler.news;


import com.playhive.batch.global.config.WebDriverConfig;
import com.playhive.batch.news.dto.NewsSaveRequest;
import com.playhive.batch.news.entity.NewsCategory;
import com.playhive.batch.news.service.NewsService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
public class TestCrawler {

    private static final String DATE_FIELD = "&date=";

    // 네이버 스포츠 뉴스 CSS 클래스명들
    private static final String NEWS_ITEM_CLASS = "NewsItem_news_item__fhEmd";
    private static final String TIME_CLASS = "time";
    private static final String TITLE_CLASS = "NewsItem_title__BXkJ6";
    private static final String CONTENT_CLASS = "NewsItem_description__+gwua";
    private static final String THUMB_CLASS = "NewsItem_image_wrap__m-fHo";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private final NewsService newsService;

    public TestCrawler(NewsService newsService) {
        this.newsService = newsService;
    }

    public void crawlForDate(String type, String url, LocalDate date, NewsCategory category) {
        WebDriver driver = null;
        boolean driverInitialized = false;

        try {
            driver = WebDriverConfig.createDriver();
            driverInitialized = true;

            String webUrl = url + DATE_FIELD + date.format(DATE_FORMAT);
            log.info("Crawling URL: {}", webUrl);
            driver.get(webUrl);

            // 페이지 로딩 대기
            Thread.sleep(3000);

            scrollAndExtractUntilSaved(driver, category);

            log.info("{} Crawling finished", type);

        } catch (Exception e) {
            log.error("크롤링 도중 예외 발생", e);
        } finally {
            if (driverInitialized && driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    log.warn("WebDriver quit 실패", e);
                }
            }
        }
    }

    /**
     * 스크롤하면서 뉴스 추출 (중복 체크 및 최근 저장된 뉴스까지만)
     */
    private void scrollAndExtractUntilSaved(WebDriver driver, NewsCategory category) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        String recentSource = newsService.findRecentPostDate(category);
        Set<String> seenSources = new HashSet<>();
        List<NewsSaveRequest> collected = new ArrayList<>();

        int scrollCount = 0;
        int maxScrolls = 50; // 무한 스크롤 방지

        while (scrollCount < maxScrolls) {
            List<WebElement> items = findNewsItems(driver);
            boolean shouldBreak = false;

            log.debug("현재 페이지에서 {}개의 뉴스 아이템 발견", items.size());

            for (WebElement item : items) {
                try {
                    String source = getNewsUrl(item);

                    if (source == null || source.isEmpty()) {
                        continue;
                    }

                    // 최근 저장된 뉴스와 비교
                    if (source.equals(recentSource)) {
                        log.info("최근 저장된 뉴스 발견. 스크롤 종료: {}", source);
                        shouldBreak = true;
                        break;
                    }

                    // 중복 체크
                    if (!seenSources.add(source)) {
                        log.debug("이미 처리된 뉴스 URL: {}", source);
                        continue;
                    }

                    // 뉴스 데이터 추출
                    NewsSaveRequest news = extractNewsData(item, category);
                    if (news != null) {
                        collected.add(news);
                        log.debug("뉴스 수집: {}", news.getTitle());
                    }

                } catch (Exception e) {
                    log.warn("뉴스 아이템 파싱 실패: {}", e.getMessage());
                }
            }

            if (shouldBreak) {
                break;
            }

            // 스크롤 수행
            if (!performScroll(js)) {
                log.info("더 이상 스크롤할 콘텐츠가 없습니다.");
                break;
            }

            scrollCount++;
            log.debug("스크롤 수행: {}/{}", scrollCount, maxScrolls);

            // 새로운 콘텐츠 로딩 대기
            waitForNewContent();
        }

        log.info("총 {}개의 뉴스를 수집했습니다.", collected.size());

        // 수집된 뉴스들을 역순으로 저장 (오래된 것부터)
        Collections.reverse(collected);
        for (NewsSaveRequest news : collected) {
            try {
                newsService.saveNews(news);
            } catch (Exception e) {
                log.error("뉴스 저장 실패: {}", news.getTitle(), e);
            }
        }
    }

    /**
     * 뉴스 아이템 요소들 찾기
     */
    private List<WebElement> findNewsItems(WebDriver driver) {
        // 여러 가능한 셀렉터로 시도
        String[] selectors = {
                NEWS_ITEM_CLASS,
                "div[class*='news_item']",
                "div[class*='article_item']",
                "article",
                "li[class*='item']"
        };

        for (String selector : selectors) {
            try {
                List<WebElement> elements;
                if (selector.startsWith("div[") || selector.startsWith("article") || selector.startsWith("li[")) {
                    elements = driver.findElements(By.cssSelector(selector));
                } else {
                    elements = driver.findElements(By.className(selector));
                }

                if (!elements.isEmpty()) {
                    log.debug("셀렉터 '{}'로 {}개 요소 발견", selector, elements.size());
                    return elements;
                }
            } catch (Exception e) {
                log.debug("셀렉터 '{}' 시도 실패: {}", selector, e.getMessage());
            }
        }

        return new ArrayList<>();
    }

    /**
     * 뉴스 데이터 추출
     */
    private NewsSaveRequest extractNewsData(WebElement item, NewsCategory category) {
        try {
            String title = getText(item, By.className(TITLE_CLASS));
            if (title.isEmpty()) {
                title = getText(item, By.cssSelector("h3, h2, h4, [class*='title']"));
            }

            String content = getText(item, By.className(CONTENT_CLASS));
            if (content.isEmpty()) {
                content = getText(item, By.cssSelector("p, [class*='desc'], [class*='summary']"));
            }

            String source = getNewsUrl(item);
            String thumb = getThumbImage(item);

            // 시간 파싱 (네이버 스포츠 페이지의 실제 시간 형식에 맞게 조정)
            LocalDateTime postDate = parsePostDate(item);

            if (title.isEmpty() || source.isEmpty()) {
                log.debug("필수 데이터 누락 - title: {}, source: {}", title, source);
                return null;
            }

            return NewsSaveRequest.createRequest(title, thumb, source, content, postDate, category);

        } catch (Exception e) {
            log.warn("뉴스 데이터 추출 실패", e);
            return null;
        }
    }

    /**
     * 뉴스 URL 추출
     */
    private String getNewsUrl(WebElement item) {
        String url = getAttr(item, By.tagName("a"), "href");
        if (url != null && !url.isEmpty()) {
            if (url.startsWith("/")) {
                return "https://m.sports.naver.com" + url;
            } else if (url.startsWith("http")) {
                return url;
            }
        }
        return null;
    }

    /**
     * 발행시간 파싱
     */
    private LocalDateTime parsePostDate(WebElement item) {
        try {
            String timeStr = getText(item, By.className(TIME_CLASS));
            if (timeStr.isEmpty()) {
                timeStr = getText(item, By.cssSelector("time, [class*='time'], [class*='date']"));
            }

            if (!timeStr.isEmpty()) {
                // 다양한 시간 형식 처리
                if (timeStr.matches("\\d{4}\\.\\d{2}\\.\\d{2} \\d{2}:\\d{2}")) {
                    return LocalDateTime.parse(timeStr, TIME_FORMAT);
                } else if (timeStr.contains("분 전") || timeStr.contains("시간 전")) {
                    return LocalDateTime.now(); // 상대시간은 현재시간으로 처리
                }
            }

            return LocalDateTime.now();

        } catch (Exception e) {
            log.debug("시간 파싱 실패: {}", e.getMessage());
            return LocalDateTime.now();
        }
    }

    /**
     * 썸네일 이미지 URL 추출
     */
    private String getThumbImage(WebElement item) {
        try {
            WebElement img = item.findElement(By.className(THUMB_CLASS))
                    .findElement(By.tagName("img"));
            return img.getAttribute("src");
        } catch (Exception e) {
            // 다른 방법으로 이미지 찾기
            try {
                WebElement img = item.findElement(By.cssSelector("img"));
                return img.getAttribute("src");
            } catch (Exception e2) {
                return null;
            }
        }
    }

    /**
     * 스크롤 수행
     *
     * @return 스크롤이 더 가능하면 true, 더 이상 스크롤할 수 없으면 false
     */
    private boolean performScroll(JavascriptExecutor js) {
        try {
            long prevHeight = (long) js.executeScript("return document.body.scrollHeight");
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

            Thread.sleep(3000); // 스크롤 후 대기

            long newHeight = (long) js.executeScript("return document.body.scrollHeight");
            return newHeight > prevHeight;

        } catch (Exception e) {
            log.warn("스크롤 수행 중 오류", e);
            return false;
        }
    }

    /**
     * 새로운 콘텐츠 로딩 대기
     */
    private void waitForNewContent() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("대기 중 인터럽트 발생", e);
        }
    }

    /**
     * 요소에서 텍스트 추출
     */
    private String getText(WebElement parent, By locator) {
        try {
            return parent.findElement(locator).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 요소에서 속성값 추출
     */
    private String getAttr(WebElement parent, By locator, String attr) {
        try {
            return parent.findElement(locator).getAttribute(attr);
        } catch (Exception e) {
            return "";
        }
    }
}