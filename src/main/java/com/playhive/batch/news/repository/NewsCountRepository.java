package com.playhive.batch.news.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.playhive.batch.news.entity.NewsCount;

@Repository
public interface NewsCountRepository extends JpaRepository<NewsCount, Long> {
}
