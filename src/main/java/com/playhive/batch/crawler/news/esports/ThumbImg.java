package com.playhive.batch.crawler.news.esports;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ThumbImg {
	private static final String URL = "https://imgnews.pstatic.net/image/origin/%s/%s/%s/%s/%s.jpg?type=nf472_236";
	private static final String HREF_URL_PATTURN = "https://m\\.sports\\.naver\\.com/esports/article/(\\d{3})/(\\d+)";

	private String category;
	private String year;
	private String month;
	private String day;
	private String articleId;

	@Builder
	public ThumbImg(String category, String year, String month, String day, String articleId) {
		this.category = category;
		this.year = year;
		this.month = month;
		this.day = day;
		this.articleId = articleId;
	}

	public void createThumbImgUrl(WebElement news, LocalDateTime newsPostDate) {
		updateCategoryArticleId(news);
		updateDate(newsPostDate);
	}

	public String getUrl() {
		return String.format(URL,
			this.category,
			this.year,
			this.month,
			this.day,
			this.articleId.replaceFirst("^0+", ""));
	}

	private void updateCategoryArticleId(WebElement news) {
		String linkUrl = news.findElement(By.tagName("a")).getAttribute("href");

		Pattern pattern = Pattern.compile(HREF_URL_PATTURN);
		Matcher matcher = pattern.matcher(linkUrl);

		if (matcher.find()) {
			this.category = matcher.group(1);
			this.articleId = matcher.group(2);
		}
	}

	private void updateDate(LocalDateTime newsPostDate) {
		this.year = String.valueOf(newsPostDate.getYear());
		this.month = String.format("%02d", newsPostDate.getMonthValue());
		this.day = String.format("%02d", newsPostDate.getDayOfMonth());
	}
}
