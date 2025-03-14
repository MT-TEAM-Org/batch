package com.playhive.batch.job;

import com.playhive.batch.crawler.game.GameCrawler;
import com.playhive.batch.job.listener.JobLoggerListener;
import lombok.RequiredArgsConstructor;
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

@Configuration
@RequiredArgsConstructor
public class GameCrawlJobConfig {

    private static final String GAME_CRAWL_JOB_NAME = "gameCrawlJob";
    private static final String GAME_CRAWL_STEP_NAME = "gameCrawlStep";

    private final GameCrawler gameCrawler;

    @Bean
    public Job gameCrawlJob(JobRepository jobRepository, Step gameCrawlStep) {
        return new JobBuilder(GAME_CRAWL_JOB_NAME, jobRepository)
                .listener(new JobLoggerListener())
                .start(gameCrawlStep)
                .build();
    }

    @Bean
    public Step gameCrawlStep(JobRepository jobRepository, Tasklet gameTasklet,
                              PlatformTransactionManager transactionManager) {
        return new StepBuilder(GAME_CRAWL_STEP_NAME, jobRepository)
                .tasklet(gameTasklet, transactionManager)
                .build();
    }

    @Bean
    public Tasklet gameTasklet() {
        return (contribution, chunkContext) -> {
            gameCrawler.crawl();
            return RepeatStatus.FINISHED;
        };
    }
}