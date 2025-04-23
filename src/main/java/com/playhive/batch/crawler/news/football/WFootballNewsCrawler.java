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
public class WFootballNewsCrawler extends FootballBaseballCrawler implements NewsCrawler {

	private static final String URL = "https://m.sports.naver.com/wfootball/news?sectionId=epl&sort=latest";

	public WFootballNewsCrawler(NewsService newsService) {
		super(newsService);
	}

	@Override
	public void crawl() {
		crawlForDate(URL, LocalDate.now(), NewsCategory.FOOTBALL); // 오늘 뉴스 크롤링
	}
}
