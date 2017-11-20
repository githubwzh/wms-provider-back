package com.womai.wms.rf.manager.window.stock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.*;
import com.womai.wms.rf.domain.stock.CreateStockMove;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.base.WarehouseInfoRemoteService;
import com.womai.wms.rf.remote.instock.InstockReasonRemoteService;
import com.womai.wms.rf.remote.stock.StockInfoRemoteService;
import com.womai.wms.rf.remote.stock.StockMoveRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.base.BaseWarehouseinfo;
import com.womai.zlwms.rfsoa.domain.instock.InstockReason;
import com.womai.zlwms.rfsoa.domain.stock.StockInfo;
import com.womai.zlwms.rfsoa.domain.stock.StockMove;
import com.womai.zlwms.rfsoa.domain.stock.StockMoveItem;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassDescribe:创建移位单
 * Author :wangzhanhua
 * Date: 2016-08-16
 * Since
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component(Constants.STOCK_MOVE_CREATE_MANAGER)
public class CreateStockMoveManagerImpl extends ReceiveManager {
    public final static String[] STOCK_TABLE_NAME = {"序号", "箱规", "    库位    ", " 状态 ", "库存BU", "可移位BU", "单位", "包装规格", "上架BU", "拣货BU", " 生产日期 "};//库存表头
    public final static String[] STOCK_TABLE_COLUMN = {"pknum", "warehousecode", "skuStatus", "stocknum", "canMoveBu", "unitname", "spec", "putawaynum", "pickoutnum", "productiondate"};//库存列名
    public final static String[] REASON_TABLE_NAME = {"序号", "原因内容                              "};//原因列表头
    public final static String[] REASON_TABLE_COLUMN = {"remark"};//原因列表头
    public final static String ORDER_TYPE = "orderType";//移位单类型
    public final static String WAREHOUSECODE = "warehouseCode";//库位编码
    public final static String BARCODE = "barcode";//商品条码
    public final static String PLANBU = "planBu";//移位数量BU
    private static final String SELECT_STOCK_INFO_NO = "selectStockInfoNo";//选择库存序号
    private static final String SELECT_REASON_NO = "selectReasonNo";//选择原因序号
    private static final String ACTIVE_ORDER = "0";//激活指令
    public final static String REG = "^[1-9]\\d{0,9}$";
    public final static int STOCKINFO_PAGE_TYPE = 0;//库存分页
    public final static int INSTOCKREASON_PAGE_TYPE = 1;//原因分页
    private ChannelHandlerContext ctx;
    private Integer orderType;
    private StockInfo stockInfo;
    private InstockReason instockReason;
    private StockMove currStockMove;//当前主单对象
    private BaseWarehouseinfo currWareOut;//扫描的移出库位
    private boolean isToMenu;//跳转到主菜单
    private boolean isToInitial;//跳转到初始页面
    private final String[] pageHeader = {Constants.BREAK_LINE, TipConstants.STOCK_MOVE_CREATE, Constants.SPLIT, ""};
    @Autowired
    private WarehouseInfoRemoteService warehouseInfoRemoteService;
    @Autowired
    private StockMoveRemoteService stockMoveRemoteService;
    @Autowired
    private StockInfoRemoteService stockInfoRemoteService;
    @Autowired
    private InstockReasonRemoteService instockReasonRemoteService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        // 创建移位单界面
        super.initBaseMap(CreateStockMove.class, pageHeader, ctx);
        //查询当前有没有未完成的移位单
        List<StockMove> stockMoves = getStockMovesBySameOperator();
        if (stockMoves == null || stockMoves.isEmpty()) {
            selectOrderType(ctx, getDataMap());//1、选择移位单类型
        } else if (stockMoves.size() == 1) {
            StockMove stockMove = stockMoves.get(0);
            if (stockMove.getOrderStatus() < WmsConstants.STOCKMOVE_STATUS_VALID) {//生效
                currStockMove = stockMove;
                showOrderTypeForContinue();//存在一个未激活的移位单，需要展示该移位单类型
            } else if (stockMove.getWorktype() == null || stockMove.getWorktype() == WmsConstants.INSTOCK_WORKTYPE_RF) {//生效及之后，操作方式RF,或者为null时提示
                systemErrorAnyKeyContinue(ErrorConstants.STOCK_MOVE_UNFIN_EXIST + stockMove.getShelfcode());
                isToMenu = true;
            } else {
                selectOrderType(ctx, getDataMap());//1、选择移位单类型
            }
        } else {
            systemErrorAnyKeyContinue(ErrorConstants.STOCK_MOVE_UNFIN_EXIST_MORE);
            isToMenu = true;
        }
    }

    /**
     * 输出展示，已经存在的未激活的移位单类型
     */
    private void showOrderTypeForContinue() {
        Map<String, Object> accepterMap = getDataMap();
        String orderTypeTip = Constants.StockMoveOrderTypeEnum.getNameByValue(currStockMove.getOrdertype());
        getCreateStockMove().setOrderType(orderTypeTip);//接收参数的对象，第一个属性“移位单类型”赋值。
        HandlerUtil.delLeft(ctx);
        HandlerUtil.moveUpN(ctx, 1);
        HandlerUtil.changeRow(ctx);
        HandlerUtil.errorBeep(ctx);
        HandlerUtil.print(ctx, TipConstants.STOCK_MOVE_UNDONE_TASK + Constants.BREAK_LINE + currStockMove.getShelfcode());
        rePrintCurColTip(accepterMap, ctx);
        HandlerUtil.println(ctx, orderTypeTip);//显示移位类型
        resetCurCol(WAREHOUSECODE, accepterMap, ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
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
            channelActive(ctx);
            isToInitial = false;
            return;
        }
        receiveDataAndNotPrintNext(ctx, msg, accepterMap);
        CreateStockMove createStockMove = getCreateStockMove();
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (ORDER_TYPE.equals(lastCompleteColName)) {//选择移位单类型后，输出“移出库位：”
            rePrintCurColTip(accepterMap, ctx);
        }
        if (WAREHOUSECODE.equals(lastCompleteColName)) {//2、扫描库位接收完成，校验库位
            String wareHouseNo = createStockMove.getWarehouseCode();
            if (!ACTIVE_ORDER.equals(wareHouseNo) && currStockMove != null && (currStockMove.getOrderStatus() == WmsConstants.STOCKMOVE_STATUS_ALLOTPART ||
                    currStockMove.getOrderStatus() == WmsConstants.STOCKMOVE_STATUS_ALLOTALL)) {//主单为部分分配或者全部分配只能激活
                noPassValidate(WAREHOUSECODE, ErrorConstants.STOCK_MOVE_ALREDY_DISTR);
                return;
            }
            if (ACTIVE_ORDER.equals(wareHouseNo)) {
                //激活
                activateStockMove();
                return;
            }
            validateWarehouse(createStockMove, accepterMap);
        }
        if (BARCODE.equals(lastCompleteColName)) {
            //分页查询可移位库存信息
            showStockInfosPage(ctx);
        }
        if (SELECT_STOCK_INFO_NO.equals(lastCompleteColName)) {
            if (chooseStockInfo()) {
                judgeTypeForShowReason(false);
            }
        }
        if (SELECT_REASON_NO.equals(lastCompleteColName)) {
            chooseReason();//选择原因
        }
        if (PLANBU.equals(lastCompleteColName) && validateQuantity()) {
            processStockMovePrimaryProcedure();
        }
        WMSDebugManager.debugLog("CreateStockMoveManagerImpl--Received:" + accepterMap);
    }

    /**
     * 校验库位
     *
     * @param createStockMove 接收参数对象
     * @param accepterMap
     */
    private void validateWarehouse(CreateStockMove createStockMove, Map<String, Object> accepterMap) {
        BaseWarehouseinfo baseWarehouseinfo = warehouseInfoRemoteService.getBaseWarehouseInfoByCode(getCredentialsVO(ctx), createStockMove.getWarehouseCode());
        if (baseWarehouseinfo == null) {//提示数据不存在
            noPassValidate(WAREHOUSECODE, ErrorConstants.DATA_NOT_FOUNT);
        } else {
            //根据用户选择，获得对应的移位类型。如：选择的“普通移位”对应类型0
            orderType = Constants.StockMoveOrderTypeEnum.getValueByName(createStockMove.getOrderType());
            //校验库位类型
            boolean passValidate = warehouseInfoRemoteService.validateWarehouse(baseWarehouseinfo, orderType);
            if (passValidate) {
                //扫描的库位，符合所选择的移位单类型
                rePrintCurColTip(accepterMap, ctx);
                currWareOut = baseWarehouseinfo;
            } else {
                //库位校验不通过
                noPassValidate(WAREHOUSECODE, ErrorConstants.ILLEGAL_DATA);
            }
        }
    }

    /**
     * 根据移位单类型，选择是否显示原因
     *
     * @param flag 一条库存可以选择
     */
    private void judgeTypeForShowReason(boolean flag) {
        //如果是正转残，列出原因列表
        if (isNormalToDamager()) {
            showInstockReasonsPage(ctx);
            HandlerUtil.delLeft(ctx);
            HandlerUtil.moveUpN(ctx, 1);
            resetCurCol(SELECT_REASON_NO, getDataMap(), ctx);
        } else {
            //跳过选择原因
            if (flag) {
                HandlerUtil.moveUpN(ctx, 1);
            }
            resetCurCol(PLANBU, getDataMap(), ctx);
        }
    }

    /**
     * 判断移位单类型是否为正转残
     */
    private boolean isNormalToDamager() {
        return orderType == Constants.StockMoveOrderTypeEnum.normalToDamage.value;
    }

    /**
     * 所有参数接收完毕，处理主要逻辑业务
     */
    private void processStockMovePrimaryProcedure() {
        List<StockMove> stockMoves = getStockMovesBySameOperator();
        if (stockMoves == null || stockMoves.isEmpty()) {
            //无未完成的移位单，创建移位单
            StockMove stockMoveNew = new StockMove();
            stockMoveNew.setOrdertype(orderType);
            processSaveStockMoveItem(stockMoveNew);
        } else if (stockMoves.size() == 1) {
            currStockMove = stockMoves.get(0);
            if (currStockMove.getOrderStatus().compareTo(WmsConstants.STOCKMOVE_STATUS_ALLOTPART) < 0) {//小于部分分配状态，继续添加
                processSaveStockMoveItem(null);
            } else {//未完成移位单
                systemErrorAnyKeyContinue(ErrorConstants.STOCK_MOVE_UNFIN_EXIST + currStockMove.getShelfcode());
            }
        } else {
            //含有多个未完成的移位单，不符合业务，任意键继续
            systemErrorAnyKeyContinue(ErrorConstants.STOCK_MOVE_UNFIN_EXIST_MORE);
        }
    }

    /**
     * 返回到扫描库位
     */
    private void backToScanWareHouseCode() {
        List<String> showStrings = CollectionUtil.newList(ORDER_TYPE);
        List<String> clearStrings = CollectionUtil.newList(WAREHOUSECODE, SELECT_STOCK_INFO_NO, BARCODE, PLANBU, SELECT_REASON_NO);
        printFieldsAndReceiveData(pageHeader, showStrings, WAREHOUSECODE, clearStrings, getDataMap(), ctx);
        initParaForShowPage();
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
     * 查询同一个创建人，RF创建方式，未完成移位的所有主单
     *
     * @return
     */
    private List<StockMove> getStockMovesBySameOperator() {
        //过滤掉激活后操作方式为WEB的单子
        List<StockMove> stockMoves = stockMoveRemoteService.queryStockMovesUnfinished(getCredentialsVO(ctx));
        List<StockMove> stockMoveForRemove = new ArrayList<StockMove>();
        if (stockMoves != null && stockMoves.size() > 0) {
            for (StockMove stockMove : stockMoves) {
                if (stockMove.getOrderStatus() > WmsConstants.STOCKMOVE_STATUS_VALID && stockMove.getWorktype() == WmsConstants.INSTOCK_WORKTYPE_WEB) {
                    stockMoveForRemove.add(stockMove);
                }
            }
        }
        stockMoves.removeAll(stockMoveForRemove);
        return stockMoves;
    }

    /**
     * 属性值接收跳转时，初始后面的属性
     *
     * @param size    该属性上面的属性个数，即已经完成接收的个数
     * @param strings 要清空的属性名字
     */
    private void setReceivedBase(int size, String[] strings) {
        Map<String, Object> accepterMap = getDataMap();
        accepterMap.put(DefaultKey.curColName.keyName, strings[0]);//跳转到，将要接收数据的属性名
        accepterMap.put(DefaultKey.lastCompleteColName.keyName, "");
        accepterMap.put(DefaultKey.completeSize.keyName, size);
        for (String columnName : strings) {
            Reflections.invokeSetter(accepterMap.get(DefaultKey.objectClass.keyName), columnName, "");
        }
    }

    /**
     * 添加明细
     *
     * @param stockMoveNew
     */
    private void processSaveStockMoveItem(StockMove stockMoveNew) {

        StockMoveItem item = new StockMoveItem();
        String planBuStr = getCreateStockMove().getPlanBu();
        Integer planBu = Integer.parseInt(planBuStr);
        item.setSkuid(stockInfo.getSkuid());
        item.setSkuStatus(stockInfo.getSkuStatus());
        item.setProductiondate(stockInfo.getProductiondate());
        item.setNearvaliddate(stockInfo.getNearvalid());
        item.setExpirationdate(stockInfo.getExpirationdate());
        item.setPkid(stockInfo.getPkid());
        item.setUnitname(stockInfo.getUnitname());
        item.setSpec(stockInfo.getSpec());
        item.setPknum(stockInfo.getPknum());
        item.setSrcwhscode(stockInfo.getWarehousecode());
        item.setPalletcode(stockInfo.getSalver());//托盘
        item.setPlanbu(planBu);
        item.setPlannum(planBu / stockInfo.getPknum());
        item.setStockid(stockInfo.getStockid());
        item.setPklevel(stockInfo.getPacklevel());
        item.setInstockcode(stockInfo.getInstockcode());
        item.setStockInfoVersion(stockInfo.getVersion());//库存version 维护库存时防止库存并发，此字段不存数据库
        if (isNormalToDamager()) {
            //正转残，重新赋值原因
            item.setReasonid(instockReason.getId());
            item.setReasonContent(instockReason.getRemark());
        } else {
            item.setReasonid(stockInfo.getReasonid());
            item.setReasonContent(stockInfo.getReasoncontent());
        }
        if (stockMoveNew == null) {//主单数据库已经存在，currStockMove 此时非空
            item.setShelfid(currStockMove.getShelfid());
            item.setStockMoveVersion(currStockMove.getVersion());
        }
        RemoteResult<StockMove> stockMoveRemoteResult = stockMoveRemoteService.saveStockMoveItem(getCredentialsVO(ctx), item, stockMoveNew);
        if (stockMoveRemoteResult.isSuccess()) {
            if (stockMoveNew != null) {//第一次添加主单时
                currStockMove = stockMoveRemoteResult.getT();
            } else {
                currStockMove.setVersion(stockMoveRemoteResult.getT().getVersion());//更新主单版本号
            }
            //继续操作
            backToScanWareHouseCode();
        } else {
            systemErrorAnyKeyContinue(stockMoveRemoteResult.getResultCode());
        }
    }

    /**
     * 发生错误，任意键继续
     *
     * @param errMsg
     */
    private void systemErrorAnyKeyContinue(String errMsg) {
        HandlerUtil.delLeft(ctx);
        HandlerUtil.changeRow(ctx);
        HandlerUtil.print(ctx, errMsg + ErrorConstants.TIP_TO_CONTINUE);
        HandlerUtil.errorBeep(ctx);
        isToInitial = true;
    }


    /**
     * 校验可移位数量Bu
     */
    private boolean validateQuantity() {
        String planBuStr = getCreateStockMove().getPlanBu();//输入的移位数量BU
        if (planBuStr.matches(REG)) {
            Integer canMoveBu = stockInfo.getCanMoveBu();//选择的库存对应的可移位数量BU
            Integer planBu = Integer.parseInt(planBuStr);
            if (canMoveBu.compareTo(planBu) < 0) {
                noPassValidate(PLANBU, ErrorConstants.PLS_INPUT_CAN_MOVE_NUM);
                return false;
            } else if (planBu % stockInfo.getPknum() != 0) {
                noPassValidate(PLANBU, ErrorConstants.PLS_INPUT_RIGHT_NUM);
                return false;
            } else {
                return true;
            }
        } else {
            noPassValidate(PLANBU, ErrorConstants.INPUT_FORMAT_ERROR);
            return false;
        }
    }

    /**
     * 激活移位单
     */
    private void activateStockMove() {
        if (currStockMove == null) {
            noPassValidate(WAREHOUSECODE, TipConstants.STOCK_MOVE_QUERY_NO_DATA);
            return;
        }
        RemoteResult<String> result = stockMoveRemoteService.activateStockMove(getCredentialsVO(ctx), currStockMove);
        if (result.isSuccess()) {
            //激活成功，跳转移出菜单
            forward(Constants.STOCK_MOVE_OUT_MANAGER, ctx);
            return;
        } else {
            noPassValidate(WAREHOUSECODE, result.getT());
        }
    }

    /**
     * 处理选择原因
     */
    private void chooseReason() {
        //组装数据
        String pageNum = getCreateStockMove().getSelectReasonNo();
        Map<String, Object> accepterMap = getDataMap();
        PageModel<InstockReason> pageModle = (PageModel<InstockReason>) accepterMap.get(PageUtil.PAGE_MODEL);
        int pageSizeCurr = (Integer) accepterMap.get(PageUtil.LINES_NUM_CLEAN_KEY);//页面当前数据条数
        if (KeyEnum.N_78.value.equalsIgnoreCase(pageNum)) {//下一页
            PageUtil.changePageNext(ctx, accepterMap, WmsConstants.KEY_INSTOCKREASON_PARRAM, pageSizeCurr);
            setColUnReceived(SELECT_REASON_NO, accepterMap);
            showInstockReasonsPage(ctx);
        } else if (KeyEnum.B_66.value.equalsIgnoreCase(pageNum)) {//上一页
            PageUtil.changePageUp(ctx, accepterMap, WmsConstants.KEY_INSTOCKREASON_PARRAM, pageSizeCurr);
            setColUnReceived(SELECT_REASON_NO, accepterMap);
            showInstockReasonsPage(ctx);
        } else {//序号
            if (pageNum.matches(REG)) {
                List<InstockReason> instockReasons = pageModle.getDatas();
                int maxIndex = instockReasons.size() - 1;
                int index = PageUtil.getIndexFromSerialnoAndPageModle(pageNum, pageModle);//(pn-1)*size+index+1==serialno
                if (index > maxIndex || index < 0) {
                    noPassValidate(SELECT_REASON_NO, ErrorConstants.INPUT_FORMAT_ERROR);
                } else {
                    //正确选择原因，保存参数
                    instockReason = instockReasons.get(index);
                    HandlerUtil.changeRow(ctx);
                    HandlerUtil.write(ctx, TipConstants.STOCK_MOVE_DAMAGE_REASON + instockReason.getRemark());
                    rePrintCurColTip(accepterMap, ctx);
                }
            } else {
                noPassValidate(SELECT_REASON_NO, ErrorConstants.INPUT_FORMAT_ERROR);
            }
        }
    }

    /**
     * 处理选择要移位的库存,只有选择明细后，返回true
     */
    private boolean chooseStockInfo() {
        //组装数据
        String pageNum = getCreateStockMove().getSelectStockInfoNo();
        Map<String, Object> accepterMap = getDataMap();
        PageModel<StockInfo> pageModle = (PageModel<StockInfo>) accepterMap.get(PageUtil.PAGE_MODEL);
        int pageSizeCurr = (Integer) accepterMap.get(PageUtil.LINES_NUM_CLEAN_KEY);//页面当前数据条数
        if (KeyEnum.N_78.value.equalsIgnoreCase(pageNum)) {//下一页
            PageUtil.changePageNext(ctx, accepterMap, WmsConstants.KEY_STOCKINFO_PARAM, pageSizeCurr);
            setColUnReceived(SELECT_STOCK_INFO_NO, accepterMap);
            showStockInfosPage(ctx);
            return false;
        } else if (KeyEnum.B_66.value.equalsIgnoreCase(pageNum)) {//上一页
            PageUtil.changePageUp(ctx, accepterMap, WmsConstants.KEY_STOCKINFO_PARAM, pageSizeCurr);
            setColUnReceived(SELECT_STOCK_INFO_NO, accepterMap);
            showStockInfosPage(ctx);
            return false;
        } else {//序号
            if (pageNum.matches(REG)) {
                List<StockInfo> stockInfos = pageModle.getDatas();
                int maxIndex = stockInfos.size() - 1;
                int index = PageUtil.getIndexFromSerialnoAndPageModle(pageNum, pageModle);//(pn-1)*size+index+1==serialno
                if (index > maxIndex || index < 0) {
                    noPassValidate(SELECT_STOCK_INFO_NO, ErrorConstants.INPUT_FORMAT_ERROR);
                    return false;
                } else {
                    //正确选择原库存，保存参数
                    StockInfo stockInfo = stockInfos.get(index);
                    if (stockInfo.getCanMoveBu() < 1) {
                        noPassValidate(SELECT_STOCK_INFO_NO, ErrorConstants.STOCKINFO_MOVE_NUM_NONEXISTENCE);
                        return false;
                    }
                    if (this.validateSkuStatus(stockInfo.getSkuStatus())) {
                        this.stockInfo = stockInfo;
                        initParaForShowPage();//初始 分页查询库存的参数，因为下面要展示原因列表
                        return true;
                    } else {
                        noPassValidate(SELECT_STOCK_INFO_NO, ErrorConstants.STOCK_MOVE_SKUSTATUS_ERR);
                        return false;
                    }
                }
            } else {
                noPassValidate(SELECT_STOCK_INFO_NO, ErrorConstants.INPUT_FORMAT_ERROR);
                return false;
            }
        }
    }

    /**
     * 获得接收参数的对象
     *
     * @return
     */
    private CreateStockMove getCreateStockMove() {
        return (CreateStockMove) getDataMap().get(DefaultKey.objectClass.keyName);
    }

    /**
     * 可移位库存分页列表
     *
     * @param ctx
     */
    private void showStockInfosPage(ChannelHandlerContext ctx) {
        Map<String, Object> accepterMap = getDataMap();
        RemoteResult<PageModel<StockInfo>> pageModelRemoteResult = stockInfoRemoteService.queryStockInfoPageList(getCredentialsVO(ctx), getParaMap(STOCKINFO_PAGE_TYPE));
        if (pageModelRemoteResult.isSuccess()) {
            //展示库存分页列表，一定有数据
            PageModel<StockInfo> instockReasonPageModel = pageModelRemoteResult.getT();
            accepterMap.put(PageUtil.PAGE_MODEL, instockReasonPageModel);
            HandlerUtil.changeRow(ctx);
            List<StockInfo> stockInfos = instockReasonPageModel.getDatas();
            String skuName = stockInfos.get(0).getSkuname();
            int currPageLinesNum = PageUtil.showTable(ctx, instockReasonPageModel, STOCK_TABLE_NAME, STOCK_TABLE_COLUMN, true, true, TipConstants.SKU_NAME + skuName);//展示列表，带有序号
            accepterMap.put(PageUtil.LINES_NUM_CLEAN_KEY, currPageLinesNum);
            if (instockReasonPageModel.getTotalCount() == 1) {
                //校验该库存商品状态（例如：正转残，不可以是冻结商品）
                StockInfo stockInfo = stockInfos.get(0);
                if (this.validateSkuStatus(stockInfo.getSkuStatus())) {
                    //跳过选择库存
                    this.stockInfo = stockInfo;
                    initParaForShowPage();
                    judgeTypeForShowReason(true);
                } else {
                    HandlerUtil.changeRow(ctx);
                    systemErrorAnyKeyContinue(ErrorConstants.STOCK_MOVE_SKUSTATUS_ERR);
                }
            } else {
                HandlerUtil.moveUpN(ctx, 1);
                rePrintCurColTip(accepterMap, ctx);
            }
        } else {//输出错误信息（无数据或异常）
            HandlerUtil.changeRow(ctx);
            systemErrorAnyKeyContinue(ErrorConstants.STOCKINFO_EMPTY);
        }
    }

    /**
     * 根据移位单类型，移出库位类型，校验移出的商品状态
     *
     * @param skuStatus 商品状态
     * @return
     */
    private boolean validateSkuStatus(Integer skuStatus) {
        return warehouseInfoRemoteService.validateSkuStatus(orderType, currWareOut.getWarehousetype(), skuStatus);
    }

    /**
     * 原因分页列表
     *
     * @param ctx
     */
    private void showInstockReasonsPage(ChannelHandlerContext ctx) {
        Map<String, Object> accepterMap = getDataMap();
        RemoteResult<PageModel<InstockReason>> pageModelRemoteResult = instockReasonRemoteService.getInstockReasonPageList(getCredentialsVO(ctx), getParaMap(INSTOCKREASON_PAGE_TYPE));
        if (pageModelRemoteResult.isSuccess()) {
            //展示原因内容列表，一定有数据
            PageModel<InstockReason> instockReasonPageModel = pageModelRemoteResult.getT();
            accepterMap.put(PageUtil.PAGE_MODEL, instockReasonPageModel);
            HandlerUtil.changeRow(ctx);
            int currPageLinesNum = PageUtil.showTable(ctx, instockReasonPageModel, REASON_TABLE_NAME, REASON_TABLE_COLUMN, true, true, null);//展示列表，带有序号
            accepterMap.put(PageUtil.LINES_NUM_CLEAN_KEY, currPageLinesNum);
            HandlerUtil.moveUpN(ctx, 1);
            rePrintCurColTip(accepterMap, ctx);
        } else {//输出错误信息（无数据或异常）
            accepterMap.remove(PageUtil.PARA_PAGE_MAP);
            systemErrorAnyKeyContinue(ErrorConstants.PLS_MAINTAION_MOVE_REASON);
        }
    }

    /**
     * 获得分页查询条件
     *
     * @param type 0 库存分页，1 原因分页
     */
    private HashMap<String, Object> getParaMap(int type) {
        Map<String, Object> accepterMap = getDataMap();
        HashMap<String, Object> map = (HashMap<String, Object>) accepterMap.get(PageUtil.PARA_PAGE_MAP);//缓存中的分页查询参数Map
        if (map == null) {
            if (type == STOCKINFO_PAGE_TYPE) {
                map = setStockInfoParaMap(accepterMap, map);
            } else if (type == INSTOCKREASON_PAGE_TYPE) {
                map = setInstockReasonParaMap(accepterMap, map);
            }
        }
        return map;
    }

    /**
     * 原因分页查询，人参Map
     *
     * @param accepterMap
     * @param map
     */
    private HashMap<String, Object> setInstockReasonParaMap(Map<String, Object> accepterMap, HashMap<String, Object> map) {
        final InstockReason instockReason = new InstockReason();
        instockReason.setPage(Constants.PAGE_START);
        instockReason.setRows(Constants.REASON_PAGE_SIZE);
        instockReason.setSidx(Constants.PAGE_REASON_SIDX);
        instockReason.setSord(Constants.PAGE_SORT_DESC);
        instockReason.setReasonStatus(WmsConstants.STATUS_ENABLE);//生效
        instockReason.setReasonType(CheckReasonEnum.damaged.value);
        map = new HashMap<String, Object>() {{
            put(WmsConstants.KEY_INSTOCKREASON_PARRAM, instockReason);
        }};
        accepterMap.put(PageUtil.PARA_PAGE_MAP, map);
        return map;
    }

    /**
     * 库存分页查询，入参Map
     *
     * @param accepterMap
     * @param map
     */
    private HashMap<String, Object> setStockInfoParaMap(Map<String, Object> accepterMap, HashMap<String, Object> map) {
        final StockInfo stockInfo = new StockInfo();
        stockInfo.setPage(Constants.PAGE_START);
        stockInfo.setRows(Constants.STOCK_INFO_PAGE_SIZE);
        stockInfo.setSidx(Constants.PAGE_STOCK_INFO_SIDX);
        stockInfo.setSord(Constants.PAGE_SORT_DESC);
        CreateStockMove createStockMove = getCreateStockMove();
        stockInfo.setWarehousecode(createStockMove.getWarehouseCode());//库位编码
        stockInfo.setBarcode(createStockMove.getBarcode());//商品条码
        map = new HashMap<String, Object>() {{
            put(WmsConstants.KEY_STOCKINFO_PARAM, stockInfo);
        }};
        accepterMap.put(PageUtil.PARA_PAGE_MAP, map);
        return map;
    }

    /**
     * 处理属性值校验不通过
     *
     * @param fieldName 属性名
     * @param errMsg    错误信息
     */
    private void noPassValidate(String fieldName, String errMsg) {
        colNeedReInput(fieldName, errMsg, getDataMap(), ctx);
    }

    /**
     * 选择移位单类型，供用户左右键选择
     *
     * @param accepterMap 存储页面，及查询数据的参数
     */
    private void selectOrderType(ChannelHandlerContext ctx, Map<String, Object> accepterMap) throws Exception {
        List<String> list = Constants.StockMoveOrderTypeEnum.getNameList();
        setNextColSwitchList(list, accepterMap, ctx);
    }
}
