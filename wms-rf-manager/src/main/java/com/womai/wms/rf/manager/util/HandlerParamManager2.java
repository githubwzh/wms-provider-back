package com.womai.wms.rf.manager.util;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 数据接收工具
 * User:zhangwei
 * Date: 2016-05-25
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@ChannelHandler.Sharable
@Component("handlerParamManager2")
public class HandlerParamManager2 extends ChannelHandlerAdapter {
    public ThreadLocal<String> Tl_STRING_TEST = new ThreadLocal<String>();


}
