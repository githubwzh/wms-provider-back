package com.womai.wms.rf.manager.window.stock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.PageUtil;
import com.womai.wms.rf.domain.stock.UnfreezeStockInfo;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.base.WarehouseInfoRemoteService;
import com.womai.wms.rf.remote.stock.StockInfoRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.base.BaseWarehouseinfo;
import com.womai.zlwms.rfsoa.domain.stock.StockInfo;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解冻
 * ClassDescribe:
 * Author :wangzhanhua
 * Date: 2016-09-26
 * Since
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component("unfreezeStockInfoManager")
public class UnfreezeStockInfoManagerImpl extends ReceiveManager {
    private final static String[] STOCK_TABLE_NAME = {"序号", "库存数量BU", "可操作BU", "包装单位", "箱规", " 生产日期 ", " 失效日期 ", " 状态 "};//库存表头
    private final static String[] STOCK_TABLE_COLUMN = {"stocknum", "canMoveBu", "unitname", "pknum", "productiondate", "expirationdate", "skuStatus",};//库存列名
    private final static String WAREHOUSECODE = "warehousecode";//库位编码
    private final static String BARCODE = "barcode";//商品条码
    private final static String STOCKNUM = "stocknum";//解冻数量BU
    private static final String SELECT_STOCK_INFO_NO = "selectStockInfoNo";//选择库存序号
    private StockInfo stockInfo;
    private boolean isToMenu;//跳转到主菜单
    private boolean isToInitial;//跳转到初始页面
    private Long whsid;//库存id
    @Autowired
    private StockInfoRemoteService stockInfoRemoteService;
    @Autowired
    private WarehouseInfoRemoteService warehouseInfoRemoteService;
    private ChannelHandlerContext ctx;
    private final String[] pageHeader = {Constants.BREAK_LINE, TipConstants.STOCK_INFO_UNFREEZE, Constants.SPLIT, ""};

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        // 冻结界面
        super.initBaseMap(UnfreezeStockInfo.class, pageHeader, ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 解冻
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
        UnfreezeStockInfo unfreezeStockInfo = getUnfreezeStockInfo();
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (WAREHOUSECODE.equals(lastCompleteColName)) {
            BaseWarehouseinfo warehouseinfo = warehouseInfoRemoteService.getBaseWarehouseInfoByCode(getCredentialsVO(ctx), unfreezeStockInfo.getWarehousecode());
            if (warehouseinfo != null) {
                Integer whsType = warehouseinfo.getWarehousetype();
                if (whsType.equals(WmsConstants.WHSTYPE_CODE_CUNHUO) || whsType.equals(WmsConstants.WHSTYPE_CODE_XURUXUCHU)) {
                    rePrintCurColTip(accepterMap, ctx);
                    this.whsid = warehouseinfo.getId();
                } else {
                    noPassValidate(WAREHOUSECODE, ErrorConstants.BASE_WAREHOUSE_INFO_TYPE_ERR);
                }
            } else {
                noPassValidate(WAREHOUSECODE, ErrorConstants.BASE_WAREHOUSE_INFO_STATUS_ERR);
            }
        }
        if (BARCODE.equals(lastCompleteColName)) {//扫描库位，商品条码后，展示可解冻库存列表
            //分页查询可解冻库存信息
            showStockInfosPage(ctx);
        }
        if (SELECT_STOCK_INFO_NO.equals(lastCompleteColName)) {//选择库存明细
            if (chooseStockInfo()) {
                rePrintCurColTip(accepterMap, ctx);
            }
        }
        if (STOCKNUM.equals(lastCompleteColName) && validateQuantity()) {//校验可解冻数量bu
            doUnfreezeStockInfo();
        }
    }

    /**
     * 处理解冻的业务
     *
     * @throws Exception
     */
    private void doUnfreezeStockInfo() throws Exception {
        UnfreezeStockInfo freezeStockInfo = getUnfreezeStockInfo();
        StockInfo stockInfoPara = new StockInfo();
        stockInfoPara.setStockid(stockInfo.getStockid());
        stockInfoPara.setStocknum(Integer.parseInt(freezeStockInfo.getStocknum()));
        RemoteResult<Boolean> remoteResult = stockInfoRemoteService.unfreezeStockInfo(getCredentialsVO(ctx), stockInfoPara);
        if (remoteResult.isSuccess()) {
            friendlyMsgSucceedAnyKeyContinue(TipConstants.STOCK_INFO_UNFREEZE_SUCCESS, true);
        } else {
            friendlyMsgAnyKeyContinue((String) remoteResult.getResultCode(), true);
        }
    }


    /**
     * 校验可冻结数量Bu
     */
    private boolean validateQuantity() {
        String operateStockInfoNum = getUnfreezeStockInfo().getStocknum();//输入的移位数量BU
        if (operateStockInfoNum.matches(TipConstants.REG_SERIAL_NO)) {
            Integer canMoveBu = stockInfo.getCanMoveBu();//选择的库存对应的可解冻数量BU
            Integer planBu = Integer.parseInt(operateStockInfoNum);
            if (canMoveBu.compareTo(planBu) < 0) {
                noPassValidate(STOCKNUM, ErrorConstants.STOCK_INFO_UNFREEZE_NUM_BEYOND);
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
     * @return
     */
    private UnfreezeStockInfo getUnfreezeStockInfo() {
        return (UnfreezeStockInfo) getDataMap().get(DefaultKey.objectClass.keyName);
    }

    /**
     * 可冻结库存分页列表
     *
     * @param ctx
     */
    private void showStockInfosPage(ChannelHandlerContext ctx) {
        Map<String, Object> accepterMap = getDataMap();
        //根据库位，商品条码，查询可操作数bu为大于0的，商品状态为正品的库存
        RemoteResult<PageModel<StockInfo>> pageModelRemoteResult = stockInfoRemoteService.queryStockInfoPageList(getCredentialsVO(ctx), getParaMap());
        if (pageModelRemoteResult.isSuccess()) {
            //展示库存分页列表，一定有数据
            PageModel<StockInfo> stockInfoPageModel = pageModelRemoteResult.getT();
            accepterMap.put(PageUtil.PAGE_MODEL, stockInfoPageModel);
            HandlerUtil.changeRow(ctx);//扫描商品条码后，换行展示库存分页
            List<StockInfo> stockInfos = stockInfoPageModel.getDatas();
            String skuName = stockInfos.get(0).getSkuname();
            int currPageLinesNum = PageUtil.showTable(ctx, stockInfoPageModel, STOCK_TABLE_NAME, STOCK_TABLE_COLUMN, true, true, TipConstants.SKU_NAME + skuName);//展示列表，带有序号
            accepterMap.put(PageUtil.LINES_NUM_CLEAN_KEY, currPageLinesNum);
            HandlerUtil.moveUpN(ctx, 1);//库存分页下面的空行去掉，输出”选择库存序号“，如果只有一条库存记录输出”请输入取消冻结数量BU“
            if (stockInfoPageModel.getTotalCount() == 1) {
                //只有一条库存，跳过选择库存
                StockInfo stockInfo = stockInfos.get(0);
                //跳过选择库存
                this.stockInfo = stockInfo;
                //展示原因选择原因
                resetCurCol(STOCKNUM, accepterMap, ctx);
            } else {
                rePrintCurColTip(accepterMap, ctx);
            }
        } else {//输出错误信息（无数据或异常）
            HandlerUtil.changeRow(ctx);
            friendlyMsgAnyKeyContinue(ErrorConstants.STOCK_INFO_UNFREEZE_EMPTY, true);
        }
    }

    /**
     * 获得分页查询条件
     */
    private HashMap<String, Object> getParaMap() {
        Map<String, Object> accepterMap = getDataMap();
        HashMap<String, Object> map = (HashMap<String, Object>) accepterMap.get(PageUtil.PARA_PAGE_MAP);//缓存中的分页查询参数Map
        if (map == null) {
            map = setStockInfoParaMap(accepterMap, map);
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
        UnfreezeStockInfo freezeStockInfo = getUnfreezeStockInfo();
        stockInfo.setWhsid(this.whsid);
        stockInfo.setBarcode(freezeStockInfo.getBarcode());//商品条码
        stockInfo.setSkuStatus(WmsConstants.STOCK_GODDSSTATUS_FROZEN);//冻结
        map = new HashMap<String, Object>() {{
            put(WmsConstants.KEY_STOCKINFO_PARAM, stockInfo);
            put(WmsConstants.IS_FROZEN_OR_NOT, true);
        }};
        accepterMap.put(PageUtil.PARA_PAGE_MAP, map);
        return map;
    }


    /**
     * 处理选择要解冻的库存,只有选择明细后，返回true
     */
    private boolean chooseStockInfo() {
        //组装数据
        String pageNum = getUnfreezeStockInfo().getSelectStockInfoNo();
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
                    return true;
                }
            } else {
                noPassValidate(SELECT_STOCK_INFO_NO, ErrorConstants.INPUT_FORMAT_ERROR);
                return false;
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
     * 发生错误操作后，给出提示语，任意键继续
     *
     * @param errMsg  错误信息
     * @param success 跳转页面标示
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
     * @param success 跳转页面标示
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
