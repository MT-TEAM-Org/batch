package com.playhive.batch.news.service;

import org.springframework.stereotype.Service;

import com.playhive.batch.news.dto.NewsSaveRequest;
import com.playhive.batch.news.repository.NewsRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class NewsService {

	private final NewsRepository newsRepository;

	public void saveNews(NewsSaveRequest newsSaveRequest) {
		newsRepository.save(newsSaveRequest.toEntity());
	}

}
