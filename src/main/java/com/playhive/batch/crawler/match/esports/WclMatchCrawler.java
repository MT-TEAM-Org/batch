package com.playhive.batch.crawler.match.esports;

import java.time.Duration;
import java.time.LocalDate;
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
import com.playhive.batch.match.match.domain.LeagueName;
import com.playhive.batch.match.match.domain.MatchCategory;
import com.playhive.batch.match.match.dto.service.request.MatchServiceRequest;
import com.playhive.batch.match.match.service.MatchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WclMatchCrawler implements MatchCrawler {
	private static final String URL = "https://game.naver.com/esports/League_of_Legends/schedule/world_championship?date=";

	private static final String DATE_GROUP_CLASS = "card_item__3Covz";
	private static final String MATCH_CLASS = "row_item__dbJjy";
	private static final String MATCH_DATE_CLASS = "card_date__1kdC3";
	private static final String MATCH_TIME_CLASS = "row_time__28bwr";
	private static final String MATCH_PLACE = "row_stadium__UOBaJ";
	private static final String TEAM_NAME_CLASS = "row_name__IDFHz";
	private static final String TEAM_LOGO_CLASS = "row_logo__c8gh0";

	private static final String SRC_ATTR = "src";

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private static final String BLANK = " ";

	private final MatchService matchService;
	private WebDriver webDriver;

	@Override
	public void crawl(LocalDateTime recentDate) {
		crawlMatch(recentDate);
	}

	private void crawlMatch(LocalDateTime recentDate) {
		webDriver = WebDriverConfig.createDriver();
		for (String date : getCrawlDate(recentDate)) {
			try {
				webDriver.get(URL + date);
				WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(DATE_GROUP_CLASS)));
				saveMatch(date);
			} catch (TimeoutException e) {
				log.error("페이지를 찾을수없습니다.");
			} catch (RuntimeException e) {
				log.error(e.getMessage());
			}
		}
		webDriver.quit();
	}

	public List<String> getCrawlDate(LocalDateTime recentDate) {
		LocalDateTime targetDate = LocalDateTime.now().plusWeeks(1);

		List<String> dateList = new ArrayList<>();

		LocalDateTime startDate = (recentDate != null) ? recentDate.plusDays(1) : LocalDateTime.now();

		while (startDate.isBefore(targetDate) || startDate.isEqual(targetDate)) {
			dateList.add(startDate.format(DATE_FORMATTER));
			startDate = startDate.plusDays(1);
		}

		return dateList;
	}

	private void saveMatch(String crawlDate) {
		for (WebElement date : getDateList()) {
			confirmLckLeague(date, crawlDate);
		}
	}

	private List<WebElement> getDateList() {
		return webDriver.findElements(By.className(DATE_GROUP_CLASS));
	}

	private void confirmLckLeague(WebElement date, String crawlDate) {
		if (isSameDate(date, crawlDate)) {
			crawlMatch(date, crawlDate);
		}
	}

	private boolean isSameDate(WebElement date, String crawlDate) {
		String matchDate = getMatchDate(date);
		String[] monthDayParts = matchDate.split("[월일]");
		int month = Integer.parseInt(monthDayParts[0].trim());
		int day = Integer.parseInt(monthDayParts[1].trim());

		String[] crawlDateParts = crawlDate.split("-");
		int year = Integer.parseInt(crawlDateParts[0]);

		LocalDate parsedDate = LocalDate.of(year, month, day);
		LocalDate expectedDate = LocalDate.parse(crawlDate);

		return parsedDate.isEqual(expectedDate);
	}

	private void crawlMatch(WebElement date, String crawlDate) {
		for (WebElement match : getMatchList(date)) {
			List<WebElement> teamNames = getTeamNames(match);
			List<WebElement> teamLogos = getTeamLogos(match);

			log.info("{} {} {} {} {} {}" + BLANK + "{}", teamNames.get(0).getText(), getLogoImg(teamLogos.get(0)),
				teamNames.get(1).getText(), getLogoImg(teamLogos.get(1)), getPlace(match), crawlDate,
				getMatchTime(match));

			save(teamNames.get(1).getText(), getLogoImg(teamLogos.get(1)), teamNames.get(0).getText(),
				getLogoImg(teamLogos.get(0)), getPlace(match), LeagueName.WCL.getName(),
				crawlDate + BLANK + getMatchTime(match));
		}
	}

	private void save(String homeTeamName, String homeTeamLogo, String awayTeamName, String awayTeamLogo, String
		place, String leagueName,
		String startDate) {
		this.matchService.save(MatchServiceRequest.createRequest(
			homeTeamName,
			homeTeamLogo,
			awayTeamName,
			awayTeamLogo,
			place,
			leagueName,
			MatchCategory.ESPORTS,
			LocalDateTime.parse(startDate, TIME_FORMATTER).plusHours(9),
			LocalDateTime.parse(startDate, TIME_FORMATTER).plusHours(9).plusMinutes(160)));
	}

	private String getMatchDate(WebElement date) {
		return date.findElement(By.className(MATCH_DATE_CLASS)).getText();
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
			return logo.getAttribute(SRC_ATTR);
		} catch (NoSuchElementException e) {
			return null;
		}
	}

}
