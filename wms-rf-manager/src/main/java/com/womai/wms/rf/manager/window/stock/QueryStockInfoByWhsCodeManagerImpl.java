package com.womai.wms.rf.manager.window.stock;

import com.womai.common.framework.domain.PageModel;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.TipConstants;
import com.womai.wms.rf.common.util.PageUtil;
import com.womai.wms.rf.domain.stock.QueryStockInfoByWhsCode;
import com.womai.zlwms.rfsoa.domain.stock.StockInfo;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * ClassDescribe:查询库存
 * Author :wangzhanhua
 * Date: 2016-08-16
 * Since
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component("queryStockInfoByWhsCodeManager")
public class QueryStockInfoByWhsCodeManagerImpl extends QueryStockInfoManagerImpl {
    private final static String[] STOCK_TABLE_NAME = {"   商品条码  ", "         商品名称       ", "库存BU", "单位", "箱规", "上架数量BU", "拣货数量BU", "库存状态", " 生产日期 ", " 近效日期 ", " 失效日期 "};//库存表头
    private final static String[] STOCK_TABLE_COLUMN = {"barcode", "skuname", "stocknum", "unitname", "pknum", "putawaynum", "pickoutnum", "skuStatus", "productiondate", "nearvalid", "expirationdate"};//库存列名
    private final String[] pageHeader = {Constants.BREAK_LINE, TipConstants.QUERY_STOCKINFO, Constants.SPLIT, ""};
    private final static String WAREHOUSECODE = "warehousecode";//库位编码
    private final static String SKUNAME = "skuname";//商品名称

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.ctx = ctx;
        super.initBaseMap(QueryStockInfoByWhsCode.class, pageHeader, ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 判断扫描库位编码完成
     *
     * @param lastCompleteColName 库位编码
     * @return
     */
    @Override
    protected boolean isBarcodeOrWarehousecode(String lastCompleteColName) {
        return WAREHOUSECODE.equals(lastCompleteColName);
    }

    /**
     * 增加分页查询库存参数条件
     *
     * @param stockInfo 库存对象
     */
    @Override
    protected void addParaToMapForQueryStockInfoPage(StockInfo stockInfo) {
        stockInfo.setWarehousecode(((QueryStockInfoByWhsCode) super.getQueryStockInfo()).getWarehousecode());//商品条码
    }

    /**
     * 重写分页展示方法（截取商品名称）
     *
     * @param stockInfoPageModel
     * @return 翻页时要清楚的行数
     */
    @Override
    protected int showTable(PageModel<StockInfo> stockInfoPageModel) {
        Set<String> set = new HashSet<String>();
        set.add(SKUNAME);
        return PageUtil.showTableCanSetCutColName(ctx, stockInfoPageModel, STOCK_TABLE_NAME, STOCK_TABLE_COLUMN, set, false, true, null);//展示列表，带有序号;
    }
}
