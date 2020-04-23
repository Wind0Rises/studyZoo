package com.liu.study.zookeeper.distributed.lock.zk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @desc
 *      模拟在多机集群部署的情况下，实现多级集群下的锁Lock。【像Synchronize、ReentrantLock这些都只能保证在单机下的同步。】
 * @author Liuweian
 * @version 1.0.0
 * @createTime 2020/4/22 17:40
 */
public class ZookeeperApiLockTest {

    private static ZookeeperApiLock lock1 = new ZookeeperApiLock();

    private static ZookeeperApiLock lock2 = new ZookeeperApiLock();

    private static Logger logger = LoggerFactory.getLogger(ZookeeperApiLockTest.class);

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                lock1.lock();
                logger.info("+++++++++++++++++++++++++++++++++线程一获取到锁==========");
                try {
                    TimeUnit.SECONDS.sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } finally {
                lock1.unlock();
                logger.info("+++++++++++++++++++++++++++++++++线程一获释放锁==========");
            }
        }, ">>>>>>>>>>>>线程一<<<<<<<<<<<").start();

        new Thread(() -> {
            try {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock2.lock();
                logger.info("+++++++++++++++++++++++++++++++++线程二获取到锁==========");
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } finally {
                lock2.unlock();
                logger.info("+++++++++++++++++++++++++++++++++线程二释放锁==========");
                try {
                    TimeUnit.SECONDS.sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ">>>>>>>>>>>>线程二<<<<<<<<<<<").start();
    }

}
