package com.playhive.batch.match.match.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LeagueName {

	LCK("LoL 챔피언스 코리아"),
	WCL("LoL 월드 챔피언십"),
	EPL("프리미어 리그"),
	KLEAGUE("K리그1"),
	KBO("한국프로야구");

	private final String name;
}
