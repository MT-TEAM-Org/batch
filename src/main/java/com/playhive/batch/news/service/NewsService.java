package com.playhive.batch.news.service;

import com.playhive.batch.news.entity.NewsDocument;
import com.playhive.batch.news.repository.NewsSearchRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.playhive.batch.news.dto.NewsSaveRequest;
import com.playhive.batch.news.entity.News;
import com.playhive.batch.news.entity.NewsCategory;
import com.playhive.batch.news.repository.NewsRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NewsService {

	private final NewsRepository newsRepository;
	private final NewsCountService newsCountService;
	private final NewsSearchRepository newsSearchRepository;

    @PostConstruct
    public void setUpES() {
        System.out.println("================ 초기화 시작 ===================");
        List<News> allNews = newsRepository.findAll();
        syncToElastic(allNews);
        System.out.println("================ 초기화 완료 ===================");
    }

    public void syncToElastic(List<News> newsList) {
        int batchSize = 1000;

        for (int i = 0; i < newsList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, newsList.size());
            List<NewsDocument> chunk = newsList.subList(i, end).stream()
                    .map(news -> NewsDocument.builder()
                            .id(news.getId().toString())
                            .title(news.getTitle())
                            .content(news.getContent())
                            .category(news.getCategory().name())
                            .postDate(news.getPostDate())
                            .build())
                    .toList();

            newsSearchRepository.saveAll(chunk);
        }
    }

	public void saveNews(NewsSaveRequest newsSaveRequest) {
		News news = newsRepository.save(newsSaveRequest.toEntity());
		newsCountService.saveNewsCount(news);

		NewsDocument doc = NewsDocument.builder()
				.id(String.valueOf(news.getId()))
				.title(news.getTitle())
				.content(news.getContent())
				.category(news.getCategory().name())
				.postDate(news.getPostDate())
				.build();

		newsSearchRepository.save(doc);
	}

	public String findRecentPostDate(NewsCategory category) {
		return newsRepository.findRecentPostDate(category);
	}

}
