package com.fzu.recommend.service;

import com.fzu.recommend.dao.elasticsearch.NewsRepository;
import com.fzu.recommend.entity.News;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ElasticsearchService {

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    public void saveNews(News news){
        newsRepository.save(news);
    }

    public void deleteNews(int id){
        newsRepository.deleteById(id);
    }

    public Page<News> searchNews(String keyword, int current, int limit){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        return elasticsearchTemplate.queryForPage(searchQuery, News.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                SearchHits hits = searchResponse.getHits();
                if(hits.getTotalHits() <= 0){
                    return null;
                }
                List<News> list = new ArrayList<>();
                for(SearchHit hit : hits){
                    News news = new News();
                    String id = hit.getSourceAsMap().get("id").toString();
                    news.setId(Integer.valueOf(id));
                    String userId = hit.getSourceAsMap().get("userId").toString();
                    news.setUserId(Integer.valueOf(userId));

                    String title = hit.getSourceAsMap().get("title").toString();
                    news.setTitle(title);
                    String keywords = hit.getSourceAsMap().get("keywords") == null ? null : hit.getSourceAsMap().get("keywords").toString();
                    news.setKeywords(keywords);

                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    news.setCommentCount(Integer.valueOf(commentCount));

                    String clickCount = hit.getSourceAsMap().get("clickCount").toString();
                    news.setClickCount(Integer.valueOf(clickCount));

                    String score = hit.getSourceAsMap().get("score").toString();
                    news.setScore(Double.valueOf(score));

                    String categoryId = hit.getSourceAsMap().get("categoryId").toString();
                    news.setCategoryId(Integer.valueOf(categoryId));

                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    news.setCreateTime(new Date(Long.valueOf(createTime)));

                    //处理高亮显示内容
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if(titleField !=  null){
                        news.setTitle(titleField.getFragments()[0].toString());
                    }

                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if(contentField !=  null){
                        news.setContent(contentField.getFragments()[0].toString());
                    }
                    if(StringUtils.isBlank(news.getContent())){
                        String content = hit.getSourceAsMap().get("content") == null ? null : hit.getSourceAsMap().get("content").toString();
                        if(content.length() > 100) {
                            news.setContent(content.substring(0, 100) + "...");
                        }
                    }
                    list.add(news);
                }
                return new AggregatedPageImpl(list, pageable, hits.getTotalHits(), searchResponse.getAggregations(), searchResponse.getScrollId(), hits.getMaxScore());
            }

            @Override
            public <T> T mapSearchHit(SearchHit searchHit, Class<T> aClass) {
                return null;
            }
        });
    }
}
