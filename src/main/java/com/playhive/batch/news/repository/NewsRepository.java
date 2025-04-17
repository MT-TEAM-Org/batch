package com.playhive.batch.news.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.playhive.batch.news.entity.News;
import com.playhive.batch.news.entity.NewsCategory;

public interface NewsRepository extends JpaRepository<News, Long> {

	@Query("SELECT n.source FROM p_news n WHERE n.category = :category ORDER BY n.postDate DESC LIMIT 1")
	String findRecentPostDate(@Param("category") NewsCategory category);

}
