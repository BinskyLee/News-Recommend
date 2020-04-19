package com.fzu.recommend.dao.elasticsearch;

import com.fzu.recommend.entity.News;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository  extends ElasticsearchRepository<News, Integer> {




}
