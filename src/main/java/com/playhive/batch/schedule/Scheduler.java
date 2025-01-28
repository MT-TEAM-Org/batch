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

	private final Job sampleJob;
	private final JobLauncher jobLauncher;

	@Scheduled(cron = "*/5 * * * * *") // 매 5초마다 실행
	public void sampleJob() throws
		JobInstanceAlreadyCompleteException,
		JobExecutionAlreadyRunningException,
		JobParametersInvalidException,
		JobRestartException {

		JobParameters jobParameters = new JobParametersBuilder()
			.addDate("date", new Date()) // 현재 날짜 및 시간을 파라미터로 추가
			.addLong("time", System.currentTimeMillis()) // 고유한 Long 파라미터 추가
			.toJobParameters();

		this.jobLauncher.run(sampleJob, jobParameters);
	}
}
