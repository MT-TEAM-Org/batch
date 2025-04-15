package com.playhive.batch.news.service;

import org.springframework.stereotype.Service;

import com.playhive.batch.news.dto.NewsSaveRequest;
import com.playhive.batch.news.entity.News;
import com.playhive.batch.news.repository.NewsRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsService {

	private final NewsRepository newsRepository;
	private final NewsCountService newsCountService;

	public void saveNews(NewsSaveRequest newsSaveRequest) {
		News news = newsRepository.save(newsSaveRequest.toEntity());
		newsCountService.saveNewsCount(news);
	}

}
