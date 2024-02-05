package com.zhuo.im.tcp;

import com.zhuo.im.codec.config.BootstrapConfig;
import com.zhuo.im.tcp.redis.RedisManager;
import com.zhuo.im.tcp.server.ImServer;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;

public class Starter {

    public static void main(String[] args) {
//        new ImServer(9000);

        if(args.length > 0){ // NOTE: IDEA at the top right => Select Run/Debug Configuration => Edit Configurations => Program parameters
            start(args[0]);
        }


    }

    private static void start(String path){
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(path);
            BootstrapConfig bootstrapConfig = yaml.loadAs(inputStream, BootstrapConfig.class);

            new ImServer(bootstrapConfig.getTcpConfig()).start();

            RedisManager.init(bootstrapConfig);

        } catch (Exception e){
            e.printStackTrace();
            System.exit(500);
        }
    }

}
