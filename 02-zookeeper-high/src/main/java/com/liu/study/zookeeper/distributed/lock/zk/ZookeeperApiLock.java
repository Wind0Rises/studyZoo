package com.liu.study.zookeeper.distributed.lock.zk;

import com.liu.study.zookeeper.CommonConstant;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

/**
 * @desc 使用Zookeeper实现分布式锁。这个类暂时不具备线程安全。
 *      第一步：设置持久化节点，作为某一个锁。
 *      第二步：如果有线程需要获取锁，就
 *
 *
 *
 *      问题：
 *          1、类暂时不具备安全性，如何处理，要保证单机下也具备高并发安全性。。
 *              答：
 *
 *          2、线程中断怎么办？
 *              答：这就是临时节点的作用。
 *
 *          3、zkClient的开关，应该放的位置。
 *
 * @author Liuweian
 * @version 1.0.0
 * @createTime 2020/4/22 16:08
 */
public class ZookeeperApiLock implements Lock {

    private static Logger logger = LoggerFactory.getLogger(ZookeeperApiLock.class);

    private static final String PARENT_PATH = "/parent";

    private static final String LOCK_NAME = "orderLock";

    private static final Object object = new Object();

    private ZkClient zkClient;

    private String concurrentPath;

    private String previousPath;

    private Set<String> localPaths = new HashSet<>();

    public ZookeeperApiLock() {

    }

    public void lock() {
        /** 1、创建锁节点，并创建本锁节点。 */
        createZNode();

        /** 2、尝试获取锁 */
        if (tryLock()) {
            return;
        }

        /**
         * 3、尝试获取锁失败，进行自旋操作。
         *
         * 应该在本节点的上一个节点注册一个Watcher，如果本节点未获取到锁
         * 则，等待，直到上一个节点被删除放弃锁为止。
         */
        boolean lockResult = false;
        while (!lockResult) {
            boolean exists = zkClient.exists(CommonConstant.PREFIX_PATH + PARENT_PATH + "/" +previousPath);
            if (!exists) {
                lockResult = tryLock();
            }

            if (!lockResult) {
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 创建【锁持久节点】，并且在锁节点下创建【本锁临时有序节点】
     */
    private void createZNode() {
        zkClient = new ZkClient(CommonConstant.COMPANY_ZOO_CLUSTER_ADDRESS, 30000, 30000);
        zkClient.waitUntilConnected();

        /** 01、创建父节点【持久化节点】 */
        if (!zkClient.exists(CommonConstant.PREFIX_PATH + PARENT_PATH)) {
            zkClient.create(CommonConstant.PREFIX_PATH + PARENT_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        /** 02、创建子节点【临时并且有序】 */
        String lockPath = zkClient.create(CommonConstant.PREFIX_PATH + PARENT_PATH + "/" + LOCK_NAME,
                Thread.currentThread().getName(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        localPaths.add(lockPath);

        /*synchronized (object) {
            if (concurrentPath == null) {
                this.concurrentPath = lockPath;
            } else {
                // 进入等待。
                LockSupport.park();
            }
        }*/

        this.concurrentPath = lockPath;


        logger.info("【== {}锁创建成功 ==】，锁路径为：{}", LOCK_NAME, concurrentPath);

    }

    public boolean tryLock() {
        /** 01、获取锁的子节点，并排序 */
        List<String> childrenPath = zkClient.getChildren(CommonConstant.PREFIX_PATH + PARENT_PATH);
        Collections.sort(childrenPath);

        int result = childrenPath.indexOf(concurrentPath.substring((CommonConstant.PREFIX_PATH + PARENT_PATH).length() + 1));

        if (result == 0) {
            logger.info(Thread.currentThread().getName() + "获取到锁，锁路径为：{}", concurrentPath);
            return true;
        }

        this.previousPath = childrenPath.get(result - 1);
        return false;
    }

    public void unlock() {
        zkClient.delete(concurrentPath);
        zkClient.close();
        logger.info(Thread.currentThread().getName() + "释放锁，锁路径为：{}", concurrentPath);
    }


    public void lockInterruptibly() throws InterruptedException {

    }



    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }





    public Condition newCondition() {
        return null;
    }
}
