package com.womai.wms.rf.manager.window.stock;

import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.TipConstants;
import com.womai.wms.rf.domain.stock.QueryStockInfoByBarcode;
import com.womai.zlwms.rfsoa.domain.stock.StockInfo;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * ClassDescribe:查询库存
 * Author :wangzhanhua
 * Date: 2016-08-16
 * Since
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component("queryStockInfoByBarcodeManager")
public class QueryStockInfoByBarcodeManagerImpl extends QueryStockInfoManagerImpl {
    private final static String[] STOCK_TABLE_NAME = {"    库位    ", "库存数量BU", "库存状态", "单位", "箱规", "上架数量BU", "拣货数量BU", " 生产日期 ", " 近效日期 ", " 失效日期 "};//库存表头
    private final static String[] STOCK_TABLE_COLUMN = {"warehousecode", "stocknum", "skuStatus", "unitname", "pknum", "putawaynum", "pickoutnum", "productiondate", "nearvalid", "expirationdate"};//库存列名
    private final String[] pageHeader = {Constants.BREAK_LINE, TipConstants.QUERY_STOCKINFO, Constants.SPLIT, ""};
    private final static String BARCODE = "barcode";//商品条码

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.STOCK_TABLE_NAME = STOCK_TABLE_NAME;
        super.STOCK_TABLE_COLUMN = STOCK_TABLE_COLUMN;
        super.ctx = ctx;
        super.initBaseMap(QueryStockInfoByBarcode.class, pageHeader, ctx);
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 判断扫描商品名称完成
     *
     * @param lastCompleteColName 商品名称
     * @return
     */
    @Override
    protected boolean isBarcodeOrWarehousecode(String lastCompleteColName) {
        return BARCODE.equals(lastCompleteColName);
    }

    /**
     * 增加分页查询库存参数条件
     *
     * @param stockInfo 库存对象
     */
    @Override
    protected void addParaToMapForQueryStockInfoPage(StockInfo stockInfo) {
        stockInfo.setBarcode(((QueryStockInfoByBarcode) super.getQueryStockInfo()).getBarcode());//商品条码
    }
}
