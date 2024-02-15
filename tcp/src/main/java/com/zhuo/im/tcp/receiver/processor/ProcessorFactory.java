package com.zhuo.im.tcp.receiver.processor;

/**
 * @description:
 * @version: 1.0
 */
public class ProcessorFactory {

    private static BaseProcessor defaultProcess;

    static {
        defaultProcess = new BaseProcessor() {
            @Override
            public void processBefore() {

            }

            @Override
            public void processAfter() {

            }
        };
    }

    public static BaseProcessor getMessageProcessor(Integer command) {
        return defaultProcess;
    }

}
