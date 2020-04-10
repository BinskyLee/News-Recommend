package com.fzu.recommend;

import com.fzu.recommend.entity.User;
import com.fzu.recommend.service.LikeService;
import com.fzu.recommend.util.HostHolder;
import com.fzu.recommend.util.RecommendUtil;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = RecommendApplication.class)
public class RedisTest {



}
