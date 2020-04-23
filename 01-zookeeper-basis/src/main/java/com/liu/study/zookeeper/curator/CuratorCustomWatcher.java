package com.liu.study.zookeeper.curator;

import com.liu.study.zookeeper.api.ZookeeperApiWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @desc
 * @author Liuweian
 * @version 1.0.0
 * @createTime 2020/4/22 10:52
 */
public class CuratorCustomWatcher implements Watcher {

    private static Logger logger = LoggerFactory.getLogger(CuratorCustomWatcher.class);

    @Override
    public void process(WatchedEvent event) {
        logger.info("this is 【【【===========    CuratorCustomWatcher    =================】】】");
    }

}
