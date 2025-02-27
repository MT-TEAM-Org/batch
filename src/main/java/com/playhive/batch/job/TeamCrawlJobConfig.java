package com.playhive.batch.job;

import java.util.List;

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

@Configuration
@RequiredArgsConstructor
public class TeamCrawlJobConfig {

	private static final String TEAM_CRAWL_JOB_NAME = "teamCrawlJob";
	private static final String TEAM_CRAWL_STEP_NAME = "teamCrawlStep";

	private final List<NewsCrawler> crawlers;

	@Bean
	public Job teamCrawlJob(JobRepository jobRepository, Step newsCrawlStep) {
		return new JobBuilder(TEAM_CRAWL_JOB_NAME, jobRepository)
			.listener(new JobLoggerListener())
			.start(newsCrawlStep)
			.build();
	}

	@Bean
	public Step teamCrawlStep(JobRepository jobRepository, Tasklet newsTasklet,
		PlatformTransactionManager transactionManager) {
		return new StepBuilder(TEAM_CRAWL_STEP_NAME, jobRepository)
			.tasklet(newsTasklet, transactionManager)
			.build();
	}

	@Bean
	public Tasklet teamTasklet() {
		return (contribution, chunkContext) -> {
			for (NewsCrawler crawler : crawlers) {
				crawler.crawl();
			}
			return RepeatStatus.FINISHED;
		};
	}
}
