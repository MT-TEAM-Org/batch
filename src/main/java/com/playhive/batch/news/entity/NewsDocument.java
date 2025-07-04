package com.playhive.batch.news.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;

@Document(indexName = "news")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsDocument {

    @Id
    private String id;

    private String title;
    private String content;
    private String category;
    private LocalDateTime postDate;
}
