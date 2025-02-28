package com.playhive.batch.match.team.domain;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TeamCategory {
    BASEBALL("야구"),
    ESPORTS("E스포츠"),
    FOOTBALL("축구");

    private final String text;

    public static TeamCategory fromText(String text) {
        return Arrays.stream(TeamCategory.values())
            .filter(category -> category.getText().equals(text))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("해당하는 TeamCategory가 없습니다: " + text));
    }
}
