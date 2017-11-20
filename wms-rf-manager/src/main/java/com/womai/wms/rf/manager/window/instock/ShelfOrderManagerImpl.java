package com.womai.wms.rf.manager.window.instock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.*;
import com.womai.wms.rf.domain.instock.ShelfOrder;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.base.PackaginginfoRemoteService;
import com.womai.wms.rf.remote.base.WarehouseInfoRemoteService;
import com.womai.wms.rf.remote.instock.InstockShelfRemoteService;
import com.womai.zlwms.rfsoa.api.WMSErrorMess;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.base.BasePackaginginfo;
import com.womai.zlwms.rfsoa.domain.base.BaseWarehouseinfo;
import com.womai.zlwms.rfsoa.domain.instock.InstockShelforder;
import com.womai.zlwms.rfsoa.domain.instock.InstockShelforderDetail;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassDescribe: 入库上架单
 * Author :zhangwei
 * Date: 2016-08-16
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component("shelfOrderManager")
public class ShelfOrderManagerImpl extends ReceiveManager {
    @Autowired
    private InstockShelfRemoteService instockShelfRemoteService;
    @Autowired
    private WarehouseInfoRemoteService warehouseInfoRemoteService;
    private final static String GO_TO_FLAG = "shelfOrder_goto";//跳转标志
    private final static Integer TO_CHANNEL_ACTIVE = -1;//跳转标志
    private final static Integer TO_SHELF_TYPE = 1;//托盘码上架，回到选择上架方式的步骤
    private final static Integer TO_SELECT_DETAIL = 2;//商品条码上架，回到选择明细的步骤
    private final static Integer TO_RE_INPUT_NUM = 3;//商品条码上架，继续输入上架数量

    public final static String REG_SERIAL_NO = "^[1-9]\\d{0,9}$";
    private final String REG_YN = "^[YNyn]{1}$"; // 输入y/n的场景校验
    private final static String[] pageHeader = {"", TipConstants.SHELF_TITLE, Constants.SPLIT, ""};
    //业务domain字段
    private final static String ORDER_CODE = "orderCode";//上架单号或网络订单号
    private final static String SELECT_SHELF_ORDER = "selectShelfOrder";//选择上架主单
    private final static String SELECT_SHELF_ORDER_SWITCH = "selectShelfOrderSwitch";//选择上架主单(自动显示多个上架单)
    private final static String SHELF_TYPE = "shelfType";//上架方式，扫描商品条码或托盘码
    private final static String BAR_CODE = "barCode";//商品条码
    //    private final static String DATE_TYPE = "dateType";//日期类型
    private final static String PALLET_CODE = "palletCode";//托盘编码
    private final static String SELECTED_DETAIL = "selectedDetail";//分页方式选择明细
    private final static String SCAN_WH_CODE = "scanWHCode";//扫描库位编码
    private final static String CONFIRM_DATE = "confirmDate";//输入生产日期进行确认
    private final static String SHELF_NUM = "shelfNum";//上架数量
    private final static String CONFIRM_SHELF_NUM = "confirmShelfNum";//托盘码上架方式，需要输入YN进行确认，一次性上架全部BU
    private final static String MOVE_FLAG = "MOVE_FLAG";//自动显示单号，第一次翻页时，下移一行
    //上架主单列表头
    public final static String[] TABLE_NAME_SHELF = {"序号", "上架单号        ", "计划上架数量BU"};//上架主单列表标题
    public final static String[] TABLE_COLUMN_SHELF = {"shelfcode", "planynnum"};//上架主单对应的属性名称

    public final static String[] TABLE_NAME_SHELF_SWITCH = {"序号", "上架单号        "};//上架主单列表标题
    public final static String[] TABLE_COLUMN_SHELF_SWITCH = {"shelfcode",};//上架主单对应的属性名称
    //按照条码上架明细列表头
    public final static String[] TABLE_NAME_DETAIL = {"序号", "状态", "分配数", "分配数BU", "生产日期  ", "条码         "};//上架明细列表标题
    public final static String[] TABLE_COLUMN_DETAIL = {"skuStatus", "distrnum", "distrbu", "productiondate", "barCode"};//上架明细属性名称
    //上架方式
    public final static String barCodeShelf = "商品条码上架";
    public final static String palletCodeShelf = "托盘编码上架";
    public final static List<String> shelfTypeList = CollectionUtil.newList(barCodeShelf, palletCodeShelf);
    //选择的一条上架主单
    private InstockShelforder TL_selectedShelfOrder;
    private InstockShelforderDetail TL_selectedShelfDetail;
    private Integer TL_SUMACTUALYNBU = 0;//当前批次的实际上架总数
    private Integer TL_BATCH_RULE = 0;//商品批次规则
    @Autowired
    private PackaginginfoRemoteService packaginginfoRemoteService;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //清空本地环境变量
        TL_selectedShelfOrder = null;
        TL_selectedShelfDetail = null;
        TL_SUMACTUALYNBU = 0;
        TL_BATCH_RULE = 0;
        super.initBaseMap(ShelfOrder.class, pageHeader, ctx);
        ChannelPipeline pipeline = ctx.pipeline();
        QuickInstockParamManagerImpl quickInstockParamManager = (QuickInstockParamManagerImpl) pipeline.get("quickInstockParamManager");
        if(quickInstockParamManager != null){
            pipeline.remove(quickInstockParamManager);
            ShelfOrder shelfOrder = quickInstockParamManager.getShelfOrder();
            super.initBaseMap(ShelfOrder.class, pageHeader, ctx);
            resetCurCol(ORDER_CODE, getDataMap(), ctx);
            channelRead(ctx, shelfOrder.getOrderCode());
            channelRead(ctx, Constants.BREAK_LINE);
        }else {
            if (validateAutoShowShelfCode(ctx)) {
                HandlerUtil.moveUpN(ctx, 2);
                showShelfOrderSwitchPage(getDataMap(), ctx);
            } else {
                resetCurCol(ORDER_CODE, getDataMap(), ctx);
            }
        }

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {

        Map<String, Object> accepterMap = getDataMap();
        //处理不同返回值的跳转
        if (goToHandler(accepterMap, ctx)) {
            return;
        }
        receiveDataAndNotPrintNext(ctx, object, accepterMap);
        ShelfOrder shelfOrder = (ShelfOrder) accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (SELECT_SHELF_ORDER_SWITCH.equalsIgnoreCase(lastCompleteColName)) {
            //上架主单分页选择
            String selectShelfOrderSwitch = shelfOrder.getSelectShelfOrderSwitch();
            PageModel<InstockShelforder> pageModel = (PageModel<InstockShelforder>) accepterMap.get(PageUtil.PAGE_MODEL);
            int pageSizeCurr = (Integer) accepterMap.get(PageUtil.LINES_NUM_CLEAN_KEY);//页面当前数据条数
            if (KeyEnum.N_78.value.equalsIgnoreCase(selectShelfOrderSwitch)) {//下一页
                PageUtil.changePageNext(ctx, accepterMap, WmsConstants.KEY_INSTOCKSHELFORDER_PARAM, pageSizeCurr);
                showShelfOrderSwitchPage(accepterMap, ctx);
            } else if (KeyEnum.B_66.value.equalsIgnoreCase(selectShelfOrderSwitch)) {//上一页
                PageUtil.changePageUp(ctx, accepterMap, WmsConstants.KEY_INSTOCKSHELFORDER_PARAM, pageSizeCurr);
                showShelfOrderSwitchPage(accepterMap, ctx);
            } else {
                if (selectShelfOrderSwitch.matches(REG_SERIAL_NO)) {//大于0的正整数
                    List<InstockShelforder> shelfOrderList = pageModel.getDatas();
                    int maxIndex = shelfOrderList.size() - 1;
                    int index = PageUtil.getIndexFromSerialnoAndPageModle(selectShelfOrderSwitch, pageModel);
                    if (index > maxIndex || index < 0) {//只能输入当前页内显示的序号
                        colNeedReInput(SELECT_SHELF_ORDER_SWITCH, ErrorConstants.INPUT_FORMAT_ERROR + ErrorConstants.COMMON_COLON, accepterMap, ctx);
                    } else {
                        InstockShelforder instockShelforder = shelfOrderList.get(index);
                        TL_selectedShelfOrder = instockShelforder;
                        //根据选择的明细进行处理
                        resetCurCol(ORDER_CODE, accepterMap, ctx);
                        accepterMap.remove(PageUtil.PARA_PAGE_MAP);
                        //校验扫描的ASN单号,或者网络订单号
                        RemoteResult<Boolean> result = instockShelfRemoteService.validateInstockShelforder(getCredentialsVO(ctx), instockShelforder.getShelfcode());
                        if (result.isSuccess()) {
                            channelRead(ctx, instockShelforder.getShelfcode());
                            channelRead(ctx, Constants.BREAK_LINE);
                        } else {
                            colNeedReInput(SELECT_SHELF_ORDER_SWITCH, result.getResultCode(), accepterMap, ctx);
                        }
                    }
                } else {
                    colNeedReInput(SELECT_SHELF_ORDER_SWITCH, ErrorConstants.INPUT_FORMAT_ERROR + ErrorConstants.COMMON_COLON, accepterMap, ctx);
                }
            }
        }

        if (ORDER_CODE.equalsIgnoreCase(lastCompleteColName)) {
            //校验扫描的ASN单号,或者网络订单号
            RemoteResult<Boolean> result = instockShelfRemoteService.validateInstockShelforder(getCredentialsVO(ctx), shelfOrder.getOrderCode());
            if (result.isSuccess()) {
                //根据查询的数据数量进行分页显示或接收字段的跳转
                showShelfOrderPage(shelfOrder.getOrderCode(), accepterMap, ctx);
            } else {
                colNeedReInput(ORDER_CODE, result.getResultCode(), accepterMap, ctx);
            }
        }
        if (SELECT_SHELF_ORDER.equalsIgnoreCase(lastCompleteColName)) {
            //上架主单分页选择
            String selectShelfOrder = shelfOrder.getSelectShelfOrder();
            PageModel<InstockShelforder> pageModel = (PageModel<InstockShelforder>) accepterMap.get(PageUtil.PAGE_MODEL);
            int pageSizeCurr = (Integer) accepterMap.get(PageUtil.LINES_NUM_CLEAN_KEY);//页面当前数据条数
            if (KeyEnum.N_78.value.equalsIgnoreCase(selectShelfOrder)) {//下一页
                PageUtil.changePageNext(ctx, accepterMap, WmsConstants.KEY_INSTOCKSHELFORDER_PARAM, pageSizeCurr);
                showShelfOrderPage(shelfOrder.getOrderCode(), accepterMap, ctx);
            } else if (KeyEnum.B_66.value.equalsIgnoreCase(selectShelfOrder)) {//上一页
                PageUtil.changePageUp(ctx, accepterMap, WmsConstants.KEY_INSTOCKSHELFORDER_PARAM, pageSizeCurr);
                showShelfOrderPage(shelfOrder.getOrderCode(), accepterMap, ctx);
//                setColUnReceived(SELECT_SHELF_ORDER, accepterMap);
            } else {
                if (selectShelfOrder.matches(REG_SERIAL_NO)) {//大于0的正整数
                    List<InstockShelforder> shelfOrderList = pageModel.getDatas();
                    int maxIndex = shelfOrderList.size() - 1;
                    int index = PageUtil.getIndexFromSerialnoAndPageModle(selectShelfOrder, pageModel);
                    if (index > maxIndex || index < 0) {//只能输入当前页内显示的序号
                        colNeedReInput(SELECT_SHELF_ORDER, ErrorConstants.INPUT_FORMAT_ERROR + ErrorConstants.COMMON_COLON, accepterMap, ctx);
                    } else {
                        InstockShelforder instockShelforder = shelfOrderList.get(index);
                        TL_selectedShelfOrder = instockShelforder;
                        accepterMap.remove(PageUtil.PARA_PAGE_MAP);
                        //根据选择的明细进行处理
                        jumpToFieldFromShelfOrder(instockShelforder, accepterMap, ctx);
                    }
                } else {
                    colNeedReInput(SELECT_SHELF_ORDER, ErrorConstants.INPUT_FORMAT_ERROR + ErrorConstants.COMMON_COLON, accepterMap, ctx);
                }
            }
        }
        if (SHELF_TYPE.equalsIgnoreCase(lastCompleteColName)) {//选择上架方式
            String shelfType = shelfOrder.getShelfType();
            if (shelfType.equalsIgnoreCase(barCodeShelf)) {
                resetCurCol(BAR_CODE, accepterMap, ctx);
            } else if (shelfType.equalsIgnoreCase(palletCodeShelf)) {
                resetCurCol(PALLET_CODE, accepterMap, ctx);
            } else {
                setColReSwitchList(shelfTypeList, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
            }
        }
        if (BAR_CODE.equalsIgnoreCase(lastCompleteColName)) {
            //输入商品条码后进行一次查询，如果不存在数据则重新扫描条码
            PageModel<InstockShelforderDetail> pageModelRemoteResult = instockShelfRemoteService.queryShelfDetailPageModel(getCredentialsVO(ctx), makeQueryShelfDetailMap(shelfOrder, ctx));
            if (pageModelRemoteResult == null || pageModelRemoteResult.getTotalCount() == 0) {
                //如果查询不到数据重新接收商品条码字段
                HandlerUtil.errorBeep(ctx);//系统错误，响铃
                reInputBarCode(accepterMap, ctx);
            } else {
                //存在可操作数据，则选择日期类型
                TL_BATCH_RULE = pageModelRemoteResult.getDatas().get(0).getBatchrule();
                List<String> dateTypeList = Constants.batchRuleEnum.makeDateTypeList(TL_BATCH_RULE);
                showShelfDetailPage(shelfOrder, accepterMap, ctx);
            }
        }

        if (SELECTED_DETAIL.equalsIgnoreCase(lastCompleteColName)) {
            //扫描商品条码上架后处理分页操作
            String selectedDetail = shelfOrder.getSelectedDetail();
            PageModel<InstockShelforderDetail> pageModel = (PageModel<InstockShelforderDetail>) accepterMap.get(PageUtil.PAGE_MODEL);
            int pageSizeCurr = (Integer) accepterMap.get(PageUtil.LINES_NUM_CLEAN_KEY);//页面当前数据条数
            if (KeyEnum.N_78.value.equalsIgnoreCase(selectedDetail)) {//下一页
                PageUtil.changePageNext(ctx, accepterMap, WmsConstants.KEY_INSTOCKSHELFORDERDETAIL_PARAM, pageSizeCurr);
                showShelfDetailPage(shelfOrder, accepterMap, ctx);
            } else if (KeyEnum.B_66.value.equalsIgnoreCase(selectedDetail)) {//上一页
                PageUtil.changePageUp(ctx, accepterMap, WmsConstants.KEY_INSTOCKSHELFORDERDETAIL_PARAM, pageSizeCurr);
                showShelfDetailPage(shelfOrder, accepterMap, ctx);
            } else {
                if (selectedDetail.matches(REG_SERIAL_NO)) {//大于0的正整数
                    List<InstockShelforderDetail> shelfOrderDetailList = pageModel.getDatas();
                    int maxIndex = shelfOrderDetailList.size() - 1;
                    int index = PageUtil.getIndexFromSerialnoAndPageModle(selectedDetail, pageModel);
                    if (index > maxIndex || index < 0) {//只能输入当前页内显示的序号
                        colNeedReInput(SELECTED_DETAIL, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                    } else {
                        TL_selectedShelfDetail = shelfOrderDetailList.get(index);
                        TL_BATCH_RULE = shelfOrderDetailList.get(index).getBatchrule();
                        handleOneDetail(shelfOrder, accepterMap, ctx);
                    }
                } else {
                    colNeedReInput(SELECTED_DETAIL, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                }
            }
        }
        if (PALLET_CODE.equalsIgnoreCase(lastCompleteColName)) {
            //扫描托盘编码上架，按照托盘码查询一条数据
            String palletCode = shelfOrder.getPalletCode();
            InstockShelforder instockShelforder = TL_selectedShelfOrder;
            RemoteResult<InstockShelforderDetail> remoteResult = instockShelfRemoteService.getShelfDetailByPalletCode(getCredentialsVO(ctx), palletCode, instockShelforder.getShelfid());
            if (remoteResult.isSuccess()) {
                InstockShelforderDetail instockShelforderDetail = remoteResult.getT();
                TL_selectedShelfDetail = instockShelforderDetail;
                TL_BATCH_RULE = instockShelforderDetail.getBatchrule();
                handleOneDetail(shelfOrder, accepterMap, ctx);
            } else {
                colNeedReInput(PALLET_CODE, remoteResult.getResultCode(), accepterMap, ctx);
            }
        }

        if (CONFIRM_DATE.equalsIgnoreCase(lastCompleteColName)) {
            //接收输入的日期，需要与所选择明细的生产日期相同
            String confirmDate = shelfOrder.getConfirmDate();
            if (StringUtils.isBlank(confirmDate)) {
                colNeedReInput(CONFIRM_DATE, "日期为空", accepterMap, ctx);
                return;
            }
            Date InputDate = DateTimeUtil.parseSimpleStr(confirmDate);
            Date toConfirm = TL_selectedShelfDetail.getProductiondate();
            String errorMess = ErrorConstants.PRO_DATE_NOT_LEGAL;
            //细化批次需要检验失效日期
            if (Constants.batchRuleEnum.xiHua.code.equals(TL_BATCH_RULE)) {
                toConfirm = TL_selectedShelfDetail.getExpirationdate();
                errorMess = ErrorConstants.EXP_DATE_NOT_LEGAL;
            }

            if (DateTimeUtil.isSimpleDate(confirmDate) && InputDate.getTime() == toConfirm.getTime()) {
                resetCurCol(SCAN_WH_CODE, accepterMap, ctx);
            } else {
                colNeedReInput(CONFIRM_DATE, errorMess, accepterMap, ctx);
            }
        }

        if (SCAN_WH_CODE.equalsIgnoreCase(lastCompleteColName)) {
            //接收并校验上架库位编码
            String scanWHCode = shelfOrder.getScanWHCode();
            boolean isPalletShelf = shelfOrder.getShelfType().equals(palletCodeShelf);//是否托盘码方式上架

            RemoteResult<Boolean> result = instockShelfRemoteService.validateWareHouseForShelf(getCredentialsVO(ctx), scanWHCode, TL_selectedShelfDetail, isPalletShelf);
            if (result.isSuccess() && result.getT()) {
                tipUnShelfNum(accepterMap, ctx);//提示未上架数量
                if (palletCodeShelf.equals(shelfOrder.getShelfType())) {
                    resetCurCol(CONFIRM_SHELF_NUM, accepterMap, ctx);
                } else {
                    setColUnReceived(SHELF_NUM, accepterMap);
                    resetCurCol(SHELF_NUM, accepterMap, ctx);
                }
            } else {
                colNeedReInput(SCAN_WH_CODE, result.getResultCode(), accepterMap, ctx);
            }
        }


        if (SHELF_NUM.equalsIgnoreCase(lastCompleteColName)) {
            String shelfNum = shelfOrder.getShelfNum();
            confirmShelf(shelfNum, shelfOrder, accepterMap, ctx);
        }

        if (CONFIRM_SHELF_NUM.equalsIgnoreCase(lastCompleteColName)) {
            String confirmShelfNum = shelfOrder.getConfirmShelfNum();
            confirmShelf(confirmShelfNum, shelfOrder, accepterMap, ctx);
        }
    }

    private void showShelfOrderSwitchPage(Map<String, Object> accepterMap, ChannelHandlerContext ctx) throws Exception {
        CredentialsVO credentialsVO = getCredentialsVO(ctx);
        PageModel<InstockShelforder> pageModelRemoteResult = instockShelfRemoteService.queryShelfOrderPageModel(credentialsVO, makeQueryShelfOrderBySwitchMap(credentialsVO, ctx));
        if (pageModelRemoteResult == null || pageModelRemoteResult.getTotalCount() == 0) {
            //如果查询不到数据重新接收上架单号字段
            HandlerUtil.errorBeep(ctx);//系统错误，响铃
            HandlerUtil.moveDownN(ctx, 4);
            reInputOrderCode(accepterMap, ctx);
            return;
        } else if (pageModelRemoteResult.getTotalCount() == 1) {
            //如果只有一条主单数据则通过主单的明细确定下一步的接收
            InstockShelforder instockShelforder = pageModelRemoteResult.getDatas().get(0);
            TL_selectedShelfOrder = instockShelforder;
            resetCurCol(ORDER_CODE, accepterMap, ctx);
            accepterMap.remove(PageUtil.PARA_PAGE_MAP);
            HandlerUtil.moveDownN(ctx, 1);
            channelRead(ctx, instockShelforder.getShelfcode());
            channelRead(ctx, Constants.BREAK_LINE);
        } else {
            //多条数据进行分页显示
            accepterMap.put(PageUtil.PAGE_MODEL, pageModelRemoteResult);
            HandlerUtil.changeRow(ctx);
            int currPageLinesNum = PageUtil.showTable(ctx, pageModelRemoteResult, TABLE_NAME_SHELF_SWITCH, TABLE_COLUMN_SHELF_SWITCH, true, true, null);//展示列表，带有序号
            accepterMap.put(PageUtil.LINES_NUM_CLEAN_KEY, currPageLinesNum);
            Boolean flag = (Boolean) accepterMap.get(MOVE_FLAG) == null ? false : true;
            if (flag) {
                HandlerUtil.moveUpN(ctx, 1);//因为分页工具中已经输出了回车，所以手动上移一行
            } else {
                accepterMap.put(MOVE_FLAG, false);
            }
            setColUnReceived(SELECT_SHELF_ORDER_SWITCH, accepterMap);
            resetCurCol(SELECT_SHELF_ORDER_SWITCH, accepterMap, ctx);
        }


    }

    /**
     * 查询可操作的上架单分页条件
     *
     * @param credentialsVO
     * @param ctx
     */
    private HashMap<String, Object> makeQueryShelfOrderBySwitchMap(CredentialsVO credentialsVO, ChannelHandlerContext ctx) {
        Map<String, Object> accepterMap = getDataMap();
        HashMap<String, Object> map = (HashMap<String, Object>) accepterMap.get(PageUtil.PARA_PAGE_MAP);//缓存中的分页查询参数Map
        if (map == null) {
            final InstockShelforder instockShelforder = new InstockShelforder();
            instockShelforder.setPage(Constants.PAGE_START);
            instockShelforder.setRows(Constants.PAGE_SIZE_THR);
            //用于查询明细中的上架为为当前用户ID或为空
            instockShelforder.setOperatorid(getCurrentUserId(ctx));
            instockShelforder.setSidx("shelfid");
            instockShelforder.setSord(Constants.PAGE_SORT_ASC);
            map = new HashMap<String, Object>() {{
                put(WmsConstants.KEY_INSTOCKSHELFORDER_PARAM, instockShelforder);
            }};
            accepterMap.put(PageUtil.PARA_PAGE_MAP, map);
        }
        return map;
    }


    /**
     * 托盘方式上架，维护开关，并且值为"Y",需要确认日期。商品条码上架，需要确认日期
     *
     * @param shelfOrder
     * @param ctx
     * @return
     */
    private boolean validateNeedConfirmDate(ShelfOrder shelfOrder, ChannelHandlerContext ctx) {
        if (shelfOrder.getShelfType().equals(palletCodeShelf)) {
            RemoteResult<String> result = instockShelfRemoteService.validateNeedConfirmDate(getCredentialsVO(ctx));
            if (result != null && result.isSuccess() && result.getT() != null) {
                if (Constants.CONFIRM_Y.equals(result.getT())) {
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * 是否自动显示上架单号，维护开关，并且值为"Y",不自动显示。没有维护开关不自动显示
     *
     * @param ctx
     * @return
     */
    private boolean validateAutoShowShelfCode(ChannelHandlerContext ctx) {
        RemoteResult<String> result = instockShelfRemoteService.validateAutoShowShelfCode(getCredentialsVO(ctx));
        if (result != null && result.isSuccess() && result.getT() != null) {
            if (Constants.CANCEL_N.equals(result.getT())) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 确认上架
     *
     * @param confirm     接收到的上架数或YN
     * @param shelfOrder  数据接收的业务对象
     * @param accepterMap
     * @param ctx
     */
    private void confirmShelf(String confirm, ShelfOrder shelfOrder, Map<String, Object> accepterMap, ChannelHandlerContext ctx) throws Exception {
        boolean palletType = false;
        //如果是托盘编码上架方式只接收YN
        InstockShelforderDetail instockShelforderDetail = TL_selectedShelfDetail;
        if (shelfOrder.getShelfType().equals(palletCodeShelf)) {
            palletType = true;
            instockShelforderDetail.setPalletcode(shelfOrder.getPalletCode());//托盘码
            if (!confirm.matches(REG_YN)) {
                colNeedReInput(CONFIRM_SHELF_NUM, ErrorConstants.ONLY_YN_AND_OTHER, accepterMap, ctx);
                return;
            } else if (confirm.equalsIgnoreCase(Constants.CANCEL_N)) {
                //如果输入N回到初始界面
                channelActive(ctx);
                return;
            }
        } else {
            //如果是商品条码上架，需要接收大于0的正整数，且不能大于当前批次的预期上架数量
            if (!confirm.matches(REG_SERIAL_NO) || confirm.length() > 9) {
                colNeedReInput(SHELF_NUM, ErrorConstants.POSITIVE_INTEGER, accepterMap, ctx);
                return;
            } else if (instockShelforderDetail.getDistrbu() < (Integer.parseInt(confirm) + TL_SUMACTUALYNBU)) {
                colNeedReInput(SHELF_NUM, ErrorConstants.TOO_MUCH_SHELF_NUM, accepterMap, ctx);
                return;
            }
        }
        //校验通过进行最后的数据提交，在soa端进行总体校验
        Integer shelfNum = 0;
        if (!palletType) {
            shelfNum = Integer.parseInt(confirm);
        }
        CredentialsVO credentialsVO = getCredentialsVO(ctx);
        RemoteResult<Integer> remoteResult = this.instockShelfRemoteService.confirmShelf(credentialsVO, instockShelforderDetail, shelfOrder.getScanWHCode(), shelfNum, palletType);
        Integer result = remoteResult.getT();
        String errorMess = remoteResult.getResultCode();
        if (remoteResult == null || WMSErrorMess.SYS_ERROR.equals(result)) {
            //系统错误
            HandlerUtil.errorBeep(ctx);//系统错误，响铃
            errorMess = errorMess + ErrorConstants.TIP_TO_CONTINUE;
            accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ACTIVE);
        } else if (WMSErrorMess.SHELF_ALL_COMPLETE.equals(result)) {
            //整单完成上架
            errorMess = TipConstants.SHELF_ALL_COMPLETE + ErrorConstants.TIP_TO_CONTINUE;
            accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ACTIVE);
        } else if (WMSErrorMess.PALLET_SHELF.equals(result)) {
            //托盘码上架，打印主单号后选择上架方式
            errorMess = TipConstants.PALLET_COMPLETE + ErrorConstants.TIP_TO_CONTINUE;
            accepterMap.put(GO_TO_FLAG, TO_SHELF_TYPE);
        } else if (WMSErrorMess.BARCODE_SHELF_UN_FINISH.equals(result)) {
            //商品条码上架，且本批次未完成
            errorMess = TipConstants.CONTINUE_INPUT_NUM;
            accepterMap.put(GO_TO_FLAG, TO_RE_INPUT_NUM);
        }
        printBeforeNextField(errorMess, accepterMap, ctx);
    }

    public boolean goToHandler(Map<String, Object> accepterMap, ChannelHandlerContext ctx) throws Exception {
        if (accepterMap.get(GO_TO_FLAG) != null) {
            ShelfOrder shelfOrder = (ShelfOrder) accepterMap.get(DefaultKey.objectClass.keyName);
            Integer goToAim = (Integer) accepterMap.get(GO_TO_FLAG);
            accepterMap.remove(GO_TO_FLAG);//清空一下，避免重复调用
            // 根据不同的返回值回到不同的步骤
            if (TO_CHANNEL_ACTIVE.equals(goToAim)) {
                channelActive(ctx);
                return true;
            } else if (TO_SHELF_TYPE.equals(goToAim)) {
                //打印主单号，重新选择上架方式
                shelfOrder.setOrderCode(TL_selectedShelfOrder.getShelfcode());//此处只显示所选择的上架主单的单号
                List<String> fieldList = CollectionUtil.newList(ORDER_CODE);
                printFieldsAndReceiveData(pageHeader, fieldList, null, "", accepterMap, ctx);
                jumpToFieldFromShelfOrder(TL_selectedShelfOrder, accepterMap, ctx);
                return true;
            } else if (TO_RE_INPUT_NUM.equals(goToAim)) {//modify by wangzhanhua 如果不是整单上架完成，重新查询该商品是否还有当前登录人可操作数据
                //显示主单号、上架方式、商品条码、日期类型、上架明细分页数据，重新选择分页
                shelfOrder.setOrderCode(TL_selectedShelfOrder.getShelfcode());//此处只显示所选择的上架主单的单号
                PageModel<InstockShelforderDetail> pageModelRemoteResult = instockShelfRemoteService.queryShelfDetailPageModel(getCredentialsVO(ctx), makeQueryShelfDetailMap(shelfOrder, ctx));
                if (pageModelRemoteResult == null || pageModelRemoteResult.getTotalCount() == 0) {
                    //如果按照当前商品商品查询不到明细数据则进入到选择上架方式的步骤
                    List<String> fieldList = CollectionUtil.newList(ORDER_CODE);
                    printFieldsAndReceiveData(pageHeader, fieldList, null, "", accepterMap, ctx);
                    //重新查询明细状态，确认下一步的接收字段为操作方式或直接输入托盘或直接输入商品条码
                    jumpToFieldFromShelfOrder(TL_selectedShelfOrder, accepterMap, ctx);
                } else {
                    List<String> fieldList = CollectionUtil.newList(ORDER_CODE, SHELF_TYPE, BAR_CODE);
                    printFieldsAndReceiveData(pageHeader, fieldList, "", accepterMap, ctx);
                    showShelfDetailPage(shelfOrder, accepterMap, ctx);
                    if (pageModelRemoteResult.getTotalCount() > 1) {
                        setColUnReceived(SELECTED_DETAIL, accepterMap);
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 显示按照上架单号或网络订单号查询上架单号的分页
     *
     * @param orderCode 上架单号或网络订单号
     * @param ctx
     */
    private void showShelfOrderPage(String orderCode, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        PageModel<InstockShelforder> pageModelRemoteResult = instockShelfRemoteService.queryShelfOrderPageModel(getCredentialsVO(ctx), makeQueryShelfOrderMap(orderCode, ctx));
        if (pageModelRemoteResult == null || pageModelRemoteResult.getTotalCount() == 0) {
            //如果查询不到数据重新接收上架单号字段
            HandlerUtil.errorBeep(ctx);//系统错误，响铃
            accepterMap.remove(PageUtil.PARA_PAGE_MAP);
            RemoteResult<String> result = instockShelfRemoteService.queryShelferNameFromRFLog(getCredentialsVO(ctx), orderCode);
            String operator = result.getT();
            if (StringUtils.isEmpty(operator)) {
                colNeedReInput(ORDER_CODE, "无可操作上架单", accepterMap, ctx);
            } else {
                colNeedReInput(ORDER_CODE, "单号" + orderCode + "已经被" + operator + "操作上架", accepterMap, ctx);
            }
            return;
        } else if (pageModelRemoteResult.getTotalCount() == 1) {
            //如果只有一条主单数据则通过主单的明细确定下一步的接收
            InstockShelforder instockShelforder = pageModelRemoteResult.getDatas().get(0);
            TL_selectedShelfOrder = instockShelforder;
            accepterMap.remove(PageUtil.PARA_PAGE_MAP);
            jumpToFieldFromShelfOrder(instockShelforder, accepterMap, ctx);
        } else {
            //多条数据进行分页显示
            accepterMap.put(PageUtil.PAGE_MODEL, pageModelRemoteResult);
            HandlerUtil.changeRow(ctx);
            int currPageLinesNum = PageUtil.showTable(ctx, pageModelRemoteResult, TABLE_NAME_SHELF, TABLE_COLUMN_SHELF, true, true, null);//展示列表，带有序号
            accepterMap.put(PageUtil.LINES_NUM_CLEAN_KEY, currPageLinesNum);
            HandlerUtil.moveUpN(ctx, 1);//因为分页工具中已经输出了回车，所以手动上移一行
            setColUnReceived(SELECT_SHELF_ORDER, accepterMap);
            resetCurCol(SELECT_SHELF_ORDER, accepterMap, ctx);
        }
    }

    /**
     * 构造上架主单的查询条件
     *
     * @param orderCode 上架单号或网络订单号
     * @return 返回map查询条件
     */
    private HashMap<String, Object> makeQueryShelfOrderMap(String orderCode, ChannelHandlerContext ctx) {
        Map<String, Object> accepterMap = getDataMap();
        HashMap<String, Object> map = (HashMap<String, Object>) accepterMap.get(PageUtil.PARA_PAGE_MAP);//缓存中的分页查询参数Map
        if (map == null) {
            final InstockShelforder instockShelforder = new InstockShelforder();
            instockShelforder.setPage(Constants.PAGE_START);
            instockShelforder.setRows(Constants.REASON_PAGE_SIZE);
            //用于查询明细中的上架为为当前用户ID或为空
            instockShelforder.setOperatorid(getCurrentUserId(ctx));
            //上架单号
            instockShelforder.setShelfcode(orderCode);
            //网络订单号
            instockShelforder.setNetordercode(orderCode);
            map = new HashMap<String, Object>() {{
                put(WmsConstants.KEY_INSTOCKSHELFORDER_PARAM, instockShelforder);
            }};
            accepterMap.put(PageUtil.PARA_PAGE_MAP, map);
        }
        return map;
    }

    /**
     * 根据确认的上架主单检测后续的接收方式及数据显示
     *
     * @param instockShelforder 上架主单数据
     * @param accepterMap
     * @param ctx
     */
    private void jumpToFieldFromShelfOrder(InstockShelforder instockShelforder, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        List<InstockShelforderDetail> ShelfDetailList = this.instockShelfRemoteService.queryShelfDetailList(getCredentialsVO(ctx), instockShelforder);
        //如果只查询到一条上架单则查此单下的全部可操作上架明细，如果无可操作明细则重新扫描主单号
        if (ShelfDetailList == null || ShelfDetailList.size() == 0) {
            //重新接收上架单号字段
            reInputOrderCode(accepterMap, ctx);
            return;
        }
        int nullPalletDetail = 0;//没有托盘编码的明细数据
        int notNullPalletDetail = 0;//有托盘编码的明细数据
        for (InstockShelforderDetail shelfDetail : ShelfDetailList) {
            if (StringUtils.isEmpty(shelfDetail.getPalletcode())) {
                nullPalletDetail++;
            } else {
                notNullPalletDetail++;
            }
        }
        ShelfOrder shelfOrder = (ShelfOrder) accepterMap.get(DefaultKey.objectClass.keyName);
        if (notNullPalletDetail == 0) {
            //如果托盘编码全都为空则打印上架方式为商品条码上架，字段接收跳转到扫描商品条码
            super.printBeforeNextField(ShelfOrder.SHELF_TYPE + barCodeShelf, accepterMap, ctx);
            Reflections.invokeSetter(shelfOrder, SHELF_TYPE, barCodeShelf);//设置上架方式为商品条码上架
            resetCurCol(BAR_CODE, accepterMap, ctx);
        } else if (nullPalletDetail == 0) {
            //如果明细中的托盘编码全都不为空则打印上架方式为托盘码上架，字段接收跳转到扫描托盘编码
            super.printBeforeNextField(ShelfOrder.SHELF_TYPE + palletCodeShelf, accepterMap, ctx);
            Reflections.invokeSetter(shelfOrder, SHELF_TYPE, palletCodeShelf);//设置上架方式为托盘编码上架
            resetCurCol(PALLET_CODE, accepterMap, ctx);
        } else {
            //如果托盘编码为空、不为空的数据全都存在则选择上架方式
            super.setColSwitchList(SHELF_TYPE, shelfTypeList, accepterMap, ctx);
        }
        //清空一下分页参数，否则商品信息查询明细的时候会造成参数重复
        accepterMap.remove(PageUtil.PARA_PAGE_MAP);
    }

    /**
     * 设置重新接收上架单号字段
     *
     * @param accepterMap
     * @param ctx
     */
    private void reInputOrderCode(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        //清空参数map
        accepterMap.remove(PageUtil.PARA_PAGE_MAP);
        colNeedReInput(ORDER_CODE, "无可操作上架单", accepterMap, ctx);
    }

    /**
     * 按照商品条码查询可操作明细的分页处理
     *
     * @param businessShelfOrder 数据接收业务对象
     * @param ctx
     */
    private void showShelfDetailPage(ShelfOrder businessShelfOrder, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        PageModel<InstockShelforderDetail> pageModelRemoteResult = instockShelfRemoteService.queryShelfDetailPageModel(getCredentialsVO(ctx), makeQueryShelfDetailMap(businessShelfOrder, ctx));
        if (pageModelRemoteResult == null || pageModelRemoteResult.getTotalCount() == 0) {
            //重新接收商品条码字段
            reInputBarCode(accepterMap, ctx);
        } else if (pageModelRemoteResult.getTotalCount() == 1) {
            //如果只有一条明细数据则通过主单的明细确定下一步的接收
            InstockShelforderDetail shelfDetail = pageModelRemoteResult.getDatas().get(0);
            TL_BATCH_RULE = shelfDetail.getBatchrule();
            TL_selectedShelfDetail = shelfDetail;
            handleOneDetail(businessShelfOrder, accepterMap, ctx);
        } else {
            //多条数据进行分页显示
            accepterMap.put(PageUtil.PAGE_MODEL, pageModelRemoteResult);
            HandlerUtil.changeRow(ctx);
            //如果是洗化批次规则则显示失效日期
            Integer batchRule = pageModelRemoteResult.getDatas().get(0).getBatchrule();
            if (Constants.batchRuleEnum.xiHua.code.equals(batchRule)) {
                TABLE_NAME_DETAIL[4] = Constants.batchRuleEnum.xiHua.dateType + "  ";//补充空格否则日期会换行
                TABLE_COLUMN_DETAIL[3] = "expirationdate";
            } else {
                TABLE_NAME_DETAIL[4] = Constants.batchRuleEnum.puTong.dateType + "  ";//补充空格否则日期会换行
                TABLE_COLUMN_DETAIL[3] = "productiondate";
            }

            int currPageLinesNum = PageUtil.showTable(ctx, pageModelRemoteResult, TABLE_NAME_DETAIL, TABLE_COLUMN_DETAIL, true, true, null);//展示列表，带有序号
            accepterMap.put(PageUtil.LINES_NUM_CLEAN_KEY, currPageLinesNum);
            HandlerUtil.moveUpN(ctx, 1);//因为分页工具中已经输出了回车，所以手动上移一行
            setColUnReceived(SELECTED_DETAIL, accepterMap);
            resetCurCol(SELECTED_DETAIL, accepterMap, ctx);
//            HandlerUtil.write(ctx, ShelfOrder.SELECT_SERIAL_NO);
        }
    }

    /**
     * 构造上架主单的查询条件
     *
     * @param businessShelfOrder 数据接收业务对象
     * @return 返回map查询条件
     */
    private HashMap<String, Object> makeQueryShelfDetailMap(ShelfOrder businessShelfOrder, ChannelHandlerContext ctx) {
        Map<String, Object> accepterMap = getDataMap();
        HashMap<String, Object> queryMap = (HashMap<String, Object>) accepterMap.get(PageUtil.PARA_PAGE_MAP);//缓存中的分页查询参数Map
        if (queryMap == null) {
            final InstockShelforderDetail shelfDetail = new InstockShelforderDetail();
            shelfDetail.setPage(Constants.PAGE_START);
            shelfDetail.setRows(Constants.REASON_PAGE_SIZE);
            queryMap = new HashMap<String, Object>();
            queryMap.put(WmsConstants.KEY_INSTOCKSHELFORDERDETAIL_PARAM, shelfDetail);
            //用于查询明细中的上架人为当前用户ID或为空
            InstockShelforder shelfOrder = TL_selectedShelfOrder;
            shelfOrder.setOperatorid(getCurrentUserId(ctx));
            queryMap.put(WmsConstants.KEY_INSTOCKSHELFORDER_PARAM, shelfOrder);
            //当前商品条码
            queryMap.put(WmsConstants.KEY_BASE_BARCODE, businessShelfOrder.getBarCode());
            accepterMap.put(PageUtil.PARA_PAGE_MAP, queryMap);
        }
        return queryMap;
    }


    /**
     * 处理选择的一条上架明细数据
     *
     * @param accepterMap
     * @param ctx
     */
    private void handleOneDetail(ShelfOrder shelfOrder, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        InstockShelforderDetail shelfDetail = TL_selectedShelfDetail;
        BasePackaginginfo packaginginfo = new BasePackaginginfo();
        packaginginfo.setSkuid(TL_selectedShelfDetail.getSkuid());
        packaginginfo.setPacklevel(WmsConstants.PACK_LEVEL_TWO);//二级包装
        packaginginfo.setPackstatus(WmsConstants.STATUS_ENABLE);//生效
        RemoteResult<BasePackaginginfo> packinfoResult = packaginginfoRemoteService.getPackagingInfoByCondition(getCredentialsVO(ctx), packaginginfo);
        if (packinfoResult != null && packinfoResult.isSuccess()) {//维护了二级包装，提示箱规
            BasePackaginginfo pack = packinfoResult.getT();
            super.printBeforeNextField(TipConstants.PKNUM_LEVEL_TWO + pack.getPknum(), accepterMap, ctx);
        }
        String goodsSkuAndBarCode = TipConstants.GOODS_SKU + shelfDetail.getSkuid() + "  " + TipConstants.GOODS_BARCODE + shelfDetail.getBarCode();
        super.printBeforeNextField(goodsSkuAndBarCode, accepterMap, ctx);
        String goodsName = RFUtil.makeStrFitPda(shelfDetail.getSkuname(), TipConstants.GOODS_NAME, 2);
        super.printBeforeNextField(TipConstants.GOODS_NAME + goodsName, accepterMap, ctx);

        Integer batchRule = TL_BATCH_RULE;
        if (Constants.batchRuleEnum.xiHua.code.equals(batchRule)) {
            //洗化批次规则显示失效日期，其它的批次规则显示生效日期
            super.printBeforeNextField(TipConstants.GOODS_EXPIRATION_DATE + DateTimeUtil.getStringSimple(shelfDetail.getExpirationdate()), accepterMap, ctx);
        } else {
            super.printBeforeNextField(TipConstants.GOODS_PRODUCTION_DATE + DateTimeUtil.getStringSimple(shelfDetail.getProductiondate()), accepterMap, ctx);
        }

        super.printBeforeNextField(TipConstants.SKU_STATUS + CheckReasonEnum.getNameByValue(shelfDetail.getSkuStatus()), accepterMap, ctx);
        super.printBeforeNextField(TipConstants.SHELF_DETAIL_DISTRBU + shelfDetail.getDistrbu(), accepterMap, ctx);
 /*       if (shelfDetail.getIssamesku() != null && WmsConstants.CON_YES == shelfDetail.getIssamesku()) {
            super.printBeforeNextField(TipConstants.IS_SAME_SKU, accepterMap, ctx);
        }*/
        //跳转到日期接收步骤
        if (validateNeedConfirmDate(shelfOrder, ctx)) {
            resetCurCol(CONFIRM_DATE, accepterMap, ctx);
        } else {
            resetCurCol(SCAN_WH_CODE, accepterMap, ctx);
        }
    }

    /**
     * 设置重新接收商品条码字段
     *
     * @param accepterMap
     * @param ctx
     */
    private void reInputBarCode(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        //清空参数map
        accepterMap.remove(PageUtil.PARA_PAGE_MAP);
        ShelfOrder shelfOrder = (ShelfOrder) accepterMap.get(DefaultKey.objectClass.keyName);
        //校验商品条码是否存在于基础信息表，是否失效，是否存在于该订单，提示语细化
        RemoteResult<Boolean> result = instockShelfRemoteService.validateBaseGoodsShelfOrder(getCredentialsVO(ctx), TL_selectedShelfOrder.getShelfid(), shelfOrder.getBarCode());
        if (result.isSuccess()) {
            colNeedReInput(BAR_CODE, "请重新扫描商品条码" + shelfOrder.getBarCode(), accepterMap, ctx);
        } else {
            colNeedReInput(BAR_CODE, result.getResultCode(), accepterMap, ctx);
        }
    }

    /**
     * 提示上架明细中的未上架数量
     *
     * @param accepterMap map数据容器
     * @param ctx         handler上下文容器
     */
    private void tipUnShelfNum(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        InstockShelforderDetail instockShelforderDetail = TL_selectedShelfDetail;
//        int sumActualYnBU = instockShelfRemoteService.getShelfActualYnBU(getCredentialsVO(ctx), instockShelforderDetail);
        TL_SUMACTUALYNBU = instockShelforderDetail.getActualynbu();
        //提示该批次商品未上架数量
        printBeforeNextField(TipConstants.SHELF_DETAIL_UN_SHELF_BU + (instockShelforderDetail.getDistrbu() - TL_SUMACTUALYNBU), accepterMap, ctx);
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

}
