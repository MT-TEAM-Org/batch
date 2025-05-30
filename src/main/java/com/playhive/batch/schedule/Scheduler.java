package com.playhive.batch.schedule;

import com.playhive.batch.crawler.game.GameCrawler;
import com.playhive.batch.crawler.news.baseball.BaseballNewsCrawler;
import com.playhive.batch.crawler.news.football.KFootballNewsCrawler;
import com.playhive.batch.crawler.news.football.WFootballNewsCrawler;
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
    private final Job matchCrawlJob;
    //    private final Job gameCrawlJob;
    private final GameCrawler gameCrawler;
    private final JobLauncher jobLauncher;

//    private final BaseballNewsCrawler baseballNewsCrawler;
//    private final WFootballNewsCrawler wFootballNewsCrawler;
//    private final KFootballNewsCrawler kFootballNewsCrawler;

    @Scheduled(cron = "0 0 */2 * * *") // 매일 2시간 마다 실행
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

//    게임 이벤트 크롤링 배치 코드
//    @Scheduled(cron = "0 0 23 * * *") // 매일 오후 11에 다음날에 노출될 게임 정보 크롤링 실행
//    public void gameEventCrawl() throws JobInstanceAlreadyCompleteException,
//            JobExecutionAlreadyRunningException,
//            JobParametersInvalidException, JobRestartException {
//        JobParameters jobParameters = new JobParametersBuilder()
//                .addDate("date", new Date())
//                .addLong("time", System.currentTimeMillis())
//                .toJobParameters();
//
//        this.jobLauncher.run(gameCrawlJob, jobParameters);
//    }

    /**
     * 게임 이벤트 크롤링은 스케줄러만 사용
     */
    @Scheduled(cron = "0 0 23 * * *") // 매일 오후 11에 다음날에 노출될 게임 정보 크롤링 실행
    public void gameEventCrawl() {
        gameCrawler.crawl();
    }

//    @Scheduled(cron = "0 * * * * *") // 뉴스 테스트용 스케줄러 (제거 예정)
//    public void testBaseballCrawl() {
//        baseballNewsCrawler.crawl();
//        kFootballNewsCrawler.crawl();
//        wFootballNewsCrawler.crawl();
//    }
}
