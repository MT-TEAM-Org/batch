package com.playhive.batch.job;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.playhive.batch.crawler.Crawler;
import com.playhive.batch.crawler.football.FootballNewsCrawler;
import com.playhive.batch.job.listener.JobLoggerListener;
import com.playhive.batch.news.entity.NewsCount;
import com.playhive.batch.news.service.NewsCountService;
import com.playhive.batch.news.service.NewsService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class NewsCrawlJobConfig {

	private static final String NEWS_CRAWL_JOB_NAME = "newsCrawlJob";
	private static final String NEWS_CRAWL_STEP_NAME = "newsCrawlStep";

	private final WebDriver webDriver;
	private final List<Crawler> crawlers;

	@Bean
	public Job newsCrawlJob(JobRepository jobRepository, Step newsCrawlStep) {
		return new JobBuilder(NEWS_CRAWL_JOB_NAME, jobRepository)
			.listener(new JobLoggerListener())
			.start(newsCrawlStep)
			.build();
	}

	@Bean
	public Step newsCrawlStep(JobRepository jobRepository, Tasklet newsTasklet,
		PlatformTransactionManager transactionManager) {
		return new StepBuilder(NEWS_CRAWL_STEP_NAME, jobRepository)
			.tasklet(newsTasklet, transactionManager)
			.build();
	}

	@Bean
	public Tasklet newsTasklet() {
		return (contribution, chunkContext) -> {
			for (Crawler crawler : crawlers) {
				crawler.crawl();
			}
			webDriver.close();
			return RepeatStatus.FINISHED;
		};
	}
}
