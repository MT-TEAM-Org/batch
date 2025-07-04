package com.playhive.batch.crawler.match.esports;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playhive.batch.crawler.match.MatchCrawler;
import com.playhive.batch.match.match.domain.LeagueName;
import com.playhive.batch.match.match.domain.MatchCategory;
import com.playhive.batch.match.match.dto.service.request.MatchServiceRequest;
import com.playhive.batch.match.match.service.MatchService;
import com.playhive.batch.match.team.domain.TeamCategory;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MSIMatchCrawler implements MatchCrawler {

    private static final String URL = "https://game.naver.com/esports/League_of_Legends/schedule/msi";

    private final MatchService matchService;

    public void crawl(LocalDateTime recentTime) {
        try {
            // Jsoup으로 HTML 파싱
            Document doc = Jsoup.connect(URL).get();

            // __NEXT_DATA__ script 태그 찾기
            Element script = doc.selectFirst("script#__NEXT_DATA__");

            if (script == null) {
                log.error("MSI 페이지에서 __NEXT_DATA__ script 태그를 찾을 수 없습니다.");
                return;
            }

            String jsonData = script.html(); // script 태그 안의 JSON 텍스트
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonData);

            JsonNode monthSchedules = root
                    .path("props")
                    .path("initialState")
                    .path("schedule")
                    .path("monthSchedule");

            for (JsonNode group : monthSchedules) {
                for (JsonNode schedule : group.get("schedules")) {
                    long startMillis = schedule.get("startDate").asLong();
                    LocalDateTime startTime = Instant.ofEpochMilli(startMillis)
                            .atZone(ZoneId.systemDefault()).toLocalDateTime();
                    LocalDateTime endTime = startTime.plusMinutes(160);

                    String place = schedule.get("stadium").asText();
                    String homeTeam = schedule.get("homeTeam").get("name").asText();
                    String awayTeam = schedule.get("awayTeam").get("name").asText();
                    String homeLogo = schedule.get("homeTeam").get("imageUrl").asText();
                    String awayLogo = schedule.get("awayTeam").get("imageUrl").asText();

                    // ✅ TBD 제외
                    if ("TBD".equalsIgnoreCase(homeTeam) || "TBD".equalsIgnoreCase(awayTeam)) {
                        log.info("TBD 경기 제외: {} vs {}", homeTeam, awayTeam);
                        continue;
                    }

                    // ✅ 중복 방지
                    if (matchService.exists(TeamCategory.ESPORTS, homeTeam, awayTeam, startTime)) {
                        log.info("이미 존재하는 경기: {} vs {} at {}", homeTeam, awayTeam, startTime);
                        continue;
                    }

                    log.info("MSI Match: {} {} vs {} {} at {} ({})", homeTeam, homeLogo, awayTeam, awayLogo, startTime,
                            place);

                    // 저장 로직
                    matchService.save(MatchServiceRequest.createRequest(
                            homeTeam,
                            homeLogo,
                            awayTeam,
                            awayLogo,
                            place,
                            LeagueName.MSI.getName(),
                            MatchCategory.ESPORTS,
                            startTime,
                            endTime
                    ));
                }
            }

        } catch (Exception e) {
            log.error("MSI HTML 파싱 크롤링 에러: {}", e.getMessage(), e);
        }
    }
}
