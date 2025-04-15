package com.playhive.batch.news.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.playhive.batch.news.entity.News;
import com.playhive.batch.news.entity.NewsCount;
import com.playhive.batch.news.repository.NewsCountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsCountService {

	private final NewsCountRepository newsCountRepository;

	public void saveNewsCount(News news) {
		newsCountRepository.save(NewsCount.createEntity(news));
	}

}
