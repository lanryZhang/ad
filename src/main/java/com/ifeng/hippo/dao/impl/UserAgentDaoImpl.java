package com.ifeng.hippo.dao.impl;

import com.ifeng.hippo.dao.UserAgentDao;
import com.ifeng.hippo.entity.UserAgentInfo;
import com.ifeng.hippo.mongo.MongoFactory;
import com.ifeng.mongo.MongoCli;
import com.ifeng.mongo.MongoSelect;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by zhanglr on 2016/8/25.
 */
@Repository
public class UserAgentDaoImpl implements UserAgentDao {
    private final static String COLLECTION_NAME = "user_agents";
    private MongoCli client;
    public UserAgentDaoImpl(){
        client = MongoFactory.createMongoClient();
        client.getCollection(COLLECTION_NAME);
    }
    @Override
    public void insert(UserAgentInfo userAgentInfo) throws Exception {
        client.insert(userAgentInfo);
    }

    @Override
    public UserAgentInfo selectOne(int page) throws Exception {
        List<UserAgentInfo> list = client.selectList(new MongoSelect().page(page,1), UserAgentInfo.class);
        if (list != null && list.size() > 0)
            return list.get(0);
        return null;
    }

    @Override
    public long count() throws Exception {
        return client.count();
    }
}
