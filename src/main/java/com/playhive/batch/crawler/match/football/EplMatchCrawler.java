package com.playhive.batch.crawler.match.football;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playhive.batch.crawler.match.MatchCrawler;
import com.playhive.batch.match.match.domain.MatchCategory;
import com.playhive.batch.match.match.dto.service.request.MatchServiceRequest;
import com.playhive.batch.match.match.service.MatchService;
import com.playhive.batch.match.team.domain.TeamCategory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class EplMatchCrawler implements MatchCrawler {

    private static final String API_URL_TEMPLATE =
            "https://api-gw.sports.naver.com/schedule/games" +
                    "?fields=basic,schedule,league,leagueName" +
                    "&upperCategoryId=wfootball&categoryId=epl&fromDate=%s&toDate=%s";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MatchService matchService;

    @Override
    public void crawl(LocalDateTime recentTime) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String from = LocalDateTime.now().format(dateFormatter);
        String to = LocalDateTime.now().plusDays(7).format(dateFormatter);
        String requestUrl = String.format(API_URL_TEMPLATE, from, to);

        log.info("EPL API 호출: {}", requestUrl);

        ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode games = root.path("result").path("games");

            if (!games.isArray()) {
                log.warn("games 데이터가 배열이 아닙니다.");
                return;
            }

            for (JsonNode game : games) {
                String homeTeamName = game.path("homeTeamName").asText();
                String awayTeamName = game.path("awayTeamName").asText();
                String stadium = game.path("stadium").asText();
                String leagueName = game.path("categoryName").asText();
                String homeLogo = game.path("homeTeamEmblemUrl").asText();
                String awayLogo = game.path("awayTeamEmblemUrl").asText();
                LocalDateTime gameDateTime = LocalDateTime.parse(game.path("gameDateTime").asText(),
                        DateTimeFormatter.ISO_DATE_TIME);

                if (matchService.exists(TeamCategory.FOOTBALL, homeTeamName, awayTeamName, gameDateTime)) {
                    log.info("[중복] 저장 생략: {} vs {} @ {}", homeTeamName, awayTeamName, gameDateTime);
                    continue;
                }

                matchService.save(MatchServiceRequest.createRequest(
                        homeTeamName,
                        homeLogo,
                        awayTeamName,
                        awayLogo,
                        stadium,
                        leagueName,
                        MatchCategory.FOOTBALL,
                        gameDateTime,
                        gameDateTime.plusMinutes(120)
                ));

                log.info("✅ 저장 완료: {} vs {}", homeTeamName, awayTeamName);
            }

        } catch (Exception e) {
            log.error("JSON 파싱 중 오류 발생", e);
        }
    }
}