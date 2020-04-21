package com.liu.study.zookeeper.api;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @desc  测试类。
 * @author Liuweian
 * @version 1.0.0
 * @createTime 2020/4/21 11:19
 */
public class ZookeeperApiTest {


    private static Logger logger = LoggerFactory.getLogger(ZookeeperApiWatcher.class);

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    private static final String TOP_PATH = "/api";

    public static void main(String[] args) throws Exception {

        ZookeeperApiTest apiTest = new ZookeeperApiTest();

        //apiTest.firstWatcherTest();


        apiTest.secondWatcherTest();

    }

    /**
     * Watcher测试。
     * Watcher这还一个异步操作。
     *
     * 更新和通知的顺序？？
     *
     * @throws Exception
     */
    public void firstWatcherTest() throws Exception {
        ZookeeperApiClient client = new ZookeeperApiClient();

        /**
         * 1、在exists(..)操作中注册Watcher。
         * 2、通过setData(..)修改数据。
         * 3、修改数据（setData(..)）以后，观察
         */
        ZookeeperApiWatcher watcher = new ZookeeperApiWatcher();
        Stat existsStat = client.exists(TOP_PATH, watcher);
        logger.info("【【=====Exists操作完成=====】】    结果为：{}", existsStat.toString());

        /** -1：匹配任何版本 */
        Stat setDataStat = client.setData(TOP_PATH, "firstWatcherTest_setData1", -1);
        logger.info("【【=====setData操作完成=====】】   结果为：{}", setDataStat.toString());

        String nodeData = client.getData(TOP_PATH, null, new Stat());
        logger.info("【【=====getData操作完成=====】】   {}的数据为：{}", TOP_PATH, nodeData);
    }


    /**
     *
     * @throws Exception
     */
    public void secondWatcherTest() throws Exception {
        ZookeeperApiClient client1 = new ZookeeperApiClient();

        /**
         * 1、在第一个连接中使用exists(..)操作中注册Watcher。
         * 2、在第二个连接通过setData(..)修改数据。并修改数据（setData(..)）
         * 3、观察：
         *         * 在第一个连接没有关闭情况下，是否可以触发Watcher
         *         * 在第一个连接关闭情况下，是否可以触发Watcher。
         *
         * 问题：第一次连接都关闭了？为什么Watcher还会被触发。
         */
        ZookeeperApiWatcher watcher = new ZookeeperApiWatcher();
        Stat existsStat = client1.exists(TOP_PATH, watcher);
        logger.info("【【=====Exists操作完成=====】】    结果为：{}", existsStat.toString());


        String nodeData11 = client1.getData(TOP_PATH, null, new Stat());
        logger.info("【【=====getData操作完成=====】】   1  开始  -- {}的数据为：{}", TOP_PATH, nodeData11);

        // 第二次测试时，打开
        client1.close();

        new Thread(() -> {
            try {
                ZookeeperApiClient client2 = new ZookeeperApiClient();

                Stat setDataStat = client2.setData(TOP_PATH, "firstWatcherTest_setData3", -1);
                logger.info("【【=====setData操作完成=====】】   结果为：{}", setDataStat.toString());

                String nodeData2 = client2.getData(TOP_PATH, null, new Stat());
                logger.info("【【=====getData操作完成=====】】   2 -- {}的数据为：{}", TOP_PATH, nodeData2);

                client2.close();
                countDownLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        countDownLatch.await();

        String nodeData12 = client1.getData(TOP_PATH, null, new Stat());
        logger.info("【【=====getData操作完成=====】】   1  结束  -- {}的数据为：{}", TOP_PATH, nodeData12);
    }
}
