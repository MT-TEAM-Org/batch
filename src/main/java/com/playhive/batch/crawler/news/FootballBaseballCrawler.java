package com.playhive.batch.crawler.news;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
				// 오전 6시 크롤링이기 때문에 전날 뉴스는 오전 6시이후로만 가져오도록
				if (isYesterday && newsPostDate.toLocalTime().isBefore(LocalTime.of(6, 0))) {
					continue;
				}
				saveNews(getTitle(news), getThumbImg(news), getSource(news), getContent(news), newsPostDate, category);
			}
		}
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
	private String getThumbImg(WebElement news) {
		try {
			return news.findElement(By.className(THUMB_CLASS)).findElement(By.tagName(IMG_TAG)).getAttribute(SRC_ATTR);
		} catch (NoSuchElementException e) {
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
		} catch (NoSuchElementException e) {
			log.error("Could not find pagination page {}", page);
		}
	}
}
