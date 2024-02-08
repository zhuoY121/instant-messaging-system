package com.zhuo.im.tcp.register;

import com.zhuo.im.common.constant.Constants;
import org.I0Itec.zkclient.ZkClient;

/**
 * @description:
 * @version: 1.0
 */
public class ZKit {

    private ZkClient zkClient;

    public ZKit(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    // Path: im-coreRoot/tcp/ip:port
    public void createRootNode(){

        boolean rootExists = zkClient.exists(Constants.ImCoreZkRoot);
        if (!rootExists) {
            zkClient.createPersistent(Constants.ImCoreZkRoot);
        }

        boolean tcpExists = zkClient.exists(Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp);
        if (!tcpExists) {
            zkClient.createPersistent(Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp);
        }

        boolean webExists = zkClient.exists(Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb);
        if (!webExists) {
            zkClient.createPersistent(Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb);
        }
    }

    // ip+port
    public void createNode(String path){
        if (!zkClient.exists(path)) {
            zkClient.createEphemeral(path);
        }
    }
}
