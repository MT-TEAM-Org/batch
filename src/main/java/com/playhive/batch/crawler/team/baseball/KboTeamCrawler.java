package com.playhive.batch.crawler.team.baseball;

import com.playhive.batch.crawler.team.TeamCrawler;
import com.playhive.batch.global.config.WebDriverConfig;
import com.playhive.batch.match.team.domain.TeamCategory;
import com.playhive.batch.match.team.dto.service.request.TeamServiceRequest;
import com.playhive.batch.match.team.service.TeamService;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KboTeamCrawler implements TeamCrawler {

    private static final String URL = "https://sports.news.naver.com/kbaseball/record/index?category=kbo";

    private static final String TEAM_LIST_ID = "regularTeamRecordList_table";
    private static final String NAME_CLASS = "tm";
    private static final String LOGO_CLASS = "emblem";

    private static final String TR_TAG = "tr";
    private static final String IMG_TAG = "img";
    private static final String SPAN_TAG = "span";
    private static final String SRC_ATTR = "src";

    private final TeamService teamService;
    private WebDriver webDriver;

    @Override
    public void crawl() {
        crawlTeam();
    }

    private void crawlTeam() {
        WebDriver webDriver = null;
        try {
            webDriver = WebDriverConfig.createDriver();
            webDriver.get(URL);
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(TEAM_LIST_ID)));
            saveTeams();
        } catch (TimeoutException e) {
            log.error("팀 리스트 페이지 로딩 실패. URL: {}", URL, e);
        } catch (RuntimeException e) {
            log.error("크롤링 중 예외 발생: {}", e.getMessage(), e);
        } finally {
            if (webDriver != null) {
                try {
                    webDriver.quit();
                } catch (Exception e) {
                    log.error("WebDriver quit 실패", e);
                }
            }
        }
    }


    private void saveTeams() {
        for (WebElement team : getTeamList()) {
            save(getName(team), getLogo(team));
        }
    }

    private void save(String name, String logo) {
        this.teamService.save(TeamServiceRequest.createRequest(name, logo, TeamCategory.BASEBALL));
    }

    private List<WebElement> getTeamList() {
        WebElement teamListElement = webDriver.findElement(By.id(TEAM_LIST_ID));
        return teamListElement.findElements(By.tagName(TR_TAG));
    }

    private String getName(WebElement team) {
        return team.findElement(By.className(NAME_CLASS)).findElements(By.tagName(SPAN_TAG)).get(1).getText();
    }

    private String getLogo(WebElement team) {
        try {
            return team.findElement(By.className(LOGO_CLASS)).findElement(By.tagName(IMG_TAG)).getAttribute(SRC_ATTR);
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
