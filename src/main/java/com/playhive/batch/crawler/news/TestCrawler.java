package com.playhive.batch.crawler.news;


import com.playhive.batch.global.config.WebDriverConfig;
import com.playhive.batch.news.dto.NewsSaveRequest;
import com.playhive.batch.news.entity.NewsCategory;
import com.playhive.batch.news.service.NewsService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
public abstract class TestCrawler {

    private static final String DATE_FIELD = "&date=";

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

    /**
     * driverInitialized 플래그를 둔 이유: createDriver() 호출 전에 예외가 터져도 driver는 null이 될 수 있음 driver.get() 호출 도중 예외가 터졌을 경우에도
     * driver는 생성됐으므로 quit()을 해야 함 이렇게 하면 정상 생성 여부와 무관하게 리소스를 안전하게 정리할 수 있음
     */
    protected void crawlForDate(String type, String url, LocalDate date, NewsCategory category) {
        WebDriver driver = null;
        boolean driverInitialized = false;

        try {
            driver = WebDriverConfig.createDriver();
            driverInitialized = true;

            String webUrl = url + DATE_FIELD + date.format(DATE_FORMAT);
            log.info("Crawling URL: {}", webUrl);
            driver.get(webUrl);

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

    private void scrollAndExtractUntilSaved(WebDriver driver, NewsCategory category) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        String recentSource = newsService.findRecentPostDate(category);
        Set<String> seenSources = new HashSet<>();
        List<NewsSaveRequest> collected = new ArrayList<>();

        while (true) {
            List<WebElement> items = driver.findElements(By.className(NEWS_ITEM_CLASS));
            boolean shouldBreak = false;

            for (WebElement item : items) {
                try {
                    String source = getAttr(item, By.tagName("a"), "href");

                    if (source.equals(recentSource)) {
                        log.info("최근 저장된 뉴스 발견. 스크롤 종료: {}", source);
                        shouldBreak = true;
                        break;
                    }

                    if (!seenSources.add(source)) {
                        log.info("중복 뉴스 URL 감지. 스크롤 종료: {}", source);
                        shouldBreak = true;
                        break;
                    }

                    String timeStr = getText(item, By.className(TIME_CLASS));
                    LocalDateTime postDate = LocalDateTime.parse(timeStr, TIME_FORMAT);

                    String title = getText(item, By.className(TITLE_CLASS));
                    String content = getText(item, By.className(CONTENT_CLASS));
                    String thumb = getThumbImage(item);

                    NewsSaveRequest news = NewsSaveRequest.createRequest(title, thumb, source, content, postDate,
                            category);
                    collected.add(news);

                } catch (Exception e) {
                    log.warn("뉴스 아이템 파싱 실패: {}", e.getMessage());
                }
            }

            if (shouldBreak) {
                break;
            }

            long prevHeight = (long) js.executeScript("return document.body.scrollHeight");
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            long newHeight = (long) js.executeScript("return document.body.scrollHeight");
            if (prevHeight == newHeight) {
                break;
            }
        }

        log.info(String.valueOf(collected.size()));
        for (NewsSaveRequest news : collected) {
            newsService.saveNews(news);
        }
    }

    private String getThumbImage(WebElement item) {
        try {
            WebElement img = item.findElement(By.className(THUMB_CLASS)).findElement(By.tagName("img"));
            return img.getAttribute("src");
        } catch (Exception e) {
            return null;
        }
    }

    private String getText(WebElement parent, By locator) {
        try {
            return parent.findElement(locator).getText();
        } catch (Exception e) {
            return "";
        }
    }

    private String getAttr(WebElement parent, By locator, String attr) {
        try {
            return parent.findElement(locator).getAttribute(attr);
        } catch (Exception e) {
            return "";
        }
    }
}
