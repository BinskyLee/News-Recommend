package com.fzu.recommend.service;

import com.fzu.recommend.dao.ActionLogMapper;
import com.fzu.recommend.entity.ActionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActionLogService {

    @Autowired
    private ActionLogMapper actionLogMapper;

    public int addActionLog(ActionLog actionLog){
        return actionLogMapper.insertAction(actionLog);
    }

    public List<Integer> getUserIds(){
        return actionLogMapper.selectUserIds();
    }

    public List<Integer> getNewsIds(){
        return actionLogMapper.selectNewsIds();
    }

    public List<ActionLog> getLatestLog(int userId){
        return actionLogMapper.selectLatestLog(userId);
    }

    public List<Integer> getReadUserIds(int newsId){
        return actionLogMapper.selectReadUserIds(newsId);
    }

    public List<ActionLog> getUserLog(int userId){
        return actionLogMapper.selectUserLog(userId);
    }


}
