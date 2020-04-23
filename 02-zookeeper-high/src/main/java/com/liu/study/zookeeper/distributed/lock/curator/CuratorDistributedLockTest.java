package com.liu.study.zookeeper.distributed.lock.curator;

import com.liu.study.zookeeper.CommonConstant;
import com.liu.study.zookeeper.distributed.lock.zk.ZookeeperApiLock;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @desc
 * 测试Curator框架中的分布式锁。
 * @author Liuweian
 * @version 1.0.0
 * @createTime 2020/4/22 16:09
 */
public class CuratorDistributedLockTest {

    private static Logger logger = LoggerFactory.getLogger(ZookeeperApiLock.class);

    private final static String TOP_PATH = "/curator/lock";

    /**
     * InterProcessMultiLock：是多锁对象，可以对多个对象进行加锁；
     * InterProcessMutex：可重入锁。
     * InterProcessReadWriteLock：读写锁
     *
     *
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        testInterProcessMutex();
    }

    /**
     * 测试InterProcessMutex（互斥锁）
     */
    public static void testInterProcessMutex() throws Exception {
        CuratorFramework curatorFramework1 = CuratorFrameworkFactory.builder()
                .connectString(CommonConstant.COMPANY_ZOO_CLUSTER_ADDRESS)
                .connectionTimeoutMs(20000)
                .sessionTimeoutMs(20000)
                .retryPolicy(new ExponentialBackoffRetry(20000, 3))
                .build();


        CuratorFramework curatorFramework2 = CuratorFrameworkFactory.builder()
                .connectString(CommonConstant.COMPANY_ZOO_CLUSTER_ADDRESS)
                .connectionTimeoutMs(20000)
                .sessionTimeoutMs(20000)
                .retryPolicy(new ExponentialBackoffRetry(20000, 3))
                .build();

        curatorFramework1.start();
        curatorFramework2.start();

        curatorFramework1.blockUntilConnected();
        curatorFramework2.blockUntilConnected();


        new Thread(() -> {
            InterProcessMutex mutex = new InterProcessMutex(curatorFramework1, TOP_PATH);
            try {
                mutex.acquire();
                logger.info(Thread.currentThread().getName() + "    【获取】锁");

                TimeUnit.SECONDS.sleep(15);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    mutex.release();
                    logger.info(Thread.currentThread().getName() + "    【释放】锁");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "========>>线程一<<=========").start();

        new Thread(() -> {
            InterProcessMutex mutex = new InterProcessMutex(curatorFramework2, TOP_PATH);
            try {
                TimeUnit.SECONDS.sleep(2);
                mutex.acquire();
                logger.info(Thread.currentThread().getName() + "    【获取】锁");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    mutex.release();
                    logger.info(Thread.currentThread().getName() + "    【释放】锁");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "========>>线程二<<=========").start();
    }

}
