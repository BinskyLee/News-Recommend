package com.fzu.recommend.util;


import com.fzu.recommend.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，代替session对象
 */
@Component
public class HostHolder {

    //底层用map实现，以线程为key进行存储
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }
}
