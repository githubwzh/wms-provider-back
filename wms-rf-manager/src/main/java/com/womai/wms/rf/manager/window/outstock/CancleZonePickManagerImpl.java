package com.womai.wms.rf.manager.window.outstock;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.DefaultKey;
import com.womai.wms.rf.common.constants.ErrorConstants;
import com.womai.wms.rf.common.constants.TipConstants;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.Reflections;
import com.womai.wms.rf.domain.outstock.CancleZonePick;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.outstock.OutstockCancleZonePickRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.instock.InstockReason;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by keke on 17-5-3.
 */
@Scope("prototype")
@Component("cancleZonePickManager")
public class CancleZonePickManagerImpl extends ReceiveManager {
    private static final String APPLY_CANCLE = "applyCancle";//确认取消
    private static final String REASONCONTENT = "reasoncontent";//取消原因
    private static final String ANYKEY = "anykey";//请输入
    private ChannelHandlerContext ctx;
    private List<String> zoneworksheetnos;//按区拣货单号
    private String containerno;
    private List<String> reasonList;
    private String reasoncontent;
    private boolean isToMenu;//跳转到主菜单
    private static final String NEXT_LOCATION = "nextLocation";//跳转的位置
    private static final int LOCATION_TO_CHANNELACTIVE = 0;//跳转到初始界面
    @Autowired
    private OutstockCancleZonePickRemoteService outstockCancleZonePickRemoteService;
    private static final String[] pageHeader = {Constants.BREAK_LINE, TipConstants.OUTSTOCK_ZONE_CANCLE_PICK_TASK_BIND, Constants.SPLIT, ""};
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        //进入集周转箱页面
        super.initBaseMap(CancleZonePick.class, pageHeader, ctx);
        Map<String, Object> accepterMap = getDataMap();
        RemoteResult<Map<String, Object>> remoteResult = outstockCancleZonePickRemoteService.getCanaleZonePickList(getCredentialsVO(ctx));
        if(remoteResult !=null && remoteResult.isSuccess()){
            Map<String, Object> resultMap = (Map<String, Object>)remoteResult.getT();
            String errorMsg = (String) resultMap.get(WmsConstants.KEY_ERROR_MSG);
            if (StringUtils.isEmpty(errorMsg)) {
                zoneworksheetnos = (List<String>)resultMap.get("zoneworksheetnos");
                reasonList = (List<String>)resultMap.get("reasonList");
                containerno = (String)resultMap.get("containerno");
                HandlerUtil.clearAll(ctx.channel());
                HandlerUtil.writer(ctx, pageHeader, 1, 1);
                if(StringUtils.isNotEmpty(containerno)){
                    HandlerUtil.println(ctx, "有周转箱未完成拣货任务，不能取消绑定！");
                    printMsessageAndToMenu(ctx,false,"周转箱号："+containerno);
                }else{

                    HandlerUtil.println(ctx, "将要取消以下未完成分区拣货单绑定：");
                    for(String zoneworksheetno : zoneworksheetnos){
                        HandlerUtil.println(ctx, zoneworksheetno);
                    }
                    //下一个字段以list切换的形式显示
                    setReceivedToSelectReanson(accepterMap);
                    setNextColSwitchList(reasonList, accepterMap, ctx);

                }

            }else{
                HandlerUtil.clearAll(ctx.channel());
                HandlerUtil.writer(ctx, pageHeader, 1, 1);
                printMsessageAndToMenu(ctx,false,errorMsg);
            }

        }else{
            printMsessageAndToMenu(ctx,false,remoteResult.getResultCode());
        }
    }
    /**
     * 接收用户输入
     *
     * @param ctx handler对象
     * @param msg 每次接收到的字节数据
     * @throws Exception 抛出异常
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Map<String, Object> accepterMap = getDataMap();
        if (isToMenu) {//跳转到主菜单
            toMenuWindow(ctx);
            return;
        }
        receiveDataAndNotPrintNext(ctx, msg, accepterMap);
        CancleZonePick cancleZonePick = (CancleZonePick) accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (REASONCONTENT.equals(lastCompleteColName)) {
            reasoncontent = cancleZonePick.getReasoncontent();
            resetCurCol(APPLY_CANCLE, accepterMap, ctx);
        }
        if (APPLY_CANCLE.equals(lastCompleteColName)) {
            String applyCancle = cancleZonePick.getApplyCancle();
            if ("Y".equalsIgnoreCase(applyCancle)) {
                RemoteResult<Map<String, Object>> remoteResult = outstockCancleZonePickRemoteService.canaleZonePick(getCredentialsVO(ctx),zoneworksheetnos,reasoncontent);
                if(remoteResult !=null && remoteResult.isSuccess()){
                    Map<String, Object> resultMap = (Map<String, Object>)remoteResult.getT();
                    String errorMsg = (String) resultMap.get(WmsConstants.KEY_ERROR_MSG);
                    if (StringUtils.isEmpty(errorMsg)) {
                        printMsessageAndToMenu(ctx,true,Constants.BREAK_LINE +"已经取消拣货任务绑定!");
                    }else{
                        printMsessageAndToMenu(ctx,false,errorMsg);
                    }

                }else{
                    printMsessageAndToMenu(ctx,false,remoteResult.getResultCode());
                }

            } else {
                colNeedReInput(APPLY_CANCLE, "请输入Y", accepterMap, ctx);
            }
          

        }

    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
    /**
     * 跳转到选择原因
     *
     * @param accepterMap
     */
    private void setReceivedToSelectReanson(Map<String, Object> accepterMap) {
        String[] strings = {REASONCONTENT};//要跳转到的属性名放第一个
        setReceivedBase(accepterMap, 1, strings);//1表示orderCode已经接收完成
    }
    /**
     * 属性值接收跳转时，初始后面的属性
     *
     * @param accepterMap
     * @param size        该属性上面的属性个数，即已经完成接收的个数
     * @param strings     要清空的属性名字
     */
    private void setReceivedBase(Map<String, Object> accepterMap, int size, String[] strings) {
        accepterMap.put(DefaultKey.curColName.keyName, strings[0]);//跳转到，将要接收数据的属性名
        accepterMap.put(DefaultKey.lastCompleteColName.keyName, "");
        accepterMap.put(DefaultKey.completeSize.keyName, size);
        accepterMap.put(DefaultKey.autoPrintNextCol.keyName,false);
        for (String columnName : strings) {
            Reflections.invokeSetter(accepterMap.get(DefaultKey.objectClass.keyName), columnName, "");
        }
    }
    private void printMsessageAndToMenu(ChannelHandlerContext ctx,boolean success,String message){
        HandlerUtil.println(ctx, message);
        HandlerUtil.println(ctx, "按任意键返回主菜单！");
        if(!success){
            HandlerUtil.errorBeep(ctx);
        }
        isToMenu = true;
    }
}
