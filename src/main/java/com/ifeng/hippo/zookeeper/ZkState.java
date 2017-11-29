package com.ifeng.hippo.zookeeper;

import com.alibaba.fastjson.JSON;
import com.ifeng.configurable.Context;
import com.ifeng.hippo.contances.Config;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.ProtectACLCreateModePathAndBytesable;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Map;

public class ZkState {
    public static final Logger LOG = LoggerFactory.getLogger(ZkState.class);
    CuratorFramework _curator;

    private CuratorFramework newCurator(Context context) throws Exception {
        String serverPorts = "";
        String[] hosts = context.getString("hosts").split(",");
        for (String server : hosts) {
            String[] s1 = server.split(":");
            serverPorts = serverPorts + s1[0] + ":" + s1[1] + ",";
        }
        return CuratorFrameworkFactory.newClient(serverPorts, context.getInt(Config.HIPPO_ZOOKEEPER_SESSION_TIMEOUT), 15000, new RetryNTimes(
                context.getInt(Config.HIPPO_ZOOKEEPER_RETRY_TIMES), context.getInt(Config.HIPPO_ZOOKEEPER_RETRY_INTERVAL)));
    }

    public CuratorFramework getCurator() {
        assert _curator != null;
        return _curator;
    }

    public ZkState(Context context) {
        try {
            _curator = newCurator(context);
            _curator.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void writeJSON(String path, Map<Object, Object> data) {
        LOG.info("Writing " + path + " the data " + data.toString());
        writeBytes(path, JSON.toJSONString(data).getBytes(Charset.forName("UTF-8")));
    }

    public void remove(){
        _curator.delete();
    }

    public void writeBytes(String path, byte[] bytes) {
        try {
            if (_curator.checkExists().forPath(path) == null) {
                CreateBuilder builder = _curator.create();
                ProtectACLCreateModePathAndBytesable<String> createAble = builder
                        .creatingParentsIfNeeded();
                createAble.withMode(CreateMode.EPHEMERAL).forPath(path, bytes);
            } else {
                _curator.setData().forPath(path, bytes);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void watch(String path,WatchCallBack callBack){
        try {
            if (_curator.checkExists().forPath(path) == null) {
                throw new KeeperException.NoNodeException(path);
            } else {
                TreeCache treeCache = new TreeCache(_curator,path);
                treeCache.getListenable().addListener((client, event) -> {
                    ChildData data = event.getData();
                    if(data !=null){
                        callBack.callback(event);
                    }else{
                        LOG.error( "data is null : "+ event.getType());
                    }
                });
                //开始监听
                treeCache.start();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public Map<Object, Object> readJSON(String path) {
        try {
            byte[] b = readBytes(path);
            if (b == null)
                return null;
            return (Map<Object, Object>) JSON.parse(new String(b, "UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] readBytes(String path) {
        try {
            if (_curator.checkExists().forPath(path) != null) {
                return _curator.getData().forPath(path);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        _curator.close();
        _curator = null;
    }
}