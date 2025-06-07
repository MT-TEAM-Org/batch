package com.playhive.batch.crawler.game.gameEvent;

import com.playhive.batch.crawler.game.GameCrawler;
import com.playhive.batch.game.gameEvent.dto.GameEventSaveRequest;
import com.playhive.batch.game.gameEvent.service.GameEventService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GameEventCrawler implements GameCrawler {

    private static final String URL = "https://event.nexon.com/event/ongoinglist.aspx";
    private static final String EVENT_LIST_CLASS = "eventList";
    private static final String LI_EVENT_ITEM = "eventItem";
    private static final String A_TAG = "a";
    private static final String HREF_ATTR = "href";
    private static final String SPAN_EVENT_IMG = "eventImg";
    private static final String IMG_TAG = "img";
    private static final String SRC_ATTR = "src";
    private static final String EVENT_TITLE = "eventTit";
    private static final String EVENT_DESCRIPTION = "eventCnts";
    private static final String EVENT_PERIOD = "eventPeriod";

    private final GameEventService gameEventService;

    public GameEventCrawler(GameEventService gameEventService) {
        this.gameEventService = gameEventService;
    }

    @Override
    public void crawl() {
        try {
            crawlGameEvent();
        } catch (Exception e) {
            log.error("Error occurred while crawling events: {}", e.getMessage(), e);
        }
    }

    private void crawlGameEvent() {
        try {
            // HTML 문서 요청 및 파싱
            Document document = Jsoup.connect(URL).timeout(60_000).get();
            saveEvent(document);
        } catch (Exception e) {
            log.error("Error occurred while crawling events: {}", e.getMessage(), e);
        }
    }

    private void saveEvent(Document document) {
        // 이벤트 목록 가져오기
        Elements eventList = document.select("." + EVENT_LIST_CLASS + " > ." + LI_EVENT_ITEM);

        for (Element event : eventList) {
            try {
                // 노출 기간
                LocalDateTime exposureDate = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT);

                String period = getPeriod(event);

                // period에 "마감임박" 텍스트가 포함되어 있으면 검사 진행
                if (period.contains("마감임박")) {
                    LocalDate eventEndDate = extractEndDate(period);

                    // 마감일이 exposureDate보다 과거라면 저장하지 않음
                    if (eventEndDate.isBefore(exposureDate.toLocalDate())) {
                        log.info("노출기간 기준(현재보다 하루 뒤 날짜)으로 지난 이벤트로 크롤링 건너뛰기: {}", period);
                        continue;
                    }
                }

                saveGameEvent(getThumbImg(event), getTitle(event), getDescription(event), period,
                        getLink(event), exposureDate);
            } catch (Exception e) {
                log.error("Error while processing event: {}", e.getMessage(), e);
            }
        }
    }

    private void saveGameEvent(String thumbImg, String title, String description, String period, String link,
                               LocalDateTime exposureDate) {
        gameEventService.saveGameEvent(
                GameEventSaveRequest.createRequest(thumbImg, title, description, period, link, exposureDate));
    }

    private String getTitle(Element event) {
        return event.select("." + EVENT_TITLE).text();
    }

    private String getDescription(Element event) {
        return event.select("." + EVENT_DESCRIPTION).text();
    }

    private String getPeriod(Element event) {
        return event.select("." + EVENT_PERIOD).text();
    }

    private LocalDate extractEndDate(String period) {
        // "YYYY-MM-DD ~ YYYY-MM-DD 마감임박" 형식에서 마감일 추출
        String[] parts = period.split(" ~ ");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid event period format: " + period);
        }
        String endDateStr = parts[1].split(" ")[0]; // 마감임박 제거
        return LocalDate.parse(endDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private String getThumbImg(Element event) {
        return event.select("." + SPAN_EVENT_IMG + " > " + IMG_TAG).attr(SRC_ATTR);
    }

    private String getLink(Element event) {
        return event.select(A_TAG).attr(HREF_ATTR);
    }
}
