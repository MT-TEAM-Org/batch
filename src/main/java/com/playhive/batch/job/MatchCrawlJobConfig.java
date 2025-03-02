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

import com.playhive.batch.crawler.match.MatchCrawler;
import com.playhive.batch.crawler.news.NewsCrawler;
import com.playhive.batch.job.listener.JobLoggerListener;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class MatchCrawlJobConfig {

	private static final String MATCH_CRAWL_JOB_NAME = "matchCrawlJob";
	private static final String MATCH_CRAWL_STEP_NAME = "matchCrawlStep";

	private final List<MatchCrawler> crawlers;

	@Bean
	public Job matchCrawlJob(JobRepository jobRepository, Step matchCrawlStep) {
		return new JobBuilder(MATCH_CRAWL_JOB_NAME, jobRepository)
			.listener(new JobLoggerListener())
			.start(matchCrawlStep)
			.build();
	}

	@Bean
	public Step matchCrawlStep(JobRepository jobRepository, Tasklet matchTasklet,
		PlatformTransactionManager transactionManager) {
		return new StepBuilder(MATCH_CRAWL_STEP_NAME, jobRepository)
			.tasklet(matchTasklet, transactionManager)
			.build();
	}

	@Bean
	public Tasklet matchTasklet() {
		return (contribution, chunkContext) -> {
			for (MatchCrawler crawler : crawlers) {
				crawler.crawl();
			}
			return RepeatStatus.FINISHED;
		};
	}
}
