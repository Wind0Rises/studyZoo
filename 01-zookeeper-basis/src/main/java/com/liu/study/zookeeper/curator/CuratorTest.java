package com.liu.study.zookeeper.curator;

import com.liu.study.zookeeper.constant.CommonConstant;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;

/**
 * @desc
 * @author Liuweian
 * @version 1.0.0
 * @createTime 2020/4/21 16:36
 */
public class CuratorTest {

    private static final String TOP_PATH = "/api";

    public static void main(String[] args) throws Exception {
        CuratorFramework curatorClient = CuratorFrameworkFactory.builder()
               .connectString(CommonConstant.COMPANY_ZOO_CLUSTER_ADDRESS)
               .connectionTimeoutMs(15000)
               .sessionTimeoutMs(15000)
               .retryPolicy(new ExponentialBackoffRetry(5000, 3))
               // .namespace(TOP_PATH)
               .build();

        curatorClient.start();

        curatorClient.blockUntilConnected();

        List<String> list = curatorClient.getChildren().forPath("/");

        curatorClient.create().withMode(CreateMode.PERSISTENT).forPath("/curator", "this is curator".getBytes());

    }

}
