package com.womai.wms.rf.manager.window.outstock;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.DefaultKey;
import com.womai.wms.rf.common.constants.ErrorConstants;
import com.womai.wms.rf.common.constants.TipConstants;
import com.womai.wms.rf.common.util.*;
import com.womai.wms.rf.domain.outstock.OutstockZonePickOrder;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.outstock.OutstockZonepickOrderRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockWarehouseGood;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockZoneworkorder;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * ClassDescribe:四期拣货
 * Author :wangzhanhua
 * Date: 2017-04-21
 * Since
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component("outstockZonePickManager")
public class OutstockZonePickManagerImpl extends ReceiveManager {
    private static final String APPLY_PICK_ORDER = "applyPickOrder";//确认申请拣货
    private static final String CONTAINER_NO = "containerno";//周转箱号
    private static final String WAREHOUSE_CODE = "warehouseCode";//库位编码
    private static final String BARCODE = "barcode";//商品条码
    private static final String PICK_BU = "pickBu";//拣货数量bu

    private static final String NEXT_LOCATION = "nextLocation";//跳转的位置
    private static final int LOCATION_TO_CHANNELACTIVE = 0;//跳转到初始界面
    private static final int LOCATION_TO_NEXT_WAREHOUSE = 1;//跳转到下个库位
    private static final int SKU_NAME_LIMIT_LINE_NUM = 2;//商品名称限制行数

    private ChannelHandlerContext ctx;
    private List<OutstockWarehouseGood> outstockWarehouseGoods;//拣货信息
    private OutstockZoneworkorder outstockZoneworkorder;//拣货子单
    private int index = 0;//当前拣货信息集合中的下标
    private static final String[] pageHeader = {Constants.BREAK_LINE, TipConstants.OUTSTOCK_ZONE_PICK_ORDER_MANAGER, Constants.SPLIT, ""};

    @Autowired
    private OutstockZonepickOrderRemoteService outstockZonepickOrderRemoteService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        // 拣货界面
        super.initBaseMap(OutstockZonePickOrder.class, pageHeader, ctx);
        RemoteResult<Map<String, Object>> remoteResult = outstockZonepickOrderRemoteService.queryOutstockZonepicksStatusPicking(getCredentialsVO(ctx));
        if (null == remoteResult || !remoteResult.isSuccess()) {//数据异常，需要人为介入
            HandlerUtil.clearAll(ctx.channel());
            forward(Constants.MENU_SHELL_MANAGER, ctx);
            HandlerUtil.errorBeep(ctx);
            HandlerUtil.write(ctx, "四期拣货，查询已经申请的拣货任务异常");
        } else {
            Map<String, Object> map = remoteResult.getT();
            //map 如果为null 提示申请任务，否则，继续拣货
            if (map == null) {
                resetCurCol(APPLY_PICK_ORDER, getDataMap(), ctx);
            } else {
                this.outstockWarehouseGoods = (List<OutstockWarehouseGood>) map.get(WmsConstants.OUTSTOCK_ZONE_WAREHOUSE_GOOD);
                this.outstockZoneworkorder = (OutstockZoneworkorder) map.get(WmsConstants.OUTSTOCK_ZONE_WORKORDER);
                index = 0;//初始为第一个
                printPickOrderMsg(getCurrOutstockWarehouseGood(), getDataMap());
                //查询该拣货子单正使用的周转箱
                RemoteResult<String> result = outstockZonepickOrderRemoteService.queryBaseContainerByWorksheetchildid(getCredentialsVO(ctx), this.outstockZoneworkorder.getId());
                HandlerUtil.moveUpN(ctx, 1);
                if (result.isSuccess()) {//有正在使用的周转箱
                    printBeforeNextField("周转箱号:" + result.getT(), getDataMap(), ctx);
                    OutstockZonePickOrder outstockZonePickOrder = (OutstockZonePickOrder) getDataMap().get(DefaultKey.objectClass.keyName);
                    outstockZonePickOrder.setContainerno(result.getT());
                    resetCurCol(WAREHOUSE_CODE, getDataMap(), ctx);
                } else {//扫描周转箱
                    resetCurCol(CONTAINER_NO, getDataMap(), ctx);
                    HandlerUtil.removeRightDown(ctx);//删除下面的提示
                }
            }
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
        Integer nextLocation = (Integer) accepterMap.get(NEXT_LOCATION);
        if (nextLocation == null) {//当有跳转标识时，不接收数据，任意键跳转业务流程
            receiveDataAndNotPrintNext(ctx, msg, accepterMap);
        } else if (nextLocation == LOCATION_TO_CHANNELACTIVE) {
            accepterMap.remove(NEXT_LOCATION);
            channelActive(ctx);
            return;
        } else if (nextLocation == LOCATION_TO_NEXT_WAREHOUSE) {
            accepterMap.remove(NEXT_LOCATION);
            HandlerUtil.clearAll(ctx.channel());
            HandlerUtil.writer(ctx, pageHeader, 1, 1);
            HandlerUtil.moveUpN(ctx, 1);
            printPickOrderMsg(getCurrOutstockWarehouseGood(), accepterMap);//拣货信息
            OutstockZonePickOrder outstockZonePickOrder = getCurrOutstockZonePickOrder();
            if(outstockZonePickOrder.getContainerno()==null){
                resetCurCol(CONTAINER_NO,accepterMap,ctx);
            }else{
                HandlerUtil.moveUpN(ctx,1);
                printBeforeNextField("周转箱号:" + outstockZonePickOrder.getContainerno(), accepterMap, ctx);
                resetCurCol(WAREHOUSE_CODE, accepterMap, ctx);
            }
            clearValuesAfterSuccessPick(outstockZonePickOrder);
            return;
        }
        OutstockZonePickOrder outstockZonePickOrder = (OutstockZonePickOrder) getDataMap().get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (APPLY_PICK_ORDER.equals(lastCompleteColName)) {
            String applyPickOrder = outstockZonePickOrder.getApplyPickOrder();
            if ("1".equalsIgnoreCase(applyPickOrder)) {
                //清除“确认申请拣货（请输入Y）:y”这行的显示
                HandlerUtil.delLeft(ctx);
                HandlerUtil.moveUpN(ctx, 1);
                applyOutstockZonePicks(accepterMap);
            } else {
                colNeedReInput(APPLY_PICK_ORDER, "请输入1", accepterMap, ctx);
            }
        }
        if (CONTAINER_NO.equals(lastCompleteColName)) {
            RemoteResult<String> result = outstockZonepickOrderRemoteService.scanContainer(getCredentialsVO(ctx), outstockZonePickOrder.getContainerno(), this.outstockZoneworkorder);
            if (result.isSuccess()) {
                resetCurCol(WAREHOUSE_CODE, accepterMap, ctx);
            } else {
                colNeedReInput(CONTAINER_NO, result.getResultCode(), accepterMap, ctx);
            }
        }
        if (WAREHOUSE_CODE.equals(lastCompleteColName)) {
            String scanWhsCode = outstockZonePickOrder.getWarehouseCode();
            if ("1".equals(scanWhsCode)) {
                //如果扫描库位的位置输入“1”，则进行换箱操作
                onlyChangeContainer(WAREHOUSE_CODE, accepterMap, ctx);
                return;
            }

            OutstockWarehouseGood currOutstockWarehouseGood = getCurrOutstockWarehouseGood();
            String screenWhsCode = currOutstockWarehouseGood.getWarehouseCode();
            if (!scanWhsCode.equals(screenWhsCode)) {
                colNeedReInput(WAREHOUSE_CODE, ErrorConstants.WAREHOUSE_CODE_ERROR, accepterMap, ctx);
            } else {
            printBeforeNextField("无此商品--000", accepterMap, ctx);
            resetCurCol(BARCODE, accepterMap, ctx);
            }
        }

        if (BARCODE.equals(lastCompleteColName)) {
            String scanBarcode = outstockZonePickOrder.getBarcode();
            if ("000".equals(scanBarcode)) {
                //此库位，该商品数量bu0，重新分配
                lackGoods(scanBarcode, outstockZonePickOrder, accepterMap);
                return;
            }
            OutstockWarehouseGood outstockWarehouseGood = getCurrOutstockWarehouseGood();
            String screenBarcode = outstockWarehouseGood.getBarcode();
            String screenpkBarcodeId = outstockWarehouseGood.getPkBarcodeId();
            if (!scanBarcode.equals(screenBarcode) && !scanBarcode.equals(screenpkBarcodeId)) {//不是商品条码，也不是外包装码
                colNeedReInput(BARCODE, ErrorConstants.BARCODE_OR_PKBARCODE_ERROR, accepterMap, ctx);
            } else {

            OutstockWarehouseGood currOutstockWarehouseGood = getCurrOutstockWarehouseGood();
            int hopefulPickNum = currOutstockWarehouseGood.getHopefulPickbu();
            if (hopefulPickNum == 1) {
                //数量为1自动确认
                confirmZonePick(1, accepterMap);
                return;
            } else {
                printBeforeNextField("缺货--00*;周转箱满--0*", accepterMap, ctx);
                resetCurCol(PICK_BU, accepterMap, ctx);
            }
            }
        }
        if (PICK_BU.equals(lastCompleteColName)) {
            String inputPickNumStr = outstockZonePickOrder.getPickBu();//PDA录入的拣货数量BU
            if (inputPickNumStr.matches(TipConstants.REG_ZONE_PICKBU_CHANGE_CONTAINER)) {//周转箱满“0*”
                if ("00".equals(inputPickNumStr)) {//相当于库位的位置，输入“1”
                    onlyChangeContainer(PICK_BU, accepterMap, ctx);
                    return;
                } else {
                    OutstockWarehouseGood currOutstockWarehouseGood = getCurrOutstockWarehouseGood();
                    inputPickNumStr = inputPickNumStr.substring(1);
                    int inputPickNum = Integer.parseInt(inputPickNumStr);


                    if (inputPickNum < currOutstockWarehouseGood.getHopefulPickbu()) {
                        //校验是否箱规的整数倍
                        if(WmsConstants.PACK_LEVEL_TWO.equals(currOutstockWarehouseGood.getPacklevel())){
                            if (inputPickNum % currOutstockWarehouseGood.getPknum() != 0) {
                                colNeedReInput(PICK_BU, ErrorConstants.PLS_INPUT_RIGHT_NUM, accepterMap, ctx);
                                return;
                            }
                        }
                        currOutstockWarehouseGood.setRealPickbu(inputPickNum);
                        currOutstockWarehouseGood.setWorkSheetId(this.outstockZoneworkorder.getWorksheetid());
                        RemoteResult<String> resultConfirm = outstockZonepickOrderRemoteService.confirmZonePickAndChangeContainer(getCredentialsVO(ctx), outstockZonePickOrder.getContainerno(), currOutstockWarehouseGood);
                        if (resultConfirm.isSuccess()) {
                            freshNextWarehouseGood(this.outstockZoneworkorder.getId(), accepterMap);
                            HandlerUtil.moveUpN(ctx,1);
                            resetCurCol(CONTAINER_NO, accepterMap, ctx);
                            Reflections.invokeSetter(outstockZonePickOrder, CONTAINER_NO, "");
                            clearValuesAfterSuccessPick(outstockZonePickOrder);
                            //如果是最后一组处理成功后，跳转到初始界面
                        } else {
                            //业务异常回到初始页面，扫描拣货单号
                            friendlyMsgAnyKeyContinue(getErrorMsgFromRemoteResult(resultConfirm));
                            accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);
                        }
                    } else {
                        colNeedReInput(PICK_BU, ErrorConstants.OUTSTOCK_PICK_ORDER_NUM_ERROR, accepterMap, ctx);
                    }
                }
            } else if (inputPickNumStr.matches(TipConstants.REG_ZONE_PICKBU_NOT_ENOUGH)) {//缺货“00*”
                lackGoods(inputPickNumStr, outstockZonePickOrder, accepterMap);
            } else if (inputPickNumStr.matches(TipConstants.REG_PKNUM)) {//输入的数字为大于1的正整数
                OutstockWarehouseGood currOutstockWarehouseGood = getCurrOutstockWarehouseGood();
                int inputPickNum = Integer.parseInt(inputPickNumStr);
                int hopefulPickNum = currOutstockWarehouseGood.getHopefulPickbu();
                if (inputPickNum != hopefulPickNum) {
                    colNeedReInput(PICK_BU, "拣货数量bu必须和推荐数量BU一致", accepterMap, ctx);
                } else {
                    currOutstockWarehouseGood.setRealPickbu(inputPickNum);
                    confirmZonePick(inputPickNum, accepterMap);
                }
            } else {
                colNeedReInput(PICK_BU, ErrorConstants.OUTSTOCK_PICK_ORDER_NUM_ERROR, accepterMap, ctx);
            }
        }
    }

    private OutstockZonePickOrder getCurrOutstockZonePickOrder() {
       return  (OutstockZonePickOrder) getDataMap().get(DefaultKey.objectClass.keyName);
    }

    private void lackGoods(String inputPickNumStr, OutstockZonePickOrder outstockZonePickOrder, Map<String, Object> accepterMap) {
        OutstockWarehouseGood currOutstockWarehouseGood = getCurrOutstockWarehouseGood();
        inputPickNumStr = inputPickNumStr.substring(2);
        int inputPickNum = Integer.parseInt(inputPickNumStr);
        //校验是否箱规的整数倍
        if(WmsConstants.PACK_LEVEL_TWO.equals(currOutstockWarehouseGood.getPacklevel())){
            if (inputPickNum % currOutstockWarehouseGood.getPknum() != 0) {
                colNeedReInput(PICK_BU, ErrorConstants.PLS_INPUT_RIGHT_NUM, accepterMap, ctx);
                return;
            }
        }

        if (inputPickNum < currOutstockWarehouseGood.getHopefulPickbu()) {
            currOutstockWarehouseGood.setRealPickbu(inputPickNum);
            if (this.outstockZoneworkorder.getState() == WmsConstants.OUTSTOCK_WORKORDER) {
                currOutstockWarehouseGood.setPossibleFirScan(true);
            }
            currOutstockWarehouseGood.setWorkSheetId(this.outstockZoneworkorder.getWorksheetid());
            RemoteResult<String> resultConfirm = outstockZonepickOrderRemoteService.confirmZonePickAndAllocation(getCredentialsVO(ctx), outstockZonePickOrder.getContainerno(), currOutstockWarehouseGood);
            if (resultConfirm.isSuccess()) {
                //如果是最后一组处理成功后，跳转到初始界面
                if (index == outstockWarehouseGoods.size() - 1) {
                    //任意键后跳转到初始界面
                    accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);//跳转初始界面
                }else{
                    //跳转到显示下个库位
                    index++;//下个拣货信息
                    accepterMap.put(NEXT_LOCATION, LOCATION_TO_NEXT_WAREHOUSE);//跳转初始界面
                }
                String tip = StringUtils.isEmpty(resultConfirm.getResultCode())?TipConstants.OUTSTOCK_INFO_PICK_FINISHED:resultConfirm.getResultCode();
                friendlyMsgSucceedAnyKeyContinue(tip);
            } else {
                //业务异常回到初始页面，扫描拣货单号
                friendlyMsgAnyKeyContinue(getErrorMsgFromRemoteResult(resultConfirm));
                accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);
            }
        } else {
            colNeedReInput(PICK_BU, ErrorConstants.OUTSTOCK_PICK_ORDER_NUM_ERROR, accepterMap, ctx);
        }

    }

    private void clearValuesAfterSuccessPick(OutstockZonePickOrder outstockZonePickOrder) {
        if (StringUtils.isNotEmpty(outstockZonePickOrder.getWarehouseCode())) {
            Reflections.invokeSetter(outstockZonePickOrder, WAREHOUSE_CODE, "");//清空库位接收的数据
        }
        if (StringUtils.isNotEmpty(outstockZonePickOrder.getBarcode())) {
            Reflections.invokeSetter(outstockZonePickOrder, BARCODE, "");//清空商品条码接收的数据
        }
        if (StringUtils.isNotEmpty(outstockZonePickOrder.getPickBu())) {
            Reflections.invokeSetter(outstockZonePickOrder, PICK_BU, "");//清空数量bu接收的数据
        }
    }

    /**
     * 重新查询拣货明细
     *
     * @param id          拣货子单
     * @param accepterMap
     */
    private void freshNextWarehouseGood(Long id, Map<String, Object> accepterMap) {
        RemoteResult<List<OutstockWarehouseGood>> listRemoteResult = outstockZonepickOrderRemoteService.queryOutstockWarehouseGoods(getCredentialsVO(ctx), id);
        if (listRemoteResult.isSuccess()) {
            this.outstockWarehouseGoods = listRemoteResult.getT();
        } else {
            friendlyMsgAnyKeyContinue(getErrorMsgFromRemoteResult(listRemoteResult));
            accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);//区分不同的异常
        }
        this.index = 0;//下个拣货信息为重新查找拣货信息的第一个
        clearScreanAndPrintheader(ctx);
        printPickOrderMsg(getCurrOutstockWarehouseGood(), accepterMap);//拣货信息
    }

    /**
     * 换周转箱
     *
     * @param accepterMap
     * @param ctx
     */
    private void onlyChangeContainer(String colName, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        OutstockZonePickOrder outstockZonePickOrder = (OutstockZonePickOrder) getDataMap().get(DefaultKey.objectClass.keyName);
        RemoteResult<String> result = outstockZonepickOrderRemoteService.onlyChangeContainer(getCredentialsVO(ctx), outstockZonePickOrder.getContainerno(), this.outstockZoneworkorder);
        if (result != null && result.isSuccess()) {
            clearScreanAndPrintheader(ctx);
            //跳转到扫描周转箱
            printPickOrderMsg(getCurrOutstockWarehouseGood(), getDataMap());
            HandlerUtil.moveUpN(ctx, 1);//去掉空行
            clearValuesAfterSuccessPick(outstockZonePickOrder);
            Reflections.invokeSetter(outstockZonePickOrder, CONTAINER_NO, "");//清空库位接收的数据
            resetCurCol(CONTAINER_NO, accepterMap, ctx);
        } else {
            colNeedReInput(colName, result.getResultCode(), accepterMap, ctx);
        }
    }

    private void clearScreanAndPrintheader(ChannelHandlerContext ctx) {
        HandlerUtil.clearAll(ctx.channel());
        HandlerUtil.writer(ctx, pageHeader, 1, 1);
        HandlerUtil.moveUpN(ctx, 1);//去掉空行
    }

    private void confirmZonePick(int inputPickNum, Map<String, Object> accepterMap) {
        //设置实际拣货数量bu
        OutstockWarehouseGood outstockWarehouseGood = getCurrOutstockWarehouseGood();
        outstockWarehouseGood.setRealPickbu(inputPickNum);
        // 拣货确认
        outstockWarehouseGood.setWorkSheetId(this.outstockZoneworkorder.getWorksheetid());//拣货主单id
        OutstockZonePickOrder outstockZonePickOrder = (OutstockZonePickOrder) getDataMap().get(DefaultKey.objectClass.keyName);
        if (this.outstockZoneworkorder.getState() == WmsConstants.OUTSTOCK_WORKORDER) {
            outstockWarehouseGood.setPossibleFirScan(true);
        }
        outstockWarehouseGood.setWorkSheetId(this.outstockZoneworkorder.getWorksheetid());
        RemoteResult<String> resultConfirm = outstockZonepickOrderRemoteService.confirmZonePick(getCredentialsVO(ctx), outstockZonePickOrder.getContainerno(), outstockWarehouseGood);
        if (resultConfirm.isSuccess()) {
            //如果是最后一组处理成功后，跳转到初始界面
            if (index == outstockWarehouseGoods.size() - 1) {
                //任意键后跳转到初始界面
                accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);//跳转初始界面
                HandlerUtil.changeRow(ctx);
                friendlyMsgSucceedAnyKeyContinue(TipConstants.OUTSTOCK_INFO_PICK_FINISHED);
                return;
            }
            index++;//下个拣货信息
            clearScreanAndPrintheader(ctx);
            printPickOrderMsg(getCurrOutstockWarehouseGood(), accepterMap);//拣货信息
            if(outstockZonePickOrder.getContainerno()==null){
                resetCurCol(CONTAINER_NO,accepterMap,ctx);
            }else{
                HandlerUtil.moveUpN(ctx, 1);
                printBeforeNextField("周转箱号:"+outstockZonePickOrder.getContainerno(),accepterMap,ctx);
                resetCurCol(WAREHOUSE_CODE,accepterMap,ctx);
            }
            clearValuesAfterSuccessPick(outstockZonePickOrder);
        } else {
            //业务异常回到初始页面，扫描拣货单号
            friendlyMsgAnyKeyContinue(getErrorMsgFromRemoteResult(resultConfirm));
            accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);
        }
    }

    /**
     * 申请拣货任务
     *
     * @param accepterMap
     */
    private void applyOutstockZonePicks(Map<String, Object> accepterMap) {
        RemoteResult<Map<String, Object>> remoteResult = outstockZonepickOrderRemoteService.applyOutstockZonepicks(getCredentialsVO(ctx));
        if (remoteResult.isSuccess()) {
            Map<String, Object> map = remoteResult.getT();
            this.outstockWarehouseGoods = (List<OutstockWarehouseGood>) map.get(WmsConstants.OUTSTOCK_ZONE_WAREHOUSE_GOOD);
            index = 0;//初始为第一个
            this.outstockZoneworkorder = (OutstockZoneworkorder) map.get(WmsConstants.OUTSTOCK_ZONE_WORKORDER);
            printPickOrderMsg(getCurrOutstockWarehouseGood(), accepterMap);
            HandlerUtil.moveUpN(ctx, 1);//去掉空行
            resetCurCol(CONTAINER_NO, getDataMap(), ctx);
        } else {
            HandlerUtil.moveDownN(ctx, 1);
            colNeedReInput(APPLY_PICK_ORDER, getErrorMsgFromRemoteResult(remoteResult), accepterMap, ctx);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 获得当前展示的拣货信息
     *
     * @return
     */
    private OutstockWarehouseGood getCurrOutstockWarehouseGood() {
        return this.outstockWarehouseGoods.get(this.index);
    }

    /**
     * 打印拣货信息
     *
     * @param accepterMap           参数容器
     * @param outstockWarehouseGood 拣货信息
     */
    private void printPickOrderMsg(OutstockWarehouseGood outstockWarehouseGood, Map<String, Object> accepterMap) {
        String dateName = Constants.batchRuleEnum.getDateTypeByCode(outstockWarehouseGood.getBatchrule());
        String sheettypeName;
        if (WmsConstants.OUTSTOCK_ZONE_WORKORDER_WAVE_AD.equals(this.outstockZoneworkorder.getWorksheettype())) {
            sheettypeName = "集货";
        } else {
            sheettypeName = "非集货";
        }
        String barcode = outstockWarehouseGood.getBarcode();
        int len = barcode.length();
        List<String> tipList = CollectionUtil.newList("库位", "  类型", "商品名称", "条码后六位", "  箱规", dateName, "待拣货数量BU", "    单位");
        List<String> valueList = CollectionUtil.newList(outstockWarehouseGood.getWarehouseCode(), sheettypeName, RFUtil.makeStrFitPda(outstockWarehouseGood.getSkuname(), "", SKU_NAME_LIMIT_LINE_NUM),
                barcode.substring(len - 6, len), outstockWarehouseGood.getPknumshow() + "", getDateValueByBatchRule(outstockWarehouseGood), outstockWarehouseGood.getHopefulPickbu() + "", outstockWarehouseGood.getUnitname());
        int[] isChangeRow;
        if (index == outstockWarehouseGoods.size() - 1) {//最后一个库位
            isChangeRow = new int[]{0, 1, 1, 0, 1, 1, 0, 1};
        } else {
            isChangeRow = new int[]{0, 1, 1, 0, 1, 1, 0, 1, 1};
            tipList.add("下一个待拣库位");
            valueList.add(outstockWarehouseGoods.get(index + 1).getWarehouseCode());
        }
        printBeforeNextField(tipList, valueList, isChangeRow, accepterMap, ctx);
    }

    /**
     * 如果是洗化，返回失效日期，否则返回生产日期
     *
     * @param outstockWarehouseGood
     * @return
     */
    private String getDateValueByBatchRule(OutstockWarehouseGood outstockWarehouseGood) {
        if (outstockWarehouseGood.getBatchrule().equals(Constants.batchRuleEnum.xiHua.code)) {//洗化显示失效日期
            return DateTimeUtil.getStringWithSeparator(outstockWarehouseGood.getExpirationdate());
        } else {
            return DateTimeUtil.getStringWithSeparator(outstockWarehouseGood.getProductiondate());
        }
    }

    /**
     * 发生错误操作后，给出提示语，任意键继续
     *
     * @param errMsg 错误信息
     */
    private void friendlyMsgAnyKeyContinue(String errMsg) {
        HandlerUtil.changeRow(ctx);
        HandlerUtil.errorBeep(ctx);
        HandlerUtil.print(ctx, errMsg + ErrorConstants.TIP_TO_CONTINUE);
    }

    /**
     * 从返回结果中，获得错误信息
     *
     * @param remoteResult 返回的结果
     */
    private String getErrorMsgFromRemoteResult(RemoteResult remoteResult) {
        String errorCode = remoteResult.getResultCode();
        if (StringUtils.isNotEmpty(errorCode)) {
            return errorCode;
        } else {
            Object obj = remoteResult.getT();
            return obj == null ? "" : obj.toString();
        }
    }

    /**
     * 发生成功操作后，给出提示语，任意键继续
     *
     * @param errMsg 错误信息
     */
    private void friendlyMsgSucceedAnyKeyContinue(String errMsg) {
        HandlerUtil.changeRow(ctx);
        HandlerUtil.print(ctx, errMsg + ErrorConstants.TIP_TO_CONTINUE);
    }
}
