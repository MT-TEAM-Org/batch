package com.playhive.batch.news.repository;

import com.playhive.batch.news.entity.NewsDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface NewsSearchRepository extends ElasticsearchRepository<NewsDocument, String> {
    
}
