package com.zhuo.im.service.utils;

import com.zhuo.im.common.constant.Constants;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description: Zookeeper tools
 **/
@Component
public class ZKit {

    private static Logger logger = LoggerFactory.getLogger(ZKit.class);

    @Autowired
    private ZkClient zkClient;

    /**
     * Get all TCP server nodes from ZooKeeper
     *
     * @return
     */
    public List<String> getAllTcpNodes() {
        List<String> children = zkClient.getChildren(Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp);
//        logger.info("Query all nodes =[{}] success.", JSON.toJSONString(children));
        return children;
    }

    /**
     * Get all WEB server nodes from ZooKeeper
     *
     * @return
     */
    public List<String> getAllWebNodes() {
        List<String> children = zkClient.getChildren(Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb);
//        logger.info("Query all nodes =[{}] success.", JSON.toJSONString(children));
        return children;
    }

}
