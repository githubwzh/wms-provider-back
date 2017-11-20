package com.womai.wms.rf.manager.window.stock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.*;
import com.womai.wms.rf.domain.stock.ReplenishMoveIn;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.base.GoodsinfoRemoteService;
import com.womai.wms.rf.remote.base.PackaginginfoRemoteService;
import com.womai.wms.rf.remote.stock.ReplenishMoveInRemoteService;
import com.womai.zlwms.rfsoa.api.WMSErrorMess;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.base.BaseGoodsinfo;
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
 * ClassDescribe:补货移入业务
 * Author :zhangwei
 * Date: 2016-10-08
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component("replenishMoveInManager")
public class ReplenishMoveInManagerImpl extends ReceiveManager {
    @Autowired
    private ReplenishMoveInRemoteService replenishMoveInRemoteService;
    @Autowired
    private GoodsinfoRemoteService goodsinfoRemoteService;
    @Autowired
    private PackaginginfoRemoteService packaginginfoRemoteService;

    private final static String GO_TO_FLAG = "replenish_goto";//跳转标志
    private final static Integer TO_CHANNEL_ACTIVE = -1;//跳转标志
    private final static Integer TO_BAR_CODE = 1;//跳转到重新扫描商品条码的步骤
    private final static Integer TO_WH_CODE = 2;//跳转到重新输入移入库位的步骤

    private static final String SHELF_CODE = "shelfCode";// 补货单号
    private static final String BAR_CODE = "barCode"; // 商品条码
    private static final String SELECT_SERIAL_NO = "selectSerialNo"; // 选择明细列表序号
    private static final String REALITY_WH_CODE = "realityWHCode";//移入库位编码
    private static final String MOVE_IN_BU = "moveInBu";// 移入数量bu

    private Long TL_SHELF_ID = 0L; //补货单id
    private BaseGoodsinfo TL_GOODS; //商品数据
    private StockReplenishItem TL_SELECTED_ITEM ; //商品数据
    private String TL_WH_CODE ; //移入库位
    private Integer TL_UNMOVEINBU = 0; //已移入数量BU

    //补货单明细列表常量
    public final static String[] TABLE_HEADERS = {TipConstants.PAGEHEADER_SERIAL_NO, TipConstants.REPLENISH_MOVEIN_UNINBU_LABEL};// 列表标题
    public final static String[] TABLE_COLUMNS = {WmsConstants.KEY_UNMOVEINBU_PARAM};// 标题对应的pageModel中的字段名，不需要序号一列
    private static final String[] PAGEHEADER = {"", TipConstants.REPLENISH_MOVEIN_TITLE, Constants.SPLIT, ""};

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        resetLocals();
        initBaseMap(ReplenishMoveIn.class, PAGEHEADER, ctx);// 重新初始化界面，重置已接受数据，打印标题
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object inputObj) throws Exception {
        Map<String, Object> accepterMap = getDataMap();
        //处理不同返回值的跳转
        if (goToHandler(accepterMap, ctx)) {
            return;
        }
        receiveDataAndNotPrintNext(ctx, inputObj, accepterMap);
        ReplenishMoveIn replenishMoveIn = (ReplenishMoveIn) accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (SHELF_CODE.equals(lastCompleteColName)) {
            String shelfCode = replenishMoveIn.getShelfCode();
            Long shelfID = replenishMoveInRemoteService.getReplenishByShelfCodeForIn(getCredentialsVO(ctx), shelfCode);
            if (shelfID == null || shelfID == 0) {
                colNeedReInput(lastCompleteColName, ErrorConstants.REPLENISH_NO_DATA, accepterMap, ctx);
            } else {
                TL_SHELF_ID = shelfID;//将获取到的补货子单ID保存
                resetCurCol(BAR_CODE, accepterMap, ctx);
            }
        }
        if (BAR_CODE.equals(lastCompleteColName)) {
            String barCode = replenishMoveIn.getBarCode();
            BaseGoodsinfo goodsInfo = goodsinfoRemoteService.getEnableGoodsByBarCode(getCredentialsVO(ctx), barCode);
            if (goodsInfo == null) {
                colNeedReInput(lastCompleteColName, ErrorConstants.GOODSINFO_BARCODE_ERROR, accepterMap, ctx);
            } else {
                TL_GOODS = goodsInfo;//保存查询到的商品数据
                //调用分页方法
                replenishItemPageQuery(accepterMap, ctx);
            }
        }
        if (SELECT_SERIAL_NO.equals(lastCompleteColName)) {
            String selectSerialNo = replenishMoveIn.getSelectSerialNo();
            PageModel<StockReplenishItem> pageModel = (PageModel<StockReplenishItem>) accepterMap.get(PageUtil.PAGE_MODEL);
            int pageSizeCurr = (Integer) accepterMap.get(PageUtil.LINES_NUM_CLEAN_KEY);//页面当前数据条数
            if (KeyEnum.N_78.value.equalsIgnoreCase(selectSerialNo)) {//下一页
                PageUtil.changePageNext(ctx, accepterMap, WmsConstants.KEY_STOCKREPLENISH_DETAIL_PARAM, pageSizeCurr);
                replenishItemPageQuery(accepterMap, ctx);
            } else if (KeyEnum.B_66.value.equalsIgnoreCase(selectSerialNo)) {//上一页
                PageUtil.changePageUp(ctx, accepterMap, WmsConstants.KEY_STOCKREPLENISH_DETAIL_PARAM, pageSizeCurr);
                replenishItemPageQuery(accepterMap, ctx);
            } else {
                if (selectSerialNo.matches(TipConstants.REG_SERIAL_NO)) {//大于0的正整数
                    List<StockReplenishItem> replenishItems = pageModel.getDatas();
                    int maxIndex = replenishItems.size() - 1;
                    int index = PageUtil.getIndexFromSerialnoAndPageModle(selectSerialNo, pageModel);
                    if (index > maxIndex || index < 0) {//只能输入当前页内显示的序号
                        colNeedReInput(lastCompleteColName, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                    } else {
                        List<StockReplenishItem> stockReplenishItemList = pageModel.getDatas();
                        StockReplenishItem replenishItem = stockReplenishItemList.get(index);
                        TL_SELECTED_ITEM = replenishItem;//保存选中的明细
                        removeQueryMap(accepterMap);//选中后清空查询参数，因为如果移入完成后需要重新扫描商品条码
//                        printBeforeNextField(TipConstants.REPLENISH_RECOMMEND_WH + replenishItem.getRecomwhcode(), accepterMap, ctx);
                        tipUnMoveInBU(accepterMap, ctx);
                        if (replenishItem.getIssamesku() != null && WmsConstants.CON_YES == replenishItem.getIssamesku()) {
                            printBeforeNextField(TipConstants.IS_SAME_SKU, accepterMap, ctx);
                        }
                        resetCurCol(REALITY_WH_CODE, accepterMap, ctx);//定位到接收移入库位
                    }
                } else {
                    colNeedReInput(lastCompleteColName, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                }
            }
        }
        if (REALITY_WH_CODE.equals(lastCompleteColName)) {
            String realityWHCode = replenishMoveIn.getRealityWHCode();
            boolean result = replenishMoveInRemoteService.validateWareHouse(getCredentialsVO(ctx), realityWHCode);
            if (!result) {
                colNeedReInput(lastCompleteColName, ErrorConstants.BASE_WAREHOUSE_INFO_TYPE_ERR, accepterMap, ctx);
            } else {
                TL_WH_CODE = realityWHCode;//保存移入库位
                resetCurCol(MOVE_IN_BU, accepterMap, ctx);//定位到接收移入库位
            }
        }
        if (MOVE_IN_BU.equals(lastCompleteColName)) {
            String moveInBU = replenishMoveIn.getMoveInBu();
            confirmMoveIn(moveInBU, replenishMoveIn, accepterMap, ctx);
        }
        WMSDebugManager.debugLog("" + accepterMap);
    }

    /**
     * 确认移入操作
     *
     * @param moveInBU        输入的移入BU数
     * @param replenishMoveIn 业务数据接收对象
     * @param accepterMap     map数据容器
     * @param ctx             ctx上下文
     */
    private void confirmMoveIn(String moveInBU, ReplenishMoveIn replenishMoveIn, Map<String, Object> accepterMap, ChannelHandlerContext ctx) throws Exception {
        if (!moveInBU.matches(TipConstants.REG_SERIAL_NO)) {
            colNeedReInput(MOVE_IN_BU, ErrorConstants.ILLEGAL_DATA, accepterMap, ctx);
            return;
        }
        Integer confirmBU = Integer.parseInt(moveInBU);
        StockReplenishItem replenishItem = TL_SELECTED_ITEM;
        int unmoveinbu = replenishItem.getUnmoveinbu();
        //不能大于未移入BU数
        if (confirmBU > unmoveinbu) {
            colNeedReInput(MOVE_IN_BU, ErrorConstants.REPLENISH_TOO_MUCH, accepterMap, ctx);
            return;
        }

        replenishItem.setSkuid(TL_GOODS.getSkuid());
        RemoteResult<Integer> remoteResult = replenishMoveInRemoteService.confirmMoveIn(getCredentialsVO(ctx), replenishItem, replenishMoveIn.getRealityWHCode(), confirmBU);
        Integer result = remoteResult.getT();
        String errorMess = remoteResult.getResultCode();
        if (remoteResult == null || WMSErrorMess.SYS_ERROR.equals(result)) {
            HandlerUtil.errorBeep(ctx);//系统错误，响铃
            //系统错误
            errorMess = errorMess + ErrorConstants.TIP_TO_CONTINUE;
            accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ACTIVE);
        } else if (WMSErrorMess.REPLENISHMOVEIN_STATUSCODE_ALL_DONE.equals(result)) {
            //整单已无可移入明细数据
            errorMess = TipConstants.REPLENISH_ALL_COMPLETE;
            accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ACTIVE);
        } else if (WMSErrorMess.REPLENISHMOVEIN_STATUSCODE_TO_BARCODE.equals(result)) {
            //某条明细移入完成，重新扫描条码
            errorMess = TipConstants.RE_INPUT_BAR_CODE;
            accepterMap.put(GO_TO_FLAG, TO_BAR_CODE);
        } else if (WMSErrorMess.REPLENISHMOVEIN_STATUSCODE_CONTINUE.equals(result)) {
            //某条明细未全部移入，重新提示未移入数量并继续进行移入操作
            errorMess = TipConstants.CONTINUE_INPUT_MOVE_IN;
            accepterMap.put(GO_TO_FLAG, TO_WH_CODE);
        }
        printBeforeNextField(errorMess, accepterMap, ctx);
    }

    public boolean goToHandler(Map<String, Object> accepterMap, ChannelHandlerContext ctx) throws Exception {
        if (accepterMap.get(GO_TO_FLAG) == null) {
            return false;
        }
        Integer goToAim = (Integer) accepterMap.get(GO_TO_FLAG);
        accepterMap.remove(GO_TO_FLAG);//清空一下，避免重复调用
        if (TO_CHANNEL_ACTIVE.equals(goToAim)) {
            channelActive(ctx);
            return true;
        } else if (TO_BAR_CODE.equals(goToAim)) {
            List<String> fieldList = CollectionUtil.newGenericList(SHELF_CODE);
            printFieldsAndReceiveData(PAGEHEADER, fieldList, BAR_CODE, accepterMap, ctx);// 回退到扫描商品条码
            return true;
        } else if (TO_WH_CODE.equals(goToAim)) {
            List<String> fieldList = CollectionUtil.newGenericList(SHELF_CODE);
            printFieldsAndReceiveData(PAGEHEADER, fieldList, BAR_CODE, accepterMap, ctx);// 回退到扫描商品条码
            channelRead(ctx, TL_GOODS.getBarcode());//输入当前条码回车
            channelRead(ctx,Constants.BREAK_LINE);//输入当前条码回车
            return true;
        }
        return true;
    }

    /**
     * 提示补货明细中的未移入数量
     *
     * @param accepterMap map数据容器
     * @param ctx         handler上下文容器
     */
    private void tipUnMoveInBU(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        StockReplenishItem replenishItem = TL_SELECTED_ITEM;
        int unMoveInBU = replenishMoveInRemoteService.getRepUnMoveInYnBU(getCredentialsVO(ctx), replenishItem);
        TL_UNMOVEINBU = unMoveInBU;
        //提示该批次商品未移入数量
        printBeforeNextField(TipConstants.REPLENISH_UN_MOVE_IN_BU + unMoveInBU, accepterMap, ctx);
    }

    /**
     * 补货明细的分页查询
     *
     * @param accepterMap map数据容器
     * @param ctx         ctx上下文
     */
    private void replenishItemPageQuery(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        PageModel<StockReplenishItem> pageModel = replenishMoveInRemoteService.queryReplenishItemPage(getCredentialsVO(ctx), makePageQueryConditionMap(accepterMap));
        if (pageModel == null || pageModel.getTotalCount() == 0) {
            colNeedReInput(BAR_CODE, ErrorConstants.REPLENISH_NO_DATA, accepterMap, ctx);
            removeQueryMap(accepterMap);
            return;
        } else {
            //如果分页中只有一条明细数据则直接显示并定位到输入移入库位的步骤
            if (pageModel.getTotalCount() == 1) {
                StockReplenishItem replenishItem = pageModel.getDatas().get(0);
                TL_SELECTED_ITEM = replenishItem;
                printOneItem(replenishItem, accepterMap, ctx);
                removeQueryMap(accepterMap);
                resetCurCol(REALITY_WH_CODE, accepterMap, ctx);//默认选中，跳转到输入移入库位的步骤
            } else {
                accepterMap.put(PageUtil.PAGE_MODEL, pageModel);
                HandlerUtil.changeRow(ctx);
                int currPageLinesNum = PageUtil.showTable(ctx, pageModel, TABLE_HEADERS, TABLE_COLUMNS, true, true, null);//展示列表，带有序号
                accepterMap.put(PageUtil.LINES_NUM_CLEAN_KEY, currPageLinesNum);
                HandlerUtil.moveUpN(ctx, 1);//因为分页工具中已经输出了回车，所以手动上移一行
                setColUnReceived(SELECT_SERIAL_NO, accepterMap);
                resetCurCol(SELECT_SERIAL_NO, accepterMap, ctx);
            }
        }
    }

    /**
     * 单个明细时打印数据
     *
     * @param replenishItem 补货明细数据
     * @param accepterMap   map数据容器
     * @param ctx           ctx上下文
     */
    private void printOneItem(StockReplenishItem replenishItem, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        List<String> tiplist = CollectionUtil.newList(TipConstants.REPLENISH_MOVEIN_UNINBU_LABEL);
        List<String> valuelist = CollectionUtil.newList(replenishItem.getUnmoveinbu().toString());
        super.printBeforeNextField(tiplist, valuelist, accepterMap, ctx);

    }

    /**
     * 构造分页查询条件并放入accpterMap
     *
     * @param accepterMap 接收信息映射表
     * @return 查询条件Map
     */
    private HashMap<String, Object> makePageQueryConditionMap(Map<String, Object> accepterMap) {
        HashMap<String, Object> map = (HashMap<String, Object>) accepterMap.get(PageUtil.PARA_PAGE_MAP);//缓存中的分页查询参数Map
        if (map == null) {
            final StockReplenishItem condition = new StockReplenishItem();
            condition.setPage(Constants.PAGE_START); //初始化页数
            condition.setRows(Constants.REASON_PAGE_SIZE); //初始化表格行数
            condition.setSplitshelfid(TL_SHELF_ID);//补货子单id
            condition.setSkuid(TL_GOODS.getSkuid()); //商品skuid
            map = new HashMap<String, Object>() {{
                put(WmsConstants.KEY_STOCKREPLENISH_DETAIL_PARAM, condition);
            }};
            accepterMap.put(PageUtil.PARA_PAGE_MAP, map);
        }
        return map;
    }

    /**
     * 清空分页查询map中的数据
     *
     * @param accepterMap map数据容器
     */
    private void removeQueryMap(Map<String, Object> accepterMap) {
        //清空参数map
        accepterMap.remove(PageUtil.PARA_PAGE_MAP);
    }

    /**
     * 清空threadLocal数据
     */
    private void resetLocals() {
        TL_SHELF_ID = 0L;
        TL_GOODS = null;
        TL_SELECTED_ITEM = null;
        TL_WH_CODE = "";
        TL_UNMOVEINBU = 0;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


}
