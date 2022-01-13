package com.wp.llrp;


import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.llrp.ltk.net.LLRPBinaryDecoder;
import org.llrp.ltk.net.LLRPBinaryEncoder;
import org.llrp.ltk.net.LLRPIoHandlerAdapterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        logger.info("Initializing reader...");

        IoAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter( new LLRPBinaryEncoder(), new LLRPBinaryDecoder()));
        acceptor.setHandler(new LLRPIoHandlerAdapterImpl());
        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
      //  acceptor.bind(new InetSocketAddress(PORT));
    }

}
