package com.playhive.batch.crawler.football;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.playhive.batch.crawler.Crawler;
import com.playhive.batch.news.dto.NewsSaveRequest;
import com.playhive.batch.news.entity.News;
import com.playhive.batch.news.service.NewsCountService;
import com.playhive.batch.news.service.NewsService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FootballNewsCrawler implements Crawler {

	private static final String URL = "https://sports.news.naver.com/wfootball/news/index?isphoto=N";

	private static final String DATE_FIELD = "&date";
	private static final String EQUALS = "=";
	private static final String PAGE_FIELD = "&page";

	private static final String NEWS_LIST_CLASS = "news_list";

	private static final String TIME_CLASS = "time";
	private static final String TITLE_CLASS = "text";
	private static final String THUMB_CLASS = "thmb";

	private static final String LI_TAG = "li";
	private static final String SPAN_TAG = "span";
	private static final String IMG_TAG = "img";
	private static final String SRC_ATTR = "src";

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

	private final WebDriver webDriver;
	private final NewsService newsService;
	private final NewsCountService newsCountService;

	@Override
	public void crawl() {
		LocalDate currentDate = LocalDate.now();
		crawlForDate(currentDate.minusDays(1), true); // 어제 뉴스 크롤링
		crawlForDate(currentDate, false); // 오늘 뉴스 크롤링
	}

	private void crawlForDate(LocalDate date, boolean yesterday) {
		int totalPages = getPaginationCount(date);
		for (int pageCount = 1; pageCount <= totalPages; pageCount++) {
			webDriver.get(URL + DATE_FIELD + EQUALS + date.format(FORMATTER) + PAGE_FIELD + EQUALS + pageCount);
			saveNews(yesterday);
		}
	}

	private void saveNews(boolean yesterday) {
		for (WebElement news : getNewsList()) {
			String postDate = getPostDate(news);
			LocalDateTime newsPostDate = LocalDateTime.parse(postDate, TIME_FORMATTER);
			// 오전 6시 크롤링이기 때문에 전날 뉴스는 오전 6시이후로만 가져오도록
			if (yesterday && newsPostDate.toLocalTime().isBefore(LocalTime.of(6, 0))) {
				break;
			}

			String title = getTitle(news);
			String thumbImg = getThumbImg(news);

			saveNews(title, thumbImg, newsPostDate);
		}
	}

	private void saveNews(String title, String thumbImg, LocalDateTime postDate) {
		News news = this.newsService.saveNews(NewsSaveRequest.createFootballRequest(title, thumbImg, postDate));
		this.newsCountService.saveNewsCount(news);
	}

	private List<WebElement> getNewsList() {
		WebElement newsListElement = webDriver.findElement(By.className(NEWS_LIST_CLASS));
		return newsListElement.findElements(By.tagName(LI_TAG));
	}

	//뉴스 시간가져오기
	private String getPostDate(WebElement news) {
		return news.findElement(By.className(TIME_CLASS)).getText();
	}

	//뉴스 타이틀가져오기
	private String getTitle(WebElement news) {
		return news.findElement(By.className(TITLE_CLASS)).findElement(By.tagName(SPAN_TAG)).getText();
	} 

	//썸네일 이미지 가져오기, 없으면 null
	private String getThumbImg(WebElement news) {
		try {
			return news.findElement(By.className(THUMB_CLASS)).findElement(By.tagName(IMG_TAG)).getAttribute(SRC_ATTR);
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	//페이징 개수 가져오기
	private int getPaginationCount(LocalDate date) {
		webDriver.get(URL + DATE_FIELD + EQUALS + date.format(FORMATTER) + PAGE_FIELD + EQUALS + 1);
		WebElement paginationElement = webDriver.findElement(By.className("paginate"));
		List<WebElement> pageLinks = paginationElement.findElements(By.tagName("a"));
		return pageLinks.size();
	}
}
