package com.playhive.batch.crawler.news.esports;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.playhive.batch.crawler.news.NewsCrawler;
import com.playhive.batch.global.config.WebDriverConfig;
import com.playhive.batch.news.dto.NewsSaveRequest;
import com.playhive.batch.news.entity.NewsCategory;
import com.playhive.batch.news.service.NewsService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Transactional
public class EsportsNewsCrawler implements NewsCrawler {

	private static final String URL = "https://game.naver.com/esports/League_of_Legends/news/lol";

	private static final String DATE_FIELD = "?date";
	private static final String EQUALS = "=";

	private static final String NEWS_LIST_CLASS = "news_list_container__1L7tH";

	private static final String TIME_CLASS = "news_card_source__1jv12";
	private static final String TITLE_CLASS = "news_card_title__1fVVk";
	private static final String LOAD_NEWS_CLASS = "news_list_more_btn__3QwSl";
	private static final String CONTENT_CLASS = "news_card_subcontent__23_y1";
	private static final String PAGE_LIST_CLASS = "news_paging_list__38qR4";

	private static final String LI_TAG = "li.news_card_item__2lh4o";
	private static final String SVG_TAG = "svg";
	private static final String A_TAG = "a";
	private static final String HREF_ATTR = "href";

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

	private static final String TIME_PATTERN = "(\\d+)\\s*(분|시간)\\s*전";
	private static final String DATE_BTN_PATTERN = "MM.dd";

	private static final String MINUTE_KOREAN = "분";
	private static final String HOUR_KOREAN = "시간";

	private WebDriver webDriver;
	private final NewsService newsService;

	public EsportsNewsCrawler(NewsService newsService) {
		this.newsService = newsService;
	}

	@Override
	public void crawl() {
		webDriver = WebDriverConfig.createDriver();
		LocalDate currentDate = LocalDate.now();
		crawlForDate(currentDate.minusDays(1), true); // 어제 뉴스 크롤링
		crawlForDate(currentDate, false); // 오늘 뉴스 크롤링
		webDriver.quit();
	}

	private void crawlForDate(LocalDate date, boolean isYesterday) {
		webDriver.get(URL + DATE_FIELD + EQUALS + date);

		try {
			//ESports기사는 직접 URL에 날짜 입력해서 접근 시 당일날짜로 redirect되는 이슈가 있어 이전날짜는 직접 클릭해서 넘어가는걸로
			clickDateBtn(isYesterday, date);

			//뉴스 더보기 클릭으로 페이징이 되어있어 클릭이 안될 때까지 클릭하여 전체기사 가져오기
			clickLoadNews();
			saveNews(isYesterday);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	private void clickDateBtn(boolean isYesterday, LocalDate date) {
		if (isYesterday) {
			String dateLinkText = date.format(DateTimeFormatter.ofPattern(DATE_BTN_PATTERN));
			try {
				webDriver.findElement(By.className(PAGE_LIST_CLASS)).findElement(By.partialLinkText(dateLinkText)).click();
			} catch (NoSuchElementException e) {
				log.error("{} Element not found", dateLinkText, e);
			}
		}
	}

	private void clickLoadNews() {
		while (true) {
			try {
				// 뉴스 더보기 버튼을 찾고 클릭
				WebElement loadMoreButton = webDriver.findElement(By.className(LOAD_NEWS_CLASS));
				loadMoreButton.click();
			} catch (NoSuchElementException | StaleElementReferenceException e) { // 뉴스 더보기 버튼이 없거만 클릭이 안되면 종료
				break;
			}
		}
	}

	private void saveNews(boolean isYesterday) {
		for (WebElement news : getNewsList()) {
			String postDate = getPostDate(news);
			//뉴스계시날짜가 없으면 기사가 없는것
			if (postDate == null) {
				break;
			}
			LocalDateTime newsPostDate = parseRelativeTime(postDate);
			// 오전 6시 크롤링이기 때문에 전날 뉴스는 오전 6시이후로만 가져오도록
			if (isYesterday && newsPostDate.toLocalTime().isBefore(LocalTime.of(6, 0))) {
				continue;
			}
			saveNews(getTitle(news), getThumbImg(news, newsPostDate), getSource(news), getContent(news), newsPostDate);
		}
	}

	private void saveNews(String title, String thumbImg, String source, String content, LocalDateTime postDate) {
		this.newsService.saveNews(
			NewsSaveRequest.createRequest(title, thumbImg, source, content, postDate, NewsCategory.ESPORTS));
	}

	private List<WebElement> getNewsList() {
		WebElement newsListElement = webDriver.findElement(By.className(NEWS_LIST_CLASS));
		return newsListElement.findElements(By.cssSelector(LI_TAG));
	}

	//뉴스 시간가져오기
	private String getPostDate(WebElement news) {
		List<WebElement> timeElements = news.findElements(By.className(TIME_CLASS));
		WebElement firstTimeElement = timeElements.get(1);
		// SVG 존재 여부 확인 존재하면 당일 기사가 없어서 인기순으로 redirect된 상황
		try {
			firstTimeElement.findElement(By.tagName(SVG_TAG));
			return null;
		} catch (NoSuchElementException e) {
			return firstTimeElement.getText();
		}
	}

	//뉴스 타이틀가져오기
	private String getTitle(WebElement news) {
		return news.findElement(By.className(TITLE_CLASS)).getText();
	}

	//뉴스썸네일 만들기, ESports는 해외축구, 국내야구와 다르게 다른 뉴스페이지인데 썸네일을 가져올 수가 없어 조합하여 사용
	private String getThumbImg(WebElement news, LocalDateTime newsPostDate) {
		ThumbImg thumbImg = new ThumbImg();
		thumbImg.createThumbImgUrl(news, newsPostDate);
		return thumbImg.getUrl();
	}

	private String getSource(WebElement news) {
		return news.findElement(By.tagName(A_TAG)).getAttribute(HREF_ATTR);
	}

	private String getContent(WebElement news) {
		return news.findElement(By.className(CONTENT_CLASS)).getText();
	}

	//ESports는 해외축구, 국내야구와 다르게 24시간까지는 날짜가 아니라 *분전, *시간전으로 표기되어 자세한 뉴스 날짜를 알수가 없어 현재 시간 기준으로 계산
	private LocalDateTime parseRelativeTime(String relativeTime) {
		Pattern pattern = Pattern.compile(TIME_PATTERN);
		Matcher matcher = pattern.matcher(relativeTime);
		if (matcher.find()) {
			return checkUnit(Integer.parseInt(matcher.group(1)), matcher.group(2));
		}
		return LocalDateTime.parse(relativeTime, TIME_FORMATTER);
	}

	private LocalDateTime checkUnit(int amount, String unit) {
		LocalDateTime now = LocalDateTime.now();
		return switch (unit) {
			case MINUTE_KOREAN -> now.minusMinutes(amount);
			case HOUR_KOREAN -> now.minusHours(amount);
			default -> throw new IllegalArgumentException("알 수 없는 시간 단위: " + unit);
		};
	}
}
