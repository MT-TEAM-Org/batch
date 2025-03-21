package com.playhive.batch.crawler.news.football;

import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.playhive.batch.crawler.news.NewsCrawler;
import com.playhive.batch.crawler.news.FootballBaseballCrawler;
import com.playhive.batch.news.entity.NewsCategory;
import com.playhive.batch.news.service.NewsService;

@Component
@Transactional
public class KFootballNewsCrawler extends FootballBaseballCrawler implements NewsCrawler {

	private static final String URL = "https://sports.news.naver.com/kfootball/news/index?isphoto=N";

	public KFootballNewsCrawler(NewsService newsService) {
		super(newsService);
	}

	@Override
	public void crawl() {
		LocalDate currentDate = LocalDate.now();
		crawlForDate(URL, currentDate.minusDays(1), true, NewsCategory.FOOTBALL); // 어제 뉴스 크롤링
		crawlForDate(URL, currentDate, false, NewsCategory.FOOTBALL); // 오늘 뉴스 크롤링
	}
}
