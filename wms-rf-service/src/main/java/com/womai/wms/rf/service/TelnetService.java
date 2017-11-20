package com.womai.wms.rf.service;

import com.womai.wms.rf.common.util.ManagerLog;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;

/**
 * Created by Jiazy on 14-10-20.
 */
@Scope("prototype")
@Service
public class TelnetService {

    @Autowired
    private ServerInitializer serverInitializer;

    public static final Integer SERVER_PORT = 3000;//netty服务的端口号

    /**
     * 初始化netty的boot服务
     */
    public void init() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup())
                    .channel(NioServerSocketChannel.class)
                    .childHandler(serverInitializer)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.bind(new InetSocketAddress(SERVER_PORT)).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            ManagerLog.errorLog("TelnetService start error........",e);
        } finally {

        }
    }

}
