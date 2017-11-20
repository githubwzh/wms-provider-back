package com.womai.wms.rf.manager.window.stock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.PageUtil;
import com.womai.wms.rf.common.util.Reflections;
import com.womai.wms.rf.common.util.WMSDebugManager;
import com.womai.wms.rf.domain.stock.FreezeStockInfo;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.base.WarehouseInfoRemoteService;
import com.womai.wms.rf.remote.instock.InstockReasonRemoteService;
import com.womai.wms.rf.remote.stock.StockInfoRemoteService;
import com.womai.zlwms.rfsoa.api.WMSErrorMess;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.base.BaseWarehouseinfo;
import com.womai.zlwms.rfsoa.domain.instock.InstockReason;
import com.womai.zlwms.rfsoa.domain.stock.StockInfo;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassDescribe:冻结
 * Author :wangzhanhua
 * Date: 2016-09-26
 * Since
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component("freezeStockInfoManager")
public class FreezeStockInfoManagerImpl extends ReceiveManager {
    private final static String[] STOCK_TABLE_NAME = {"序号", "库存数量BU", "可操作BU", "包装单位", "箱规", " 生产日期 ", " 失效日期 ", " 状态 "};//库存表头
    private final static String[] STOCK_TABLE_COLUMN = {"stocknum", "canMoveBu", "unitname", "pknum", "productiondate", "expirationdate", "skuStatus",};//库存列名
    private final static String[] REASON_TABLE_NAME = {"序号", "原因内容                              "};//原因列表头
    private final static String[] REASON_TABLE_COLUMN = {"remark"};//原因列表头
    private final static String WAREHOUSECODE = "warehousecode";//库位编码
    private final static String BARCODE = "barcode";//商品条码
    private final static String STOCKNUM = "stocknum";//冻结数量BU
    private static final String SELECT_STOCK_INFO_NO = "selectStockInfoNo";//选择库存序号
    private static final String SELECT_REASON_NO = "selectReasonNo";//选择原因序号
    private final static int STOCKINFO_PAGE_TYPE = 0;//库存分页
    private final static int INSTOCKREASON_PAGE_TYPE = 1;//原因分页
    private StockInfo stockInfo;
    private InstockReason instockReason;
    private boolean isToMenu;//跳转到主菜单
    private boolean isToInitial;//跳转到初始页面
    private Long whsid;//库位id
    @Autowired
    private StockInfoRemoteService stockInfoRemoteService;
    @Autowired
    private InstockReasonRemoteService instockReasonRemoteService;
    @Autowired
    private WarehouseInfoRemoteService warehouseInfoRemoteService;
    private ChannelHandlerContext ctx;
    private final String[] pageHeader = {Constants.BREAK_LINE, TipConstants.STOCK_INFO_FREEZE, Constants.SPLIT, ""};
    private StockInfo stockInfoPara;//ic 在途数提示后，选择Y继续冻结时，入参
    private boolean validateIC;//是否校验ic在途数，提示后，输入“Y"确认冻结时，无需在校验

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        // 冻结界面
        super.initBaseMap(FreezeStockInfo.class, pageHeader, ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 冻结
     *
     * @param ctx handler对象
     * @param msg 每次接收到的字节数据
     * @throws Exception 抛出异常
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (isToMenu) {
            toMenuWindow(ctx);
            return;
        }
        if (isToInitial) {
            channelActive(ctx);
            isToInitial = false;
            return;
        }
        Map<String, Object> accepterMap = getDataMap();
        receiveDataAndNotPrintNext(ctx, msg, accepterMap);
        FreezeStockInfo freezeStockInfo = getFreezeStockInfo();
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (WAREHOUSECODE.equals(lastCompleteColName)) {
            BaseWarehouseinfo warehouseinfo = warehouseInfoRemoteService.getBaseWarehouseInfoByCode(getCredentialsVO(ctx), freezeStockInfo.getWarehousecode());
            if (warehouseinfo != null) {//校验库位类型
                Integer whsType = warehouseinfo.getWarehousetype();
                if (whsType.equals(WmsConstants.WHSTYPE_CODE_CUNHUO) || whsType.equals(WmsConstants.WHSTYPE_CODE_XURUXUCHU)) {//库位类型为存货或者虚入虚出
                    rePrintCurColTip(accepterMap, ctx);
                    this.whsid = warehouseinfo.getId();
                } else {
                    noPassValidate(WAREHOUSECODE, ErrorConstants.BASE_WAREHOUSE_INFO_TYPE_ERR);
                }
            } else {
                noPassValidate(WAREHOUSECODE, ErrorConstants.BASE_WAREHOUSE_INFO_STATUS_ERR);
            }
        }
        if (BARCODE.equals(lastCompleteColName)) {//扫描库位，商品条码后，展示可冻结库存列表
            //分页查询可冻结库存信息
            showStockInfosPage(ctx);
        }
        if (SELECT_STOCK_INFO_NO.equals(lastCompleteColName)) {//选择库存明细
            if (chooseStockInfo()) {
                showInstockReasonsPage(ctx);
            }
        }
        if (SELECT_REASON_NO.equals(lastCompleteColName)) {//选择冻结原因
            chooseReason();//选择原因
        }
        if (STOCKNUM.equals(lastCompleteColName)) {//校验可冻结数量bu
            if (toChannelActive(ctx)) {
                if (this.stockInfoPara != null) {
                    this.stockInfoPara = null;
                }
                return;
            }
            doFreezeStockInfo();
        }
        WMSDebugManager.debugLog("FreezeStockInfoManagerImpl--Received:" + accepterMap);
    }

    /**
     * 处理冻结业务
     *
     * @throws Exception
     */
    private void doFreezeStockInfo() throws Exception {
        FreezeStockInfo freezeStockInfo = getFreezeStockInfo();
        StockInfo argStockInfo;
        if (this.stockInfoPara != null) {
            if ("Y".equalsIgnoreCase(freezeStockInfo.getStocknum())) {
                argStockInfo = stockInfoPara;
                validateIC = false;//第二次提交冻结数量
            } else {
                colNeedReInput(STOCKNUM,"重新输入冻结数量BU",getDataMap(),ctx);
                this.stockInfoPara = null;
                return;
            }
        } else if (validateQuantity()) {
            validateIC = true;//第一次提交冻结数量，校验ic在途
            StockInfo stockInfoParam = new StockInfo();
            stockInfoParam.setStockid(stockInfo.getStockid());
            stockInfoParam.setStocknum(Integer.parseInt(freezeStockInfo.getStocknum()));
            stockInfoParam.setReasonid(instockReason.getId());
            stockInfoParam.setReasoncontent(instockReason.getRemark());
            argStockInfo = stockInfoParam;
        } else {
            return;
        }
        RemoteResult<Boolean> remoteResult = stockInfoRemoteService.freezeStockInfo(getCredentialsVO(ctx), argStockInfo, validateIC);
        if (remoteResult.isSuccess()) {
            friendlyMsgSucceedAnyKeyContinue(TipConstants.STOCK_INFO_FREEZE_SUCCESS, true);
        } else {
            Boolean retFlag = remoteResult.getT();
            String errMsg = remoteResult.getResultCode() == null ? "" : remoteResult.getResultCode();
            if (retFlag != null && retFlag) {//提示ic在途数
                colNeedReInput(STOCKNUM,errMsg+",要冻结数量"+argStockInfo.getStocknum()+"BU,Y继续，其它键重新输入冻结数量",getDataMap(),ctx);
                this.validateIC = false;
                this.stockInfoPara = argStockInfo;
                return;
            } else {
                friendlyMsgAnyKeyContinue((String) remoteResult.getResultCode(), true);
            }
        }
        stockInfoPara = null;
    }

    /**
     * 校验可冻结数量Bu
     */
    private boolean validateQuantity() {
        String operateStockInfoNum = getFreezeStockInfo().getStocknum();//输入的移位数量BU
        if (operateStockInfoNum.matches(TipConstants.REG_SERIAL_NO)) {
            Integer canMoveBu = stockInfo.getCanMoveBu();//选择的库存对应的可移位数量BU
            Integer planBu = Integer.parseInt(operateStockInfoNum);
            if (canMoveBu.compareTo(planBu) < 0) {
                noPassValidate(STOCKNUM, ErrorConstants.STOCK_INFO_FREEZE_NUM_BEYOND);
                return false;
            } else if (planBu % stockInfo.getPknum() != 0) {
                noPassValidate(STOCKNUM, ErrorConstants.PLS_INPUT_RIGHT_NUM);
                return false;
            } else {
                return true;
            }
        } else {
            noPassValidate(STOCKNUM, ErrorConstants.INPUT_FORMAT_ERROR);
            return false;
        }
    }

    /**
     * 获得接收参数的对象
     *
     * @return 冻结接收参数对象
     */
    private FreezeStockInfo getFreezeStockInfo() {
        return (FreezeStockInfo) getDataMap().get(DefaultKey.objectClass.keyName);
    }

    /**
     * 可冻结库存分页列表
     *
     * @param ctx
     */
    private void showStockInfosPage(ChannelHandlerContext ctx) {
        Map<String, Object> accepterMap = getDataMap();
        //根据库位，商品条码，查询可操作数bu为大于0的，商品状态为正品的库存
        RemoteResult<PageModel<StockInfo>> pageModelRemoteResult = stockInfoRemoteService.queryStockInfoPageList(getCredentialsVO(ctx), getParaMap(STOCKINFO_PAGE_TYPE));
        if (pageModelRemoteResult.isSuccess()) {
            //展示库存分页列表，一定有数据
            PageModel<StockInfo> stockInfoPageModel = pageModelRemoteResult.getT();
            accepterMap.put(PageUtil.PAGE_MODEL, stockInfoPageModel);
            HandlerUtil.changeRow(ctx);
            List<StockInfo> stockInfos = stockInfoPageModel.getDatas();
            String skuName = stockInfos.get(0).getSkuname();
            int currPageLinesNum = PageUtil.showTable(ctx, stockInfoPageModel, STOCK_TABLE_NAME, STOCK_TABLE_COLUMN, true, true, TipConstants.SKU_NAME + skuName);//展示列表，带有序号
            accepterMap.put(PageUtil.LINES_NUM_CLEAN_KEY, currPageLinesNum);
            if (stockInfoPageModel.getTotalCount() == 1) {
                //只有一条库存，跳过选择库存
                StockInfo stockInfo = stockInfos.get(0);
                //跳过选择库存
                this.stockInfo = stockInfo;
                accepterMap.remove(PageUtil.PARA_PAGE_MAP);//清空库存分页查询参数，因为下面还有分页查询冻结原因
                //展示原因选择原因
                resetCurCol(SELECT_REASON_NO, accepterMap, ctx);
                HandlerUtil.delLeft(ctx);//此时光标停留在”选择原因序号“的右侧，需要清除该行
                HandlerUtil.moveUpN(ctx, 2);//因为光标不是停留在最左侧，需要上移两行，回车后光标停留在最左侧输出
                HandlerUtil.changeRow(ctx);
                showInstockReasonsPage(ctx);
            } else {
                HandlerUtil.moveUpN(ctx, 1);//库存分页列表下面有一空行
                rePrintCurColTip(accepterMap, ctx);
            }
        } else {//输出错误信息（无数据或异常）
            HandlerUtil.changeRow(ctx);//使光标换行，否则调用下面公共方法时，会清除掉输入的信息。
            friendlyMsgAnyKeyContinue(ErrorConstants.STOCK_INFO_FREEZE_EMPTY, true);
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
        FreezeStockInfo freezeStockInfo = getFreezeStockInfo();
        stockInfo.setWhsid(this.whsid);
        stockInfo.setBarcode(freezeStockInfo.getBarcode());//商品条码
        stockInfo.setSkuStatus(WmsConstants.STOCK_GODDSSTATUS_NORMAL);//正品
        map = new HashMap<String, Object>() {{
            put(WmsConstants.KEY_STOCKINFO_PARAM, stockInfo);
            put(WmsConstants.IS_FROZEN_OR_NOT, true);//sql中使用该标识，过滤掉可操作数为零的库存
        }};
        accepterMap.put(PageUtil.PARA_PAGE_MAP, map);
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
        instockReason.setReasonType(CheckReasonEnum.frozen.value);//原因类型：冻结
        map = new HashMap<String, Object>() {{
            put(WmsConstants.KEY_INSTOCKREASON_PARRAM, instockReason);
        }};
        accepterMap.put(PageUtil.PARA_PAGE_MAP, map);
        return map;
    }

    /**
     * 处理选择要冻结的库存,只有选择明细后，返回true
     */
    private boolean chooseStockInfo() {
        //组装数据
        String pageNum = getFreezeStockInfo().getSelectStockInfoNo();
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
            if (pageNum.matches(TipConstants.REG_SERIAL_NO)) {
                List<StockInfo> stockInfos = pageModle.getDatas();
                int maxIndex = stockInfos.size() - 1;
                int index = PageUtil.getIndexFromSerialnoAndPageModle(pageNum, pageModle);//(pn-1)*size+index+1==serialno
                if (index > maxIndex || index < 0) {
                    noPassValidate(SELECT_STOCK_INFO_NO, ErrorConstants.INPUT_FORMAT_ERROR);
                    return false;
                } else {
                    //正确选择要冻结的库存，保存参数
                    this.stockInfo = stockInfos.get(index);
                    accepterMap.remove(PageUtil.PARA_PAGE_MAP);//清空库存分页查询参数，因为下面还有分页查询冻结原因
                    HandlerUtil.changeRow(ctx);//选择库存后，换行输出原因分页列表
                    return true;
                }
            } else {
                noPassValidate(SELECT_STOCK_INFO_NO, ErrorConstants.INPUT_FORMAT_ERROR);
                return false;
            }
        }
    }

    /**
     * 处理选择原因
     */
    private void chooseReason() {
        //组装数据
        String pageNum = getFreezeStockInfo().getSelectReasonNo();
        Map<String, Object> accepterMap = getDataMap();
        PageModel<InstockReason> pageModle = (PageModel<InstockReason>) accepterMap.get(PageUtil.PAGE_MODEL);
        int pageSizeCurr = (Integer) accepterMap.get(PageUtil.LINES_NUM_CLEAN_KEY);//页面当前数据条数
        if (KeyEnum.N_78.value.equalsIgnoreCase(pageNum)) {//下一页
            PageUtil.changePageNext(ctx, accepterMap, WmsConstants.KEY_INSTOCKREASON_PARRAM, pageSizeCurr);
            setColUnReceived(SELECT_REASON_NO, accepterMap);
            HandlerUtil.moveDownN(ctx, 1);//如果不下移一行，翻页时会覆盖”选择库存序号“行
            showInstockReasonsPage(ctx);
        } else if (KeyEnum.B_66.value.equalsIgnoreCase(pageNum)) {//上一页
            PageUtil.changePageUp(ctx, accepterMap, WmsConstants.KEY_INSTOCKREASON_PARRAM, pageSizeCurr);
            setColUnReceived(SELECT_REASON_NO, accepterMap);
            HandlerUtil.moveDownN(ctx, 1);//如果不下移一行，翻页时会覆盖”选择库存序号“行
            showInstockReasonsPage(ctx);
        } else {//序号
            if (pageNum.matches(TipConstants.REG_SERIAL_NO)) {
                List<InstockReason> instockReasons = pageModle.getDatas();
                int maxIndex = instockReasons.size() - 1;
                int index = PageUtil.getIndexFromSerialnoAndPageModle(pageNum, pageModle);//(pn-1)*size+index+1==serialno
                if (index > maxIndex || index < 0) {
                    noPassValidate(SELECT_REASON_NO, ErrorConstants.INPUT_FORMAT_ERROR);
                } else {
                    //正确选择原因，保存参数
                    instockReason = instockReasons.get(index);
                    HandlerUtil.changeRow(ctx);//输出”选择原因序号:“后换行，输出
                    HandlerUtil.write(ctx, TipConstants.STOCK_INFO_FREEZE_REASON + instockReason.getRemark());
                    rePrintCurColTip(accepterMap, ctx);
                }
            } else {
                noPassValidate(SELECT_REASON_NO, ErrorConstants.INPUT_FORMAT_ERROR);
            }
        }
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
            int currPageLinesNum = PageUtil.showTable(ctx, instockReasonPageModel, REASON_TABLE_NAME, REASON_TABLE_COLUMN, true, true, null);//展示列表，带有序号
            accepterMap.put(PageUtil.LINES_NUM_CLEAN_KEY, currPageLinesNum);
            HandlerUtil.moveUpN(ctx, 1);//去掉原因分页下面的空行
            rePrintCurColTip(accepterMap, ctx);
        } else {//输出错误信息（无数据或异常）
            accepterMap.remove(PageUtil.PARA_PAGE_MAP);
            friendlyMsgAnyKeyContinue(ErrorConstants.PLS_MAINTAION_MOVE_REASON, true);
        }
    }

    /**
     * 发生错误操作后，给出提示语，任意键继续
     *
     * @param errMsg  错误信息
     * @param success 跳转标示
     */
    private void friendlyMsgAnyKeyContinue(String errMsg, boolean success) {
        HandlerUtil.delLeft(ctx);
        HandlerUtil.changeRow(ctx);
        HandlerUtil.errorBeep(ctx);
        HandlerUtil.print(ctx, errMsg + ErrorConstants.TIP_TO_CONTINUE);
        if (success) {
            isToInitial = true;
        } else {
            isToMenu = true;
        }
    }

    /**
     * 成功操作后，给出提示语，任意键继续
     *
     * @param errMsg  错误信息
     * @param success 跳转标示
     */
    private void friendlyMsgSucceedAnyKeyContinue(String errMsg, boolean success) {
        HandlerUtil.delLeft(ctx);
        HandlerUtil.changeRow(ctx);
        HandlerUtil.print(ctx, errMsg + ErrorConstants.TIP_TO_CONTINUE);
        if (success) {
            isToInitial = true;
        } else {
            isToMenu = true;
        }
    }

}
