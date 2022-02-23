package com.wp.llrp.proxy;

import org.apache.mina.core.session.IoSession;
import org.llrp.ltk.generated.messages.KEEPALIVE;
import org.llrp.ltk.generated.messages.KEEPALIVE_ACK;
import org.llrp.ltk.generated.messages.READER_EVENT_NOTIFICATION;
import org.llrp.ltk.types.LLRPMessage;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProxySocketWriter implements Runnable {

    private ArrayBlockingQueue<LLRPMessage> messageQueue;
    private IoSession ioSession;

    public ProxySocketWriter(ArrayBlockingQueue<LLRPMessage> messageQueue, IoSession ioSession) {
        this.messageQueue = messageQueue;
        this.ioSession = ioSession;
    }

    @Override
    public void run() {

        while (true) {
            LLRPMessage message;
            message = messageQueue.poll();
            if (message != null) {
                ioSession.write(message);
            }
        }
    }
}
