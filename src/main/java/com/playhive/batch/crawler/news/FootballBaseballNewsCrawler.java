package com.playhive.batch.crawler.news;

import com.playhive.batch.global.config.WebDriverConfig;
import com.playhive.batch.news.dto.NewsSaveRequest;
import com.playhive.batch.news.entity.NewsCategory;
import com.playhive.batch.news.service.NewsService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Slf4j
@RequiredArgsConstructor
public class FootballBaseballNewsCrawler {

    private final NewsService newsService;

    private static final String DATE_FIELD = "&date=";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private static final String ITEM_CLASS = "NewsItem_news_item__fhEmd";
    private static final String TITLE_CLASS = "NewsItem_title__BXkJ6";
    private static final String CONTENT_CLASS = "NewsItem_description__+gwua";
    private static final String TIME_CLASS = "time";

    public void crawlForDate(String url, LocalDate date, NewsCategory category) {
        WebDriver driver = null;
        try {
            driver = WebDriverConfig.createDriver();
            String fullUrl = url + DATE_FIELD + date.format(DATE_FORMAT);
            log.info("크롤링 시작: {}", fullUrl);
            driver.get(fullUrl);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className(ITEM_CLASS)));

            List<NewsSaveRequest> newsList = collectNews(driver, category);
            save(newsList);

            log.info("{} 크롤링 완료", category.getText());
        } catch (Exception e) {
            log.error("크롤링 실패", e);
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    log.warn("드라이버 종료 실패", e);
                }
            }
        }
    }

    private List<NewsSaveRequest> collectNews(WebDriver driver, NewsCategory category) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String recent = newsService.findRecentPostDate(category);

        Set<String> seenUrls = new HashSet<>();
        List<NewsSaveRequest> result = new ArrayList<>();
        int scroll = 0;

        while (scroll++ < 50) {
            List<WebElement> items = driver.findElements(By.className(ITEM_CLASS));
            log.debug("뉴스 아이템 {}개 발견", items.size());

            boolean stop = false;
            for (WebElement item : items) {
                String url = getAttr(item, By.tagName("a"), "href");
                if (url == null || url.isBlank()) {
                    continue;
                }
                if (url.startsWith("/")) {
                    url = "https://m.sports.naver.com" + url;
                }

                if (url.equals(recent)) {
                    stop = true;
                    break;
                }
                if (!seenUrls.add(url)) {
                    continue;
                }

                String title = getText(item, By.className(TITLE_CLASS));
                String content = getText(item, By.className(CONTENT_CLASS));
                String thumb = extractImage(item);

                // 썸네일 없으면 상세 진입
                if (thumb == null || thumb.isBlank()) {
                    thumb = fetchDetailThumbByJsoup(url);
                    if (thumb == null || thumb.isBlank()) {
                        log.warn("상세 썸네일 없음 (Jsoup): {}", url);
                    }
                }

                LocalDateTime postDate = parseTime(item);
                if (title.isBlank()) {
                    continue;
                }

                NewsSaveRequest news = NewsSaveRequest.createRequest(title, thumb, url, content, postDate, category);
                result.add(news);
            }

            if (stop || !scrollDown(js)) {
                break;
            }
            waitForNewContent();
        }

        Collections.reverse(result);
        return result;
    }

    private String fetchDetailThumbByJsoup(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla") // user-agent 필수
                    .timeout(5000)
                    .get();

            // 변경된 클래스명 기준으로 이미지 찾기
            Element imgEl = doc.selectFirst(".ArticleImage_image_wrap__cm1wZ img");
            if (imgEl != null) {
                return imgEl.attr("src");
            }
        } catch (Exception e) {
            log.warn("Jsoup 상세 썸네일 실패: {}", url, e);
        }
        return null;
    }

    private String getText(WebElement parent, By by) {
        try {
            return parent.findElement(by).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private String getAttr(WebElement parent, By by, String attr) {
        try {
            return parent.findElement(by).getAttribute(attr);
        } catch (Exception e) {
            return "";
        }
    }

    private String extractImage(WebElement item) {
        try {
            WebElement img = item.findElement(By.tagName("img"));
            String src = img.getAttribute("src");
            if (src == null || src.isBlank() || src.startsWith("data:")) {
                src = img.getAttribute("data-src");
            }
            return (src != null && !src.isBlank()) ? src : null;
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseTime(WebElement item) {
        try {
            String timeStr = getText(item, By.className(TIME_CLASS));
            if (timeStr.matches("\\d{4}\\.\\d{2}\\.\\d{2} \\d{2}:\\d{2}")) {
                return LocalDateTime.parse(timeStr, TIME_FORMAT);
            } else if (timeStr.contains("분 전")) {
                return LocalDateTime.now().minusMinutes(Long.parseLong(timeStr.replaceAll("[^0-9]", "")));
            } else if (timeStr.contains("시간 전")) {
                return LocalDateTime.now().minusHours(Long.parseLong(timeStr.replaceAll("[^0-9]", "")));
            }
        } catch (Exception e) {
            log.debug("시간 파싱 실패", e);
        }
        return LocalDateTime.now();
    }

    private boolean scrollDown(JavascriptExecutor js) {
        try {
            long prev = (long) js.executeScript("return document.body.scrollHeight");
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2000);
            long curr = (long) js.executeScript("return document.body.scrollHeight");
            return curr > prev;
        } catch (Exception e) {
            log.warn("스크롤 실패", e);
            return false;
        }
    }

    private void waitForNewContent() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void save(List<NewsSaveRequest> list) {
        for (NewsSaveRequest news : list) {
            try {
                newsService.saveNews(news);
                log.debug("저장 완료: {}", news.getTitle());
            } catch (Exception e) {
                log.error("저장 실패: {}", news.getTitle(), e);
            }
        }
    }
}