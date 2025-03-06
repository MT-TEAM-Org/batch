package com.playhive.batch.game.gameEvent.dto;

import com.playhive.batch.game.gameEvent.entity.GameEvent;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GameEventSaveRequest {
    /**
     * 이벤트 이미지
     */
    private String thumbImg;
    /**
     * 이벤트 제목
     */
    private String title;
    /**
     * 이벤트 설명
     */
    private String description;
    /**
     * 이벤트 기간
     */
    private String period;
    /**
     * 연결 링크
     */
    private String link;
    /**
     * 노출 날짜
     */
    private LocalDateTime exposureDate;

    @Builder
    public GameEventSaveRequest(String thumbImg, String title, String description, String period, String link,
                                LocalDateTime exposureDate) {
        this.thumbImg = thumbImg;
        this.title = title;
        this.description = description;
        this.period = period;
        this.link = link;
        this.exposureDate = exposureDate;
    }

    public static GameEventSaveRequest createRequest(String thumbImg, String title, String description, String period,
                                                     String link, LocalDateTime exposureDate) {
        return GameEventSaveRequest.builder()
                .thumbImg(thumbImg)
                .title(title)
                .description(description)
                .period(period)
                .link(link)
                .exposureDate(exposureDate)
                .build();
    }

    public GameEvent toEntity() {
        return GameEvent.builder()
                .thumbImg(thumbImg)
                .title(title)
                .description(description)
                .period(period)
                .link(link)
                .exposureDate(exposureDate)
                .build();
    }
}