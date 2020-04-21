package com.liu.study.zookeeper.api;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @desc  zookeeper的回调（增删改节点）。
 * @author Liuweian
 * @version 1.0.0
 * @createTime 2020/4/21 10:01
 */
public class ZookeeperApiWatcher implements Watcher {

    /**
     * zookeeper可以通过getData(..)、getChildren(..)、exits(..)三个接口向zookeeper服务端注册watcher。
     */
    private static Logger logger = LoggerFactory.getLogger(ZookeeperApiWatcher.class);

    @Override
    public void process(WatchedEvent watchedEvent) {

        logger.info("【=====自定义ZookeeperApiWatcher被触发=====】    操作路径：{}，操作类型：{}，通知状态：{}", watchedEvent.getPath(), watchedEvent.getType(), watchedEvent.getState());

    }

}
