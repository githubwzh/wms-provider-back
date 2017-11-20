package com.womai.wms.rf.manager.window.stock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.DefaultKey;
import com.womai.wms.rf.common.constants.ErrorConstants;
import com.womai.wms.rf.common.constants.KeyEnum;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.PageUtil;
import com.womai.wms.rf.common.util.Reflections;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.stock.StockInfoRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.stock.StockInfo;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassDescribe:查询库存
 * Author :wangzhanhua
 * Date: 2016-08-16
 * Since
 * To change this template use File | Settings | File Templates.
 */
public abstract class QueryStockInfoManagerImpl extends ReceiveManager {
    protected String[] STOCK_TABLE_NAME;//库存表头
    protected String[] STOCK_TABLE_COLUMN;//库存列名
    protected ChannelHandlerContext ctx;
    private boolean isToInitial;//跳转到初始页面
    private final static String SELECT_STOCK_INFO_NO = "selectStockInfoNo";//翻页
    @Autowired
    private StockInfoRemoteService stockInfoRemoteService;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 库存查询
     *
     * @param ctx handler对象
     * @param msg 每次接收到的字节数据
     * @throws Exception 抛出异常
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Map<String, Object> accepterMap = getDataMap();
        if (isToInitial) {
            channelActive(ctx);
            isToInitial = false;
            return;
        }
        receiveDataAndNotPrintNext(ctx, msg, accepterMap);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (isBarcodeOrWarehousecode(lastCompleteColName)) {//2、扫描库位或商品条码接收完成
            showStockInfosPage(ctx);
        }
        if (SELECT_STOCK_INFO_NO.equals(lastCompleteColName)) {
            chooseStockInfo((String) Reflections.getFieldValue(getQueryStockInfo(), SELECT_STOCK_INFO_NO));
        }
    }

    /**
     * 判断扫描商品条码或者库位编码完成
     *
     * @param lastCompleteColName
     * @return
     */
    protected abstract boolean isBarcodeOrWarehousecode(String lastCompleteColName);

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
        isToInitial = true;
    }

    /**
     * 翻页或任意键退出
     *
     * @param pageNum 翻页时的页码参数（B/N,或任意键退出）
     * @throws Exception
     */

    private void chooseStockInfo(String pageNum) throws Exception {
        //组装数据
        Map<String, Object> accepterMap = getDataMap();
        int pageSizeCurr = (Integer) accepterMap.get(PageUtil.LINES_NUM_CLEAN_KEY) + 1;//页面当前数据条数
        if (KeyEnum.N_78.value.equalsIgnoreCase(pageNum)) {//下一页
            PageUtil.changePageNext(ctx, accepterMap, WmsConstants.KEY_STOCKINFO_PARAM, pageSizeCurr);
            setColUnReceived(SELECT_STOCK_INFO_NO, accepterMap);
            showStockInfosPage(ctx);
        } else if (KeyEnum.B_66.value.equalsIgnoreCase(pageNum)) {//上一页
            PageUtil.changePageUp(ctx, accepterMap, WmsConstants.KEY_STOCKINFO_PARAM, pageSizeCurr);
            setColUnReceived(SELECT_STOCK_INFO_NO, accepterMap);
            showStockInfosPage(ctx);
        } else {//序号
            channelActive(ctx);
        }
    }

    /**
     * 获得接收参数的对象
     *
     * @return
     */

    protected Object getQueryStockInfo() {
        return getDataMap().get(DefaultKey.objectClass.keyName);
    }

    /**
     * 库存分页列表
     *
     * @param ctx
     */
    protected void showStockInfosPage(ChannelHandlerContext ctx) {
        Map<String, Object> accepterMap = getDataMap();
        RemoteResult<PageModel<StockInfo>> pageModelRemoteResult = stockInfoRemoteService.queryStockInfoPageList(getCredentialsVO(ctx), getParaMap());
        if (pageModelRemoteResult.isSuccess()) {
            //展示库存分页列表，一定有数据
            PageModel<StockInfo> stockInfoPageModel = pageModelRemoteResult.getT();
            accepterMap.put(PageUtil.PAGE_MODEL, stockInfoPageModel);
            HandlerUtil.changeRow(ctx);
            accepterMap.put(PageUtil.LINES_NUM_CLEAN_KEY, showTable(stockInfoPageModel));
            HandlerUtil.moveUpN(ctx, 1);
            rePrintCurColTip(accepterMap, ctx);
        } else {//输出错误信息（无数据或异常）
            HandlerUtil.changeRow(ctx);
            systemErrorAnyKeyContinue(ErrorConstants.QUERY_STOCKINFO_EMPTY);
        }
    }

    /**
     * 库存分页展示，子类可以复写该方法
     *
     * @param stockInfoPageModel
     * @return
     */
    protected int showTable(PageModel<StockInfo> stockInfoPageModel) {
        return PageUtil.showTable(ctx, stockInfoPageModel, STOCK_TABLE_NAME, STOCK_TABLE_COLUMN, false, true, null);//展示列表，带有序号;
    }

    /**
     * 获得分页查询条件
     */
    private HashMap<String, Object> getParaMap() {
        Map<String, Object> accepterMap = getDataMap();
        HashMap<String, Object> map = (HashMap<String, Object>) accepterMap.get(PageUtil.PARA_PAGE_MAP);//缓存中的分页查询参数Map
        if (map == null) {
            final StockInfo stockInfo = new StockInfo();
            stockInfo.setPage(Constants.PAGE_START);
            stockInfo.setRows(Constants.QUERY_STOCK_INFO_PAGE_SIZE);
            stockInfo.setSidx(Constants.PAGE_STOCK_INFO_SIDX);
            stockInfo.setSord(Constants.PAGE_SORT_DESC);
            addParaToMapForQueryStockInfoPage(stockInfo);
            map = new HashMap<String, Object>() {{
                put(WmsConstants.KEY_STOCKINFO_PARAM, stockInfo);
            }};
            accepterMap.put(PageUtil.PARA_PAGE_MAP, map);
        }
        return map;
    }

    /**
     * 增加库存分页查询条件
     *
     * @param stockInfo
     */
    protected abstract void addParaToMapForQueryStockInfoPage(StockInfo stockInfo);

}
