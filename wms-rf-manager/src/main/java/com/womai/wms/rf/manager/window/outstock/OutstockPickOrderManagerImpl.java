package com.womai.wms.rf.manager.window.outstock;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.person.api.domain.User;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.*;
import com.womai.wms.rf.domain.outstock.OutstockPickOrder;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.instock.InstockReasonRemoteService;
import com.womai.wms.rf.remote.outstock.OutstockPickOrderRemoteService;
import com.womai.wms.rf.remote.user.UserRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.instock.InstockReason;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockWarehouseGood;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * ClassDescribe:拣货管理
 * Author :wangzhanhua
 * Date: 2016-11-05
 * Since
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component("outstockPickOrderManager")
public class OutstockPickOrderManagerImpl extends ReceiveManager {
    private static final String WORK_SHEET_NO = "workSheetNo";//拣货单号
    private static final String WAREHOUSE_CODE = "warehouseCode";//库位编码
    private static final String BARCODE = "barcode";//商品条码
    private static final String PICK_BU = "pickBu";//拣货数量bu
    private static final String SELECT_NO_FIR = "selectNoFir";//第一次选择是否重新分配
    private static final String SELECT_NO_SEC = "selectNoSec";//第二次选择是否重新分配
    private static final String USERNAME = "username";//账号
    private static final String PASSWORD = "password";//密码
    private static final String REASON_CONTENT = "reasonContent";//原因内容

    private static final String NEXT_LOCATION = "nextLocation";//跳转的位置
    private static final int LOCATION_TO_CHANNELACTIVE = 0;//跳转到初始界面
    private static final int LOCATION_TO_NEXT_WAREHOUSE = 1;//跳转到下个库位
    private static final int SKU_NAME_LIMIT_LINE_NUM = 2;//商品名称限制行数

    private ChannelHandlerContext ctx;
    private List<OutstockWarehouseGood> outstockWarehouseGoods;//拣货信息
    private int index = 0;//当前拣货信息集合中的下标
    private User user;//用户

    private static final String[] pageHeader = {Constants.BREAK_LINE, TipConstants.OUTSTOCK_INFO_PICK_ORDER_MANAGER, Constants.SPLIT, ""};
    @Autowired
    private OutstockPickOrderRemoteService outstockPickOrderRemoteService;
    @Autowired
    private UserRemoteService userRemoteService;
    @Autowired
    private InstockReasonRemoteService instockReasonRemoteService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        // 拣货界面
        super.initBaseMap(OutstockPickOrder.class, pageHeader, ctx);
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
            nextWarehouseGoodAfterAllocateAndConfirm(getOutstockPickOrder(), accepterMap);
            return;
        }
        OutstockPickOrder outstockPickOrder = getOutstockPickOrder();
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (WORK_SHEET_NO.equals(lastCompleteColName)) {
            queryOutstockWarehouseGoods(outstockPickOrder, accepterMap);
        }
        if (WAREHOUSE_CODE.equals(lastCompleteColName)) {
            if(toChannelActive(ctx)){
                return;
            }
            String scanWhsCode = outstockPickOrder.getWarehouseCode();
            OutstockWarehouseGood outstockWarehouseGood = getCurrentWarehouseGoods();
            String screenWhsCode = outstockWarehouseGood.getWarehouseCode();
            if (!scanWhsCode.equals(screenWhsCode)) {
                colNeedReInput(WAREHOUSE_CODE, ErrorConstants.WAREHOUSE_CODE_ERROR, accepterMap, ctx);
            } else {
                rePrintCurColTip(accepterMap, ctx);
            }
        }
        if (BARCODE.equals(lastCompleteColName)) {
            if(toChannelActive(ctx)){
                return;
            }
            String scanBarcode = outstockPickOrder.getBarcode();
            OutstockWarehouseGood outstockWarehouseGood = getCurrentWarehouseGoods();
            String screenBarcode = outstockWarehouseGood.getBarcode();
            String screenpkBarcodeId = outstockWarehouseGood.getPkBarcodeId();
            if (!scanBarcode.equals(screenBarcode) && !scanBarcode.equals(screenpkBarcodeId)) {//不是商品条码，也不是外包装码
                colNeedReInput(BARCODE, ErrorConstants.BARCODE_OR_PKBARCODE_ERROR, accepterMap, ctx);
            } else {
                rePrintCurColTip(accepterMap, ctx);
            }
        }
        if (PICK_BU.equals(lastCompleteColName)) {
            if(toChannelActive(ctx)){
                return;
            }
            String inputPickNumStr = outstockPickOrder.getPickBu();//PDA录入的拣货数量BU
            if (inputPickNumStr.matches(TipConstants.REG_ZERO_OR_POSITIVE)) {
                OutstockWarehouseGood outstockWarehouseGood = getCurrentWarehouseGoods();
                int inputPickNum = Integer.parseInt(inputPickNumStr);
                int hopefulPickNum = outstockWarehouseGood.getHopefulPickbu();
                if (inputPickNum > hopefulPickNum) {
                    colNeedReInput(PICK_BU, ErrorConstants.DATA_ERROR_01, accepterMap, ctx);
                } else if (inputPickNum == hopefulPickNum) {
                    //设置实际拣货数量bu
                    getCurrentWarehouseGoods().setRealPickbu(inputPickNum);
                    // 拣货确认
                    RemoteResult<String> resultConfirm = outstockPickOrderRemoteService.confirmPick(getCredentialsVO(ctx), getCurrentWarehouseGoods());
                    if (resultConfirm.isSuccess()) {
                        //如果是最后一组处理成功后，跳转到初始界面
                        if (index == outstockWarehouseGoods.size() - 1) {
                            //modify by wzh 2017-03-02,任意键后跳转到初始界面
                            accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);//跳转初始界面
                            friendlyMsgSucceedAnyKeyContinue(TipConstants.OUTSTOCK_INFO_PICK_FINISHED);
                            return;
                        }
                        //跳转到显示下个库位
                        List<String> showStrings = CollectionUtil.newList(WORK_SHEET_NO);
                        List<String> clearStrings = CollectionUtil.newList(WAREHOUSE_CODE, BARCODE, PICK_BU);
                        printFieldsAndReceiveData(pageHeader, showStrings, WAREHOUSE_CODE, clearStrings, accepterMap, ctx);
                        HandlerUtil.moveUpN(ctx, 1);//上移一行去掉空格
                        index++;//下个拣货信息
                        printPickOrderMsg(getCurrentWarehouseGoods(), accepterMap);//拣货信息
                    } else {
                        //业务异常回到初始页面，扫描拣货单号
                        friendlyMsgAnyKeyContinue(getErrorMsgFromRemoteResult(resultConfirm));
                        accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);
                    }
                } else {
                    //校验是否箱规的整数倍
                    if (inputPickNum % outstockWarehouseGood.getPknum() != 0) {
                        colNeedReInput(PICK_BU, ErrorConstants.PLS_INPUT_RIGHT_NUM, accepterMap, ctx);
                        return;
                    } else {//提示是否重新分配
                        rePrintCurColTip(accepterMap, ctx);
                        //设置实际拣货数量bu
                        getCurrentWarehouseGoods().setRealPickbu(inputPickNum);
                    }
                }
            } else {
                colNeedReInput(PICK_BU, ErrorConstants.OUTSTOCK_PICK_ORDER_NUM_ERROR, accepterMap, ctx);
            }
        }
        if (SELECT_NO_FIR.equals(lastCompleteColName)) {
            if(toChannelActive(ctx)){
                return;
            }
            String selectNofir = outstockPickOrder.getSelectNoFir();
            if (selectNofir.matches(TipConstants.REG_YN)) {
                if (Constants.CONFIRM_Y.equalsIgnoreCase(selectNofir)) {
                    List<String> tipList = CollectionUtil.newList("缺货库位", "缺货商品名称", "待拣货数量BU", "实物数量BU");
                    OutstockWarehouseGood outstockWarehouseGood = getCurrentWarehouseGoods();
                    List<String> valueList = CollectionUtil.newList(outstockPickOrder.getWarehouseCode(), RFUtil.makeStrFitPda(outstockWarehouseGood.getSkuname(), "", SKU_NAME_LIMIT_LINE_NUM),
                            outstockWarehouseGood.getHopefulPickbu() + "", outstockPickOrder.getPickBu());
                    printBeforeNextField(tipList, valueList, accepterMap, ctx);
                    rePrintCurColTip(accepterMap, ctx);//第二个提示是否重新分配
                } else {
                    //跳转到输入数量bu，显示的字段中间有额外的信息
                    List<String> showStrings = CollectionUtil.newList(WAREHOUSE_CODE, BARCODE);
                    List<String> clearStrings = CollectionUtil.newList(PICK_BU, SELECT_NO_FIR);
                    printFieldsAndReceiveData(pageHeader, WAREHOUSE_CODE, showStrings, PICK_BU, clearStrings, accepterMap, ctx);
                }
            } else {
                colNeedReInput(SELECT_NO_FIR, ErrorConstants.INPUT_ERROR, accepterMap, ctx);
            }
        }

        if (SELECT_NO_SEC.equals(lastCompleteColName)) {
            if(toChannelActive(ctx)){
                return;
            }
            String selectNoSec = outstockPickOrder.getSelectNoSec();
            if (selectNoSec.matches(TipConstants.REG_YN)) {
                if (Constants.CONFIRM_Y.equalsIgnoreCase(selectNoSec)) {
                    //提示差异信息
                    rePrintCurColTip(accepterMap, ctx);//第二个提示是否重新分配
                } else {
                    //跳转到输入数量bu，显示的字段中间有额外的信息
                    List<String> showStrings = CollectionUtil.newList(WAREHOUSE_CODE, BARCODE);
                    List<String> clearStrings = CollectionUtil.newList(PICK_BU, SELECT_NO_FIR, SELECT_NO_SEC);
                    printFieldsAndReceiveData(pageHeader, WAREHOUSE_CODE, showStrings, PICK_BU, clearStrings, accepterMap, ctx);
                }
            } else {
                colNeedReInput(SELECT_NO_SEC, ErrorConstants.INPUT_ERROR, accepterMap, ctx);
            }
        }
        if (USERNAME.equals(lastCompleteColName)) {
            if(toChannelActive(ctx)){
                return;
            }
            String username = outstockPickOrder.getUsername();
            RemoteResult<Boolean> isPass = outstockPickOrderRemoteService.validateUsernameForPickAuthority(getCredentialsVO(ctx), username);
            if (isPass.isSuccess()) {
                User user = userRemoteService.findUser(username);
                this.user = user;
                if (user == null) {
                    colNeedReInput(USERNAME, ErrorConstants.USERNAME_NOTEXSIT, accepterMap, ctx);
                } else {
                    rePrintCurColTip(accepterMap, ctx);
                }
            } else {
                colNeedReInput(USERNAME, ErrorConstants.OUTSTOCK_PICK_ORDER_USERNAME_ERROR, accepterMap, ctx);
            }
        }
        if (PASSWORD.equals(lastCompleteColName)) {
            if(toChannelActive(ctx)){
                return;
            }
            String password = outstockPickOrder.getPassword();
            if (MD5Util.encodeString(password).equals(user.getPassword())) {
                //密码验证通过
                if (getCurrentWarehouseGoods().getSkuStatus().equals(WmsConstants.STOCK_GODDSSTATUS_NORMAL)) {//正品
                    rePrintCurColTip(accepterMap, ctx);
                } else {//残品或者冻结,跳过原因的选择
                    //拣货确认，重新分配
                    RemoteResult<String> remoteResult = outstockPickOrderRemoteService.confirmAndReallocatePick(getCredentialsVO(ctx), getCurrentWarehouseGoods());
                    if (remoteResult.isSuccess()) {//1分配成功后，按实际拣货数量bu确认。2分配失败后，除采购退货单外，按计划数量bu
                        String msgSucceed = remoteResult.getT();
                        if (StringUtils.isEmpty(msgSucceed)) {
                            //实物数量bu不等于计划数量bu情况下，操作。跳转到下一组拣货信息
                            nextWarehouseGoodAfterAllocateAndConfirm(outstockPickOrder, accepterMap);
                        } else {//分配失败后，安照分配捡货数量bu确认，或者安照实际输入数量bu确认（采购退货单）
                            if (outstockWarehouseGoods.indexOf(getCurrentWarehouseGoods()) == outstockWarehouseGoods.size() - 1) { //最后一组
                                accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);//跳转初始界面
                            } else {
                                accepterMap.put(NEXT_LOCATION, LOCATION_TO_NEXT_WAREHOUSE);//跳转到下个库位
                            }
                            HandlerUtil.changeRow(ctx);//防止清除密码展示行
                            HandlerUtil.print(ctx, msgSucceed + ErrorConstants.TIP_TO_CONTINUE);
                        }
                    } else {
                        HandlerUtil.changeRow(ctx);//防止清除密码展示行
                        HandlerUtil.errorBeep(ctx);
                        HandlerUtil.print(ctx, getErrorMsgFromRemoteResult(remoteResult) + ErrorConstants.TIP_TO_CONTINUE);
                        accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);//区分不同的异常
                    }
                }
            } else {
                colNeedReInput(PASSWORD, ErrorConstants.PASSWORD_ERROR, accepterMap, ctx);
            }
        }
        if (REASON_CONTENT.equals(lastCompleteColName)) {
            if(toChannelActive(ctx)){
                return;
            }
            String reasonInputValue = outstockPickOrder.getReasonContent();
            InstockReason instockReason = null;
            if (reasonInputValue.matches(TipConstants.REG_REASON_FOR_PICK)) {//用户输入“1”或者“2”
                RemoteResult<List<InstockReason>> reasonListResult = instockReasonRemoteService.queryInstockReasonsByType(getCredentialsVO(ctx),
                        CollectionUtil.newList(WmsConstants.WhsType.RFjianhuoquehuo.getValue(), WmsConstants.WhsType.RFjianhuocanpin.getValue()));
                if (reasonListResult.isSuccess()) {
                    Integer reasonType;
                    if (reasonInputValue.equals(Constants.REASON_ONE)) {
                        reasonType = Constants.REASON_TYPE_FOR_INPUT_ONE;
                    } else {
                        reasonType = Constants.REASON_TYPE_FOR_INPUT_TWO;
                    }
                    List<InstockReason> instockReasons = reasonListResult.getT();
                    for (InstockReason reason : instockReasons) {
                        if (reasonType.equals(reason.getReasonType())) {
                            instockReason = reason;
                        }
                    }
                    if (instockReason == null) {
                        //没有匹配到原因，需要维护
                        friendlyMsgAnyKeyContinue(getErrorMsgFromRemoteResult(reasonListResult));
                        accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);//区分不同的异常
                    }
                } else {
                    //没有原因列表，需要维护
                    friendlyMsgAnyKeyContinue(getErrorMsgFromRemoteResult(reasonListResult));
                    accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);//区分不同的异常
                }
            } else {
                colNeedReInput(REASON_CONTENT, ErrorConstants.INPUT_ERROR, accepterMap, ctx);
                return;
            }
            OutstockWarehouseGood outstockWarehouseGood = getCurrentWarehouseGoods();
            outstockWarehouseGood.setReasonid(instockReason.getId());
            outstockWarehouseGood.setReasonContent(instockReason.getRemark());
            //拣货确认，重新分配
            confirmAndReallocatePick(outstockPickOrder, accepterMap, outstockWarehouseGood);
        }
    }

    /**
     * 拣货确认，重新分配
     *
     * @param outstockPickOrder     接收参数的对象
     * @param accepterMap           接收参数的容器
     * @param outstockWarehouseGood 拣货信息对象
     */
    private void confirmAndReallocatePick(OutstockPickOrder outstockPickOrder, Map<String, Object> accepterMap, OutstockWarehouseGood outstockWarehouseGood) {
        RemoteResult<String> remoteResult = outstockPickOrderRemoteService.confirmAndReallocatePick(getCredentialsVO(ctx), outstockWarehouseGood);
        if (remoteResult.isSuccess()) {//1分配成功后，按实际拣货数量bu确认。2分配失败后，除采购退货单外，按计划数量bu
            String msgSucceed = remoteResult.getT();
            if (StringUtils.isEmpty(msgSucceed)) {
                //实物数量bu不等于计划数量bu情况下，操作。跳转到下一组拣货信息
                nextWarehouseGoodAfterAllocateAndConfirm(outstockPickOrder, accepterMap);
            } else {
                if (outstockWarehouseGoods.indexOf(outstockWarehouseGood) == outstockWarehouseGoods.size() - 1) { //最后一组
                    accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);//跳转初始界面
                } else {
                    accepterMap.put(NEXT_LOCATION, LOCATION_TO_NEXT_WAREHOUSE);//跳转到下个库位
                }
                friendlyMsgSucceedAnyKeyContinue(msgSucceed);
            }
        } else {
            friendlyMsgAnyKeyContinue(getErrorMsgFromRemoteResult(remoteResult));
            accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);//区分不同的异常
        }
    }


    /**
     * @param outstockPickOrder
     * @param accepterMap
     */
    private void nextWarehouseGoodAfterAllocateAndConfirm(OutstockPickOrder outstockPickOrder, Map<String, Object> accepterMap) {
        RemoteResult<List<OutstockWarehouseGood>> listRemoteResult = outstockPickOrderRemoteService.queryOutstockWarehouseGoodAfterValidateOrder(getCredentialsVO(ctx), outstockPickOrder.getWorkSheetNo());
        if (listRemoteResult.isSuccess()) {
            this.outstockWarehouseGoods = listRemoteResult.getT();
        } else {
            friendlyMsgAnyKeyContinue(getErrorMsgFromRemoteResult(listRemoteResult));
            accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);//区分不同的异常
        }
        List<String> showStrings = CollectionUtil.newList(WORK_SHEET_NO);
        List<String> clearStrings = CollectionUtil.newList(WAREHOUSE_CODE, BARCODE, PICK_BU, SELECT_NO_FIR, SELECT_NO_SEC, USERNAME, PASSWORD, REASON_CONTENT);
        printFieldsAndReceiveData(pageHeader, showStrings, WAREHOUSE_CODE, clearStrings, accepterMap, ctx);
        HandlerUtil.moveUpN(ctx, 1);//上移一行去掉空格
        this.index = 0;//下个拣货信息为重新查找拣货信息的第一个
        printPickOrderMsg(getCurrentWarehouseGoods(), accepterMap);//拣货信息
    }

    /**
     * 查询拣货信息
     *
     * @param outstockPickOrder 接收参数bean
     * @param accepterMap       接收参数容器
     */
    private void queryOutstockWarehouseGoods(OutstockPickOrder outstockPickOrder, Map<String, Object> accepterMap) {
        RemoteResult<List<OutstockWarehouseGood>> remoteResult = outstockPickOrderRemoteService.queryOutstockWarehouseGoodAfterValidateOrder(getCredentialsVO(ctx), outstockPickOrder.getWorkSheetNo());
        if (remoteResult.isSuccess()) {
            this.outstockWarehouseGoods = remoteResult.getT();
            index = 0;//初始为第一个
            printPickOrderMsg(getCurrentWarehouseGoods(), accepterMap);
        } else {
            colNeedReInput(WORK_SHEET_NO, getErrorMsgFromRemoteResult(remoteResult), accepterMap, ctx);
        }
    }

    /**
     * 从返回结果中，获得错误信息
     *
     * @param remoteResult 返回的结果
     */
    private String getErrorMsgFromRemoteResult(RemoteResult remoteResult) {
        String errorCode = remoteResult.getResultCode();
        if (StringUtils.isNotEmpty(errorCode)) {
            return ErrorConstants.ErrorCodeParzer.parzeErrorCode(errorCode);
        } else {
            Object obj = remoteResult.getT();
            return obj == null ? "" : obj.toString();
        }
    }

    @Override
    protected void printMsg(Object objectClass, Map<String, Object> accepterMap) {
        printWorkSheetNoAndPickOrderMsg(getCurrentWarehouseGoods(), (OutstockPickOrder) objectClass, accepterMap);
    }

    /**
     * 获得当前展示的拣货信息
     *
     * @return
     */
    private OutstockWarehouseGood getCurrentWarehouseGoods() {
        return this.outstockWarehouseGoods.get(this.index);
    }

    /**
     * 获得接收页面参数的对象
     *
     * @return 参数对象
     */
    private OutstockPickOrder getOutstockPickOrder() {
        return (OutstockPickOrder) getDataMap().get(DefaultKey.objectClass.keyName);
    }

    /**
     * 打印拣货信息
     *
     * @param accepterMap           参数容器
     * @param outstockWarehouseGood 拣货信息
     */
    private void printPickOrderMsg(OutstockWarehouseGood outstockWarehouseGood, Map<String, Object> accepterMap) {
        String dateName = Constants.batchRuleEnum.getDateTypeByCode(outstockWarehouseGood.getBatchrule());
        List<String> tipList = CollectionUtil.newList("推荐库位", "商品条码", "商品名称", "包装", "包装规格", "状态", dateName, "待拣货数量BU");
        List<String> valueList = CollectionUtil.newList(outstockWarehouseGood.getWarehouseCode(), outstockWarehouseGood.getBarcode(),
                RFUtil.makeStrFitPda(outstockWarehouseGood.getSkuname(), "", SKU_NAME_LIMIT_LINE_NUM), outstockWarehouseGood.getUnitname(), outstockWarehouseGood.getSpec(), CheckReasonEnum.getNameByValue(outstockWarehouseGood.getSkuStatus()),
                getDateValueByBatchRule(outstockWarehouseGood), outstockWarehouseGood.getHopefulPickbu() + "");
        HandlerUtil.changeRow(ctx);
        printBeforeNextField(tipList, valueList, accepterMap, ctx);
        HandlerUtil.changeRow(ctx);
        rePrintCurColTip(accepterMap, ctx);
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
     * 输出拣货单号和拣货信息
     *
     * @param outstockWarehouseGood 拣货信息
     * @param outstockPickOrder     参数Bean
     * @param accepterMap           参数容器
     */
    private void printWorkSheetNoAndPickOrderMsg(OutstockWarehouseGood outstockWarehouseGood, OutstockPickOrder outstockPickOrder, Map<String, Object> accepterMap) {
        String dateName = Constants.batchRuleEnum.getDateTypeByCode(outstockWarehouseGood.getBatchrule());
        List<String> tipList = CollectionUtil.newList("拣货单号", "推荐库位", "商品条码", "商品名称", "包装", "包装规格", "状态", dateName, "待拣货数量BU");
        List<String> valueList = CollectionUtil.newList(outstockPickOrder.getWorkSheetNo(), outstockWarehouseGood.getWarehouseCode(), outstockWarehouseGood.getBarcode(),
                RFUtil.makeStrFitPda(outstockWarehouseGood.getSkuname(), "", SKU_NAME_LIMIT_LINE_NUM), outstockWarehouseGood.getUnitname(), outstockWarehouseGood.getSpec(), CheckReasonEnum.getNameByValue(outstockWarehouseGood.getSkuStatus()),
                getDateValueByBatchRule(outstockWarehouseGood), outstockWarehouseGood.getHopefulPickbu() + "");
        HandlerUtil.changeRow(ctx);
        printBeforeNextField(tipList, valueList, accepterMap, ctx);
        HandlerUtil.changeRow(ctx);
    }

    /**
     * 发生错误操作后，给出提示语，任意键继续
     *
     * @param errMsg 错误信息
     */
    private void friendlyMsgAnyKeyContinue(String errMsg) {
        HandlerUtil.delLeft(ctx);
        HandlerUtil.changeRow(ctx);
        HandlerUtil.errorBeep(ctx);
        HandlerUtil.print(ctx, errMsg + ErrorConstants.TIP_TO_CONTINUE);
    }
    /**
     * 发生成功操作后，给出提示语，任意键继续
     *
     * @param errMsg 错误信息
     */
    private void friendlyMsgSucceedAnyKeyContinue(String errMsg) {
        HandlerUtil.delLeft(ctx);
        HandlerUtil.changeRow(ctx);
        HandlerUtil.print(ctx, errMsg + ErrorConstants.TIP_TO_CONTINUE);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

}
