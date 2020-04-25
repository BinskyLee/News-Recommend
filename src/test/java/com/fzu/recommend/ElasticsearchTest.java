package com.fzu.recommend;

import com.fzu.recommend.dao.NewsMapper;
import com.fzu.recommend.dao.elasticsearch.NewsRepository;
import com.fzu.recommend.entity.News;
import com.fzu.recommend.util.RecommendUtil;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = RecommendApplication.class)
public class ElasticsearchTest {

    @Autowired
    private NewsMapper newsMapper;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired(required = false)
    private ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void testInsert(){
        newsRepository.save(newsMapper.selectNewsById(11));
        newsRepository.save(newsMapper.selectNewsById(12));
        newsRepository.save(newsMapper.selectNewsById(13));
    }

    @Test
    public void testInsertList(){
        List<News> list = newsMapper.selectNews(33, 0, 100, 0, 0);
        for(News news : list){
            news.setContent(RecommendUtil.htmlReplace(news.getContent()));
        }
        newsRepository.saveAll(list);
    }

    @Test
    public void testDelete(){
        newsRepository.deleteAll();
    }

    @Test
    public void testSearchByRepository(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("测试", "title", "content"))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        Page<News> page = newsRepository.search(searchQuery);
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for(News news : page){
            System.out.println(news);
        }
    }

    @Test
    public void testSearchByTemplate(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("测试", "title", "content"))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        Page<News> page = elasticsearchTemplate.queryForPage(searchQuery, News.class, new SearchResultMapper() {
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
                    news.setUserId(Integer.valueOf(id));

                    String title = hit.getSourceAsMap().get("title").toString();
                    news.setTitle(title);
                    String content = hit.getSourceAsMap().get("content").toString();
                    news.setTitle(content);

                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    news.setCreateTime(new Date(Long.valueOf(createTime)));

                    //处理高亮显示内容
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if(titleField !=  null){
                        news.setTitle(titleField.getFragments()[0].toString());
                    }

                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if(contentField !=  null){
                        news.setTitle(contentField.getFragments()[0].toString());
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
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for(News news : page){
            System.out.println(news);
        }
    }
}
