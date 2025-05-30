package com.playhive.batch.crawler.news.football;

import com.playhive.batch.crawler.news.NewsCrawler;
import com.playhive.batch.crawler.news.TestCrawler;
import com.playhive.batch.news.entity.NewsCategory;
import com.playhive.batch.news.service.NewsService;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class KFootballNewsCrawler extends TestCrawler implements NewsCrawler {

    private static final String URL = "https://m.sports.naver.com/kfootball/news?sectionId=kfootball&sort=latest";

    public KFootballNewsCrawler(NewsService newsService) {
        super(newsService);
    }

    @Override
    public void crawl() {
        crawlForDate("KFOOTBALL", URL, LocalDate.now(), NewsCategory.FOOTBALL); // 오늘 뉴스 크롤링
    }
}
