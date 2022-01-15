package com.wp.llrp;


import com.wp.llrp.proxy.ProxyLLRPServer;
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
        logger.info("Starting...");

        ProxyLLRPServer proxyLLRPServer = new ProxyLLRPServer();
        proxyLLRPServer.start(55555);

    }

}
