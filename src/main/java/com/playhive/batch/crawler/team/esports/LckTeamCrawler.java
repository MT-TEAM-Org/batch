package com.playhive.batch.crawler.team.esports;

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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LckTeamCrawler implements TeamCrawler {

    private static final String URL = "https://game.naver.com/esports/League_of_Legends/record/lck/team";

    private static final String TEAM_LIST_CLASS = "record_list_wrap_team__215Gz";
    private static final String NAME_CLASS = "record_list_name__27huQ";
    private static final String LOGO_CLASS = "record_list_thumb_logo__1s1BT";

    private static final String LI_TAG = "li";
    private static final String STYLE_ATTR = "style";

    private static final String NONE_BACKGROUND = "background-image: none;";

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
            log.error("페이지를 찾을 수 없습니다. URL: {}", URL, e);
        } catch (RuntimeException e) {
            log.error("크롤링 중 에러 발생: {}", e.getMessage(), e);
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
        this.teamService.save(TeamServiceRequest.createRequest(name, logo, TeamCategory.ESPORTS));
    }

    private List<WebElement> getTeamList() {
        WebElement teamListElement = webDriver.findElement(By.className(TEAM_LIST_CLASS));
        return teamListElement.findElements(By.tagName(LI_TAG));
    }

    private String getName(WebElement team) {
        return team.findElement(By.className(NAME_CLASS)).getText();
    }

    private String getLogo(WebElement team) {
        String logo = team.findElement(By.className(LOGO_CLASS))
                .getAttribute(STYLE_ATTR)
                .replaceAll(".*url\\((.*)\\).*", "$1").replaceAll("&quot;", "");

        return logo.equals(NONE_BACKGROUND) ? null : logo;
    }
}
