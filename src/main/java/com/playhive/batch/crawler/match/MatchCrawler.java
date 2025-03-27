package com.playhive.batch.crawler.match;

import java.time.LocalDateTime;

public interface MatchCrawler {
	void crawl(LocalDateTime recentTime);
}
