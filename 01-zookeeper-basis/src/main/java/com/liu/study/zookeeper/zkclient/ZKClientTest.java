package com.liu.study.zookeeper.zkclient;

import com.liu.study.zookeeper.constant.CommonConstant;
import com.liu.study.zookeeper.curator.CuratorCustomWatcher;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * @desc
 * @author Liuweian
 * @version 1.0.0
 * @createTime 2020/4/21 16:08
 */
public class ZKClientTest {

    private static final String TOP_PATH = "/zk";

    private static Logger logger = LoggerFactory.getLogger(ZKClientTest.class);

    public static void main(String[] args) throws NoSuchAlgorithmException {
        ZkClient zkClient = new ZkClient(CommonConstant.COMPANY_ZOO_CLUSTER_ADDRESS, 30000, 30000);

        /**
         *
         */
        boolean exists = zkClient.exists(TOP_PATH);
        logger.info("【======  查询是否存在  =======】   结果为：{}，", exists);

        if (!exists) {
            String aclString = DigestAuthenticationProvider.generateDigest("username:password");
            String createResult = zkClient.create(TOP_PATH, "this is zkClient", Arrays.asList(new ACL(1,
                    new Id("digest", aclString))), CreateMode.PERSISTENT);
            logger.info("【======  创建成功  =======】   节点为：{}，权限：{}", createResult, aclString);
        }
        zkClient.close();

        /**
         * 报权限不存在。
         */
        /*ZkClient zkClient2 = new ZkClient(CommonConstant.COMPANY_ZOO_CLUSTER_ADDRESS, 30000, 30000);
        String readData = zkClient2.readData(TOP_PATH);
        logger.info("【======  读取节点数据成功  =======】   节点为：{}，返回结果：{}", TOP_PATH, readData);
        zkClient2.close();*/



        ZkClient zkClient3 = new ZkClient(CommonConstant.COMPANY_ZOO_CLUSTER_ADDRESS, 30000, 30000);
        zkClient3.addAuthInfo("digest", "username:password".getBytes());
        String readData2 = zkClient3.readData(TOP_PATH);
        logger.info("【======  读取节点数据成功  =======】   节点为：{}，返回结果：{}", TOP_PATH, readData2);
    }

}
