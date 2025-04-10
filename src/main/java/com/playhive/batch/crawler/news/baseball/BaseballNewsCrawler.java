package com.playhive.batch.crawler.news.baseball;

import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.playhive.batch.crawler.news.NewsCrawler;
import com.playhive.batch.crawler.news.FootballBaseballCrawler;
import com.playhive.batch.news.entity.NewsCategory;
import com.playhive.batch.news.service.NewsService;

@Component
@Transactional
public class BaseballNewsCrawler extends FootballBaseballCrawler implements NewsCrawler {

	private static final String URL = "https://m.sports.naver.com/kbaseball/news?sectionId=wbaseball&sort=latest";

	public BaseballNewsCrawler(NewsService newsService) {
		super(newsService);
	}

	@Override
	public void crawl() {
		LocalDate currentDate = LocalDate.now();
		crawlForDate(URL, currentDate.minusDays(1), true, NewsCategory.BASEBALL); // 어제 뉴스 크롤링
		crawlForDate(URL, currentDate, false, NewsCategory.BASEBALL); // 오늘 뉴스 크롤링
	}
}
