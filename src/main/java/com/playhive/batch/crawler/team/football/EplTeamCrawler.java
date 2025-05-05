package com.playhive.batch.crawler.team.football;

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
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EplTeamCrawler implements TeamCrawler {

    private static final String URL = "https://m.sports.naver.com/wfootball/record/epl?seasonCode=TxzT&tab=teamRank";

    private static final String TEAM_LIST_CLASS = "TableBody_table_body__DlwwS";
    private static final String NAME_CLASS = "TeamInfo_team_name__dni7F";
    private static final String LOGO_CLASS = "TeamInfo_emblem__5JUAY";

    private static final String LI_TAG = "li";
    private static final String IMG_TAG = "img";
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
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(TEAM_LIST_CLASS)));
            saveTeams();
        } catch (TimeoutException e) {
            log.error("팀 리스트 페이지 로딩 실패. URL: {}", URL, e);
        } catch (RuntimeException e) {
            log.error("팀 크롤링 중 에러 발생: {}", e.getMessage(), e);
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
        this.teamService.save(TeamServiceRequest.createRequest(name, logo, TeamCategory.FOOTBALL));
    }

    private List<WebElement> getTeamList() {
        WebElement teamListElement = webDriver.findElement(By.className(TEAM_LIST_CLASS));
        return teamListElement.findElements(By.tagName(LI_TAG));
    }

    private String getName(WebElement team) {
        return team.findElement(By.className(NAME_CLASS)).getText();
    }

    private String getLogo(WebElement team) {
        try {
            return team.findElement(By.className(LOGO_CLASS)).findElement(By.tagName(IMG_TAG)).getAttribute(SRC_ATTR);
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
