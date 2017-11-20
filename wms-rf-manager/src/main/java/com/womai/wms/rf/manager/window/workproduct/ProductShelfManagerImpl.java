package com.womai.wms.rf.manager.window.workproduct;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.*;
import com.womai.wms.rf.domain.instock.ShelfOrder;
import com.womai.wms.rf.domain.workproduct.ProductShelf;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.instock.InstockShelfRemoteService;
import com.womai.wms.rf.remote.workproduct.ProductShelfRemoteService;
import com.womai.zlwms.rfsoa.api.WMSErrorMess;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.instock.InstockShelforderDetail;
import com.womai.zlwms.rfsoa.domain.workproduct.WorkProductInfo;
import com.womai.zlwms.rfsoa.domain.workproduct.WorkProductShelf;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * ClassDescribe: 加工上架Manager
 * Author :zhangwei
 * Date: 2017-03-07
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component("productShelfManager")
public class ProductShelfManagerImpl extends ReceiveManager {
    @Autowired
    private ProductShelfRemoteService productShelfRemoteService;
    @Autowired
    private InstockShelfRemoteService instockShelfRemoteService;

    private final static String GO_TO_FLAG = "shelfOrder_goto";//跳转标志
    private final static Integer TO_CHANNEL_ACTIVE = -1;//跳转标志
    private final static Integer TO_SHELF_TYPE = 1;//托盘码上架，回到选择上架方式的步骤
    private final static Integer TO_SELECT_DETAIL = 2;//商品条码上架，回到选择明细的步骤
    private final static Integer TO_RE_INPUT_NUM = 3;//商品条码上架，继续输入上架数量
    private final static String[] pageHeader = {"", TipConstants.PROCESS_SHELF_TITLE, Constants.SPLIT, ""};

    private final static String ORDER_CODE = "orderCode";//上架单号或网络订单号
    private final static String SHELF_TYPE = "shelfType";//上架方式，扫描商品条码或托盘码
    private final static String BAR_CODE = "barCode";//商品条码
    //    private final static String DATE_TYPE = "dateType";//日期类型
    private final static String PALLET_CODE = "palletCode";//托盘编码
    private final static String SELECTED_DETAIL = "selectedDetail";//分页方式选择明细
    private final static String SCAN_WH_CODE = "scanWHCode";//扫描库位编码
    private final static String CONFIRM_DATE = "confirmDate";//输入生产日期进行确认
    private final static String SHELF_NUM = "shelfNum";//上架数量
    private final static String CONFIRM_SHELF_NUM = "confirmShelfNum";//托盘码上架方式，需要输入YN进行确认，一次性上架全部BU
    //上架方式
    public final static String barCodeShelf = "商品条码上架";
    public final static String palletCodeShelf = "托盘编码上架";
    public final static List<String> shelfTypeList = CollectionUtil.newList(barCodeShelf, palletCodeShelf);
    //按照条码上架明细列表头
    public final static String[] TABLE_NAME_DETAIL = {"序号", "单位", "计划数", "计划数BU", "生产日期  ", "条码         "};//上架明细列表标题
    public final static String[] TABLE_COLUMN_DETAIL = {"unitname", "plannum", "planbu", "productiondate", "barCode"};//上架明细属性名称
    //保存数据的全局变量
    private WorkProductInfo workProductInfo;
    private WorkProductShelf workProductShelf;
    private Integer TL_SUMACTUALYNBU = 0;//当前批次的实际上架总数
    private Integer TL_BATCH_RULE = 0;//商品批次规则


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.initBaseMap(ProductShelf.class, pageHeader, ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        Map<String, Object> accepterMap = getDataMap();
        //处理不同返回值的跳转
        if (goToHandler(accepterMap, ctx)) {
            return;
        }
        receiveDataAndNotPrintNext(ctx, object, accepterMap);
        ProductShelf reveiveProductShelf = (ProductShelf) accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);

        if (ORDER_CODE.equals(lastCompleteColName)) {
            String orderCode = reveiveProductShelf.getOrderCode();
            RemoteResult<Map<String, Object>> remoteResult = productShelfRemoteService.getProductInfoByNO(getCredentialsVO(ctx), orderCode);
            if (remoteResult.isSuccess()) {
                Map<String, Object> resultMap = remoteResult.getT();
                WorkProductInfo productInfo = (WorkProductInfo) resultMap.get(WmsConstants.KEY_WORK_PRODUCT_INFO);
                workProductInfo = productInfo;//保存全局变量
                List<WorkProductShelf> productShelfList = (List<WorkProductShelf>) resultMap.get(WmsConstants.KEY_WORK_PRODUCT_SHELF_LIST);
                //按照明细中托盘编码的值判断下一步的接收
                jumpToFieldFromShelfOrder(productShelfList, accepterMap, ctx);
            } else {
                colNeedReInput(ORDER_CODE, remoteResult.getResultCode(), accepterMap, ctx);
            }
        }
        if (SHELF_TYPE.equals(lastCompleteColName)) {
            String shelfType = reveiveProductShelf.getShelfType();
            if (shelfType.equalsIgnoreCase(barCodeShelf)) {
                resetCurCol(BAR_CODE, accepterMap, ctx);
            } else if (shelfType.equalsIgnoreCase(palletCodeShelf)) {
                resetCurCol(PALLET_CODE, accepterMap, ctx);
            } else {
                setColReSwitchList(shelfTypeList, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
            }
        }
        if (BAR_CODE.equals(lastCompleteColName)) {
            //处理商品条码分页查询
            showShelfDetailPage(reveiveProductShelf, accepterMap, ctx);
        }

        if (SELECTED_DETAIL.equals(lastCompleteColName)) {
            //扫描商品条码上架后处理分页操作
            String selectedDetail = reveiveProductShelf.getSelectedDetail();
            PageModel<WorkProductShelf> pageModel = (PageModel<WorkProductShelf>) accepterMap.get(PageUtil.PAGE_MODEL);
            int pageSizeCurr = (Integer) accepterMap.get(PageUtil.LINES_NUM_CLEAN_KEY);//页面当前数据条数
            if (KeyEnum.N_78.value.equalsIgnoreCase(selectedDetail)) {//下一页
                PageUtil.changePageNext(ctx, accepterMap, WmsConstants.KEY_WORK_PRODUCT_SHELF, pageSizeCurr);
                showShelfDetailPage(reveiveProductShelf, accepterMap, ctx);
            } else if (KeyEnum.B_66.value.equalsIgnoreCase(selectedDetail)) {//上一页
                PageUtil.changePageUp(ctx, accepterMap, WmsConstants.KEY_WORK_PRODUCT_SHELF, pageSizeCurr);
                showShelfDetailPage(reveiveProductShelf, accepterMap, ctx);
            } else {
                if (selectedDetail.matches(TipConstants.REG_SERIAL_NO)) {//大于0的正整数
                    List<WorkProductShelf> shelfOrderDetailList = pageModel.getDatas();
                    int maxIndex = shelfOrderDetailList.size() - 1;
                    int index = PageUtil.getIndexFromSerialnoAndPageModle(selectedDetail, pageModel);
                    if (index > maxIndex || index < 0) {//只能输入当前页内显示的序号
                        colNeedReInput(SELECTED_DETAIL, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                    } else {
                        workProductShelf = shelfOrderDetailList.get(index);
                        TL_BATCH_RULE = shelfOrderDetailList.get(index).getBatchrule();
                        handleOneDetail(accepterMap, ctx);
                    }
                } else {
                    colNeedReInput(SELECTED_DETAIL, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                }
            }
        }

        if (PALLET_CODE.equals(lastCompleteColName)) {
            //扫描托盘编码上架，按照托盘码查询一条数据
            String palletCode = reveiveProductShelf.getPalletCode();
            WorkProductShelf queryProductShelf = new WorkProductShelf();
            queryProductShelf.setWorkid(workProductInfo.getId());
            queryProductShelf.setPalletcode(palletCode);
            RemoteResult<WorkProductShelf> remoteResult = productShelfRemoteService.getProductShelfByPalletCode(getCredentialsVO(ctx), queryProductShelf);
            if (remoteResult.isSuccess()) {
                workProductShelf = remoteResult.getT();
                TL_BATCH_RULE = queryProductShelf.getBatchrule();
                handleOneDetail(accepterMap, ctx);
            } else {
                colNeedReInput(PALLET_CODE, remoteResult.getResultCode(), accepterMap, ctx);
            }
        }

        if (CONFIRM_DATE.equals(lastCompleteColName)) {
            //接收输入的日期，需要与所选择明细的生产日期相同
            String confirmDate = reveiveProductShelf.getConfirmDate();
            if (StringUtils.isBlank(confirmDate)) {
                colNeedReInput(CONFIRM_DATE, "日期为空", accepterMap, ctx);
                return;
            }
            Date InputDate = DateTimeUtil.parseSimpleStr(confirmDate);
            Date toConfirm = workProductShelf.getProductiondate();
            String errorMess = ErrorConstants.PRO_DATE_NOT_LEGAL;
            //细化批次需要检验失效日期
            if (Constants.batchRuleEnum.xiHua.code.equals(TL_BATCH_RULE)) {
                toConfirm = workProductShelf.getExpirationdate();
                errorMess = ErrorConstants.EXP_DATE_NOT_LEGAL;
            }
            if (DateTimeUtil.isSimpleDate(confirmDate) && InputDate.getTime() == toConfirm.getTime()) {
                resetCurCol(SCAN_WH_CODE, accepterMap, ctx);
            } else {
                colNeedReInput(CONFIRM_DATE, errorMess, accepterMap, ctx);
            }
        }

        if (SCAN_WH_CODE.equals(lastCompleteColName)) {
            //接收并校验上架库位编码
            String scanWHCode = reveiveProductShelf.getScanWHCode();
            boolean isPalletShelf = reveiveProductShelf.getShelfType().equals(palletCodeShelf);//是否托盘码方式上架
            InstockShelforderDetail shelfOrderDetail = new InstockShelforderDetail();
            shelfOrderDetail.setSkuStatus(workProductShelf.getSkustatus());
            //复用上架的库位校验
            boolean isWareHouseLegal = instockShelfRemoteService.checkWareHouseForShelf(getCredentialsVO(ctx), scanWHCode, shelfOrderDetail, isPalletShelf);
            if (isWareHouseLegal) {
                tipUnShelfNum(accepterMap, ctx);//提示未上架数量
                if (palletCodeShelf.equals(reveiveProductShelf.getShelfType())) {
                    resetCurCol(CONFIRM_SHELF_NUM, accepterMap, ctx);
                } else {
                    setColUnReceived(SHELF_NUM, accepterMap);
                    resetCurCol(SHELF_NUM, accepterMap, ctx);
                }
            } else {
                colNeedReInput(SCAN_WH_CODE, ErrorConstants.ILLEGAL_DATA, accepterMap, ctx);
            }
        }

        if (SHELF_NUM.equals(lastCompleteColName)) {
            String shelfNum = reveiveProductShelf.getShelfNum();
            confirmShelf(shelfNum, reveiveProductShelf, accepterMap, ctx);
        }

        if (CONFIRM_SHELF_NUM.equals(lastCompleteColName)) {
            String confirmShelfNum = reveiveProductShelf.getConfirmShelfNum();
            confirmShelf(confirmShelfNum, reveiveProductShelf, accepterMap, ctx);
        }
    }

    /**
     * 确认上架
     *
     * @param confirm     接收到的上架数或YN
     * @param shelfOrder  数据接收的业务对象
     * @param accepterMap
     * @param ctx
     */
    private void confirmShelf(String confirm, ProductShelf shelfOrder, Map<String, Object> accepterMap, ChannelHandlerContext ctx) throws Exception {
        boolean palletType = false;
        //如果是托盘编码上架方式只接收YN
        WorkProductShelf instockShelforderDetail = workProductShelf;
        if (shelfOrder.getShelfType().equals(palletCodeShelf)) {
            palletType = true;
            if (!confirm.matches(TipConstants.REG_YN)) {
                colNeedReInput(CONFIRM_SHELF_NUM, ErrorConstants.ONLY_YN_AND_OTHER, accepterMap, ctx);
                return;
            } else if (confirm.equalsIgnoreCase(Constants.CANCEL_N)) {
                //如果输入N回到初始界面
                channelActive(ctx);
                return;
            }
        } else {
            Integer pkLevel = instockShelforderDetail.getPklevel();
            Integer pkNum = instockShelforderDetail.getPknum();
            //如果是商品条码上架，需要接收大于0的正整数，且不能大于当前批次的预期上架数量
            if (!confirm.matches(TipConstants.REG_SERIAL_NO) || confirm.length() > 9) {
                colNeedReInput(SHELF_NUM, ErrorConstants.ILLEGAL_DATA, accepterMap, ctx);
                return;
            } else if (instockShelforderDetail.getDistrbu() < (Integer.parseInt(confirm) + TL_SUMACTUALYNBU)) {
                colNeedReInput(SHELF_NUM, ErrorConstants.TOO_MUCH_SHELF_NUM, accepterMap, ctx);
                return;
            } else if (pkLevel.equals(WmsConstants.PACK_LEVEL_TWO) && Integer.parseInt(confirm) % pkNum != 0) {
                colNeedReInput(SHELF_NUM, ErrorConstants.NOT_MATCH_PK_NUM, accepterMap, ctx);
                return;
            }
        }
        //校验通过进行最后的数据提交，在soa端进行总体校验
        Integer shelfNum = 0;
        if (!palletType) {
            shelfNum = Integer.parseInt(confirm);
        } else {
            shelfNum = instockShelforderDetail.getDistrbu();
        }
        CredentialsVO credentialsVO = getCredentialsVO(ctx);
        RemoteResult<Integer> remoteResult = this.productShelfRemoteService.confirmShelf(credentialsVO, instockShelforderDetail, shelfOrder.getScanWHCode(), shelfNum, palletType);
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
        } else if (WMSErrorMess.BARCODE_SHELF_COMPLETE.equals(result)) {
            //商品条码上架，且本批次的全部明细全都完成上架
            errorMess = TipConstants.SELECT_OTHER_DETAIL;
            accepterMap.put(GO_TO_FLAG, TO_SELECT_DETAIL);
        } else if (WMSErrorMess.BARCODE_SHELF_UN_FINISH.equals(result)) {
            //商品条码上架，且本批次未完成
            errorMess = TipConstants.CONTINUE_INPUT_NUM;
            accepterMap.put(GO_TO_FLAG, TO_RE_INPUT_NUM);
        }
        printBeforeNextField(errorMess, accepterMap, ctx);
    }


    public boolean goToHandler(Map<String, Object> accepterMap, ChannelHandlerContext ctx) throws Exception {
        if (accepterMap.get(GO_TO_FLAG) != null) {
            ProductShelf shelfOrder = (ProductShelf) accepterMap.get(DefaultKey.objectClass.keyName);
            Integer goToAim = (Integer) accepterMap.get(GO_TO_FLAG);
            accepterMap.remove(GO_TO_FLAG);//清空一下，避免重复调用
            // 根据不同的返回值回到不同的步骤
            if (TO_CHANNEL_ACTIVE.equals(goToAim)) {
                channelActive(ctx);
                return true;
            } else if (TO_SHELF_TYPE.equals(goToAim)) {
                //打印主单号，重新选择上架方式
                shelfOrder.setOrderCode(workProductInfo.getWorkproductno());//此处只显示所选择的上架主单的单号
                List<String> fieldList = CollectionUtil.newList(ORDER_CODE);
                printFieldsAndReceiveData(pageHeader, fieldList, null, "", accepterMap, ctx);
                //重新查询明细状态，确认下一步的接收字段为操作方式或直接输入托盘或直接输入商品条码
                List<WorkProductShelf> productShelfList = new ArrayList<WorkProductShelf>();
                RemoteResult<Map<String, Object>> remoteResult = productShelfRemoteService.getProductInfoByNO(getCredentialsVO(ctx), workProductInfo.getWorkproductno());
                if (remoteResult.getT().get(WmsConstants.KEY_WORK_PRODUCT_SHELF_LIST) != null) {
                    productShelfList = (List<WorkProductShelf>) remoteResult.getT().get(WmsConstants.KEY_WORK_PRODUCT_SHELF_LIST);
                }
                jumpToFieldFromShelfOrder(productShelfList, accepterMap, ctx);
                return true;
            } else if (TO_SELECT_DETAIL.equals(goToAim)) {
                //显示主单号、上架方式、商品条码、日期类型、上架明细分页数据，重新选择分页
                shelfOrder.setOrderCode(workProductInfo.getWorkproductno());//此处只显示所选择的上架主单的单号
                PageModel<WorkProductShelf> pageModelRemoteResult = productShelfRemoteService.queryShelfDetailPageModel(getCredentialsVO(ctx), makeQueryShelfDetailMap(shelfOrder, ctx));
                if (pageModelRemoteResult == null || pageModelRemoteResult.getTotalCount() == 0) {
                    //如果按照当前商品商品查询不到明细数据则进入到选择上架方式的步骤
                    List<String> fieldList = CollectionUtil.newList(ORDER_CODE);
                    printFieldsAndReceiveData(pageHeader, fieldList, null, "", accepterMap, ctx);
                    //重新查询明细状态，确认下一步的接收字段为操作方式或直接输入托盘或直接输入商品条码
                    List<WorkProductShelf> productShelfList = new ArrayList<WorkProductShelf>();
                    RemoteResult<Map<String, Object>> remoteResult = productShelfRemoteService.getProductInfoByNO(getCredentialsVO(ctx), workProductInfo.getWorkproductno());
                    if (remoteResult.getT().get(WmsConstants.KEY_WORK_PRODUCT_SHELF_LIST) != null) {
                        productShelfList = (List<WorkProductShelf>) remoteResult.getT().get(WmsConstants.KEY_WORK_PRODUCT_SHELF_LIST);
                    }
                    jumpToFieldFromShelfOrder(productShelfList, accepterMap, ctx);
                } else {
                    List<String> fieldList = CollectionUtil.newList(ORDER_CODE, SHELF_TYPE, BAR_CODE);
                    printFieldsAndReceiveData(pageHeader, fieldList, "", accepterMap, ctx);
                    showShelfDetailPage(shelfOrder, accepterMap, ctx);
                    if (pageModelRemoteResult.getTotalCount() > 1) {
                        setColUnReceived(SELECTED_DETAIL, accepterMap);
                    }
                }
                return true;
            } else if (TO_RE_INPUT_NUM.equals(goToAim)) {
                //清空当前的数量输入行，再清空已经存在的未上架数量提示行
                HandlerUtil.delALL(ctx);//当前操作提示
                HandlerUtil.moveUpN(ctx, 1);//定位到输入的上架数量
                HandlerUtil.delALL(ctx);//清除输入的操作数量
                HandlerUtil.moveUpN(ctx, 1);//定位到未上架数量提示
                HandlerUtil.delALL(ctx);//清除未上架数量提示
                HandlerUtil.moveUpN(ctx, 1);//定位到库位
                HandlerUtil.clearOneRow(ctx);//清除扫描的库位并上移一行
                setColUnReceived(SCAN_WH_CODE, accepterMap);
                resetCurCol(SCAN_WH_CODE, accepterMap, ctx);
                return true;
            }
        }

        return false;
    }

    /**
     * 提示上架明细中的未上架数量
     *
     * @param accepterMap map数据容器
     * @param ctx         handler上下文容器
     */
    private void tipUnShelfNum(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        WorkProductShelf productShelf = workProductShelf;
        int sumActualYnBU = productShelfRemoteService.getShelfActualYnBU(getCredentialsVO(ctx), productShelf);
        TL_SUMACTUALYNBU = sumActualYnBU;
        //提示该批次商品未上架数量
        printBeforeNextField(TipConstants.SHELF_DETAIL_UN_SHELF_BU + (productShelf.getDistrbu() - sumActualYnBU), accepterMap, ctx);
    }

    /**
     * 按照商品条码查询可操作明细的分页处理
     *
     * @param businessShelfOrder 数据接收业务对象
     * @param ctx
     */
    private void showShelfDetailPage(ProductShelf businessShelfOrder, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        PageModel<WorkProductShelf> pageModelRemoteResult = productShelfRemoteService.queryShelfDetailPageModel(getCredentialsVO(ctx), makeQueryShelfDetailMap(businessShelfOrder, ctx));
        if (pageModelRemoteResult == null || pageModelRemoteResult.getTotalCount() == 0) {
            //重新接收商品条码字段
            accepterMap.remove(PageUtil.PARA_PAGE_MAP);
            colNeedReInput(BAR_CODE, ErrorConstants.DATA_EXPIRED, accepterMap, ctx);
        } else if (pageModelRemoteResult.getTotalCount() == 1) {
            //如果只有一条明细数据则通过主单的明细确定下一步的接收
            WorkProductShelf shelfDetail = pageModelRemoteResult.getDatas().get(0);
            TL_BATCH_RULE = shelfDetail.getBatchrule();
            workProductShelf = shelfDetail;
            handleOneDetail(accepterMap, ctx);
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
     * 构造加工上架明细的查询条件
     *
     * @param productShelf 数据接收业务对象
     * @return 返回map查询条件
     */
    private HashMap<String, Object> makeQueryShelfDetailMap(ProductShelf productShelf, ChannelHandlerContext ctx) {
        Map<String, Object> accepterMap = getDataMap();
        HashMap<String, Object> queryMap = (HashMap<String, Object>) accepterMap.get(PageUtil.PARA_PAGE_MAP);//缓存中的分页查询参数Map
        if (queryMap == null) {
            final WorkProductShelf shelfDetail = new WorkProductShelf();
            shelfDetail.setShelferid(getCurrentUserId(ctx));
            shelfDetail.setWorkid(workProductInfo.getId());
            shelfDetail.setPage(Constants.PAGE_START);
            shelfDetail.setRows(Constants.REASON_PAGE_SIZE);
            queryMap = new HashMap<String, Object>();
            queryMap.put(WmsConstants.KEY_WORK_PRODUCT_SHELF, shelfDetail);
            //当前商品条码
            queryMap.put(WmsConstants.KEY_BASE_BARCODE, productShelf.getBarCode());
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
    private void handleOneDetail(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        WorkProductShelf productShelf = workProductShelf;
        super.printBeforeNextField(TipConstants.RECOM_WHCODE + productShelf.getRecomwhcode(), accepterMap, ctx);
        String goodsSkuAndBarCode = TipConstants.GOODS_SKU + productShelf.getSkuid() + "  " + TipConstants.GOODS_BARCODE + productShelf.getBarCode();
        super.printBeforeNextField(goodsSkuAndBarCode, accepterMap, ctx);
        String goodsName = RFUtil.makeStrFitPda(productShelf.getSkuname(), TipConstants.GOODS_NAME, 2);
        super.printBeforeNextField(TipConstants.GOODS_NAME + goodsName, accepterMap, ctx);

        Integer batchRule = TL_BATCH_RULE;
        if (Constants.batchRuleEnum.xiHua.code.equals(batchRule)) {
            //洗化批次规则显示失效日期，其它的批次规则显示生效日期
            super.printBeforeNextField(TipConstants.GOODS_EXPIRATION_DATE + DateTimeUtil.getStringSimple(productShelf.getExpirationdate()), accepterMap, ctx);
        } else {
            super.printBeforeNextField(TipConstants.GOODS_PRODUCTION_DATE + DateTimeUtil.getStringSimple(productShelf.getProductiondate()), accepterMap, ctx);
        }

        super.printBeforeNextField(TipConstants.SKU_STATUS + CheckReasonEnum.getNameByValue(productShelf.getSkustatus()), accepterMap, ctx);
        super.printBeforeNextField(TipConstants.SHELF_DETAIL_PLANBU + productShelf.getPlanbu(), accepterMap, ctx);
        if (productShelf.getIssamesku() != null && WmsConstants.CON_YES == productShelf.getIssamesku()) {
            super.printBeforeNextField(TipConstants.IS_SAME_SKU, accepterMap, ctx);
        }
        //清除一下分页数据
//        colNeedReInput(BAR_CODE, ErrorConstants.DATA_EXPIRED, accepterMap, ctx);
        //跳转到日期接收步骤
        resetCurCol(CONFIRM_DATE, accepterMap, ctx);
    }


    /**
     * 根据确认的上架主单检测后续的接收方式及数据显示
     *
     * @param productShelfList 可操作明细数据
     * @param accepterMap      map数据容器
     * @param ctx              ctx上下文
     */
    private void jumpToFieldFromShelfOrder(List<WorkProductShelf> productShelfList, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        //如果无可操作明细则重新扫描主单号
        if (productShelfList == null || productShelfList.size() == 0) {
            //重新接收上架单号字段
            colNeedReInput(ORDER_CODE, "未查询到该商品上架数据", accepterMap, ctx);
            return;
        }
        int nullPalletDetail = 0;//没有托盘编码的明细数据
        int notNullPalletDetail = 0;//有托盘编码的明细数据
        for (WorkProductShelf shelfDetail : productShelfList) {
            if (StringUtils.isEmpty(shelfDetail.getPalletcode())) {
                nullPalletDetail++;
            } else {
                notNullPalletDetail++;
            }
        }
        ProductShelf shelfOrder = (ProductShelf) accepterMap.get(DefaultKey.objectClass.keyName);
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


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


}
