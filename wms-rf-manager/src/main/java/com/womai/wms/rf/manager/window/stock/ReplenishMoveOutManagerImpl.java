package com.womai.wms.rf.manager.window.stock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.*;
import com.womai.wms.rf.domain.stock.ReplenishMoveOut;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.base.PackaginginfoRemoteService;
import com.womai.wms.rf.remote.stock.ReplenishMoveOutRemoteService;
import com.womai.zlwms.rfsoa.api.WMSErrorMess;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.base.BasePackaginginfo;
import com.womai.zlwms.rfsoa.domain.stock.StockReplenishItem;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassDescribe: RF补货移出业务Handler
 * Author :Xiafei Qi
 * Date: 2016-09-28
 * Since
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component(Constants.REPLENISH_MOVEOUT_MANAGER)
public class ReplenishMoveOutManagerImpl extends ReceiveManager {
    @Autowired
    private ReplenishMoveOutRemoteService replenishMoveOutRemoteService;
    @Autowired
    private PackaginginfoRemoteService packaginginfoRemoteService;

    // =====业务流程字段名===//
    private static final String SHELFCODE = "shelfcode";// 补货单号
    private static final String SRC_WHS_CODE = "srcWhsCode";// 原库位编码
    private static final String BAR_CODE = "barCode";// 商品条码
    private static final String SELECT_SERIAL_NO = "selectSerialNo";// 选择明细列表序号
    private static final String MOVEOUT_BU = "moveoutBu";// 移出数量bu
    // ====补货单明细列表常量===//
    private final static String[] TABLE_HEADERS = {TipConstants.PAGEHEADER_SERIAL_NO,
            TipConstants.REPLENISH_MOVEOUT_DISTRNUM_LABEL, TipConstants.REPLENISH_MOVEOUT_DISTRBU_LABEL,
            TipConstants.REPLENISH_MOVEOUT_UNITNAME_LABEL,TipConstants.REPLENISH_MOVEOUT_BARCODE_LABEL};// 列表标题
    private final static String[] TABLE_COLUMNS = {WmsConstants.KEY_DISTRNUM_PARAM,
            WmsConstants.KEY_DISTRBU_PARAM, WmsConstants.KEY_UNITNAME_PARAM,
            WmsConstants.KEY_BASE_BARCODE};// 标题对应的pageModel中的字段名，不需要序号一列

    // ====本地线程变量===//
    private Long localShelfId = 0L; // 补货单id
    private String localsrcwhscode = "";// 库位编码
    private String localBarCode = "";// 商品条码
    private StockReplenishItem localSelectedReplenishItem;// 选择的明细
    private Long skuidForChangePage;//临时保存，翻页时，入参

    // ====任意键跳转key-value===//
    private static final String GOTO_KEY = "replenishMoveOutManagerGotoKey";
    private static final String GOTO_CHANNELACTIVE = "channelActive";// GOTO到channelActive的value


    // 界面标题:pageHeader中无论放的什么内容，每个数组元素占一行。若需要换行不要使用\r\n，使用一个新的数组元素。
    private static final String[] PAGEHEADER = {"", TipConstants.REPLENISH_MOVEOUT_TITLE, Constants.SPLIT, ""};

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        resetLocals();// 初始化时清空本地变量内容
        initBaseMap(ReplenishMoveOut.class, PAGEHEADER, ctx);// 重新初始化界面，重置已接受数据，打印标题
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object inputObj) throws Exception {
        Map<String, Object> accepterMap = getDataMap(); // ReceiveManager中记录输入状态的Map
        // 处理跳转请求，GOTO的值代表要跳转到的字段名，该字段前的字段继续打印在屏幕上，从该字段开始接受值
        if (goToHandler(accepterMap, ctx)) {
            return;
        }
        receiveDataAndNotPrintNext(ctx, inputObj, accepterMap); // 接收输入不自动打印下一个字段
        ReplenishMoveOut replenishMoveOut = (ReplenishMoveOut) accepterMap.get(DefaultKey.objectClass.keyName);// 当前业务对象
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);//上一个回车所在的字段

        if (SHELFCODE.equals(lastCompleteColName)) { // 如果是扫描补货单号
            String shelfCode = replenishMoveOut.getShelfcode(); // 补货单号
            // 校验补货单号是否正确
            RemoteResult<Long> result = replenishMoveOutRemoteService.validShelfCodeAndReturnShelfId(getCredentialsVO(ctx), shelfCode);
            if (!result.isSuccess()) {
                // 如果校验不通过，把问题原因提示给用户,重新接收移位单号
                colNeedReInput(lastCompleteColName, result.getResultCode(), accepterMap, ctx);
                return;
            }
            localShelfId = result.getT();// 刷新本地变量保存的补货单id
            resetCurCol(SRC_WHS_CODE, accepterMap, ctx);// 打印下一字段
        } else if (SRC_WHS_CODE.equals(lastCompleteColName)) {

            String srcWhsCode = replenishMoveOut.getSrcWhsCode();
            // 校验库位是否正确
            StockReplenishItem condition = new StockReplenishItem();
            condition.setSplitshelfid(localShelfId);//查询条件补货子单id
            condition.setSrcwhscode(srcWhsCode);//查询条件库位编码
            RemoteResult<String> validShelfCodeForMoveOutResult = replenishMoveOutRemoteService.validSrcWhsCode(getCredentialsVO(ctx), condition);//验证库位编码是否合法
            if (!validShelfCodeForMoveOutResult.isSuccess()) {
                // 如果校验不通过，把问题原因提示给用户
                colNeedReInput(lastCompleteColName, validShelfCodeForMoveOutResult.getResultCode(), accepterMap, ctx);
                return;
            }
            localsrcwhscode = srcWhsCode;// 刷新本地变量保存的库位编码
            resetCurCol(BAR_CODE, accepterMap, ctx);// 打印下一字段

        } else if (BAR_CODE.equals(lastCompleteColName)) {
            String barCode = replenishMoveOut.getBarCode();
            if (Constants.KEY_BACK.equals(barCode)) { // 如果用户输入了回退关键字，则退回到扫描库存编码
                localsrcwhscode = ""; // 清除本地库位编码
                List<String> fieldList = CollectionUtil.newGenericList(SHELFCODE);
                printFieldsAndReceiveData(PAGEHEADER, fieldList, SRC_WHS_CODE, accepterMap, ctx);// 回退到扫描库位编码
                return;
            }
            //接下来需要根据补货单号、库位编码、商品条码决定接下来的行为
            handleAfterBarCode(barCode, accepterMap, ctx);

        } else if (SELECT_SERIAL_NO.equals(lastCompleteColName)) {
            String selectSerialNo = replenishMoveOut.getSelectSerialNo(); // 用户输入
            @SuppressWarnings("unchecked")
            PageModel<StockReplenishItem> pageModel = (PageModel<StockReplenishItem>) accepterMap.get(PageUtil.PAGE_MODEL);
            int pageSizeCurr = (Integer) accepterMap.get(PageUtil.LINES_NUM_CLEAN_KEY);//页面当前数据条数
            if (KeyEnum.N_78.value.equalsIgnoreCase(selectSerialNo)) {//下一页
                if (pageModel.getTotalPageNum() == pageModel.getPageNum()) {// 如果已经是最后一页，提示重新输入
                    colNeedReInput(lastCompleteColName, ErrorConstants.LAST_PAGE, accepterMap, ctx);
                    return;
                }
                PageUtil.changePageNext(ctx, accepterMap, WmsConstants.KEY_STOCKREPLENISH_DETAIL_PARAM, pageSizeCurr - 1);
                showPage(accepterMap, ctx);// 打印分页
                setColUnReceived(lastCompleteColName, accepterMap);
                resetCurCol(lastCompleteColName, accepterMap, ctx);
            } else if (KeyEnum.B_66.value.equalsIgnoreCase(selectSerialNo)) {//上一页
                if (pageModel.getPageNum() == 1) {// 如果已经是第一页，提示重新输入
                    colNeedReInput(lastCompleteColName, ErrorConstants.FIRST_PAGE, accepterMap, ctx);
                    return;
                }
                PageUtil.changePageUp(ctx, accepterMap, WmsConstants.KEY_STOCKREPLENISH_DETAIL_PARAM, pageSizeCurr - 1);
                showPage(accepterMap, ctx);// 打印分页
                setColUnReceived(lastCompleteColName, accepterMap);
                resetCurCol(lastCompleteColName, accepterMap, ctx);
            } else {
                if (selectSerialNo.matches(TipConstants.REG_SERIAL_NO)) {//大于0的正整数
                    List<StockReplenishItem> replenishItems = pageModel.getDatas();
                    int maxIndex = replenishItems.size() - 1;
                    int index = PageUtil.getIndexFromSerialnoAndPageModle(selectSerialNo, pageModel);
                    if (index > maxIndex || index < 0) {//只能输入当前页内显示的序号
                        colNeedReInput(lastCompleteColName, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                    } else {
                        // 打印确认提示
                        skuidForChangePage = null;
                        localSelectedReplenishItem = replenishItems.get(index);
                        String tip = TipConstants.REPLENISH_MOVEOUT_SELECTITEM_CONFIRM_HINT + (replenishItems.get(index).getUnmoveoutbu());
                        printBeforeNextField(tip, accepterMap, ctx);
                        resetCurCol(MOVEOUT_BU, accepterMap, ctx);
                    }
                } else {
                    colNeedReInput(lastCompleteColName, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                }
            }
        } else if (MOVEOUT_BU.equals(lastCompleteColName)) {
            String moveoutBu = replenishMoveOut.getMoveoutBu();
            if (!moveoutBu.matches(TipConstants.REG_SERIAL_NO)) {//若输入不是大于0的正整数
                colNeedReInput(lastCompleteColName, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                return;
            }
            StockReplenishItem selectedStockReplenishItem = localSelectedReplenishItem;// 选择明细，包括只有一条明细的情况
            int moveoutBuIntValue = Integer.parseInt(moveoutBu);// 输入的移出bu的int值
//            int notMoveoutBu = selectedStockReplenishItem.getDistrbu() - selectedStockReplenishItem.getMovebu();//未移出数量bu
            int notMoveoutBu = selectedStockReplenishItem.getUnmoveoutbu();//未移出数量bu
            if (moveoutBuIntValue > notMoveoutBu) { // 如果想移出比未移出数量bu还大的值肯定是不行的
                colNeedReInput(lastCompleteColName, ErrorConstants.PLS_INPUT_CAN_MOVE_NUM, accepterMap, ctx);
                return;
            }
            //按照源库位包装级别进行校验
            BasePackaginginfo packaginginfo = packaginginfoRemoteService.getPackagingInfoById(getCredentialsVO(ctx), selectedStockReplenishItem.getSrcpkid());
            if (packaginginfo == null) {
                colNeedReInput(lastCompleteColName, "未查询到源库位数据", accepterMap, ctx);
                return;
            }
 /*           if (packaginginfo.getPacklevel().equals(WmsConstants.PACK_LEVEL_TWO) && moveoutBuIntValue % packaginginfo.getPknum() != 0) { TODO 箱柜数量
                colNeedReInput(lastCompleteColName, ErrorConstants.PLS_INPUT_RIGHT_NUM, accepterMap, ctx);
                return;
            }*/
            // 提交并获得返回值
            /**
             * 1、若移出后当前补货单下全部移出，则提示“当前补货单已全部移出，任意键继续”，跳转到页面初始界面。
             * 2、若移出后当前补货单下未全部移出，但无可操作明细，则提示“操作成功，任意键继续”，跳转到页面初始界面。
             * 3、若当前库位全部移出，但是还存在其他货位未全部移出，则提示“当前库位已全部移出，任意键继续”，跳转到扫描库位。
             * 4、若当前库位未全部移出，但无可操作明细，则提示“操作成功，任意键继续”，跳转到扫描库位。
             * 5、若当前库位未全部移出，并且有可操作明细，则提示“操作成功，任意键继续”，跳转到扫描商品条码。
             */
            RemoteResult<Integer> result = replenishMoveOutRemoteService.submitAndGetReturnCode(getCredentialsVO(ctx), selectedStockReplenishItem, moveoutBuIntValue);
            if (!result.isSuccess()) {
                printMessageWaitAnyKeyToCol(result.getResultCode(), GOTO_CHANNELACTIVE, accepterMap, ctx);
                return;
            }
            // 提交后处理
            handlerAfterSubmit(result.getT(), accepterMap, ctx);
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
        localShelfId = 0L;
        localsrcwhscode = "";
        localSelectedReplenishItem = null;
        localBarCode = "";
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
            if (goTo.equals(GOTO_CHANNELACTIVE)) { // 重新初始化界面
                channelActive(ctx);
                return true;
            }
            if (goTo.equals(SRC_WHS_CODE)) { // 库位编码
                localsrcwhscode = ""; // 清除本地库位编码
                localBarCode = "";
                localSelectedReplenishItem = null; // 清除本地已选择明细
                removePageKey(accepterMap); // 清除分页相关数据
                List<String> fieldList = CollectionUtil.newGenericList(SHELFCODE);
                printFieldsAndReceiveData(PAGEHEADER, fieldList, goTo, accepterMap, ctx);// 回退到扫描库位编码
                return true;
            }
            if (goTo.equals(BAR_CODE)) {// 商品条码
                localBarCode = "";
                localSelectedReplenishItem = null; // 清除本地已选择明细
                removePageKey(accepterMap); // 清除分页相关数据
                List<String> fieldList = CollectionUtil.newGenericList(SHELFCODE, SRC_WHS_CODE);
                printFieldsAndReceiveData(PAGEHEADER, fieldList, goTo, accepterMap, ctx);// 回退到扫描商品条码
                return true;
            }
        }
        return false;
    }

    /**
     * 把商品条码装入分页数据
     *
     * @param page 分页数据
     */
    private void putBarCodeInPage(PageModel<StockReplenishItem> page) {
        for (StockReplenishItem item : page.getDatas()) {
            item.setBarcode(localBarCode);
        }
    }


    /**
     * 清除分页相关的key
     *
     * @param accepterMap 接收信息映射表
     */
    private void removePageKey(Map<String, Object> accepterMap) {
        accepterMap.remove(PageUtil.PARA_PAGE_MAP); // 清除分页查询条件
        accepterMap.remove(PageUtil.PAGE_MODEL); // 清除分页对象
    }

    /**
     * 扫描商品条码后的处理
     *
     * @param barCode     商品条码
     * @param accepterMap 接收信息map
     * @param ctx         netty上下文
     */
    private void handleAfterBarCode(String barCode, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        // 校验并查询可补货明细分页数据
        RemoteResult<PageModel<StockReplenishItem>> result = replenishMoveOutRemoteService
                .validBarCodeAndGetItemPage(getCredentialsVO(ctx), makePageQueryConditionMap(accepterMap), barCode);
        if (!result.isSuccess()) {
            // 如果校验不通过，把问题原因提示给用户
            colNeedReInput(BAR_CODE, result.getResultCode(), accepterMap, ctx);
            return;
        }
        PageModel<StockReplenishItem> page = result.getT();
        if (page.getTotalCount() == 0) { // 商品下全部移出
            // 如果查询结果长度为0
            colNeedReInput(BAR_CODE, ErrorConstants.REPLENISH_NO_DATA, accepterMap, ctx);
            return;
        }
        localBarCode = barCode;// 本地保存一份商品条码
        if (page.getTotalCount() == 1) {// 如果明细只有一条跳过分页选择，直接打印信息
            localSelectedReplenishItem = page.getDatas().get(0); // 本地保存一个补货单明细对象
            printBeforeSingleItem(accepterMap, ctx);
            resetCurCol(MOVEOUT_BU, accepterMap, ctx);
        } else {
            //多条数据进行分页显示
            skuidForChangePage = page.getDatas().get(0).getSkuid();
            accepterMap.put(PageUtil.PAGE_MODEL, page); // 将分页对象放入map
            putBarCodeInPage(page);// 将条形码放入分页数据
            HandlerUtil.changeRow(ctx);// 另起一行
            int currPageLinesNum = PageUtil.showTable(ctx, page, TABLE_HEADERS, TABLE_COLUMNS, true, true, null);//展示列表，带有序号
            accepterMap.put(PageUtil.LINES_NUM_CLEAN_KEY, currPageLinesNum);// 当前页的行数，可能由于数据多自动换行
            HandlerUtil.moveUpN(ctx, 1);//因为分页工具中已经输出了回车，所以手动上移一行
            setColUnReceived(SELECT_SERIAL_NO, accepterMap);
            resetCurCol(SELECT_SERIAL_NO, accepterMap, ctx);
        }
    }


    /**
     * 构造分页查询条件并放入accpterMap
     *
     * @param accepterMap 接收信息映射表
     * @return 查询条件Map
     */
    private HashMap<String, Object> makePageQueryConditionMap(Map<String, Object> accepterMap) {
        @SuppressWarnings("unchecked")
        HashMap<String, Object> map = (HashMap<String, Object>) accepterMap.get(PageUtil.PARA_PAGE_MAP);//缓存中的分页查询参数Map
        if (map == null) {
            final StockReplenishItem condition = new StockReplenishItem();
            condition.setPage(Constants.PAGE_START); // 初始化页数
            condition.setRows(Constants.REASON_PAGE_SIZE); // 初始化表格行数
            map = new HashMap<String, Object>() {{
                put(WmsConstants.KEY_STOCKREPLENISH_DETAIL_PARAM, condition);
            }};
            accepterMap.put(PageUtil.PARA_PAGE_MAP, map);
        }
        ((StockReplenishItem) map.get(WmsConstants.KEY_STOCKREPLENISH_DETAIL_PARAM)).setSplitshelfid(localShelfId);// 补货单id
        ((StockReplenishItem) map.get(WmsConstants.KEY_STOCKREPLENISH_DETAIL_PARAM)).setSrcwhscode(localsrcwhscode); // 库位编码
        if (skuidForChangePage != null) {
            ((StockReplenishItem) map.get(WmsConstants.KEY_STOCKREPLENISH_DETAIL_PARAM)).setSkuid(skuidForChangePage);
        }

        return map;
    }

    /**
     * 当只有一条明细时在显示字段之前打印一些信息
     *
     * @param accepterMap 用于保存接收信息的映射表
     * @param ctx         netty上下文
     */
    private void printBeforeSingleItem(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        StockReplenishItem stockReplenishItem = localSelectedReplenishItem;
//        String notMoveOutBu = String.valueOf(stockReplenishItem.getDistrbu() - stockReplenishItem.getMovebu());
        String notMoveOutBu = ""+stockReplenishItem.getUnmoveoutbu();
        List<String> tiplist = CollectionUtil.newGenericList(TipConstants.REPLENISH_MOVEOUT_DISTRNUM_LABEL, TipConstants.REPLENISH_MOVEOUT_DISTRBU_LABEL,
                TipConstants.REPLENISH_MOVEOUT_UNITNAME_LABEL, TipConstants.REPLENISH_MOVEOUT_SINGLEITEM_CONFIRM_HINT);
        List<String> valuelist = CollectionUtil.newGenericList(stockReplenishItem.getDistrnum().toString(), stockReplenishItem.getDistrbu().toString(),
                stockReplenishItem.getUnitname(), notMoveOutBu);
        super.printBeforeNextField(tiplist, valuelist, accepterMap, ctx);
    }

    private void showPage(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        RemoteResult<PageModel<StockReplenishItem>> result = replenishMoveOutRemoteService.getPage(getCredentialsVO(ctx), makePageQueryConditionMap(accepterMap));
        if (!result.isSuccess()) {
            printMessageWaitAnyKeyToCol(result.getResultCode(), GOTO_CHANNELACTIVE, accepterMap, ctx);
            return;
        }
        //展示补货明细列表
        PageModel<StockReplenishItem> page = result.getT();
        if (page.getTotalCount() == 0) {
            printMessageWaitAnyKeyToCol(ErrorConstants.DATA_EXPIRED_CONTINUE, GOTO_CHANNELACTIVE, accepterMap, ctx);
            return;
        }
        accepterMap.put(PageUtil.PAGE_MODEL, page);  // 更新map中的分页数据
        putBarCodeInPage(page);// 将条形码放入分页数据
        int currPageLinesNum = PageUtil.showTable(ctx, page, TABLE_HEADERS, TABLE_COLUMNS, true, true, null);//展示列表，带有序号
        HandlerUtil.moveUpN(ctx, 1);//因为分页工具中已经输出了回车，所以手动上移一行
        accepterMap.put(PageUtil.LINES_NUM_CLEAN_KEY, currPageLinesNum);
    }

    /**
     * 打印一些信息，并且等待任意键跳转
     *
     * @param msg 要打印的信息
     * @param ctx netty上下文
     */
    private void printMessageWaitAnyKeyToCol(String msg, String colName, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        HandlerUtil.writeAndFlush(ctx, Constants.BREAK_LINE.concat(RFUtil.makeStrFitPda(msg, "", 3)));
        accepterMap.put(GOTO_KEY, colName);
    }

    /**
     * 移出提交后处理
     *
     * @param returnCode  提交后返回码
     *                    返回值分五种情况：
     *                    1、若移出后当前补货单下全部移出，则提示“当前补货单已全部移出，任意键继续”，跳转到页面初始界面。
     *                    2、若移出后当前补货单下未全部移出，但无可操作明细，则提示“操作成功，任意键继续”，跳转到页面初始界面。
     *                    3、若当前库位全部移出，但是还存在其他货位未全部移出，则提示“当前库位已全部移出，任意键继续”，跳转到扫描库位。
     *                    4、若当前库位未全部移出，但无可操作明细，则提示“操作成功，任意键继续”，跳转到扫描库位。
     *                    5、若当前库位未全部移出，并且有可操作明细，则提示“操作成功，任意键继续”，跳转到扫描商品条码。
     * @param accepterMap 接收信息映射表
     * @param ctx         netty上下文
     */
    private void handlerAfterSubmit(int returnCode, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        switch (returnCode) {
            case WMSErrorMess.REPLENISHMOVEOUT_STATUSCODE_ALLOUT:
                // 打印成功信息,并等待用户任意键继续，退回到初始
                printMessageWaitAnyKeyToCol(TipConstants.REPLENISH_MOVEOUT_SHELFCODE_ALLOUT, GOTO_CHANNELACTIVE, accepterMap, ctx);
                break;
            case WMSErrorMess.REPLENISHMOVEOUT_STATUSCODE_NOBUCANOPER:
                // 打印成功信息,并等待用户任意键继续，退回到初始
                printMessageWaitAnyKeyToCol(TipConstants.REPLENISH_MOVEOUT_SUCCESS, GOTO_CHANNELACTIVE, accepterMap, ctx);
                break;
            case WMSErrorMess.REPLENISHMOVEOUT_STATUSCODE_CONTAINS_UNMOVEOUT_WHS:
                // 打印成功信息,并等待用户任意键继续，退回到库位编码输入
                printMessageWaitAnyKeyToCol(TipConstants.REPLENISH_MOVEOUT_SRCWHSCODE_ALLOUT, SRC_WHS_CODE, accepterMap, ctx);
                break;
            case WMSErrorMess.REPLENISHMOVEOUT_STATUSCODE_WHS_NOBUCANOPER:
                // 打印成功信息,并等待用户任意键继续，退回到库位编码输入
                printMessageWaitAnyKeyToCol(TipConstants.REPLENISH_MOVEOUT_SUCCESS, SRC_WHS_CODE, accepterMap, ctx);
                break;
            case WMSErrorMess.REPLENISHMOVEOUT_STATUSCODE_CONTAINS_UNMOVEOUT_SKU:
                // 打印成功信息,并等待用户任意键继续，退回到商品条码输入
                printMessageWaitAnyKeyToCol(TipConstants.REPLENISH_MOVEOUT_SUCCESS, BAR_CODE, accepterMap, ctx);
                break;
        }
    }
}
