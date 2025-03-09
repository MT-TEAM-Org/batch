package com.playhive.batch.crawler.game.gameDiscount;

import com.playhive.batch.crawler.game.GameCrawler;
import com.playhive.batch.game.gameDiscount.dto.GameDiscountSaveRequest;
import com.playhive.batch.game.gameDiscount.service.GameDiscountService;
import com.playhive.batch.global.config.WebDriverConfig;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GameDiscountCrawler implements GameCrawler {

    private static final String URL = "https://store.steampowered.com";

    private static final String TRIGGER_TAB = "#tab_specials_content_trigger";
    private static final String JAVASCRIPT_CLICK = "arguments[0].click();";
    private static final String SPECIALS_CONTENT = "tab_specials_content";
    private static final String ITEM = ".tab_item";

    // 링크
    private static final String HREF_ATTR = "href";
    // 게임명
    private static final String GAME_NAME = "tab_item_name";
    // 이미지
    private static final String GAME_IMAGE = "tab_item_cap_img";
    private static final String SRC_ATTR = "src";
    // 가격 정보
    private static final String DISCOUNT_BLOCK = "discount_block";
    // 할인율
    private static final String DISCOUNT_PCT = "discount_pct";
    // 할인 전 가격
    private static final String ORIGINAL_PRICE = "discount_original_price";
    // 할인된 가격
    private static final String FINAL_PRICE = "discount_final_price";

    private WebDriver webDriver;
    private final GameDiscountService gameDiscountService;

    public GameDiscountCrawler(GameDiscountService gameDiscountService) {
        this.gameDiscountService = gameDiscountService;
    }

    @Override
    public void crawl() {
        try {
            webDriver = WebDriverConfig.createDriver();
            crawlGameDiscount();
        } finally {
            webDriver.quit();
        }
    }

    private void crawlGameDiscount() {
        webDriver.get(URL);
        // 명시적 대기 설정
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(15));
        clickSpecialsTab(wait);
        saveGameDiscount(wait);
    }

    private void saveGameDiscount(WebDriverWait wait) {
        for (WebElement gameElement : getGameList(wait)) {
            if (gameElement == null) {
                break;
            }

            // 노출 기간
            LocalDateTime exposureDate = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT);

            // 가격 정보 요소 (없으면 null)
            WebElement discountBlock = gameElement.findElements(By.className(DISCOUNT_BLOCK))
                    .stream()
                    .findFirst()
                    .orElse(null);

            gameDiscountService.saveGameDiscount(GameDiscountSaveRequest.createRequest(
                    getThumbImg(gameElement), getTitle(gameElement), getOriginalPrice(discountBlock),
                    getDiscountPercent(discountBlock), getFinalPrice(discountBlock),
                    getLink(gameElement), exposureDate));
        }
    }

    /**
     * 할인 게임 목록 가져오기
     */
    private List<WebElement> getGameList(WebDriverWait wait) {

        // 특가 콘텐츠가 로드될 때까지 대기
        log.info("특가 콘텐츠 로딩 대기 중...");
        WebElement specialContent = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id(SPECIALS_CONTENT))
        );

        try {
            // 페이지 완전히 로드될 때까지 잠시 대기
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread sleep 중단됨", e);
            return null;
        }

        // 할인 게임 목록 찾기
        List<WebElement> gameElements = specialContent.findElements(
                By.cssSelector(ITEM));

        log.info("발견된 게임 수: {}", gameElements.size());
        return gameElements;
    }

    /**
     * 스페셜 탭 클릭
     */
    private void clickSpecialsTab(WebDriverWait wait) {
        // 스페셜 탭 클릭
        log.info("스페셜 탭 찾는 중...");
        WebElement specialsTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(TRIGGER_TAB)));

        log.info("스페셜 탭 클릭 시도...");
        // 자바스크립트 실행기로 클릭 (더 안정적인 방법)
        JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;
        jsExecutor.executeScript(JAVASCRIPT_CLICK, specialsTab);
    }

    private String getFinalPrice(WebElement discountBlock) {
        if (discountBlock == null) {
            return null;
        }
        String finalPrice = discountBlock.findElement(By.className(FINAL_PRICE)).getText();
        return finalPrice.replaceAll("[^0-9]", "");
    }

    private String getDiscountPercent(WebElement discountBlock) {
        if (discountBlock == null) {
            return null;
        }
        return discountBlock.findElement(By.className(DISCOUNT_PCT)).getText();
    }

    private String getOriginalPrice(WebElement discountBlock) {
        if (discountBlock == null) {
            return null;
        }
        String originalPrice = discountBlock.findElement(By.className(ORIGINAL_PRICE)).getText();
        return originalPrice.replaceAll("[^0-9]", "");
    }

    private String getLink(WebElement gameElement) {
        return gameElement.getAttribute(HREF_ATTR);
    }

    private String getTitle(WebElement gameElement) {
        return gameElement.findElement(By.className(GAME_NAME)).getText();
    }

    private String getThumbImg(WebElement gameElement) {
        WebElement imgElement = gameElement.findElement(By.className(GAME_IMAGE));
        return imgElement.getAttribute(SRC_ATTR);
    }
}