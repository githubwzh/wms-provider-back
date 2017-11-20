package com.womai.wms.rf.manager.auth.testreceive;

import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.DefaultKey;
import com.womai.wms.rf.common.util.CollectionUtil;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.WMSDebugManager;
import com.womai.wms.rf.domain.testreceive.ReceiveTest;
import com.womai.wms.rf.manager.util.HandlerParamManager;
import com.womai.wms.rf.manager.util.HandlerParamManager2;
import com.womai.wms.rf.manager.util.ReceiveManager;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 用户登录处理
 * User: zhangwei
 * Date: 2016-04-26
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component("receiveTestManager")
public class ReceiveTestManagerImpl extends ReceiveManager {


    private final static String USERNAME = "userName";
    private final static String PASSWORD = "password";
    private final static String SEX = "sex";
    private final static String MOBILE = "mobile";
    private final static String ADDRESS = "address";
    private final static String POSTCODE = "postCode";
    private final static String GO_TO_FLAG = "inStock_goto";//跳转标志
    private final static Integer TO_CHANNEL_ACTIVE = -1;//跳转标志

    private ThreadLocal<List<String>> TL_ASNCodeList = new ThreadLocal<List<String>>();//按照网络订单号查询到的多个ASN单号
    String[] pageHeader = {"", "接收测试界面", Constants.SPLIT, ""};

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("进入到测试Handler");
        TL_ASNCodeList.set(CollectionUtil.newList("list1", "list2", "list3", "list4", "list5"));
        super.initBaseMap(ReceiveTest.class, pageHeader, ctx);
//        Map<String, Object> accepterMap = mapThread.get();
//        resetCurCol(PASSWORD, accepterMap, ctx);
    }


    public static String getByteString(String mess){
        byte[] bytes = mess.getBytes();
        StringBuilder sb = new StringBuilder();
        for(byte b:bytes){
            sb.append(b).append(",");
        }
        return sb.toString();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        System.out.println("read到的数据:"+object.toString()+"字节码:"+getByteString(object.toString()));
//        List<String> list = CollectionUtil.newList("list1","list2","list3","list4","list5");
        Map<String, Object> accepterMap = getDataMap();

        Integer receiveResult = receiveDataAndNotPrintNext(ctx, object, accepterMap);
        ReceiveTest receiveTest = (ReceiveTest) accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (lastCompleteColName.equals(USERNAME)) {
            String userName = receiveTest.getUserName();
            //设置输出红色字体
            HandlerUtil.setFontRed(ctx);
            HandlerUtil.writeAndFlush(ctx, "红色的字");
            HandlerUtil.removeCustomStyle(ctx);
            HandlerUtil.writeAndFlush(ctx, "不是红色的了");
            //响铃两次
            HandlerUtil.errorBeep(ctx);

            resetCurCol(PASSWORD, accepterMap, ctx);
        }
        if (lastCompleteColName.equals(PASSWORD)) {
            setColSwitchList(SEX, CollectionUtil.newList("男","女"), accepterMap, ctx);
        }
        if (lastCompleteColName.equals(SEX)) {

            resetCurCol(MOBILE, accepterMap, ctx);

        }
        if (lastCompleteColName.equals(MOBILE)) {
            resetCurCol(ADDRESS, accepterMap, ctx);
        }
        if (lastCompleteColName.equals(ADDRESS)) {
            resetCurCol(POSTCODE, accepterMap, ctx);
        }
        if (lastCompleteColName.equals(POSTCODE)) {
        }
        if (receiveResult == RECEIVER_TYPE_FINISHED) {
            System.out.println("完整接收:"+receiveTest);
        }
        WMSDebugManager.debugLog("每个接收:" + accepterMap);

    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

}
