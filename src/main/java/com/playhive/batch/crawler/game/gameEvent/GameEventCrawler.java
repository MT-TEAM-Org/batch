package com.playhive.batch.crawler.game.gameEvent;

import com.playhive.batch.crawler.game.GameCrawler;
import com.playhive.batch.game.gameEvent.dto.GameEventSaveRequest;
import com.playhive.batch.game.gameEvent.service.GameEventService;
import com.playhive.batch.global.config.WebDriverConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GameEventCrawler implements GameCrawler {

    private static final String URL = "https://event.nexon.com/event/ongoinglist.aspx";

    private static final String EVENT_LIST_CLASS = "eventList";
    private static final String LI_EVENT_ITEM = "eventItem";
    // 링크
    private static final String A_TAG = "a";
    private static final String HREF_ATTR = "href";
    // 썸네일 이미지
    private static final String SPAN_EVENT_IMG = "eventImg";
    private static final String IMG_TAG = "img";
    private static final String SRC_ATTR = "src";
    // 이벤트 제목
    private static final String EVENT_TITLE = "eventTit";
    // 이벤트 소개
    private static final String EVENT_DESCRIPTION = "eventCnts";
    // 이벤트 기간
    private static final String EVENT_PERIOD = "eventPeriod";

    private final GameEventService gameEventService;
    private WebDriver webDriver;

    public GameEventCrawler(GameEventService gameEventService) {
        this.gameEventService = gameEventService;
    }

    @Override
    public void crawl() {
        webDriver = WebDriverConfig.createDriver();
        crawlGameEvent();
        webDriver.quit();
    }

    private void crawlGameEvent() {
        webDriver.get(URL);
        try {
            saveEvent();
        } catch (Exception e) {
            log.error("Error occurred while crawling events", e.getMessage());
        }
    }

    private void saveEvent() {
        for (WebElement event : getEventList()) {
            try {
                saveGameEvent(getThumbImg(event), getTitle(event), getDescription(event), getPeriod(event),
                        getLink(event));
            } catch (Exception e) {
                log.error("Error while processing event: {}", e.getMessage(), e);
            }
        }
    }

    private void saveGameEvent(String thumbImg, String title, String description, String period, String link) {
        // 노출 기간
        LocalDateTime exposureDate = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT);
        gameEventService.saveGameEvent(
                GameEventSaveRequest.createRequest(thumbImg, title, description, period, link, exposureDate));
    }

    private String getTitle(WebElement event) {
        return event.findElement(By.className(EVENT_TITLE)).getText();
    }

    private String getDescription(WebElement event) {
        return event.findElement(By.className(EVENT_DESCRIPTION)).getText();
    }

    private String getPeriod(WebElement event) {
        return event.findElement(By.className(EVENT_PERIOD)).getText();
    }

    private String getThumbImg(WebElement event) {
        return event.findElement(By.className(SPAN_EVENT_IMG)).findElement(By.tagName(IMG_TAG)).getAttribute(SRC_ATTR);
    }

    private String getLink(WebElement event) {
        return event.findElement(By.tagName(A_TAG)).getAttribute(HREF_ATTR);
    }

    private List<WebElement> getEventList() {
        WebElement eventListElement = webDriver.findElement(By.className(EVENT_LIST_CLASS));
        return eventListElement.findElements(By.className(LI_EVENT_ITEM));
    }
}