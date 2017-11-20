package com.womai.wms.rf.manager.auth.testreceive;

import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.DefaultKey;
import com.womai.wms.rf.common.util.CollectionUtil;
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
@Component("receiveTestManager2")
public class ReceiveTestManager2Impl extends ReceiveManager {


    private final static String USERNAME = "userName";
    private final static String PASSWORD = "password";
    private final static String SEX = "sex";
    private final static String MOBILE = "mobile";
    private final static String ADDRESS = "address";
    private final static String POSTCODE = "postCode";


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        HandlerParamManager handlerParamManager = (HandlerParamManager) ctx.pipeline().first();
        System.out.println("pipeLine里边的数据=" + handlerParamManager.Tl_STRING_TEST.get());
        HandlerParamManager2 handlerParamManager2 = (HandlerParamManager2) ctx.pipeline().get("handlerParamManager2");
        System.out.println("pipeLine里边的数据22222=" + handlerParamManager2.Tl_STRING_TEST.get());

        String[] pageHeader = {"", Constants.LOGIN + "测试2", Constants.SPLIT, ""};
        super.initBaseMap(ReceiveTest.class, pageHeader, ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        List<String> list = CollectionUtil.newList("list1", "list2", "list3", "list4", "list5");
        List<String> list2 = CollectionUtil.newList("lista", "listb", "listc", "listd", "liste");
        Map<String, Object> accepterMap = getDataMap();
        Integer receiveResult = receiveDataAndNotPrintNext(ctx, object, accepterMap);
        ReceiveTest receiveTest = (ReceiveTest) accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (lastCompleteColName.equals(USERNAME)) {
            String userName = receiveTest.getUserName();
            if (userName.equals("a")) {
                super.colNeedReInput(lastCompleteColName, "名称不对", accepterMap, ctx);
            } else {
                rePrintCurColTip(accepterMap, ctx);
            }
        }

        if (lastCompleteColName.equals(PASSWORD)) {
            String password = receiveTest.getPassword();
            if (password.equals("a")) {
                super.colNeedReInput(lastCompleteColName, "密码不对", accepterMap, ctx);
            } else {
                rePrintCurColTip(accepterMap, ctx);
            }
        }

        if (lastCompleteColName.equals(SEX)) {
            String sex = receiveTest.getSex();
            if (sex.equals("a")) {
                super.colNeedReInput(lastCompleteColName, "性别不对", accepterMap, ctx);
            } else {
                rePrintCurColTip(accepterMap, ctx);
            }
        }

        if (lastCompleteColName.equals(MOBILE)) {
            String mobile = receiveTest.getMobile();
            if (mobile.equals("a")) {
                super.colNeedReInput(lastCompleteColName, "手机不对", accepterMap, ctx);
            } else {
//                rePrintCurColTip(accepterMap, ctx);
                resetCurCol(POSTCODE, accepterMap, ctx);
            }
        }

        if (lastCompleteColName.equals(ADDRESS)) {
            String address = receiveTest.getAddress();
            if (address.equals("a")) {
                super.colNeedReInput(lastCompleteColName, "地址不对", accepterMap, ctx);
            } else {
                rePrintCurColTip(accepterMap, ctx);
            }
        }

        if (lastCompleteColName.equals(POSTCODE)) {
            String postCode = receiveTest.getPostCode();
            if (postCode.equals("a")) {
                super.colNeedReInput(lastCompleteColName, "邮编不对", accepterMap, ctx);
            } else {

            }
        }

        if (receiveResult == RECEIVER_TYPE_FINISHED) {
            WMSDebugManager.debugLog("接收完成=" + accepterMap);
        }

    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

}
