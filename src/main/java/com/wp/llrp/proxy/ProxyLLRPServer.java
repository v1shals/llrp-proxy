package com.wp.llrp.proxy;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.llrp.ltk.net.LLRPBinaryDecoder;
import org.llrp.ltk.net.LLRPBinaryEncoder;
import org.llrp.ltk.net.LLRPConnectionAttemptFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ProxyLLRPServer {

    private static final Logger logger = LoggerFactory.getLogger(ProxyLLRPServer.class);

    public void start(Integer port) throws Exception {
        logger.info("Initializing reader...");

        IoAcceptor acceptor = new NioSocketAcceptor();
       // acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new LLRPBinaryEncoder(), new LLRPBinaryDecoder()));
        ProxyIoHandler proxyIoHandler = new ProxyIoHandler();
        proxyIoHandler.init();
        acceptor.setHandler(proxyIoHandler);
        acceptor.getSessionConfig().setReadBufferSize(204800);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
        acceptor.bind(new InetSocketAddress(port));

    }
}
