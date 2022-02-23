package com.wp.llrp.proxy;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.FilterEvent;
import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import org.llrp.ltk.generated.messages.KEEPALIVE;
import org.llrp.ltk.generated.messages.KEEPALIVE_ACK;
import org.llrp.ltk.generated.messages.READER_EVENT_NOTIFICATION;
import org.llrp.ltk.generated.messages.RO_ACCESS_REPORT;
import org.llrp.ltk.net.LLRPConnectionAttemptFailedException;
import org.llrp.ltk.net.LLRPConnector;
import org.llrp.ltk.net.LLRPEndpoint;
import org.llrp.ltk.types.LLRPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProxyIoHandler implements IoHandler, LLRPEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(ProxyIoHandler.class);

    private IoSession proxySession;

    private LLRPConnector llrpConnector;

    private READER_EVENT_NOTIFICATION reader_event_notification;
    private ArrayBlockingQueue<LLRPMessage> queue;

    public ProxyIoHandler() throws LLRPConnectionAttemptFailedException {

        queue = new ArrayBlockingQueue<LLRPMessage>(5000);

    }

    public void init() throws Exception {
        llrpConnector = new LLRPConnector(this, "localhost", 5084);
        //llrpConnector = new LLRPConnector(this, "192.168.0.111", 5084);
        llrpConnector.connect(30000);
        logger.info("******* ready to accept connections.");
    }

    @Override
    public void sessionCreated(IoSession ioSession) {
          logger.info("Created New Session at {}", ioSession.getCreationTime());

        ProxySocketWriter proxySocketWriter = new ProxySocketWriter(queue, ioSession);
        Thread thread = new Thread(proxySocketWriter);
        thread.start();


          logger.info("***** Created New Session at {}", ioSession.getCreationTime());

    }

    @Override
    public void sessionOpened(IoSession ioSession) throws Exception {
         logger.info("***********************session opening message :{}", ioSession);
        queue.add(reader_event_notification);
    }

    @Override
    public void sessionClosed(IoSession ioSession) throws Exception {
         logger.info("session closing message :{}", ioSession);
    }

    @Override
    public void sessionIdle(IoSession ioSession, IdleStatus idleStatus) throws Exception {

       // ioSession.write(new KEEPALIVE());
          logger.info("session idle message :{}", ioSession.getIdleCount(IdleStatus.BOTH_IDLE));
    }

    @Override
    public void exceptionCaught(IoSession ioSession, Throwable throwable) throws Exception {
        logger.error("Error", throwable);
    }

    @Override
    public void messageReceived(IoSession actualIoSession, Object message) throws Exception {
        LLRPMessage llrpMessage = (LLRPMessage) message;
        //  logger.info("Message received from proxy connection : {} ", llrpMessage.toXMLString());
        llrpConnector.send((LLRPMessage) message);
    }

    @Override
    public void messageReceived(LLRPMessage message) {

        if (message instanceof READER_EVENT_NOTIFICATION) {
            reader_event_notification = (READER_EVENT_NOTIFICATION) message;
            logger.info("Message READER_EVENT_NOTIFICATION");
        } else if (message instanceof RO_ACCESS_REPORT) {
            logger.info("Message RO_ACCESS_REPORT");
        }else{
            logger.info("Message OTHER");
            queue.add(message);
        }
    }

    @Override
    public void messageSent(IoSession ioSession, Object o) throws Exception {
        logger.info("** messageSent");

        //  logger.info("message sent :{}", o.toString());
    }

    @Override
    public void inputClosed(IoSession ioSession) throws Exception {
            logger.info("session input closed  :{}", ioSession.getId());
    }

    @Override
    public void event(IoSession ioSession, FilterEvent filterEvent) throws Exception {
        logger.info("session event :{}", filterEvent.toString());
    }

    @Override
    public void errorOccured(String message) {
        logger.error("errorOccured :{}", message);
    }
}
