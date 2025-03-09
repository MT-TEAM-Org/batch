package com.playhive.batch.schedule;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Scheduler {

    private final Job newsCrawlJob;
    private final Job teamCrawlJob;
    private final Job matchCrawlJob;
    private final Job gameCrawlJob;
    private final JobLauncher jobLauncher;

    @Scheduled(cron = "0 0 6 * * *") // 매일 오전 6시 0분 0초에 실행
    public void newsCrawlJob() throws
            JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException,
            JobRestartException {

        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("date", new Date())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        this.jobLauncher.run(newsCrawlJob, jobParameters);
    }

    @Scheduled(cron = "0 0 5 * * *") // 매일 오전 5시 0분 0초에 실행
    public void teamCrawlJob() throws
            JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException,
            JobRestartException {

        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("date", new Date())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        this.jobLauncher.run(teamCrawlJob, jobParameters);
    }

    @Scheduled(cron = "0 0 7 * * *") // 매일 오전 7시 0분 0초에 실행
    public void matchCrawlJob() throws
            JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException,
            JobRestartException {

        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("date", new Date())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        this.jobLauncher.run(matchCrawlJob, jobParameters);
    }

    //    @Scheduled(cron = "0 0 23 * * *") // 매일 오후 11에 다음날에 노출될 게임 정보 크롤링 실행
    @Scheduled(cron = "0 10 0 * * *") // 매일 00시 10분(밤 12시 10분)에 실행 (에러 테스트)
    public void gameEventCrawl() throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("date", new Date())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        this.jobLauncher.run(gameCrawlJob, jobParameters);
    }
}