package com.playhive.batch.football;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;

import com.playhive.batch.IntegrationTestSupport;
import com.playhive.batch.crawler.Crawler;
import com.playhive.batch.crawler.football.FootballNewsCrawler;
import com.playhive.batch.news.repository.NewsRepository;
import com.playhive.batch.news.service.NewsService;

public class FootBallCrawlerTest extends IntegrationTestSupport {

	@Autowired
	private NewsService newsService;
	@Autowired
	private NewsRepository newsRepository;
	@Autowired
	private WebDriver webDriver;

	@AfterEach
	public void cleanup() {
		newsRepository.deleteAllInBatch();
	}

	@DisplayName("축구뉴스를 크롤링한다.")
	@Test
	void footballCrawlTest() {
		Crawler footBallCrawler = new FootballNewsCrawler(webDriver, newsService);
		footBallCrawler.crawl();
	}
}
