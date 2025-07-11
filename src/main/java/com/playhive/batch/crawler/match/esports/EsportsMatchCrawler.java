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
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
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
public class EsportsMatchCrawler implements MatchCrawler {

    private static final String BASE_URL = "https://game.naver.com/esports/League_of_Legends/schedule/";

    private static final List<String> LEAGUE_CODES = Arrays.asList(
            "msi", "ewc_lol", "lck", "lck_cl", "lck_as", "first_stand_lol",
            "season_opening", "lol_kespa", "world_championship", "asci",
            "lpl", "lcs", "lec", "msc", "lol_allstar", "riftrivals", "ck"
    );

    private static final String DATE = "?date=";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

    private final MatchService matchService;

    public void crawl(LocalDateTime recentTime) {
        for (String leagueCode : LEAGUE_CODES) {
            crawlLeague(leagueCode, recentTime);
        }
    }

    private void crawlLeague(String leagueCode, LocalDateTime recentTime) {
        String formattedDate = recentTime.format(formatter);

        String url = BASE_URL + leagueCode + DATE + formattedDate;
        String leagueName = getLeagueName(leagueCode);

        try {
            // Jsoup으로 HTML 파싱
            Document doc = Jsoup.connect(url).get();

            // __NEXT_DATA__ script 태그 찾기
            Element script = doc.selectFirst("script#__NEXT_DATA__");

            if (script == null) {
                log.error("{} 페이지에서 __NEXT_DATA__ script 태그를 찾을 수 없습니다.", leagueName);
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

                    log.info("{} Match: {} {} vs {} {} at {} ({})", leagueName, homeTeam, homeLogo, awayTeam, awayLogo,
                            startTime,
                            place);

                    // 저장 로직
                    matchService.save(MatchServiceRequest.createRequest(
                            homeTeam,
                            homeLogo,
                            awayTeam,
                            awayLogo,
                            place,
                            leagueName,
                            MatchCategory.ESPORTS,
                            startTime,
                            endTime
                    ));
                }
            }

        } catch (Exception e) {
            log.error("{} HTML 파싱 크롤링 에러: {}", leagueName, e.getMessage(), e);
        }
    }

    private String getLeagueName(String leagueCode) {
        switch (leagueCode) {
            case "msi":
                return LeagueName.MSI.getName();
            case "ewc_lol":
                return LeagueName.EWC_LOL.getName();
            case "lck":
                return LeagueName.LCK.getName();
            case "lck_cl":
                return LeagueName.LCK_CL.getName();
            case "lck_as":
                return LeagueName.LCK_AS.getName();
            case "first_stand_lol":
                return LeagueName.FIRST_STAND_LOL.getName();
            case "season_opening":
                return LeagueName.SEASON_OPENING.getName();
            case "lol_kespa":
                return LeagueName.LOL_KESPA.getName();
            case "world_championship":
                return LeagueName.WCL.getName();
            case "asci":
                return LeagueName.ASCI.getName();
            case "lpl":
                return LeagueName.LPL.getName();
            case "lcs":
                return LeagueName.LCS.getName();
            case "lec":
                return LeagueName.LEC.getName();
            case "msc":
                return LeagueName.MSC.getName();
            case "lol_allstar":
                return LeagueName.LOL_ALLSTAR.getName();
            case "riftrivals":
                return LeagueName.RIFTRIVALS.getName();
            case "ck":
                return LeagueName.CK.getName();
            default:
                return leagueCode.toUpperCase();
        }
    }
}