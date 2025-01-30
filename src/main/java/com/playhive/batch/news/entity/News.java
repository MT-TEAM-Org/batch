package com.playhive.batch.news.entity;

import java.time.LocalDateTime;

import com.playhive.batch.global.domain.BaseTime;

import jakarta.persistence.Entity;
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

	private NewsCategory category;

	private String title;

	private String thumbImg;

	private LocalDateTime postDate;

	@Builder
	public News(Long id, NewsCategory category, String title, String thumbImg, LocalDateTime postDate) {
		this.id = id;
		this.category = category;
		this.title = title;
		this.thumbImg = thumbImg;
		this.postDate = postDate;
	}
}
