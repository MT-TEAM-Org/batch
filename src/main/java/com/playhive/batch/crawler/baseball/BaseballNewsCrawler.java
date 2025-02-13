package com.playhive.batch.crawler.baseball;

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
import org.springframework.stereotype.Component;

import com.playhive.batch.crawler.Crawler;
import com.playhive.batch.crawler.FootballBaseballCrawler;
import com.playhive.batch.news.dto.NewsSaveRequest;
import com.playhive.batch.news.service.NewsService;

import lombok.RequiredArgsConstructor;

@Component
public class BaseballNewsCrawler extends FootballBaseballCrawler implements Crawler {

	private static final String URL = "https://sports.news.naver.com/kbaseball/news/index?isphoto=N";

	public BaseballNewsCrawler(WebDriver webDriver, NewsService newsService) {
		super(webDriver, newsService);
	}

	@Override
	public void crawl() {
		LocalDate currentDate = LocalDate.now();
		crawlForDate(URL, currentDate.minusDays(1), true); // 어제 뉴스 크롤링
		crawlForDate(URL, currentDate, false); // 오늘 뉴스 크롤링
	}
}
