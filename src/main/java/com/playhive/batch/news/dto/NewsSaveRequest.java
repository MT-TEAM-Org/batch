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
	private String source;
	private String content;
	private LocalDateTime postDate;

	@Builder
	public NewsSaveRequest(NewsCategory category, String title, String thumbImg, String source, String content,
		LocalDateTime postDate) {
		this.category = category;
		this.title = title;
		this.thumbImg = thumbImg;
		this.source = source;
		this.content = content;
		this.postDate = postDate;
	}

	public static NewsSaveRequest createRequest(String title, String thumbImg, String source, String content,
		LocalDateTime postDate,
		NewsCategory category) {
		return NewsSaveRequest.builder()
			.category(NewsCategory.FOOTBALL)
			.title(title)
			.thumbImg(thumbImg)s
			.source(source)
			.content(content)
			.postDate(postDate)
			.category(category)
			.build();
	}

	public News toEntity() {
		return News.builder()
			.category(category)
			.title(title)
			.thumbImg(thumbImg)
			.source(source)
			.postDate(postDate)
			.build();
	}
}
