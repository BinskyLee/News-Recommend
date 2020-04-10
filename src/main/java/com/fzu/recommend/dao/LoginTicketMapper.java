package com.fzu.recommend.dao;

import com.fzu.recommend.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
@Deprecated
public interface LoginTicketMapper {

    String insertField = "user_id, ticket, status, expired ";

    String selectField = "id, user_id, ticket, status, expired ";

    @Insert({
            "insert into login_ticket (",
            insertField,
            ") values(#{userId}, #{ticket}, #{status}, #{expired})" ,
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select ",
            selectField,
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    @Update({
            "update login_ticket set status=#{status} where ticket=#{ticket}"
    })
    int updateStatus(String ticket, int status);
}
