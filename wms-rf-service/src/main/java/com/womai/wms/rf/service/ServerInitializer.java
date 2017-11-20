package com.womai.wms.rf.service;

import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.util.SpringContextHolder;
import com.womai.wms.rf.manager.auth.login.LoginShellManagerImpl;
import com.womai.wms.rf.manager.auth.testreceive.ReceiveTestManagerImpl;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

//import com.womai.wms.rf.manager.auth.testreceive.ReceiveTestManagerImpl;

/**
 * 服务初始化类，初始化pipeline
 * User: zhangwei
 * Date: 2016-04-26
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Service
public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired
    private LoginShellManagerImpl loginShellManager;


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
//        字符串解码 和 编码
//        pipeline.addLast("LineBasedFrameDecoder",new LineBasedFrameDecoder(1024));
//        pipeline.addLast("decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        //定长解码，长度为1，每次输入都接收，相当于不限制长度，但是此编码器可以解决Tcp粘包问题
        pipeline.addLast("fixedLengthFrameDecoder", new FixedLengthFrameDecoder(1));
        pipeline.addLast(Constants.DECODE_HANDLER, new StringDecoder());
        pipeline.addLast(Constants.ENCODE_HANDLER, new ByteArrayEncoder());

        // 业务逻辑Handler入口
        pipeline.addLast("handler", SpringContextHolder.getBean(LoginShellManagerImpl.class));
//        pipeline.addLast("handler", new ReceiveTestManagerImpl());

    }


}
