package com.playhive.batch.crawler.football;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.playhive.batch.crawler.Crawler;
import com.playhive.batch.crawler.FootballBaseballCrawler;
import com.playhive.batch.news.entity.NewsCategory;
import com.playhive.batch.news.service.NewsService;

@Component
public class WFootballNewsCrawler extends FootballBaseballCrawler implements Crawler {

	private static final String URL = "https://sports.news.naver.com/wfootball/news/index?isphoto=N";

	public WFootballNewsCrawler(NewsService newsService) {
		super(newsService);
	}

	@Override
	public void crawl() {
		LocalDate currentDate = LocalDate.now();
		crawlForDate(URL, currentDate.minusDays(1), true, NewsCategory.FOOTBALL); // 어제 뉴스 크롤링
		crawlForDate(URL, currentDate, false, NewsCategory.FOOTBALL); // 오늘 뉴스 크롤링
	}
}
