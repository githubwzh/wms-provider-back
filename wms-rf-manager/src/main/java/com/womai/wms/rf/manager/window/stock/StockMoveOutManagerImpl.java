package com.womai.wms.rf.manager.window.stock;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.DefaultKey;
import com.womai.wms.rf.common.constants.ErrorConstants;
import com.womai.wms.rf.common.constants.TipConstants;
import com.womai.wms.rf.common.util.CollectionUtil;
import com.womai.wms.rf.common.util.DateTimeUtil;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.RFUtil;
import com.womai.wms.rf.domain.stock.StockMoveOut;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.base.GoodsinfoRemoteService;
import com.womai.wms.rf.remote.stock.StockMoveRemoteService;
import com.womai.zlwms.rfsoa.api.WMSErrorMess;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.stock.StockMove;
import com.womai.zlwms.rfsoa.domain.stock.StockMoveItem;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * ClassDescribe: 移位管理RF移出Handler
 * Author :Xiafei Qi
 * Date: 2016-08-19
 * Since
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component(Constants.STOCK_MOVE_OUT_MANAGER)
public class StockMoveOutManagerImpl extends ReceiveManager {

    @Autowired
    StockMoveRemoteService stockMoveRemoteService;
    @Autowired
    GoodsinfoRemoteService goodsinfoRemoteService;

    // 业务流程字段名
    private static final String CONFIRM_MOVEALL = "confirmMoveAll"; // 确认全部RF移位单移出
    private static final String SHELF_CODE = "shelfcode"; // 移位单号
    private static final String SRC_WHS_CODE = "srcWhsCode"; // 移出库位
    private static final String BARCODE = "barCode"; // 商品条码
    private static final String SELECT_UNIT = "selectUnit"; // 请选择单位
    private static final String CONFIRM_MOVE_ALLDETAIL = "confirmMoveAllDetail";// 确认移出全部明细

    //本地变量
    private StockMove localStockMove; // 移位单主单
    //===因为有些步骤会单独的清除生产日期、单位、商品id、移出库位，所以这里不适合用一个移位单明细对象保存如下数据======//
    private String localSrcWhsCode = "";// 移出库位
    private Long localSkuid = 0L;// 商品id
    private String localUnitName = "";// 包装单位
    private Date localProductionDate;// 生产日期

    //字段输入校验
    private static final String YN_REGX = "^[YNyn]$"; // 输入y/n的场景校验

    // 任意键跳转key-value
    private static final String GOTO_KEY = "stockMoveOutManagerGotoKey";
    private static final String GOTO_CHANNELACTIVE = "channelActive";// GOTO到channelActive的value
    private static final String GOTO_MAINMENU = "mainMenu";// GOTO到主菜单_
    private boolean AUTO_MOVE_OUT = false;// ture 系统自动移出（RF创建的移位单），false 手动移出

    // 界面标题:pageHeader中无论放的什么内容，每个数组元素占一行。若需要换行不要使用\r\n，使用一个新的数组元素。
    private static final String[] PAGEHEADER = {"", TipConstants.STOCK_MOVEOUT_TITLE, Constants.SPLIT, ""};


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        resetLocals();// 初始化时清空本地变量内容
        initBaseMap(StockMoveOut.class, PAGEHEADER, ctx);// 重新初始化界面，重置已接受数据，打印标题
        Map<String, Object> accepterMap = getDataMap(); // ReceiveManager中记录输入状态的Map
        resetCurCol(SHELF_CODE, accepterMap, ctx);// 为了不让在与后台交互时显示是否确认全部移出令用户感觉很奇怪，先将下个字段变成移位单号
        //  查询由当前用户在RF创建的、激活状态的、未移出的、非报损的移位单，若有的话，将下一个字段修改为确认全部移出
        RemoteResult<StockMove> result = stockMoveRemoteService.getActiveNotMoveOutRFStockMoveByUser(getCredentialsVO(ctx));
        if (!result.isSuccess()) {// 查询错误，有可能是查到多条数据，也有可能是数据库异常
            // 打印错误提示.因为这种错误就算当前页面重载也是会反复提示，所以这里等待用户输入任意键后跳转到主菜单
            printMessageWaitAnyKeyToCol(result.getResultCode(), GOTO_MAINMENU, accepterMap, ctx);
        }
        StockMove stockMove = result.getT();
        if (stockMove != null) {// 如果该用户有一个未完成的RF移位任务，先操作这个任务
            localStockMove = stockMove;// 刷新本地变量保存的移位单信息
            resetCurCol(CONFIRM_MOVEALL, accepterMap, ctx);// 下个字段变成确认全部移出
            printBeforeConfirmMoveAll(stockMove, accepterMap, ctx);// 在让用户确认全部移出之前打印一些信息
            //modify wzh 20170309
            AUTO_MOVE_OUT = true;
            channelRead(ctx, Constants.CONFIRM_Y);//RF创建的移位单，系统自动移出
            channelRead(ctx, Constants.BREAK_LINE);
        }else{
            AUTO_MOVE_OUT = false;
        }
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object inputObj) throws Exception {
        Map<String, Object> accepterMap = getDataMap(); // ReceiveManager中记录输入状态的Map
        // 处理跳转请求，GOTO的值代表要跳转到的字段名，该字段前的字段继续打印在屏幕上，从该字段开始接受值
        if (goToHandler(accepterMap, ctx)) {
            return;
        }
        receiveDataAndNotPrintNext(ctx, inputObj, accepterMap); // 接收输入不自动打印下一个字段
        StockMoveOut stockMoveOut = (StockMoveOut) accepterMap.get(DefaultKey.objectClass.keyName);// 当前业务对象
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);//上一个回车所在的字段
        if (CONFIRM_MOVEALL.equals(lastCompleteColName)) {
            String confirmMoveAll = stockMoveOut.getConfirmMoveAll();
            if (confirmMoveAll.equalsIgnoreCase(Constants.CONFIRM_Y)) {// 如果用户输入y
                // 确认移出提交
                HandlerUtil.writeAndFlush(ctx, Constants.BREAK_LINE + "正在分配，请等待...");
                RemoteResult<Integer> result = stockMoveRemoteService.confirmMoveOutRFCreate(this.getCredentialsVO(ctx), localStockMove);
                if (result.isSuccess()) {
                    // 打印成功信息,并等待用户任意键继续回到channelActive
//                    printMessageSucceedWaitAnyKeyToCol(ErrorConstants.SUCCESS_CONTINUE, GOTO_CHANNELACTIVE, accepterMap, ctx);
                    Thread.sleep(Constants.SLEEP_TIME);//上面提示，显示3秒
                    forward(Constants.STOCK_MOVE_IN_MANAGER, ctx);
                } else {
                    if(AUTO_MOVE_OUT){
                        // 打印错误提示.因为这种错误就算当前页面重载也是会反复提示，所以这里等待用户输入任意键后跳转到主菜单
                        printMessageWaitAnyKeyToCol(result.getResultCode(), GOTO_MAINMENU, accepterMap, ctx);
                    }else{
                        // 打印失败信息,并等待用户任意键继续回到channelActive
                        printMessageWaitAnyKeyToCol(result.getResultCode(), GOTO_CHANNELACTIVE, accepterMap, ctx);
                    }
                }
            } else {// 如果用户输入不是y
                colNeedReInput(lastCompleteColName, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);//提示重新输入
            }
        } else if (SHELF_CODE.equals(lastCompleteColName)) {
            // 移位单号
            String shelfCode = stockMoveOut.getShelfcode();
            // 校验移位单号是否正确
            RemoteResult<StockMove> result = stockMoveRemoteService.validShelfCodeAndGetStockMoveByItForMoveOut(getCredentialsVO(ctx), shelfCode);
            if (!result.isSuccess()) {
                // 如果校验不通过，把问题原因提示给用户,重新接收移位单号
                colNeedReInput(lastCompleteColName, result.getResultCode(), accepterMap, ctx);
                return;
            }
            StockMove resultStockMove = result.getT();// 刷新本地变量保存的移位单信息
            if (resultStockMove.getCreateType().equals(WmsConstants.STOCK_CREATETYPE_RF)) {//RF创建的移位单，校验
                colNeedReInput(SHELF_CODE, ErrorConstants.STOCK_MOVE_SCAN_CREATE_TYPE_ERR, accepterMap, ctx);
                return;
            } else {
                localStockMove = result.getT();// 刷新本地变量保存的移位单信息
            }
            resetCurCol(SRC_WHS_CODE, accepterMap, ctx);// 打印下一字段

        } else if (SRC_WHS_CODE.equals(lastCompleteColName)) {

            String srcWhsCode = stockMoveOut.getSrcWhsCode();
            // 校验库位是否正确
            StockMoveItem condition = new StockMoveItem();
            condition.setShelfid(localStockMove.getShelfid());//查询条件移位单id
            condition.setSrcwhscode(srcWhsCode);//查询条件库位编码
            RemoteResult<String> validShelfCodeForMoveOutResult = stockMoveRemoteService.validSrcWhsCodeForMoveOut(getCredentialsVO(ctx), condition);//验证库位编码是否合法
            if (!validShelfCodeForMoveOutResult.isSuccess()) {
                // 如果校验不通过，把问题原因提示给用户
                colNeedReInput(lastCompleteColName, validShelfCodeForMoveOutResult.getT(), accepterMap, ctx);
                return;
            }
            localSrcWhsCode = srcWhsCode;// 刷新本地变量保存的库位编码
            resetCurCol(BARCODE, accepterMap, ctx);// 打印下一字段

        } else if (BARCODE.equals(lastCompleteColName)) {
            String barCode = stockMoveOut.getBarCode();
            //接下来需要根据移位单号、库位编码、商品条码决定接下来的行为
            handleAfterBarCode(barCode, accepterMap, ctx);

        } else if (SELECT_UNIT.equals(lastCompleteColName)) {
            String selectUnit = stockMoveOut.getSelectUnit();
            String unitName = selectUnit.split(TipConstants.STOCK_MOVEOUT_SPLIT_SELECTUNIT)[0];
            // 查询未移出数量bu
            StockMoveItem condition = new StockMoveItem();
            condition.setShelfid(localStockMove.getShelfid());
            condition.setSrcwhscode(localSrcWhsCode); // 移出库位编码
            condition.setSkuid(localSkuid); // 商品id
            condition.setUnitname(unitName);// 单位
            Date productionDate = DateTimeUtil.parseSimpleStr(selectUnit.split(TipConstants.STOCK_MOVEOUT_SPLIT_SELECTUNIT)[1]);
            condition.setProductiondate(productionDate);//生产日期
            RemoteResult<Integer> result = stockMoveRemoteService.countNotYetMoveOutBuByUnitAndProductionDate(getCredentialsVO(ctx), condition);// 查询该包装单位/生产日期下未移出数量
            if (!result.isSuccess()) {
                // 打印失败信息,这里只可能会因为数据库异常查询失败，等待用户任意键继续回到channelActive
                printMessageWaitAnyKeyToCol(result.getResultCode(), GOTO_CHANNELACTIVE, accepterMap, ctx);
            }
            if (result.getT() == 0) { // 如果该单位下没有待移出数据，说明数据过期了，打印错误提示退回到扫描商品条码
                printMessageWaitAnyKeyToCol(WMSErrorMess.DATA_EXPIRY, BARCODE, accepterMap, ctx);
                return;
            }
            localUnitName = unitName;
            localProductionDate = productionDate;
            // 在确认明细全部移出之前打印未移出数量bu信息
            printBeforeNextField(RFUtil.makeStrFitPda(TipConstants.STOCK_MOVEOUT_TIP_BEFORE_MOVEOUTALLDETAIL_PREFIX
                    .concat(result.getT().toString()), "", 2), accepterMap, ctx);
            resetCurCol(CONFIRM_MOVE_ALLDETAIL, accepterMap, ctx);
        } else if (CONFIRM_MOVE_ALLDETAIL.equals(lastCompleteColName)) {
            String confirmMoveAllDetail = stockMoveOut.getConfirmMoveAllDetail();
            if (!confirmMoveAllDetail.matches(YN_REGX)) {
                colNeedReInput(lastCompleteColName, ErrorConstants.ONLY_YN, accepterMap, ctx);
                return;
            }

            if (confirmMoveAllDetail.equalsIgnoreCase(Constants.CONFIRM_Y)) {
                //提交后做一些处理
                handleAfterConfirmMoveAllDetail(accepterMap, ctx);
            } else {
                localSkuid = 0L;
                localProductionDate = null;
                localUnitName = "";
                List<String> fieldList = CollectionUtil.newGenericList(SHELF_CODE, SRC_WHS_CODE);
                printFieldsAndReceiveData(PAGEHEADER, fieldList, BARCODE, accepterMap, ctx);// 回退到扫描商品条码
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


    /**
     * 重置所有本地线程变量
     */

    private void resetLocals() {
        localStockMove = null;
        localSrcWhsCode = "";
        localSkuid = 0L;
    }


    /**
     * 在RF移位任务确认全部移出之前打印一些信息
     *
     * @param info 信息对象
     */
    private void printBeforeConfirmMoveAll(StockMove info, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        String printStr = TipConstants.STOCK_MOVEOUT_SHELFCODE.concat(ErrorConstants.COMMON_COLON).concat(info.getShelfcode());
        printBeforeNextField(printStr, accepterMap, ctx);

    }

    /**
     * 处理跳转请求
     *
     * @param accepterMap 接收信息map
     * @param ctx         netty上下文
     * @return true--跳转了  false--无跳转
     * @throws Exception
     */
    private boolean goToHandler(Map<String, Object> accepterMap, ChannelHandlerContext ctx) throws Exception {
        if (accepterMap.get(GOTO_KEY) != null) {
            String goTo = accepterMap.get(GOTO_KEY).toString();
            accepterMap.put(GOTO_KEY, null);
            if (goTo.equals(GOTO_MAINMENU)) { // 跳转到主菜单
                HandlerUtil.clearAll(ctx.channel());
                forward(Constants.MENU_SHELL_MANAGER, ctx);
                return true;
            }
            if (goTo.equals(GOTO_CHANNELACTIVE)) { // 重新初始化界面
                channelActive(ctx);
                return true;
            }
            if (goTo.equals(SRC_WHS_CODE)) {// 跳转到扫描库位
                // 清空库位及库位之后输入的本地变量
                localSrcWhsCode = "";
                localSkuid = 0L;
                localProductionDate = null;
                localUnitName = "";
                List<String> fieldList = CollectionUtil.newGenericList(SHELF_CODE);
                printFieldsAndReceiveData(PAGEHEADER, fieldList, goTo, accepterMap, ctx);// 回退到扫描移出库位
                return true;
            }
            if (goTo.equals(BARCODE)) {// 跳转到扫描商品条码
                // 清空商品条码及之后输入的本地变量
                localSkuid = 0L;
                localProductionDate = null;
                localUnitName = "";
                List<String> fieldList = CollectionUtil.newGenericList(SHELF_CODE, SRC_WHS_CODE);
                printFieldsAndReceiveData(PAGEHEADER, fieldList, goTo, accepterMap, ctx);// 回退到扫描商品条码
                return true;
            }
            if (goTo.equals(SELECT_UNIT)) {// 跳转到选择单位/生产日期
                // 清空单位、生产日期本地变量
                localProductionDate = null;
                localUnitName = "";
                List<String> fieldList = CollectionUtil.newGenericList(SHELF_CODE, SRC_WHS_CODE, BARCODE);// 屏幕打印出来的保留字段
                // 查询包装单位/生产日期分组信息
                StockMoveItem condition = new StockMoveItem();
                condition.setShelfid(localStockMove.getShelfid());
                condition.setSrcwhscode(localSrcWhsCode);
                condition.setSkuid(localSkuid);
                RemoteResult<List<Map<String, Object>>> validBarCodeForMoveOutResult = stockMoveRemoteService.queryMoveOutInfoGroupbyPkidAndProductionDate(getCredentialsVO(ctx), condition);
                List<Map<String, Object>> infoList = validBarCodeForMoveOutResult.getT();//包装单位、生产日期、待移出数量bu信息列表
                List<String> switchList = new ArrayList<String>(infoList.size());
                for (Map<String, Object> value : infoList) {
                    String option = value.get(WmsConstants.KEY_STOCKMOVE_ITEM_UNITNAME).toString().
                            concat(TipConstants.STOCK_MOVEOUT_SPLIT_SELECTUNIT).
                            concat(DateTimeUtil.getStringSimple((Date) value.get(WmsConstants.KEY_STOCKMOVE_ITEM_PRODUCTIONDATE)));
                    switchList.add(option);
                }
                printFieldsAndReceiveData(PAGEHEADER, fieldList, switchList, goTo, accepterMap, ctx);// 回退到选择单位
                return true;
            }
        }
        return false;
    }

    /**
     * 处理Web移位单确认移出后的行为
     *
     * @param accepterMap 接收信息map
     * @param ctx         netty上下文
     */
    private void handleAfterConfirmMoveAllDetail(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        // 确认移出提交,并返回移出单状态码
        // 1-移出后当前商品还有其他未移出的“单位/生产日期”，提示用户继续选择
        // 2-移出后货位下还有其他商品，则继续让用户扫描商品条码。
        // 3-若移出后当前货位下全部移出，但是还存在其他货位未移出，则提示“当前货位已经全部移出，任意键继续”，跳转到扫描货位
        // 4-移出后当前移位单下全部移出，则提示“当前移位单下商品已全部移出，任意键继续“，跳转到页面初始界面。
        RemoteResult<Integer> result = stockMoveRemoteService.confirmMoveOutAndReturnStatusCode(this.getCredentialsVO(ctx), localStockMove, localSrcWhsCode, localSkuid, localUnitName, localProductionDate);
        if (!result.isSuccess()) {
            // 打印失败信息,并等待用户任意键继续回到channelActive
            printMessageWaitAnyKeyToCol(RFUtil.makeStrFitPda(result.getResultCode(), "", 3), GOTO_CHANNELACTIVE, accepterMap, ctx);
        }
        int resultCode = result.getT();
        switch (resultCode) {
            case WMSErrorMess.STOCKMOVEOUT_STATUSCODE_CONTAINS_UNMOVEOUT_UNITNAME_PRODUCTIONDATE:
                // 打印成功信息,并等待用户任意键继续到选择单位
                printMessageWaitAnyKeyToCol(TipConstants.STOCK_MOVEOUT_UNITNAME_PRODUCTIONDATE_ALLOUT, SELECT_UNIT, accepterMap, ctx);
                break;
            case WMSErrorMess.STOCKMOVEOUT_STATUSCODE_CONTAINS_UNMOVEOUT_SKU:
                // 打印成功信息,并等待用户任意键继续到扫描商品条码
                printMessageWaitAnyKeyToCol(TipConstants.STOCK_MOVEOUT_BARCODE_ALLOUT, BARCODE, accepterMap, ctx);
                break;
            case WMSErrorMess.STOCKMOVEOUT_STATUSCODE_CONTAINS_UNMOVEOUT_WHS:
                // 打印成功信息,并等待用户任意键继续到扫描库位
                printMessageWaitAnyKeyToCol(TipConstants.STOCK_MOVEOUT_SRCWHSCODE_ALLOUT, SRC_WHS_CODE, accepterMap, ctx);
                break;
            case WMSErrorMess.STOCKMOVEOUT_STATUSCODE_ALLOUT:
                // 打印成功信息,并等待用户任意键继续到channelActive
                printMessageWaitAnyKeyToCol(TipConstants.STOCK_MOVEOUT_SHELFCODE_ALLOUT, GOTO_CHANNELACTIVE, accepterMap, ctx);
                break;
        }

    }

    /**
     * 扫描商品条码后的处理
     *
     * @param barCode     商品条码
     * @param accepterMap 接收信息map
     * @param ctx         netty上下文
     */
    private void handleAfterBarCode(String barCode, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        // 查询包装单位/生产日期分组信息
        StockMoveItem condition = new StockMoveItem();
        condition.setShelfid(localStockMove.getShelfid());// 移位单id
        condition.setSrcwhscode(localSrcWhsCode);// 库位编码
        RemoteResult<List<Map<String, Object>>> validBarCodeForMoveOutResult = stockMoveRemoteService.queryMoveOutInfoGroupbyPkidAndProductionDate(getCredentialsVO(ctx), condition, barCode);
        if (!validBarCodeForMoveOutResult.isSuccess()) {
            // 如果校验不通过，把问题原因提示给用户
            colNeedReInput(BARCODE, validBarCodeForMoveOutResult.getResultCode(), accepterMap, ctx);
            return;
        }
        List<Map<String, Object>> infoList = validBarCodeForMoveOutResult.getT();//包装单位、生产日期、待移出数量bu信息列表，在列表的第一个对象中存放了商品id信息
        localSkuid = (Long) infoList.get(0).get(WmsConstants.KEY_PARAM_SKUID);// 将商品id放入本地变量
        if (infoList.size() == 1) {// 如果明细只有一种包装+生产日期的组合，直接让用户确认是否全部移出
            Map<String, Object> info = infoList.get(0);
            String unitName = (String) info.get(WmsConstants.KEY_STOCKMOVE_ITEM_UNITNAME);// 包装单位
            Date productionDate = (Date) info.get(WmsConstants.KEY_STOCKMOVE_ITEM_PRODUCTIONDATE); // 生产日期
            localUnitName = unitName;
            localProductionDate = productionDate;
            // 在确认明细全部移出之前打印未移出数量bu信息
            printBeforeNextField(RFUtil.makeStrFitPda(TipConstants.STOCK_MOVEOUT_TIP_BEFORE_MOVEOUTALLDETAIL_PREFIX
                    .concat(info.get(WmsConstants.KEY_NOT_YET_STOCKMOVEOUTBU).toString()), "", 2), accepterMap, ctx);
            resetCurCol(CONFIRM_MOVE_ALLDETAIL, accepterMap, ctx);// 直接跳转到确认移出
        } else {
            List<String> switchList = new ArrayList<String>(infoList.size());
            for (Map<String, Object> value : infoList) {
                String option = value.get(WmsConstants.KEY_STOCKMOVE_ITEM_UNITNAME).toString().
                        concat(TipConstants.STOCK_MOVEOUT_SPLIT_SELECTUNIT).
                        concat(DateTimeUtil.getStringSimple((Date) value.get(WmsConstants.KEY_STOCKMOVE_ITEM_PRODUCTIONDATE)));
                switchList.add(option);
            }
            setNextColSwitchList(switchList, accepterMap, ctx);
        }
    }

    /**
     * 打印一些信息，并且等待任意键跳转
     *
     * @param msg 要打印的信息
     * @param ctx netty上下文
     */
    private void printMessageWaitAnyKeyToCol(String msg, String colName, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        HandlerUtil.writeAndFlush(ctx, Constants.BREAK_LINE.concat(RFUtil.makeStrFitPda(msg, "", 3)));
        HandlerUtil.errorBeep(ctx);
        accepterMap.put(GOTO_KEY, colName);
    }

    /**
     * 打印一些成功操作信息，并且等待任意键跳转
     *
     * @param msg 要打印的信息
     * @param ctx netty上下文
     */
    private void printMessageSucceedWaitAnyKeyToCol(String msg, String colName, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        HandlerUtil.writeAndFlush(ctx, Constants.BREAK_LINE.concat(RFUtil.makeStrFitPda(msg, "", 3)));
        accepterMap.put(GOTO_KEY, colName);
    }
}
