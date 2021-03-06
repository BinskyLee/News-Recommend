package com.fzu.recommend.dao;

import com.fzu.recommend.entity.News;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NewsMapper {

    List<News> selectNews(int userId, int offset, int limit, int categoryId, int sort);

    //@Param注解用于给参数取别名
    //如果只有一个参数，并且要使用动态条件，则必须用别名
    int selectNewsRows(int userId, int categoryId);

    int insertNews(News news);

    News selectNewsById(int id);

    int updateCommentCount(int id, int commentCount);

    int updateKeywords(int id, String keywords);

    int updateContent(int id, String content);

    int updateScore(int id, double score);

    int updateClickCount(int id, int clickCount);



}
