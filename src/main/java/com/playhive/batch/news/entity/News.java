package com.playhive.batch.news.entity;

import java.time.LocalDateTime;

import com.playhive.batch.global.domain.BaseTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "p_news")
public class News extends BaseTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private NewsCategory category;

	private String title;

	private String thumbImg;

	private String source;

	private LocalDateTime postDate;

	@Builder
	public News(Long id, NewsCategory category, String title, String thumbImg, String source, LocalDateTime postDate) {
		this.id = id;
		this.category = category;
		this.title = title;
		this.thumbImg = thumbImg;
		this.source = source;
		this.postDate = postDate;
	}
}
