package com.playhive.batch.job;

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

import com.playhive.batch.job.listener.JobLoggerListener;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SampleJobConfig {

	private static final String SAMPLE_JOB_NAME = "sampleJob";
	private static final String SAMPLE_STEP_NAME = "sampleStep";

	@Bean
	public Job sampleCalendarJob(JobRepository jobRepository, Step sampleCalendarStep) {
		return new JobBuilder(SAMPLE_JOB_NAME, jobRepository)
			.listener(new JobLoggerListener())
			.start(sampleCalendarStep)
			.build();
	}

	@Bean
	public Step sampleCalendarStep(JobRepository jobRepository, Tasklet tasklet,
		PlatformTransactionManager transactionManager) {
		return new StepBuilder(SAMPLE_STEP_NAME, jobRepository)
			.tasklet(tasklet, transactionManager)
			.build();
	}

	@Bean
	public Tasklet tasklet() {
		return ((contribution, chunkContext) -> {
			System.out.println("sampleStep Run Complete");
			return RepeatStatus.FINISHED;
		});
	}
}
