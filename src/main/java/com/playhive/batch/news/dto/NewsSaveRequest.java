package com.playhive.batch.news.dto;

import java.time.LocalDateTime;

import com.playhive.batch.news.entity.News;
import com.playhive.batch.news.entity.NewsCategory;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NewsSaveRequest {

	private NewsCategory category;
	private String title;
	private String thumbImg;
	private LocalDateTime postDate;

	@Builder
	public NewsSaveRequest(NewsCategory category, String title, String thumbImg, LocalDateTime postDate) {
		this.category = category;
		this.title = title;
		this.thumbImg = thumbImg;
		this.postDate = postDate;
	}

	public static NewsSaveRequest createFootballRequest(String title, String thumbImg, LocalDateTime postDate) {
		return NewsSaveRequest.builder()
			.category(NewsCategory.FOOTBALL)
			.title(title)
			.thumbImg(thumbImg)
			.postDate(postDate)
			.build();
	}

	public News toEntity() {
		return News.builder()
			.category(category)
			.title(title)
			.thumbImg(thumbImg)
			.postDate(postDate)
			.build();
	}
}
