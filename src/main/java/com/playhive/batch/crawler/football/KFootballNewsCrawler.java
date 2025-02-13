package com.playhive.batch.crawler.football;

import java.time.LocalDate;

import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

import com.playhive.batch.crawler.Crawler;
import com.playhive.batch.crawler.FootballBaseballCrawler;
import com.playhive.batch.news.service.NewsService;

@Component
public class KFootballNewsCrawler extends FootballBaseballCrawler implements Crawler {

	private static final String URL = "https://sports.news.naver.com/kfootball/news/index?isphoto=N";

	public KFootballNewsCrawler(WebDriver webDriver, NewsService newsService) {
		super(webDriver, newsService);
	}

	@Override
	public void crawl() {
		LocalDate currentDate = LocalDate.now();
		crawlForDate(URL, currentDate.minusDays(1), true); // 어제 뉴스 크롤링
		crawlForDate(URL, currentDate, false); // 오늘 뉴스 크롤링
	}
}
