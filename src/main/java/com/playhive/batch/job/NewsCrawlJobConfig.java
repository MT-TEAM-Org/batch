package com.playhive.batch.job;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

import com.playhive.batch.crawler.news.NewsCrawler;
import com.playhive.batch.job.listener.JobLoggerListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class NewsCrawlJobConfig {

	private static final String NEWS_CRAWL_JOB_NAME = "newsCrawlJob";
	private static final String NEWS_CRAWL_STEP_NAME = "newsCrawlStep";

	private final List<NewsCrawler> crawlers;

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
			for (NewsCrawler crawler : crawlers) {
				crawler.crawl();
			}
			return RepeatStatus.FINISHED;
		};
	}
}
