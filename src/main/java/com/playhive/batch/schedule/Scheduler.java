package com.playhive.batch.schedule;

import java.util.Date;

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

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Scheduler {

	private final Job newsCrawlJob;
	private final Job teamCrawlJob;
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
}
