package com.playhive.batch.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.playhive.batch.crawler.news.NewsCrawler;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trigger")
public class TriggerController {

	private final List<NewsCrawler> crawlers;

	@PostMapping("/news")
	public void newsCrawlerTrigger() {
		for (NewsCrawler crawler : crawlers) {
			crawler.crawl();
		}
	}
}
