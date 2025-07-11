package com.playhive.batch.match.match.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LeagueName {

    LCK("LoL 챔피언스 코리아"),
    WCL("LoL 월드 챔피언십"),
    MSI("LoL 미드 시즌 인비테이셔널"),
    EWC_LOL("EWC LoL"),
    LCK_CL("LCK 챌린저스 리그"),
    LCK_AS("LCK 아카데미 시리즈"),
    FIRST_STAND_LOL("First Stand LoL"),
    SEASON_OPENING("시즌 오프닝"),
    LOL_KESPA("LoL KeSPA컵"),
    ASCI("아시아 챔피언스 인비테이셔널"),
    LPL("리그 오브 레전드 프로리그"),
    LCS("리그 오브 레전드 챔피언십 시리즈"),
    LEC("리그 오브 레전드 유럽 챔피언십"),
    MSC("미드 시즌 컵"),
    LOL_ALLSTAR("LoL 올스타"),
    RIFTRIVALS("리프트 라이벌즈"),
    CK("챌린저스 코리아"),
    EPL("프리미어 리그"),
    KLEAGUE("K리그1"),
    KBO("한국프로야구");

    private final String name;
}
