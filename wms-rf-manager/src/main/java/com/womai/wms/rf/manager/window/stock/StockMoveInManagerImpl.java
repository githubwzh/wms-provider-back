package com.womai.wms.rf.manager.window.stock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.*;
import com.womai.wms.rf.domain.stock.StockMoveIn;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.base.WarehouseInfoRemoteService;
import com.womai.wms.rf.remote.instock.InstockReasonRemoteService;
import com.womai.wms.rf.remote.stock.StockInfoRemoteService;
import com.womai.wms.rf.remote.stock.StockMoveRemoteService;
import com.womai.zlwms.rfsoa.api.WMSErrorMess;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.base.BaseWarehouseinfo;
import com.womai.zlwms.rfsoa.domain.stock.StockMove;
import com.womai.zlwms.rfsoa.domain.stock.StockMoveItem;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassDescribe:RF移入
 * Author :wangzhanhua
 * Date: 2016-08-23
 * Since
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component(Constants.STOCK_MOVE_IN_MANAGER)
public class StockMoveInManagerImpl extends ReceiveManager {
    public final static String[] TABLE_NAME = {"序号", "推荐移入库位", "推荐移入数量BU", " 生产日期 ","商品状态", "单位"};//明细表头
    public final static String[] TABLE_COLUMN = {"recomwhcode", "movebu", "productiondate","skuStatus", "unitname"};//明细列名
    public final static String MOVE_ORDER_CODE = "moveOrderCode";//移位单号
    public final static String BARCODE = "barcode";//商品条码
    private static final String MOVE_ITEM_PAGE_NUM = "selectItemNo";//选择库存序号
    public final static String MOVEIN_BU = "moveInBu";//移位数量BU
    public final static String WAREHOUSE_CODE = "warehouseCode";//移入库位
    public final static String REG = "^[1-9]\\d{0,9}$";
    private StockMove currStockMove;//当前主单对象
    private StockMoveItem currStockMoveItem;//当前操作的质检明细
    private boolean isToMenu;//跳转到主菜单
    private boolean isToInitial;//跳转到初始页面
    private boolean hasNotRFStockMove = true;//没有RF创建的移位单
    private ChannelHandlerContext ctx;
    // 创建移位单界面
    private String[] pageHeader = {Constants.BREAK_LINE, TipConstants.STOCK_MOVE_IN, Constants.SPLIT, ""};
    @Autowired
    private WarehouseInfoRemoteService warehouseInfoRemoteService;
    @Autowired
    private StockMoveRemoteService stockMoveRemoteService;
    @Autowired
    private StockInfoRemoteService stockInfoRemoteService;
    @Autowired
    private InstockReasonRemoteService instockReasonRemoteService;
    private boolean isRefreshCurrBarcode;//刷新该商品的分页列表
    private boolean isReScanBarcode;//重新扫描商品条码

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        super.initBaseMap(StockMoveIn.class, pageHeader, ctx);
        //查询RF移出完成的(激活状态，或上架中)，并且RF创建的移位单
        RemoteResult<StockMove> result = stockMoveRemoteService.getStockMoveCreatedByRF(getCredentialsVO(ctx));
        if (result.isSuccess()) {
            StockMove stockMove = result.getT();
            if (stockMove != null) {
                //显示RF创建的移位单号
                setAndPrintDefaultValue(stockMove.getShelfcode(), getDataMap(), ctx);
                currStockMove = stockMove;
                hasNotRFStockMove = false;
                channelRead(ctx, Constants.BREAK_LINE);
                resetCurCol(BARCODE, getDataMap(), ctx);
            }else{
                hasNotRFStockMove = true;
            }
        } else {
            systemErrorAnyKeyContinue(result.getResultCode());
        }
    }


    /**
     * 创建移位单
     *
     * @param ctx handler对象
     * @param msg 每次接收到的字节数据
     * @throws Exception 抛出异常
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Map<String, Object> accepterMap = getDataMap();
        if (isToMenu) {//例如：有激活后未完成的移位单
            toMenuWindow(ctx);
            return;
        }
        if (isToInitial) {
            isToInitial = false;
            channelActive(ctx);
            return;
        }
        if (isReScanBarcode) {//某一商品，全部移入完成后，重新扫描商品条码
            isReScanBarcode = false;
            List<String> showStrings = CollectionUtil.newList(MOVE_ORDER_CODE);
            List<String> clearStrings = CollectionUtil.newList(BARCODE, MOVE_ITEM_PAGE_NUM, WAREHOUSE_CODE, MOVEIN_BU);
            printFieldsAndReceiveData(pageHeader, showStrings, BARCODE, clearStrings, getDataMap(), ctx);
            initParaForShowPage();
            return;
        }
        receiveDataAndNotPrintNext(ctx, msg, accepterMap);
        StockMoveIn stockMoveIn = getStockMoveIn();
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (MOVE_ORDER_CODE.equals(lastCompleteColName) && hasNotRFStockMove) {//扫描移位单号
            String moveCode = getStockMoveIn().getMoveOrderCode();
            RemoteResult<StockMove> result = stockMoveRemoteService.queryStockMoveByCode(getCredentialsVO(ctx), moveCode);
            if (result.isSuccess()) {
                StockMove stockMove = result.getT();
                if (stockMove.getCreateType() == WmsConstants.STOCK_CREATETYPE_RF) {//扫描的单号，为他人RF创建
                    NoPassValidate(MOVE_ORDER_CODE, ErrorConstants.STOCK_MOVE_CREATE_TYPE_ERR);
                    return;
                }
                Integer orderStatus = stockMove.getOrderStatus();
                if (orderStatus != WmsConstants.STOCKMOVE_STATUS_VALID &&//激活状态
                        orderStatus != WmsConstants.STOCKMOVE_STATUS_SHELFING) {//移位中状态
                    NoPassValidate(MOVE_ORDER_CODE, TipConstants.STATUS_ERR);
                } else if(stockMove.getWorktype() == null){
                    NoPassValidate(MOVE_ORDER_CODE, TipConstants.STOCK_MOVEOUT_NO_ITEM_OUT);
                } else if(stockMove.getWorktype() != WmsConstants.INSTOCK_WORKTYPE_RF) {
                    NoPassValidate(MOVE_ORDER_CODE, TipConstants.WORK_TYPE_ERR);
                } else {
                    currStockMove = stockMove;
                    rePrintCurColTip(accepterMap, ctx);
                }
            } else {//系统异常，或业务异常（例如：1移位中状态的非同操作人，2激活状态非移出完成）
                NoPassValidate(MOVE_ORDER_CODE, result.getResultCode());
            }
        }
        if (BARCODE.equals(lastCompleteColName)) {
            showStockMoveItemsPage(ctx);
        }
        if (MOVE_ITEM_PAGE_NUM.equals(lastCompleteColName)) {
            processChoiceItem();
        }
        if (WAREHOUSE_CODE.equals(lastCompleteColName)) {
            validateWarehouse(stockMoveIn, accepterMap);
        }
        if (MOVEIN_BU.equals(lastCompleteColName) && validateQuantity()) {
            processStockMoveIn();
        }
        WMSDebugManager.debugLog("StockMoveInManagerImpl--Received:" + accepterMap);
        }

    /**
     * 提示推荐库位上是否存在相同的商品
     *
     * @param currStockMoveItem 当前操作的明细
     * @param accepterMap       接收参数，存储参数的容器
     */
    private void remindSameSku(StockMoveItem currStockMoveItem, Map<String, Object> accepterMap) {
        if (currStockMoveItem.getIssamesku() != null && WmsConstants.CON_YES == currStockMoveItem.getIssamesku()) {
            super.printBeforeNextField(TipConstants.IS_SAME_SKU, accepterMap, ctx);
        }
    }

    /**
     * 初始分页查询，所需的常量
     */
    private void initParaForShowPage() {
        Map<String, Object> accepterMap = getDataMap();
        accepterMap.remove(PageUtil.PARA_PAGE_MAP);//查询分页的Map
        accepterMap.remove(PageUtil.PAGE_MODEL);//存放分页模型
        accepterMap.remove(PageUtil.LINES_NUM_CLEAN_KEY);//翻页时，需要清除的数据行数
    }

    /**
     * 处理移入逻辑
     */
    private void processStockMoveIn() throws Exception {
        StockMoveIn stockMoveIn = getStockMoveIn();
        currStockMoveItem.setRealitywhcode(stockMoveIn.getWarehouseCode());
        currStockMoveItem.setYnbu(Integer.parseInt(stockMoveIn.getMoveInBu()));
        currStockMoveItem.setYnnum(currStockMoveItem.getYnbu() / currStockMoveItem.getPknum());
        if (currStockMoveItem.getYnbu().compareTo(currStockMoveItem.getCanMoveInBu()) == 0) {//所有剩余的都移入
            currStockMoveItem.setIsFinishedItem(true);
        }
        if(currStockMove.getOrdertype().equals(WmsConstants.STOCKMOVE_ORDERTYPE_CZZ)){//残转正，清空原因列表
            currStockMoveItem.setReasonid(null);
            currStockMoveItem.setReasonContent(null);
        }
        RemoteResult<StockMove> remoteResult = stockMoveRemoteService.processStockMoveIn(getCredentialsVO(ctx), currStockMove, currStockMoveItem);
        if (remoteResult.isSuccess()) {
            Map<String, Object> accepterMap = getDataMap();
            currStockMove = remoteResult.getT();
            if (currStockMove.getOrderStatus() == WmsConstants.STOCKMOVE_STATUS_FINISH) {
                //该单全部完成
                systemSuccessAnyKeyContinue(TipConstants.STOCK_MOVE_IN_ALL_BARCODE_COMPLETE);
                isToMenu = false;
                isToInitial = true;
                return;
            }
            if (currStockMoveItem.isFinishedItem()) {//该明细全部移入，刷新分页列表 ，同行号所有明细移入完成
                List<String> showStrings = CollectionUtil.newList(MOVE_ORDER_CODE, BARCODE);
                List<String> clearStrings = CollectionUtil.newList(MOVE_ITEM_PAGE_NUM, WAREHOUSE_CODE, MOVEIN_BU);
                printFieldsAndReceiveData(pageHeader, showStrings, MOVE_ITEM_PAGE_NUM, clearStrings, accepterMap, ctx);
                HandlerUtil.delLeft(ctx);
                HandlerUtil.moveUpN(ctx, 1);
                this.isRefreshCurrBarcode = true;
                showStockMoveItemsPage(ctx);
            } else {//继续扫描库位
                currStockMoveItem.setVersion(currStockMove.getDetailVersion());//更新明细版本号
                currStockMoveItem.setCanMoveInBu(currStockMoveItem.getCanMoveInBu() - currStockMoveItem.getYnbu());
                //输出移位单号，商品条码,明细分页，如果一条明细，不显示”选择序号“
                List<String> showStrings = CollectionUtil.newList(MOVE_ORDER_CODE, BARCODE);
                List<String> clearStrings = CollectionUtil.newList(WAREHOUSE_CODE, MOVEIN_BU);
                printFieldsAndReceiveData(pageHeader, showStrings, WAREHOUSE_CODE, clearStrings, accepterMap, ctx);
                HandlerUtil.moveUpN(ctx, 1);
                HandlerUtil.delLeft(ctx);
                HandlerUtil.moveUpN(ctx, 1);
                HandlerUtil.changeRow(ctx);
                PageModel<StockMoveItem> pageModelItem = (PageModel<StockMoveItem>) accepterMap.get(PageUtil.PAGE_MODEL);
                PageUtil.showTable(ctx, pageModelItem, TABLE_NAME, TABLE_COLUMN, true, true, null);//展示列表，带有序号
                if (pageModelItem.getTotalCount() > 1) {
                    HandlerUtil.print(ctx, TipConstants.STOCK_MOVE_IN_SELECT + getStockMoveIn().getSelectItemNo());
                } else {
                    HandlerUtil.moveUpN(ctx, 1);
                }
                remindSameSku(currStockMoveItem, accepterMap);
                rePrintCurColTip(accepterMap, ctx);
                channelRead(ctx, Constants.BREAK_LINE);
            }

        } else {
            if (remoteResult.getResultCode().equals(WMSErrorMess.ERROR_SYSTEM)) {//不是系统错误，跳转到初始页面
                systemErrorAnyKeyContinue(remoteResult.getResultCode());
            } else {
                systemErrorAnyKeyContinue(remoteResult.getResultCode());
                isToMenu = false;
                isToInitial = true;
            }
        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 校验可移位数量Bu
     */
    private boolean validateQuantity() {
        String planBuStr = getStockMoveIn().getMoveInBu();//输入的移位数量BU
        if (planBuStr.matches(REG)) {
            Integer canMoveBu = this.currStockMoveItem.getCanMoveInBu();//选择的库存对应的可移位数量BU
            Integer planBu = Integer.parseInt(planBuStr);
            if (canMoveBu.compareTo(planBu) < 0) {
                NoPassValidate(MOVEIN_BU, ErrorConstants.PLS_INPUT_CAN_MOVE_NUM);
                return false;
            } else if (planBu % currStockMoveItem.getPknum() != 0) {
                NoPassValidate(MOVEIN_BU, ErrorConstants.PLS_INPUT_RIGHT_NUM);
                return false;
            } else {
                return true;
            }
        } else {
            NoPassValidate(MOVEIN_BU, ErrorConstants.INPUT_FORMAT_ERROR);
            return false;
        }
    }

    /**
     * 获得接收参数的对象
     *
     * @return
     */
    private StockMoveIn getStockMoveIn() {
        return (StockMoveIn) getDataMap().get(DefaultKey.objectClass.keyName);
    }

    /**
     * 发生错误，任意键继续
     *
     * @param errMsg
     */
    private void systemErrorAnyKeyContinue(String errMsg) {
        HandlerUtil.delLeft(ctx);
        HandlerUtil.changeRow(ctx);
        HandlerUtil.errorBeep(ctx);
        HandlerUtil.print(ctx, errMsg + ErrorConstants.TIP_TO_CONTINUE);
        isToMenu = true;
    }
    /**
     * 发生错误，任意键继续
     *
     * @param errMsg
     */
    private void systemSuccessAnyKeyContinue(String errMsg) {
        HandlerUtil.delLeft(ctx);
        HandlerUtil.changeRow(ctx);
        HandlerUtil.print(ctx, errMsg + ErrorConstants.TIP_TO_CONTINUE);
        isToMenu = true;
    }

    /**
     * 某商品移入完成，任意键继续
     *
     * @param errMsg
     */
    private void reScanBarcode(String errMsg) {
        HandlerUtil.changeRow(ctx);
        HandlerUtil.print(ctx, errMsg + ErrorConstants.TIP_TO_CONTINUE);
        isRefreshCurrBarcode = false;
        isReScanBarcode = true;
    }

    /**
     * 处理属性值校验不通过
     *
     * @param fieldName 属性名
     * @param errMsg    错误信息
     */
    private void NoPassValidate(String fieldName, String errMsg) {
        colNeedReInput(fieldName, errMsg, getDataMap(), ctx);
    }

    /**
     * 可移位库存分页列表
     *
     * @param ctx
     */
    private void showStockMoveItemsPage(ChannelHandlerContext ctx) {
        Map<String, Object> accepterMap = getDataMap();
        RemoteResult<PageModel<StockMoveItem>> pageModelRemoteResult = stockMoveRemoteService.queryStockMoveItemsPage(getCredentialsVO(ctx), getParaMap());
        if (pageModelRemoteResult.isSuccess()) {//展示库存分页列表，一定有数据
            PageModel<StockMoveItem> stockMoveItemPageModel = pageModelRemoteResult.getT();
            accepterMap.put(PageUtil.PAGE_MODEL, stockMoveItemPageModel);
            HandlerUtil.changeRow(ctx);
            int currPageLinesNum = PageUtil.showTable(ctx, stockMoveItemPageModel, TABLE_NAME, TABLE_COLUMN, true, true, null);//展示列表，带有序号
            accepterMap.put(PageUtil.LINES_NUM_CLEAN_KEY, currPageLinesNum);
            HandlerUtil.moveUpN(ctx, 1);
            if (stockMoveItemPageModel.getTotalCount() == 1) {
                //跳过选择明细
                this.currStockMoveItem = stockMoveItemPageModel.getDatas().get(0);
                remindSameSku(currStockMoveItem, accepterMap);
                resetCurCol(WAREHOUSE_CODE, accepterMap, ctx);
            } else {
                rePrintCurColTip(accepterMap, ctx);
            }
        } else {//输出错误信息（无数据或异常）
            if (isRefreshCurrBarcode) {//该商品所有明细全部移入完成后，该标示会为true
                //继续扫描商品条码
                reScanBarcode(TipConstants.STOCK_MOVE_IN_CURR_BARCODE_COMPLETE);
            } else {
                NoPassValidate(BARCODE, pageModelRemoteResult.getResultCode());
                //清理查询参数map
                accepterMap.remove(PageUtil.PARA_PAGE_MAP);
            }
        }
    }


    /**
     * 获得移位单明细分页查询条件
     */
    private HashMap<String, Object> getParaMap() {
        Map<String, Object> accepterMap = getDataMap();
        HashMap<String, Object> map = (HashMap<String, Object>) accepterMap.get(PageUtil.PARA_PAGE_MAP);//缓存中的分页查询参数Map
        if (map == null) {
            final StockMoveItem item = new StockMoveItem();
            item.setPage(Constants.PAGE_START);
            item.setRows(Constants.STOCK_MOVE_DETAIL_PAGE_SIZE);
            StockMoveIn stockMoveIn = getStockMoveIn();
            item.setShelfid(currStockMove.getShelfid());//主单id
            item.setBarcode(stockMoveIn.getBarcode());//商品条码
            map = new HashMap<String, Object>() {{
                put(WmsConstants.KEY_STOCKMOVE_DETAIL_PARAM, item);
            }};
            accepterMap.put(PageUtil.PARA_PAGE_MAP, map);
        }
        return map;
    }

    /**
     * 校验库位
     *
     * @param stockMoveIn 接收参数对象
     * @param accepterMap
     */
    private void validateWarehouse(StockMoveIn stockMoveIn, Map<String, Object> accepterMap) {
        //校验移入移出是否同库位
        String inputWarehouseCode = stockMoveIn.getWarehouseCode();
        CredentialsVO credentialsVO = getCredentialsVO(ctx);
        Map<String, BaseWarehouseinfo> baseWareMap = warehouseInfoRemoteService.getBaseWarehouseInfoByCodes(credentialsVO, currStockMoveItem.getSrcwhscode(), stockMoveIn.getWarehouseCode());
        if (baseWareMap == null) {
            //提示数据不存在
            NoPassValidate(WAREHOUSE_CODE, ErrorConstants.DATA_NOT_FOUNT);
            return;
        }
        BaseWarehouseinfo baseWareIn = baseWareMap.get(WmsConstants.STOCKMOVE_REALWHSCODE);//实际移入库位
        BaseWarehouseinfo baseWareOut = baseWareMap.get(WmsConstants.STOCKMOVE_SRCWHSCODE);//实际移出库位
        if (baseWareIn != null && baseWareOut != null) {
            //校验库位类型
            boolean passValidate = warehouseInfoRemoteService.validateWarehouseMoveIn(currStockMove.getOrdertype(), baseWareOut.getWarehousetype(),
                    baseWareIn.getWarehousetype(), currStockMoveItem.getSkuStatus());
            if (passValidate) {
                //扫描的库位，符合所选择的移位单类型
                //显示待移入数量Bu
                RemoteResult<Integer> remoteResult = stockMoveRemoteService.getCanMoveInBuSameSerialid(getCredentialsVO(ctx), currStockMoveItem);
                if(remoteResult.isSuccess()){
                    printBeforeNextField(TipConstants.STOCK_MOVE_IN_UNDONE_NUM + remoteResult.getT(), accepterMap, ctx);
                    rePrintCurColTip(accepterMap, ctx);
                }else{
                    NoPassValidate(WAREHOUSE_CODE, "查询明细可移入数量BU异常");
                }
            } else {
                //库位校验不通过
                NoPassValidate(WAREHOUSE_CODE, ErrorConstants.ILLEGAL_DATA);
            }
        } else {
            //提示数据不存在
            NoPassValidate(WAREHOUSE_CODE, ErrorConstants.DATA_NOT_FOUNT);

        }
    }

    /**
     * 处理选择明细,只有选择明细后，返回true
     */
    private boolean processChoiceItem() {
        //组装数据
        String pageNum = getStockMoveIn().getSelectItemNo();
        Map<String, Object> accepterMap = getDataMap();
        PageModel<StockMoveItem> pageModle = (PageModel<StockMoveItem>) accepterMap.get(PageUtil.PAGE_MODEL);
        int pageSizeCurr = (Integer) accepterMap.get(PageUtil.LINES_NUM_CLEAN_KEY);//页面当前数据条数
        if (KeyEnum.N_78.value.equalsIgnoreCase(pageNum)) {//下一页
            PageUtil.changePageNext(ctx, accepterMap, WmsConstants.KEY_STOCKMOVE_DETAIL_PARAM, pageSizeCurr);
            setColUnReceived(MOVE_ITEM_PAGE_NUM, accepterMap);
            showStockMoveItemsPage(ctx);
            return false;
        } else if (KeyEnum.B_66.value.equalsIgnoreCase(pageNum)) {//上一页
            PageUtil.changePageUp(ctx, accepterMap, WmsConstants.KEY_STOCKMOVE_DETAIL_PARAM, pageSizeCurr);
            setColUnReceived(MOVE_ITEM_PAGE_NUM, accepterMap);
            showStockMoveItemsPage(ctx);
            return false;
        } else {//序号
            if (pageNum.matches(TipConstants.REG_SERIAL_NO)) {
                List<StockMoveItem> items = pageModle.getDatas();
                int maxIndex = items.size() - 1;
                int index = PageUtil.getIndexFromSerialnoAndPageModle(pageNum, pageModle);//(pn-1)*size+index+1==serialno
                if (index > maxIndex || index < 0) {
                    NoPassValidate(MOVE_ITEM_PAGE_NUM, ErrorConstants.INPUT_FORMAT_ERROR);
                    return false;
                } else {
                    //正确选择原库存，保存参数
                    StockMoveItem stockMoveItem = items.get(index);
                    this.currStockMoveItem = stockMoveItem;
                    remindSameSku(currStockMoveItem, accepterMap);//提示推荐库位是否存在相同商品
                    rePrintCurColTip(accepterMap, ctx);
                    return true;
                }
            } else {
                NoPassValidate(MOVE_ITEM_PAGE_NUM, ErrorConstants.INPUT_FORMAT_ERROR);
                return false;
            }
        }
    }
}
