package com.playhive.batch.crawler.news;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.playhive.batch.global.config.WebDriverConfig;
import com.playhive.batch.news.dto.NewsSaveRequest;
import com.playhive.batch.news.entity.NewsCategory;
import com.playhive.batch.news.service.NewsService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class FootballBaseballCrawler {

	private static final String DATE_FIELD = "&date";
	private static final String EQUALS = "=";

	private static final String NEWS_SECTION_LIST_CLASS = "NewsList_comp_news_list__oXAbN";
	private static final String NEWS_LIST_CLASS = "NewsItem_news_item__fhEmd";
	private static final String TIME_CLASS = "time";
	private static final String TITLE_CLASS = "NewsItem_title__BXkJ6";
	private static final String THUMB_CLASS = "NewsItem_image_wrap__m-fHo";
	private static final String DETAIL_THUMB_CLASS = "NewsEndMain_article_image__SwnGO";
	private static final String PAGE_CLASS = "Pagination_pagination_list__4LIj7";
	private static final String CONTENT_CLASS = "NewsItem_description__+gwua";

	private static final String UL_TAG = "ul";
	private static final String IMG_TAG = "img";
	private static final String SRC_ATTR = "src";
	private static final String A_TAG = "a";
	private static final String BUTTON_TAG = "button";
	private static final String HREF_ATTR = "href";

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

	private final NewsService newsService;
	private WebDriver webDriver;

	public FootballBaseballCrawler(NewsService newsService) {
		this.newsService = newsService;
	}

	protected void crawlForDate(String url, LocalDate date, boolean isYesterday, NewsCategory category) {
		webDriver = WebDriverConfig.createDriver();
		webDriver.get(url + DATE_FIELD + EQUALS + date.format(FORMATTER));

		IntStream.rangeClosed(1, getPaginationCount()).forEach(value -> {
			clickPage(value);
			saveNews(isYesterday, category);
		});
		webDriver.quit();
	}

	private void saveNews(boolean isYesterday, NewsCategory category) {
		for (WebElement section : getNewsSectoinList()) {
			for (WebElement news : getNewsList(section)) {
				String postDate = getPostDate(news);
				LocalDateTime newsPostDate = LocalDateTime.parse(postDate, TIME_FORMATTER);
				// 오전 5시 크롤링이기 때문에 전날 뉴스는 오전 6시이후로만 가져오도록
				if (isYesterday && newsPostDate.toLocalTime().isBefore(LocalTime.of(5, 0))) {
					continue;
				}

				//뉴스 항목 클릭
				String newsUrl = getSource(news);

				//새청 열기
				String originalWindow = openDetailTab(newsUrl);

				// 필요한 데이터 크롤링
				String thumbImg = getDetailThumbImg(webDriver); // 썸네일 이미지 가져오기

				// 원래 창으로 돌아가기
				closeDetailTab(originalWindow);

				saveNews(getTitle(news), thumbImg == null ? getThumbImg(news) : thumbImg, newsUrl, getContent(news), newsPostDate, category);
			}
		}
	}

	private String openDetailTab(String newsUrl) {
		// JavascriptExecutor를 사용하여 새 탭에서 링크 열기
		String script = "window.open('" + newsUrl + "', '_blank');";
		((JavascriptExecutor)webDriver).executeScript(script);

		// 새로운 탭으로 전환
		String originalWindow = webDriver.getWindowHandle();
		for (String windowHandle : webDriver.getWindowHandles()) {
			if (!windowHandle.equals(originalWindow)) {
				webDriver.switchTo().window(windowHandle);
				break;
			}
		}
		return originalWindow;
	}

	private void closeDetailTab(String originalWindow) {
		webDriver.close(); // 새 창 닫기
		webDriver.switchTo().window(originalWindow); // 원래 창으로 전환
	}

	private void saveNews(String title, String thumbImg, String source, String content, LocalDateTime postDate,
		NewsCategory category) {
		this.newsService.saveNews(NewsSaveRequest.createRequest(title, thumbImg, source, content, postDate, category));
	}

	private List<WebElement> getNewsSectoinList() {
		WebElement newsSectionListElement = webDriver.findElement(By.className(NEWS_SECTION_LIST_CLASS));
		return newsSectionListElement.findElements(By.tagName(UL_TAG));
	}

	private List<WebElement> getNewsList(WebElement section) {
		return section.findElements(By.className(NEWS_LIST_CLASS));
	}

	//뉴스 시간가져오기
	private String getPostDate(WebElement news) {
		return news.findElement(By.className(TIME_CLASS)).getText();
	}

	//뉴스 타이틀가져오기
	private String getTitle(WebElement news) {
		return news.findElement(By.className(TITLE_CLASS)).getText();
	}

	//썸네일 이미지 가져오기, 없으면 null
	private String getDetailThumbImg(WebDriver webDriver) {
		try {
			// 최대 10초 대기하여 썸네일 이미지 요소가 나타날 때까지 기다리기
			WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(120));
			WebElement thumbElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.className(DETAIL_THUMB_CLASS)
			));

			return thumbElement.findElement(By.tagName(IMG_TAG)).getAttribute(SRC_ATTR);
		} catch (NoSuchElementException e) {
			log.error(e.getMessage());
			return null;
		} catch (TimeoutException e) {
			log.error("Timed out waiting for thumb image to be visible");
			return null;
		}
	}

	private String getThumbImg(WebElement news) {
		try {
			return news.findElement(By.className(THUMB_CLASS)).findElement(By.tagName(IMG_TAG)).getAttribute(SRC_ATTR);
		} catch (NoSuchElementException e) {
			log.error(e.getMessage());
			return null;
		}
	}

	private String getSource(WebElement news) {
		return news.findElement(By.tagName(A_TAG)).getAttribute(HREF_ATTR);
	}

	private String getContent(WebElement news) {
		return news.findElement(By.className(CONTENT_CLASS)).getText();
	}

	//페이징 개수 가져오기
	private int getPaginationCount() {
		WebElement paginationElement = webDriver.findElement(By.className(PAGE_CLASS));
		List<WebElement> pageLinks = paginationElement.findElements(By.tagName(BUTTON_TAG));
		return pageLinks.size();
	}

	private void clickPage(int page) {
		try {
			webDriver.findElement(By.className("Pagination_pagination_list__4LIj7"))
				.findElement(By.xpath(".//button[text()='" + page + "']")).click();
		} catch (Exception e) {
			log.error("Could not find pagination page {}", page);
		}
	}
}
