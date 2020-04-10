package com.fzu.recommend.controller;

import com.fzu.recommend.annotation.LoginRequired;
import com.fzu.recommend.entity.Comment;
import com.fzu.recommend.entity.News;
import com.fzu.recommend.entity.Page;
import com.fzu.recommend.entity.User;
import com.fzu.recommend.service.*;
import com.fzu.recommend.util.HostHolder;
import com.fzu.recommend.util.RecommendConstant;
import com.fzu.recommend.util.RecommendUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

@Controller
@RequestMapping("/news")
public class NewsController implements RecommendConstant {

    @Value("${recommend.path.upload}")
    private String uploadPath;

    @Value("${recommend.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private NewsService newsService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private FollowService followService;

    private static final Logger logger = LoggerFactory.getLogger(NewsController.class);

    @LoginRequired
    @RequestMapping(path = "/publish", method = RequestMethod.GET)
    public String getPublishPage(){
        return "/site/publish";
    }

    @LoginRequired
    @RequestMapping(path = "/publish", method = RequestMethod.POST)
    public String publishNews(String title, String content, int category){
        User user = hostHolder.getUser();
        News news = new News();
        news.setTitle(title);
        news.setContent(content);
        news.setCategoryId(category);
        news.setCommentCount(0);
        news.setCreateTime(new Date());
        news.setUserId(user.getId());
        Map<String, Object> map = newsService.addNews(news);
        if(map != null){
            return RecommendUtil.getJSONString(1, (String)map.get("msg"));
        }
        return "redirect:/";
    }


    @RequestMapping(path = "/uploadMultipleFile", method = RequestMethod.POST)
    @ResponseBody
    public String uploadMultipleFileHandler(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        if(file != null){
            Map<String, Object> map = new HashMap<>();
            map = RecommendUtil.uploadImg(file, uploadPath);
            if(map.get("fileName") != null) {
                String fileName = (String) map.get("fileName");
                String saveUrl = domain + contextPath + "/news/image/" + fileName;
                return saveUrl;
            }
        }
        return null;
    }

    @RequestMapping(path = "/image/{fileName}", method = RequestMethod.GET)
    public void getImg(@PathVariable("fileName") String fileName, HttpServletResponse response){
        try{
            OutputStream os = response.getOutputStream();
            RecommendUtil.getImg(fileName, uploadPath, os);
        }catch (IOException e) {
            logger.error("获取图像失败:" + e.getMessage());
        }
    }

    @RequestMapping(path = "/detail/{newsId}", method = RequestMethod.GET)
    public ModelAndView getNews(@PathVariable("newsId") int newsId, Model model,
                                @RequestParam(value = "current", defaultValue = "1", required = false) int current,
                                @RequestParam(value = "commentId",  defaultValue = "0", required = false) int commentId,
                                @RequestParam(value = "type", defaultValue = "0", required = false) int type){ //1评论分页、2回复分页

        //新闻
        News news = newsService.findNewsById(newsId);
        model.addAttribute("news", news);
        //评论分页信息
        Page commentPage = new Page();
        commentPage.setPath("/news/detail/" + newsId);
        commentPage.setLimit(5);
        commentPage.setRows(commentService.findCountByEntity(ENTITY_TYPE_NEWS, newsId));
        model.addAttribute("commentPage", commentPage);
        //评论VO列表
        List<Map<String, Object>> commentVOList = new ArrayList<>();
        if(type == 2){ //回复分页
            //回复分页信息
            Page replyPage = new Page();
            replyPage.setPath("/news/detail/" + newsId);
            replyPage.setLimit(5);
            replyPage.setCurrent(current);
            //评论VO
            Map<String, Object> commentVO = new HashMap<>();
            //回复列表
            List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, commentId, replyPage.getOffset(), replyPage.getLimit());
            int replyCount = commentService.findCountByEntity(ENTITY_TYPE_COMMENT, commentId);
            replyPage.setRows(replyCount);
            commentVO.put("replies",getReplyVOList(replyList));
            commentVO.put("replyPage", replyPage);
            commentVO.put("replyCount", replyCount);
            commentVO.put("comment", commentService.findCommentById(commentId));
            model.addAttribute("cvo", commentVO);
            return new ModelAndView("/site/news-detail::.reply-list-holder");
        }
        commentPage.setCurrent(current);
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_NEWS, newsId, commentPage.getOffset(), commentPage.getLimit());
        for(Comment comment : commentList){
            //评论VO
            Map<String, Object> commentVO = new HashMap<>();
            //评论
            commentVO.put("comment", comment);
            //作者
            commentVO.put("user", userService.findUserById(comment.getUserId()));
            //点赞信息
            commentVO.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId()));
            commentVO.put("likeStatus", hostHolder.getUser() == null ? 0 :
                    likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId()));
            //回复数量
            int replyCount = commentService.findCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
            //回复列表
            //回复分页
            Page replyPage = new Page();
            replyPage.setPath("/news/detail/" + newsId);
            replyPage.setLimit(5);
            List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), replyPage.getOffset(), replyPage.getLimit());
            //回复VO列表
            List<Map<String, Object>> replyVOList = getReplyVOList(replyList);
            replyPage.setRows(replyCount);
            commentVO.put("replyPage", replyPage);
            commentVO.put("replies", replyVOList);
            commentVO.put("replyCount", replyCount);
            commentVOList.add(commentVO);
        }
        model.addAttribute("comments", commentVOList);
        if(type == 1){
            return new ModelAndView("/site/news-detail::.comment-list-holder");
        }
        //作者
        User user = userService.findUserById(news.getUserId());
        model.addAttribute("user", user);
        //点赞信息
        model.addAttribute("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_NEWS, newsId));
        model.addAttribute("likeStatus", hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_NEWS, newsId));
        //收藏信息
        model.addAttribute("favoriteCount", favoriteService.findNewsFavoriteCount(newsId));
        model.addAttribute("favoriteStatus", hostHolder.getUser() == null ? false :
                favoriteService.findUserFavoriteStatus(hostHolder.getUser().getId(), newsId));
        //发布者关注信息
        model.addAttribute("followeeCount", followService.findFolloweeCount(news.getUserId(), ENTITY_TYPE_USER));
        model.addAttribute("followerCount", followService.findFollowerCount(ENTITY_TYPE_USER, news.getUserId()));
        boolean hasFollowed = hostHolder.getUser() == null ? false :
                followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, news.getUserId());
        model.addAttribute("hasFollowed", hasFollowed);
        return new ModelAndView("/site/news-detail");
    }

    private List<Map<String, Object>> getReplyVOList(List<Comment> replyList){
        List<Map<String, Object>> replyVOList = new ArrayList<>();
        for(Comment reply : replyList){
            //回复VO
            Map<String, Object> replyVO = new HashMap<>();
            //回复
            replyVO.put("reply", reply);
            //作者
            replyVO.put("user", userService.findUserById(reply.getUserId()));
            //点赞信息
            long likeCount =  likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
            replyVO.put("likeCount", likeCount);
            int likeStatus = hostHolder.getUser() == null ? 0 :
                    likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
            replyVO.put("likeStatus", likeStatus);
            //回复目标
            User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
            replyVO.put("target", target);
            replyVOList.add(replyVO);
        }
        return replyVOList;
    }

//    @RequestMapping(path = "/detail/{newsId}", method = RequestMethod.GET)
//    public String getNews(@PathVariable("newsId") int newsId, Model model, Page page){
//        //新闻
//        News news = newsService.findNewsById(newsId);
//        model.addAttribute("news", news);
//        //作者
//        User user = userService.findUserById(news.getUserId());
//        model.addAttribute("user", user);
//        //点赞信息
//        long likeCount =  likeService.findEntityLikeCount(ENTITY_TYPE_NEWS, newsId);
//        model.addAttribute("likeCount", likeCount);
//        int likeStatus = hostHolder.getUser() == null ? 0 :
//                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_NEWS, newsId);
//        model.addAttribute("likeStatus", likeStatus);
//        //收藏信息
//        model.addAttribute("favoriteCount", favoriteService.findNewsFavoriteCount(newsId));
//        model.addAttribute("favoriteStatus", hostHolder.getUser() == null ? false :
//                favoriteService.findUserFavoriteStatus(hostHolder.getUser().getId(), newsId));
//        //发布者关注信息
//        model.addAttribute("followeeCount", followService.findFolloweeCount(news.getUserId(), ENTITY_TYPE_USER));
//        model.addAttribute("followerCount", followService.findFollowerCount(ENTITY_TYPE_USER, news.getUserId()));
//        boolean hasFollowed = hostHolder.getUser() == null ? false :
//                followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, news.getUserId());
//        model.addAttribute("hasFollowed", hasFollowed);
//
//        //评论分页信息
//        page.setLimit(10);
//        page.setPath("/news/detail/" + newsId);
//        page.setRows(commentService.findCountByEntity(ENTITY_TYPE_NEWS, newsId));
//        //评论列表
//        List<Comment> commentsList = commentService.findCommentsByEntity(ENTITY_TYPE_NEWS, newsId, page.getOffset(), page.getLimit());
//        //评论VO列表
//        List<Map<String, Object>> commentVoList = new ArrayList<>();
//        if(commentsList != null){
//            for(Comment comment : commentsList){
//                //评论VO
//                Map<String, Object> commentVo = new HashMap<>();
//                //评论
//                commentVo.put("comment", comment);
//                //作者
//                commentVo.put("user", userService.findUserById(comment.getUserId()));
//                //点赞信息
//                likeCount =  likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
//                commentVo.put("likeCount", likeCount);
//                likeStatus = hostHolder.getUser() == null ? 0 :
//                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
//                commentVo.put("likeStatus", likeStatus);
//                //回复数量
//                int replyCount = commentService.findCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
//                //回复分页信息
////                Page replyPage = new Page();
////                replyPage.setRows(replyCount);
////                replyPage.setPath("/news/detail/" + newsId);
////                replyPage.setLimit(5);
//                //回复列表
//                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
//                //回复VO列表
//                List<Map<String, Object>> replyVoList = new ArrayList<>();
//                for(Comment reply : replyList){
//                    //回复VO
//                    Map<String, Object> replyVo = new HashMap<>();
//                    //回复
//                    replyVo.put("reply", reply);
//                    //作者
//                    replyVo.put("user", userService.findUserById(comment.getUserId()));
//                    //点赞信息
//                    likeCount =  likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
//                    replyVo.put("likeCount", likeCount);
//                    likeStatus = hostHolder.getUser() == null ? 0 :
//                            likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
//                    replyVo.put("likeStatus", likeStatus);
//                    //回复目标
//                    User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
//                    replyVo.put("target", target);
//                    replyVoList.add(replyVo);
//                }
////                commentVo.put("page", replyPage);
//                commentVo.put("replies", replyVoList);
//                commentVo.put("replyCount", replyCount);
//                commentVoList.add(commentVo);
//            }
//            model.addAttribute("comments", commentVoList);
//            return "/site/news-detail";
//        }
//        return "/site/news-detail";
//    }
}
