package com.fzu.recommend.dao;

import com.fzu.recommend.entity.News;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NewsMapper {

    List<News> selectNews(int userId, int offset, int limit);

    //@Param注解用于给参数取别名
    //如果只有一个参数，并且要使用动态条件，则必须用别名
    int selectNewsRows(@Param("userId") int userId);

    int insertNews(News news);



}
