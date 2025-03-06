package com.playhive.batch.game.gameDiscount.dto;

import com.playhive.batch.game.gameDiscount.entity.GameDiscount;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GameDiscountSaveRequest {
    /**
     * 게임 이미지
     */
    private String thumbImg;
    /**
     * 게임명
     */
    private String title;
    /**
     * 할인 전 가격
     */
    private String originalPrice;
    /**
     * 할인율
     */
    private String discountPercent;
    /**
     * 할인된 가격
     */
    private String finalPrice;
    /**
     * 연결 링크
     */
    private String link;
    /**
     * 노출 날짜
     */
    private LocalDateTime exposureDate;

    @Builder
    public GameDiscountSaveRequest(String thumbImg, String title, String originalPrice, String discountPercent,
                                   String finalPrice, String link, LocalDateTime exposureDate) {
        this.thumbImg = thumbImg;
        this.title = title;
        this.originalPrice = originalPrice;
        this.discountPercent = discountPercent;
        this.finalPrice = finalPrice;
        this.link = link;
        this.exposureDate = exposureDate;
    }

    public static GameDiscountSaveRequest createRequest(String thumbImg, String title, String originalPrice,
                                                        String discountPercent,
                                                        String finalPrice, String link, LocalDateTime exposureDate) {
        return GameDiscountSaveRequest.builder()
                .thumbImg(thumbImg)
                .title(title)
                .originalPrice(originalPrice)
                .discountPercent(discountPercent)
                .finalPrice(finalPrice)
                .link(link)
                .exposureDate(exposureDate)
                .build();
    }

    public GameDiscount toEntity() {
        return GameDiscount.builder()
                .thumbImg(thumbImg)
                .title(title)
                .originalPrice(originalPrice)
                .discountPercent(discountPercent)
                .finalPrice(finalPrice)
                .link(link)
                .exposureDate(exposureDate)
                .build();
    }
}