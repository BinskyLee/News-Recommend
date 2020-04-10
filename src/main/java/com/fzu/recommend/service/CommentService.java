package com.fzu.recommend.service;


import com.fzu.recommend.dao.CommentMapper;
import com.fzu.recommend.entity.Comment;
import com.fzu.recommend.entity.News;
import com.fzu.recommend.util.RecommendConstant;
import com.fzu.recommend.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements RecommendConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private NewsService newsService;



    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCountByEntity(int entityType, int entityId){
        return commentMapper.selectCountByEntity(entityType, entityId);
    }
    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment, int newsId){
        if(comment == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //HTML过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        //敏感词过滤
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        //添加评论
        int rows = commentMapper.insertComment(comment);
        //更新评论数
        News news = newsService.findNewsById(newsId);
        newsService.updateCommentCount(newsId, news.getCommentCount() + 1);
        return rows;
    }
}
