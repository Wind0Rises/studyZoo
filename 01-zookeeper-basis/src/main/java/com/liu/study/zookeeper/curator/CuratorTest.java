package com.liu.study.zookeeper.curator;

import com.liu.study.zookeeper.constant.CommonConstant;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.imps.ExtractingCuratorOp;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc
 *      问题一：KeeperErrorCode = Unimplemented for
 *      原因：curator客户端使用的版本比ZK server使用的版本高引起的
 *
 * @author Liuweian
 * @version 1.0.0
 * @createTime 2020/4/21 16:36
 */
public class CuratorTest {

    private static final String TOP_PATH = "/curator";

    private static Logger logger = LoggerFactory.getLogger(CuratorCustomWatcher.class);

    public static void main(String[] args) throws Exception {


        /**
         * 这里使用【【构建者模式】】 + 【【工厂模式】】
         * 可以通过CuratorFramework的newClient(..)创建。
         *
         *
         */
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                // 下面返回的都是Builder对象。
               .connectString(CommonConstant.COMPANY_ZOO_CLUSTER_ADDRESS)
               .connectionTimeoutMs(15000)
               .sessionTimeoutMs(15000)
               // .authorization("digest", "username:password".getBytes())  // 设置权限
               // .defaultData("defaultData")   // 设置默认数据
               // .namespace("")       // 如果在此处设置了名称空间，则所有路径都将在名称空间之前
               .retryPolicy(new ExponentialBackoffRetry(5000, 3))
               // .namespace(TOP_PATH)
               .build();

        //
        curatorFramework.start();
        curatorFramework.blockUntilConnected();



        List<String> list = curatorFramework.getChildren().forPath("/");

        /**
         * checkExists() ---> ExistsBuilder
         * 调用顺序：Watchable --> BackgroundPathable
         *
         * creatingParentsIfNeeded：递归方式创建节点。
         * creatingParentContainersIfNeeded：以容器方式递归创建节点。就版本的Zookeeper等于creatingParentsIfNeeded
         */
        Stat stat = curatorFramework.checkExists().creatingParentsIfNeeded().forPath(TOP_PATH);
        logger.info("【检查节点】=============  节点：{}，返回结果：{}", TOP_PATH, stat == null ? "不已经存在" : stat.toString());



        /**
         * create() --->  CreateBuilder
         */
        if (stat == null) {
            String result = curatorFramework.create().orSetData().creatingParentContainersIfNeeded()
                    .withMode(CreateMode.PERSISTENT).forPath(TOP_PATH, "this is curator".getBytes());
            logger.info("【创建节点】=============  节点：{}，返回结果：{}", TOP_PATH, result);
        }



        /**
         * getData() --->  GetDataBuilder
         *
         * Decompressible：压缩工具吗？？
         * Statable：让操作填充提供的stat对象。
         *
         * 调用顺序：Decompressible --> Statable --> Watchable --> BackgroundPathable
         * Decompressible中的方法会返回一个GetDataWatchBackgroundStatable。这个GetDataWatchBackgroundStatable
         *      继承了Watchable、Statable、BackgroundPathable
         * Statable中的方法会返回一个Watchable
         * Watchable中的方法会返回一个BackgroundPathable
         */
        Stat getDataStat = new Stat();
        byte[] resultByte = curatorFramework.getData().storingStatIn(stat).usingWatcher(new CuratorCustomWatcher()).forPath(TOP_PATH);
        logger.info("【查询内容】=============  路径：{}，查询的数据结果为：{}，状态为：{}", TOP_PATH, new String(resultByte), getDataStat.toString());



        /**
         * 事务怎么用
         */
        List<CuratorOp> curatorOps = new ArrayList<>();
        CuratorOp curatorOp = new ExtractingCuratorOp();
        // curatorFramework.transaction().forOperations(curatorOps);


    }

}
