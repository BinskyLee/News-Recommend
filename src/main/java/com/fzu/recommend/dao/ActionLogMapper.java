package com.fzu.recommend.dao;


import com.fzu.recommend.entity.ActionLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface ActionLogMapper {

    int insertAction(ActionLog actionLog);

    // 获取所有有行为的用户
    List<Integer> selectUserIds();

    // 获取所有被浏览过的新闻
    List<Integer> selectNewsIds();

    // 获取用户对某新闻的最新行为记录
    List<ActionLog> selectLatestLog(int userId);

    // 获取某条新闻的读者集合
    List<Integer> selectReadUserIds(int newsId);

    // 获取某用户的所有行为记录
    List<ActionLog> selectUserLog(int userId);





}
