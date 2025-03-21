package com.playhive.batch.crawler.match.football;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.playhive.batch.crawler.match.MatchCrawler;
import com.playhive.batch.global.config.WebDriverConfig;
import com.playhive.batch.match.match.domain.MatchCategory;
import com.playhive.batch.match.match.dto.service.request.MatchServiceRequest;
import com.playhive.batch.match.match.service.MatchReadService;
import com.playhive.batch.match.match.service.MatchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EplMatchCrawler implements MatchCrawler {

	private static final String URL = "https://m.sports.naver.com/wfootball/schedule/index?date=";

	private static final String LEAGUE_CLASS = "ScheduleAllType_match_list_group__1nFDy";
	private static final String LEAGUE_NAME = "ScheduleAllType_title___Qfd4";
	private static final String MATCH_CLASS = "MatchBox_match_item__3_D0Q";
	private static final String MATCH_TIME_CLASS = "MatchBox_time__nIEfd";
	private static final String MATCH_PLACE = "MatchBox_stadium__13gft";
	private static final String TEAM_NAME_CLASS = "MatchBoxHeadToHeadArea_team__40JQL";
	private static final String TEAM_LOGO_CLASS = "MatchBoxHeadToHeadArea_emblem__15NcN";

	private static final String IMG_TAG = "img";
	private static final String SRC_ATTR = "src";

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private static final String EPL_NAME = "프리미어리그";
	private static final String BLANK = " ";

	private final MatchService matchService;
	private final MatchReadService matchReadService;
	private WebDriver webDriver;

	@Override
	public void crawl() {
		crawlMatch();
	}

	private void crawlMatch() {
		webDriver = WebDriverConfig.createDriver();
		for (String date : getCrawlDate()) {
			try {
				webDriver.get(URL + date);
				WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(LEAGUE_CLASS)));
				saveMatch(date);
			} catch (TimeoutException e) {
				log.error("페이지를 찾을수없습니다.");
			}
		}
		webDriver.quit();
	}

	public List<String> getCrawlDate() {
		LocalDateTime recentDate = matchReadService.getLatestMatchStartTime();
		LocalDateTime targetDate = LocalDateTime.now().plusWeeks(1);

		List<String> dateList = new ArrayList<>();

		LocalDateTime startDate = (recentDate != null) ? recentDate.plusDays(1) : LocalDateTime.now();

		while (startDate.isBefore(targetDate) || startDate.isEqual(targetDate)) {
			dateList.add(startDate.format(DATE_FORMATTER));
			startDate = startDate.plusDays(1);
		}

		return dateList;
	}

	private void saveMatch(String date) {
		for (WebElement league : getLeagueList()) {
			confirmEqlLeague(league, date);
		}
	}

	private void confirmEqlLeague(WebElement league, String date) {
		if (getLeagueName(league).equals(EPL_NAME)) {
			crawlMatch(league, date);
		}
	}

	private void crawlMatch(WebElement league, String date) {
		for (WebElement match : getMatchList(league)) {
			List<WebElement> teamNames = getTeamNames(match);
			List<WebElement> teamLogos = getTeamLogos(match);

			log.info("{} {} {} {} {} {}" + BLANK + "{}", teamNames.get(0).getText(), getLogoImg(teamLogos.get(0)),
				teamNames.get(1).getText(), getLogoImg(teamLogos.get(1)), getPlace(match), date, getMatchTime(match));

			save(teamNames.get(0).getText(), getLogoImg(teamLogos.get(0)), teamNames.get(1).getText(),
				getLogoImg(teamLogos.get(1)), getPlace(match), date + BLANK + getMatchTime(match));
		}
	}

	private void save(String homeTeamName, String homeTeamLogo, String awayTeamName, String awayTeamLogo, String place,
		String startDate) {
		this.matchService.save(MatchServiceRequest.createRequest(
			homeTeamName,
			homeTeamLogo,
			awayTeamName,
			awayTeamLogo,
			place,
			MatchCategory.FOOTBALL,
			LocalDateTime.parse(startDate, TIME_FORMATTER)));
	}

	private List<WebElement> getLeagueList() {
		return webDriver.findElements(By.className(LEAGUE_CLASS));
	}

	private String getLeagueName(WebElement league) {
		return league.findElement(By.className(LEAGUE_NAME)).getText();
	}

	private List<WebElement> getMatchList(WebElement league) {
		return league.findElements(By.className(MATCH_CLASS));
	}

	private String getMatchTime(WebElement match) {
		return match.findElement(By.className(MATCH_TIME_CLASS)).getAttribute("innerText").replace("경기 시간\n", "");
	}

	private String getPlace(WebElement match) {
		return match.findElement(By.className(MATCH_PLACE)).getAttribute("innerText").replace("경기장\n", "");
	}

	private List<WebElement> getTeamNames(WebElement team) {
		return team.findElements(By.className(TEAM_NAME_CLASS));
	}

	private List<WebElement> getTeamLogos(WebElement team) {
		return team.findElements(By.className(TEAM_LOGO_CLASS));
	}

	private String getLogoImg(WebElement logo) {
		try {
			return logo.findElement(By.tagName(IMG_TAG)).getAttribute(SRC_ATTR);
		} catch (NoSuchElementException e) {
			return null;
		}
	}

}
