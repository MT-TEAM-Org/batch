package com.playhive.batch.news.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.playhive.batch.news.entity.News;

public interface NewsRepository extends JpaRepository<News, Long> {
}
