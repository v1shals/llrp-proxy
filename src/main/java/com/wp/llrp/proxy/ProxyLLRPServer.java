package com.wp.llrp.proxy;

import com.wp.llrp.reader.LLRPReader;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.llrp.ltk.net.LLRPBinaryDecoder;
import org.llrp.ltk.net.LLRPBinaryEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ProxyLLRPServer {

    private static final Logger logger = LoggerFactory.getLogger(ProxyLLRPServer.class);

    public void start(Integer port) throws IOException {
        logger.info("Initializing reader...");

        IoAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new LLRPBinaryEncoder(), new LLRPBinaryDecoder()));
        acceptor.setHandler(new ProxyIoHandler());
        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 20);
        acceptor.bind(new InetSocketAddress(port));
    }
}
