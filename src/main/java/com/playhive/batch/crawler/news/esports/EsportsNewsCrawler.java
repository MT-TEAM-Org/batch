package com.playhive.batch.crawler.news.esports;

import com.playhive.batch.crawler.news.NewsCrawler;
import com.playhive.batch.global.config.WebDriverConfig;
import com.playhive.batch.news.dto.NewsSaveRequest;
import com.playhive.batch.news.entity.NewsCategory;
import com.playhive.batch.news.service.NewsService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
@Transactional
public class EsportsNewsCrawler implements NewsCrawler {

    private static final String URL = "https://game.naver.com/esports/League_of_Legends/news/lol";
    private static final String DATE_FIELD = "?date=";

    private static final String NEWS_LIST_CLASS = "news_list_container__1L7tH";
    private static final String TIME_CLASS = "news_card_source__1jv12";
    private static final String TITLE_CLASS = "news_card_title__1fVVk";
    private static final String LOAD_NEWS_CLASS = "news_list_more_btn__3QwSl";
    private static final String CONTENT_CLASS = "news_card_subcontent__23_y1";
    private static final String LI_TAG = "li.news_card_item__2lh4o";
    private static final String SVG_TAG = "svg";
    private static final String A_TAG = "a";
    private static final String HREF_ATTR = "href";
    private static final String TIME_PATTERN = "(\\d+)\\s*(분|시간)\\s*전";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private final NewsService newsService;
    private WebDriver webDriver;

    @Override
    public void crawl() {
        try {
            webDriver = WebDriverConfig.createDriver();
            LocalDate currentDate = LocalDate.now();
            crawlForDate(currentDate);
        } catch (Exception e) {
            log.error("크롤링 중 에러 발생", e);
        } finally {
            if (webDriver != null) {
                try {
                    webDriver.quit();
                } catch (Exception e) {
                    log.warn("WebDriver quit 실패", e);
                }
            }
        }
    }

    private void crawlForDate(LocalDate date) {
        String fullUrl = URL + DATE_FIELD + date;
        webDriver.get(fullUrl);
        log.info("[크롤 시작] URL: {}", fullUrl);

        clickLoadNews();

        String recentUrl = newsService.findRecentPostDate(NewsCategory.ESPORTS);
        Set<String> seenUrls = new HashSet<>();
        List<NewsSaveRequest> newsList = new ArrayList<>();

        for (WebElement news : getNewsList()) {
            String postDateStr = getPostDate(news);
            if (postDateStr == null) {
                log.debug("⛔ 무시됨 - 인기순 기사");
                continue;
            }

            String source = getSource(news);
            if (source == null || source.isBlank()) {
                log.debug("⛔ 무시됨 - URL 없음");
                continue;
            }

            if (source.equals(recentUrl)) {
                log.info("🛑 수집 중단 - 이미 저장된 최신 뉴스 도달: {}", source);
                break;
            }

            if (!seenUrls.add(source)) {
                log.debug("🔁 중복 URL (세션 내): {}", source);
                continue;
            }

            if (newsService.existsByUrl(source)) {
                log.debug("📦 DB에 이미 존재하는 뉴스: {}", source);
                continue;
            }

            LocalDateTime postDate = parseRelativeTime(postDateStr);
            String title = getTitle(news);
            String content = getContent(news);
            String thumb = extractImage(news);
            
            if (thumb == null || thumb.isBlank()) {
                log.debug("🔍 썸네일 없음, 기본값 또는 상세 진입 고려: {}", source);
            }

            if (title.isBlank()) {
                log.debug("⛔ 무시됨 - 제목 없음: {}", source);
                continue;
            }

            log.debug("✅ 기사 수집됨: [{}] {} ({})", postDate, title, source);
            newsList.add(NewsSaveRequest.createRequest(
                    title, null, source, content, postDate, NewsCategory.ESPORTS
            ));
        }

        log.info("총 {}건의 뉴스 저장 시도 중...", newsList.size());
        save(newsList);
    }

    private void clickLoadNews() {
        while (true) {
            try {
                WebElement loadMoreButton = webDriver.findElement(By.className(LOAD_NEWS_CLASS));
                loadMoreButton.click();
                Thread.sleep(1000); // wait for new content
            } catch (NoSuchElementException | StaleElementReferenceException | InterruptedException e) {
                break;
            }
        }
    }

    private List<WebElement> getNewsList() {
        WebElement newsListElement = webDriver.findElement(By.className(NEWS_LIST_CLASS));
        return newsListElement.findElements(By.cssSelector(LI_TAG));
    }

    private String getPostDate(WebElement news) {
        try {
            List<WebElement> timeElements = news.findElements(By.className(TIME_CLASS));
            WebElement timeElement = timeElements.get(1);
            timeElement.findElement(By.tagName(SVG_TAG)); // 인기순이면 SVG 있음
            return null; // 인기순일 경우 무시
        } catch (NoSuchElementException e) {
            return news.findElements(By.className(TIME_CLASS)).get(1).getText();
        }
    }

    private String getTitle(WebElement news) {
        return news.findElement(By.className(TITLE_CLASS)).getText().trim();
    }

    private String getSource(WebElement news) {
        return news.findElement(By.tagName(A_TAG)).getAttribute(HREF_ATTR);
    }

    private String getContent(WebElement news) {
        return news.findElement(By.className(CONTENT_CLASS)).getText().trim();
    }

    private LocalDateTime parseRelativeTime(String timeStr) {
        Matcher matcher = Pattern.compile(TIME_PATTERN).matcher(timeStr);
        if (matcher.find()) {
            int amount = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            return switch (unit) {
                case "분" -> LocalDateTime.now().minusMinutes(amount);
                case "시간" -> LocalDateTime.now().minusHours(amount);
                default -> LocalDateTime.now();
            };
        }
        try {
            return LocalDateTime.parse(timeStr, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            return LocalDateTime.now();
        }
    }

    private String extractImage(WebElement news) {
        try {
            WebElement thumbnailDiv = news.findElement(By.className("news_card_thumbnail__3thTg"));
            String style = thumbnailDiv.getAttribute("style"); // style="background-image: url(...)"
            Matcher matcher = Pattern.compile("url\\([\"']?(.*?)[\"']?\\)").matcher(style);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (NoSuchElementException e) {
            log.debug("❌ 썸네일 div 없음");
        }
        return null;
    }

    private void save(List<NewsSaveRequest> newsList) {
        for (NewsSaveRequest news : newsList) {
            try {
                newsService.saveNews(news);
                log.debug("저장 완료: {}", news.getTitle());
            } catch (Exception e) {
                log.error("❌ 저장 실패: {} / URL: {}", news.getTitle(), news.getSource(), e);
            }
        }
    }
}