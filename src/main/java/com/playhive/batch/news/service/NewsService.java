package com.playhive.batch.news.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.playhive.batch.news.dto.NewsSaveRequest;
import com.playhive.batch.news.entity.News;
import com.playhive.batch.news.entity.NewsCategory;
import com.playhive.batch.news.repository.NewsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NewsService {

	private final NewsRepository newsRepository;
	private final NewsCountService newsCountService;

	public void saveNews(NewsSaveRequest newsSaveRequest) {
		News news = newsRepository.save(newsSaveRequest.toEntity());
		newsCountService.saveNewsCount(news);
	}

	public String findRecentPostDate(NewsCategory category) {
		return newsRepository.findRecentPostDate(category);
	}

}
