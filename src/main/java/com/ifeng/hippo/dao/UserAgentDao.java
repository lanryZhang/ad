package com.ifeng.hippo.dao;


import com.ifeng.hippo.entity.UserAgentInfo;

/**
 * Created by zhanglr on 2016/8/25.
 */
public interface UserAgentDao {
    void insert(UserAgentInfo userAgentInfo) throws Exception;
    UserAgentInfo selectOne(int page) throws Exception;
    long count() throws Exception ;
}
