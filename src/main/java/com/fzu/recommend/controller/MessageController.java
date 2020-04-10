package com.fzu.recommend.controller;

import com.alibaba.fastjson.JSONObject;
import com.fzu.recommend.annotation.LoginRequired;
import com.fzu.recommend.entity.Message;
import com.fzu.recommend.entity.Page;
import com.fzu.recommend.entity.User;
import com.fzu.recommend.service.MessageService;
import com.fzu.recommend.service.UserService;
import com.fzu.recommend.util.HostHolder;
import com.fzu.recommend.util.RecommendConstant;
import com.fzu.recommend.util.RecommendUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements RecommendConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    @LoginRequired
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUser();
        //分页信息
        page.setRows(messageService.findConversationCount(user.getId()));
        page.setPath("/letter/list");
        page.setLimit(5);

        //会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        //会话VO列表
        List<Map<String, Object>> replyVOList= new ArrayList<>();
        if(conversationList != null){
            for(Message conversation : conversationList){
                Map<String, Object> conversationVO = new HashMap<>();
                conversationVO.put("conversation", conversation);
                conversationVO.put("letterCount", messageService.findLetterCount(conversation.getConversationId()));
                conversationVO.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), conversation.getConversationId()));
                int targetId = user.getId() == conversation.getFromId() ? conversation.getToId() : conversation.getFromId();
                conversationVO.put("target", userService.findUserById(targetId));
                replyVOList.add(conversationVO);
            }
        }
        model.addAttribute("conversations", replyVOList);
        //查询用户未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    @LoginRequired
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model){
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        User user = hostHolder.getUser();

        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letterVOList = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        if(letterList != null){
            for(Message letter : letterList){
                Map<String, Object> letterVO = new HashMap<>();
                letterVO.put("letter", letter);
                letterVO.put("user", userService.findUserById(letter.getFromId()));
                letterVOList.add(letterVO);
                if(user.getId() == letter.getToId() && letter.getStatus() == 0){
                    ids.add(letter.getId());
                }
            }
        }
        model.addAttribute("letters", letterVOList);
        model.addAttribute("target", getLetterTarget(conversationId));
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";
    }


    private User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if(hostHolder.getUser().getId() == id0){
            return userService.findUserById(id1);
        }else{
            return userService.findUserById(id0);
        }
    }

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String sendLetter(String toName, String content){
        if(StringUtils.isBlank(toName)){
            return RecommendUtil.getJSONString(1, "私信对象不能为空");
        }
        if(StringUtils.isBlank(content)){
            return RecommendUtil.getJSONString(2, "私信内容不能为空");
        }
        User target = userService.findUserByName(toName);
        User user = hostHolder.getUser();
        if(target == null){
            return RecommendUtil.getJSONString(3, "私信对象不存在");
        }
        if(target.getId() == user.getId()){
            return RecommendUtil.getJSONString(4, "不能给自己发送私信");
        }
        Message message = new Message();
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setStatus(0);
        message.setFromId(user.getId());
        message.setToId(target.getId());
        if(message.getFromId() < message.getToId()){
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        messageService.addMessage(message);
        return RecommendUtil.getJSONString(0);
    }

    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    @LoginRequired
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();
        //查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        Map<String, Object> messageVO = new HashMap<>();
        if(message != null){
            messageVO.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user", userService.findUserById((Integer)data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));//判断是评论了回复/新闻
            messageVO.put("newsId", data.get("newsId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread", unread);
            model.addAttribute("commentNotice", messageVO);
        }

        //查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        messageVO = new HashMap<>();
        if(message != null){
            messageVO.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user", userService.findUserById((Integer)data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));//判断是评论了回复/新闻
            messageVO.put("newsId", data.get("newsId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unread", unread);
            model.addAttribute("likeNotice", messageVO);
        }

        //查看关注类通知

        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        messageVO = new HashMap<>();
        if(message != null){
            messageVO.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user", userService.findUserById((Integer)data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));//判断是评论了回复/新闻

            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread", unread);
            model.addAttribute("followNotice", messageVO);
        }
        //查询未读消息数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    @LoginRequired
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model){
        User user = hostHolder.getUser();

        //分页信息
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));


        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVOList = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        if(noticeList != null){
            for(Message notice : noticeList){
                Map<String, Object> noticeVO = new HashMap<>();
                noticeVO.put("notice", notice);
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                noticeVO.put("user", userService.findUserById((Integer)data.get("userId")));
                noticeVO.put("entityType", data.get("entityType"));
                noticeVO.put("entityId", data.get("entityId"));
                noticeVO.put("newsId", data.get("newsId"));

                noticeVOList.add(noticeVO);
                if(notice.getStatus() == 0){
                    ids.add(notice.getId());
                }
            }
        }
        model.addAttribute("notices", noticeVOList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/notice-detail";
    }

}
