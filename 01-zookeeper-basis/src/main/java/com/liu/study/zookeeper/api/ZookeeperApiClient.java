package com.liu.study.zookeeper.api;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @desc  用于测试原生的Zookeeper Api
 * @author Liuweian
 * @version 1.0.0
 * @createTime 2020/4/21 9:21
 */
public class ZookeeperApiClient {

    private static Logger logger = LoggerFactory.getLogger(ZookeeperApiClient.class);

    /** 信号量，阻塞程序执行，用于等待zookeeper连接成功，发送成功信号 */
    private CountDownLatch countDown = new CountDownLatch(1);

    private static final String COMPANY_ZOO_CLUSTER_ADDRESS = "22.144.101.47:2181,22.144.101.47:2182,22.144.101.47:2183";

    private static final String MYSELF_ZOO_SINGLETON_ADDRESS = "";

    private static final String MYSELF_ZOO_CLUSTER_ADDRESS = "";

    private ZooKeeper zookeeper;

    public ZookeeperApiClient() {
        try {
            zookeeper = new ZooKeeper(COMPANY_ZOO_CLUSTER_ADDRESS, 15000,  new Watcher() {

               @Override
               public void process(WatchedEvent watchedEvent) {
                   String path = watchedEvent.getPath();
                   Event.KeeperState state = watchedEvent.getState();
                   Event.EventType type = watchedEvent.getType();

                   logger.info("操作路径：{}，操作类型：{}，状态：{}", path, type, state);

                   if(Event.KeeperState.SyncConnected.equals(state)){

                       if(Event.EventType.None.equals(type)){
                           /** 连接建立成功，则释放信号量，让阻塞的程序继续向下执行 */
                           countDown.countDown();
                           logger.info("与zookeeper服务端建立连接成功！！！！");
                       }
                   }
               }
           });
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            /** 等待连接成。 */
            countDown.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("创建zookeeper成功！连接地址为：{}", COMPANY_ZOO_CLUSTER_ADDRESS);
    }

    public void close () throws InterruptedException {
        zookeeper.close();
    }

    public ZooKeeper getZooKeeper() {
        return zookeeper;
    }

    /**
     * register(Watcher watch)：这个会改变注册时候的Watcher。
     */


    /**
     *
     * @param path
     * @param data
     * @param ids
     * @param createMode
     * @throws IOException
     */
    public void addNode(String path, String data, List<ACL> ids, CreateMode createMode) throws IOException {
        try {
            zookeeper.create(path, data.getBytes(), ids, createMode);
            logger.info("创建节点--【成功】");
        } catch (Exception e) {
            logger.info("创建节点--【失败】");
            e.printStackTrace();
        }
    }

    /**
     * 获取数据。
     * @param path
     * @param watcher
     * @param stat
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public String getData(String path, Watcher watcher, Stat stat) throws KeeperException, InterruptedException {
        if (watcher == null) {
            byte[] resultByte = zookeeper.getData(path, false, stat);
            return new String(resultByte);
        }

        byte[] resultByte = zookeeper.getData(path, watcher, stat);
        return new String(resultByte);
    }

    /**
     *
     * @param path
     * @param watcher
     */
    public Stat exists(String path, Watcher watcher) throws KeeperException, InterruptedException {
        /**
         * 如果使用exists(String path, boolean watch)这个方法，watch如果是true，并且没有向zookeeper中注册过Watcher，
         * 那么就默认使用创建Zookeeper时候传入的Watcher。
         */
        return  zookeeper.exists(path, watcher);
    }


    /**
     * 对给定path设置数据。
     * @param path
     * @param data
     * @param version
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public Stat setData(String path, String data, int version) throws KeeperException, InterruptedException {
        return zookeeper.setData(path, data.getBytes(), version);
    }

    /**
     *
     * @param path
     * @param version
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void delete(String path, int version) throws KeeperException, InterruptedException {
        zookeeper.delete(path, version);
    }
}
