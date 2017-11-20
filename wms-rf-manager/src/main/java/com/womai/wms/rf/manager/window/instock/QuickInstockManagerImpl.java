package com.womai.wms.rf.manager.window.instock;

import com.google.common.collect.Lists;
import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.*;
import com.womai.wms.rf.domain.instock.IntentionInstock;
import com.womai.wms.rf.domain.instock.PurchaseInstock;
import com.womai.wms.rf.domain.instock.QuickInstock;
import com.womai.wms.rf.domain.instock.ShelfOrder;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.base.GoodsinfoRemoteService;
import com.womai.wms.rf.remote.instock.InstockReasonRemoteService;
import com.womai.wms.rf.remote.instock.InstockRemoteService;
import com.womai.zlwms.rfsoa.api.WMSErrorMess;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.base.BaseGoodsinfo;
import com.womai.zlwms.rfsoa.domain.base.BasePackaginginfo;
import com.womai.zlwms.rfsoa.domain.base.BasePallet;
import com.womai.zlwms.rfsoa.domain.instock.Instock;
import com.womai.zlwms.rfsoa.domain.instock.InstockDetail;
import com.womai.zlwms.rfsoa.domain.instock.InstockReason;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by keke on 17-8-28.
 */
@Scope("prototype")
@Component("quickInstockManager")
public class QuickInstockManagerImpl extends ReceiveManager {
    private ChannelHandlerContext ctx;
    private static final String[] pageHeader = {"", TipConstants.QUICK_INSTOCK, Constants.SPLIT, ""};
    private final static String GO_TO_FLAG = "inStock_goto";//跳转标志
    private final static Integer TO_CHANNEL_ACTIVE = -1;//跳转标志
    private final static Integer TO_SCAN_CODE = -2;//跳转到扫描商品条码
    private final static Integer TO_PRODATE_CODE = -3;//跳转到日期录入
    private final static Integer TO_SUGGEST_PRODATE_CODE = -4;//Y继续，其他键跳转到日期录入

    private final static String SCAN_CODE = "scanCode";//扫描的ASN单号/网络订单号
    private final static String SELECT_ASN = "selectAsn";//选择ASN单号
    private final static String BARCODE = "barCode";//商品条码
    private final static String SELECT_PAGE = "selectPage";//翻页
    private final static String PROD_OR_EXP_DATE = "prodOrExpDate";//生产日期或失效日期
    private final static String CHECK_NUM = "checkNum";//收货数量
    private final static String CHECK_RESULT = "checkResult";//质检结果
    private final static String REASONID = "reasonid";//
    private final static String PALLET_CODE = "palletCode";//托盘编码
    private final static String CONFIRM_IN = "confirmIn";//确认收货
    private final static String CONFIRM_CHECK = "confirmCheck";//确认过账质检
    private final static String CONFIRM_SHELF = "confirmShelf";//确认上架

    private final static String SMALLEST_DATE = "20000101";//所能输入的最小日期

    private List<Instock> inStockList = new ArrayList<Instock>();//按照ASN单号或网络订单号查询到的多条数据
    private List<String> asnCodeList = new ArrayList<String>();//按照网络订单号查询到的多个ASN单号
    private Instock inStock;//选择的一条主单数据
    private InstockDetail initInStockDetail;//选择的入库明细
    private Integer receivedBU = 0;//未收货BU数量
    private List<String> dateTypeList = CollectionUtil.newList(Constants.batchRuleEnum.puTong.dateType, Constants.batchRuleEnum.xiHua.dateType);//日期类型列表
    private final static String DATE_TYPE = "dateType";//日期类型，选择生产日期或失效期
    private final static String[] ITEM_TABLE_NAME = {"序号", "包装单位","计划收货数量BU","收货数量BU" };//明细表头
    private final static String[] ITEM_TABLE_COLUMN = {  "unitname","expectnumbu", "sumreceivenumbu"};//明细列名
    public final static String[] TABLE_NAME = {"序号", "原因内容                              "};//原因列表头
    public final static String[] TABLE_COLUMN = {"remark"};//原因列表头
    private String shelfCode;
    @Autowired
    private InstockRemoteService instockRemoteService;
    @Autowired
    private InstockReasonRemoteService instockReasonRemoteService;

    private final static String PARA_MAP = "para_map";//登记时的入参Map

    @Autowired
    private GoodsinfoRemoteService goodsinfoRemoteService;
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        removeLocals();
        super.initBaseMap(QuickInstock.class, pageHeader, ctx);
    }
    /**
     * 接收用户输入
     *
     * @param ctx handler对象
     * @param object 每次接收到的字节数据
     * @throws Exception 抛出异常
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx,Object object) throws Exception {
        Map<String, Object> accepterMap = getDataMap();
        if (accepterMap.get(GO_TO_FLAG) != null) {
            Integer goToflag = (Integer) accepterMap.get(GO_TO_FLAG);

            if (TO_PRODATE_CODE.equals(goToflag)) {
                //跳转到日期录入，重新查询商品信息获得新的保质期，预警时间
                boolean succ = resetInstockDetail(ctx, accepterMap, initInStockDetail);
                if (succ) {
                    toProDateFromLast(accepterMap, ctx);
                    accepterMap.remove(GO_TO_FLAG);
                } else {
                    accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ACTIVE);
                }
                return;
            }

            if (TO_SUGGEST_PRODATE_CODE.equals(goToflag)) {
                //Y继续，其它键，跳转到日期录入，重新查询商品信息
                if (Constants.CONFIRM_Y.equalsIgnoreCase(object.toString())) {
                    boolean succ = resetInstockDetail(ctx, accepterMap, initInStockDetail);
                    if(succ){
                        //获得提交参数，重新提交数据
                        HashMap<String, Object> map  = (HashMap<String, Object>)accepterMap.get(PARA_MAP);
                        RemoteResult<Boolean> result = this.instockRemoteService.confirmInStockFast(getCredentialsVO(ctx), map);
                        accepterMap.remove(PARA_MAP);
                        HandlerUtil.moveUpN(ctx,1);
                        HandlerUtil.changeRow(ctx);
                        HandlerUtil.clearRight(ctx);
                        if(result.isSuccess()){
                            Boolean isCanCheck = result.getT();
                            if(isCanCheck){
                                rePrintCurColTip(accepterMap, ctx);
                            }else{
                                HandlerUtil.write(ctx, Constants.BREAK_LINE +"收货成功，"+ ErrorConstants.ANY_KEY_CONTINUE);
                                accepterMap.put(GO_TO_FLAG, TO_SCAN_CODE);
                            }
                        }else{
                            printErrorMsessage(ctx, accepterMap, result.getResultCode());
                        }
                    }else{
                        accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ACTIVE);
                    }

                } else {
                    accepterMap.remove(PARA_MAP);
                    boolean succ = resetInstockDetail(ctx, accepterMap, initInStockDetail);
                    if (succ) {
                        toProDateFromLast(accepterMap, ctx);
                        accepterMap.remove(GO_TO_FLAG);
                    } else {
                        accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ACTIVE);
                    }
                }
                return;
            }

            if (TO_CHANNEL_ACTIVE.equals(goToflag)) {
                channelActive(ctx);
                return;
            }
            if (TO_SCAN_CODE.equals(goToflag)) {
                //跳转到扫描商品条码
                List<String> showStrings = CollectionUtil.newList(SCAN_CODE,SELECT_ASN);
                List<String> clearStrings = CollectionUtil.newList(SELECT_PAGE,PROD_OR_EXP_DATE, CHECK_NUM,REASONID, PALLET_CODE, CONFIRM_IN);
                printFieldsAndReceiveData(pageHeader, showStrings, BARCODE, clearStrings, accepterMap, ctx);
                accepterMap.remove(GO_TO_FLAG);
                receivedBU = 0;
                return;
            }

        }
        receiveDataAndNotPrintNext(ctx, object, accepterMap);
        QuickInstock quickInstock = (QuickInstock) accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if(SCAN_CODE.equals(lastCompleteColName)){
            String scanCode = quickInstock.getScanCode();
            RemoteResult<List<Instock>> listRemoteResult = instockRemoteService.queryIntentionByASNCodeOrPurchaseCodeFast(getCredentialsVO(ctx), scanCode);
            //未查询到数据则提示重新扫描
            inStockList = listRemoteResult.getT();
            if(!listRemoteResult.isSuccess() || inStockList == null ){
                colNeedReInput(lastCompleteColName, listRemoteResult.getResultCode(), accepterMap, ctx);
            }else if(inStockList.size() == 1){
                //只查询到一条数据则直接跳过选择ASN单号的步骤，直接到输入商品条码
                Instock selectedInstock = inStockList.get(0);
                //如果是换货意向单则需要验证是否已经出库
                if (selectedInstock.getOrdertype().equals(WmsConstants.INSTOCK_ORDER_TYPE_HUANHUO)) {
                    String serialNo = selectedInstock.getSerialno();
                    Boolean isOutStock = instockRemoteService.intentionIsOutStock(getCredentialsVO(ctx), serialNo);
                    if (!isOutStock) {
                        colNeedReInput(lastCompleteColName, ErrorConstants.NOT_OUT_STOCK, accepterMap, ctx);
                        return;
                    }
                }
                inStock = selectedInstock;
                setDefaultValue(selectedInstock.getAsninstockcode(), SELECT_ASN, accepterMap, ctx);
                resetCurCol(BARCODE, accepterMap, ctx);
            }else if(inStockList.size() > 1){
                //如果存在多条数据，则需要切换选择ASN单号
                List<String> list = new LinkedList();
                for (Instock instock : inStockList) {
                    list.add(instock.getAsninstockcode());
                }
                asnCodeList = list;
                setNextColSwitchList(asnCodeList, accepterMap, ctx);
            }else {
                colNeedReInput(lastCompleteColName, listRemoteResult.getResultCode(), accepterMap, ctx);
            }
        }
        if(SELECT_ASN.equals(lastCompleteColName)){
            String selectAsn = quickInstock.getSelectAsn();
            if(asnCodeList.contains(selectAsn)){
                Integer listIndex = (Integer) accepterMap.get(DefaultKey.listIndex.keyName);
                Instock selectedInstock = inStockList.get(listIndex);
                //如果是换货意向单则需要验证是否已经出库
                if (selectedInstock.getOrdertype().equals(WmsConstants.INSTOCK_ORDER_TYPE_HUANHUO)) {
                    String serialNo = selectedInstock.getSerialno();
                    Boolean isOutStock = instockRemoteService.intentionIsOutStock(getCredentialsVO(ctx), serialNo);
                    if (!isOutStock) {
                        HandlerUtil.errorBeep(ctx);
                        super.printBeforeNextField(ErrorConstants.NOT_OUT_STOCK + ErrorConstants.COMMON_PUNCTUATION + ErrorConstants.ANY_KEY_CONTINUE, accepterMap, ctx);
                        accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ACTIVE);
                        return;
                    } else {
                        rePrintCurColTip(accepterMap, ctx);
                    }
                } else {
                    //选择后提示输入商品条码
                    rePrintCurColTip(accepterMap, ctx);
                }
                inStock = selectedInstock;
            }
        }
        if(BARCODE.equals(lastCompleteColName)){
            String barCode = quickInstock.getBarCode();
            Long asnInStockId = inStock.getAsninstockId();
            Map<String, Object> detailAndPackagingList = instockRemoteService.queryDetailAndGoodsAndPackaging(getCredentialsVO(ctx), asnInStockId, barCode);
            if (detailAndPackagingList == null || detailAndPackagingList.isEmpty()) {
                super.colNeedReInput(BARCODE, ErrorConstants.DATA_NOT_FOUNT_CONTINUE, accepterMap, ctx);
                return;
            } else {
                //扫描条码是先判断商品状态，失效时提示
                String errorMsg = (String)detailAndPackagingList.get(WmsConstants.KEY_ERROR_MSG);
                if(StringUtils.isNotEmpty(errorMsg)){
                    super.colNeedReInput(BARCODE, errorMsg, accepterMap, ctx);
                    return;
                }
                initInStockDetail = (InstockDetail) detailAndPackagingList.get(WmsConstants.KEY_INSTOCKDETAIL_PARAM);
                Integer initDetailSize =(Integer)detailAndPackagingList.get(WmsConstants.KEY_INSTOCK_DETAIL_INIT_SIZE);


                if(initDetailSize != null && initDetailSize.intValue() == 1){
                    String skuName = initInStockDetail.getSkuname() == null ? "" : initInStockDetail.getSkuname();
                    skuName = RFUtil.makeStrFitPda(skuName, TipConstants.SKU_NAME, 2);
                    super.printBeforeNextField(TipConstants.SKU_NAME + skuName, accepterMap, ctx);
                    super.printBeforeNextField(TipConstants.UNIT_NAME + initInStockDetail.getUnitname(), accepterMap, ctx);
                    resetCurColNoPrint(DATE_TYPE, accepterMap, ctx);//跳过分页查询明细
                    //根据批次提示输入生产日期或失效日期
                    String DateTypeTip = Constants.batchRuleEnum.getDateTypeByCode(initInStockDetail.getBatchrule());
                    super.setNextColSwitchList(DateTypeTip,dateTypeList, accepterMap, ctx);

                }else{
                    //同skuid所有收货的明细都显示出来
                    showItemsPage(ctx);
                }
            }
        }
        if(SELECT_PAGE.equals(lastCompleteColName)){
            String pageNum = quickInstock.getSelectPage();
            PageModel<InstockDetail> pageModle = (PageModel<InstockDetail>) accepterMap.get(PageUtil.PAGE_MODEL);
            int pageSizeCurr = (Integer) accepterMap.get(PageUtil.LINES_NUM_CLEAN_KEY);//页面当前数据条数
            if (KeyEnum.N_78.value.equalsIgnoreCase(pageNum)) {//下一页
                PageUtil.changePageNext(ctx, accepterMap, WmsConstants.KEY_INSTOCKDETAILS_PARAM, pageSizeCurr);
                setColUnReceived(SELECT_PAGE, accepterMap);
                showItemsPage(ctx);
                return ;
            } else if (KeyEnum.B_66.value.equalsIgnoreCase(pageNum)) {//上一页
                PageUtil.changePageUp(ctx, accepterMap, WmsConstants.KEY_INSTOCKDETAILS_PARAM, pageSizeCurr);
                setColUnReceived(SELECT_PAGE, accepterMap);
                showItemsPage(ctx);
                return ;
            } else {//序号
                if (pageNum.matches(TipConstants.REG_SERIAL_NO)) {
                    List<InstockDetail> items = pageModle.getDatas();
                    int maxIndex = items.size() - 1;
                    int index = PageUtil.getIndexFromSerialnoAndPageModle(pageNum, pageModle);//(pn-1)*size+index+1==serialno
                    if (index > maxIndex || index < 0) {
                        colNeedReInput(SELECT_PAGE, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                        return ;
                    } else {
                        InstockDetail detail = items.get(index);
                        if(detail.getExpectnumbu().compareTo(detail.getSumreceivenumbu())<1){//已经收货的大于，或等于期待收货数量bu
                            colNeedReInput(SELECT_PAGE, ErrorConstants.BIGGER_THAN_EXPECT, accepterMap, ctx);
                            return ;
                        }
                        initInStockDetail =  detail;
                        //清空明细分页查询条件
                        accepterMap.remove(PageUtil.PARA_PAGE_MAP);
                        super.printBeforeNextField(TipConstants.UNIT_NAME + initInStockDetail.getUnitname(), accepterMap, ctx);
                        resetCurColNoPrint(DATE_TYPE, accepterMap, ctx);
                        // /根据批次提示输入生产日期或失效日期
                        String DateTypeTip = Constants.batchRuleEnum.getDateTypeByCode(initInStockDetail.getBatchrule());
                        super.setNextColSwitchList(DateTypeTip, dateTypeList, accepterMap, ctx);


                    }
                } else {
                    colNeedReInput(SELECT_PAGE, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                    return ;
                }
            }
        }
        if (DATE_TYPE.equalsIgnoreCase(lastCompleteColName)) {
            String dateType = quickInstock.getDateType();
            if (!dateTypeList.contains(dateType)) {
                setColReSwitchList(dateTypeList, ErrorConstants.DATE_TYPE_NOT_IN_LIST, accepterMap, ctx);
            } else {
                resetCurCol(PROD_OR_EXP_DATE, accepterMap, ctx);
            }
        }
        if(PROD_OR_EXP_DATE.equals(lastCompleteColName)){
            String prodOrExpDate = quickInstock.getProdOrExpDate();
            Boolean dateReg = DateTimeUtil.isSimpleDate(prodOrExpDate);
            if (dateReg) {
                //根据选择的日期类型及输入的日期计算生产、近效期、允收期、失效期
                Date inputDate = DateTimeUtil.parseSimpleStr(prodOrExpDate);
                Date smallestDate = DateTimeUtil.parseSimpleStr(SMALLEST_DATE);
                Boolean isSmallest = DateTimeUtil.compareDate(smallestDate, inputDate);
                if (isSmallest) {
                    super.colNeedReInput(PROD_OR_EXP_DATE, ErrorConstants.ILLEGAL_DATE_PATTERN, accepterMap, ctx);
                } else {
                    quickInstock = countProdAndExpDate(quickInstock, initInStockDetail);
                    //校验生产日期不可以大于今天
                    if (DateTimeUtil.isDateAfterToday(quickInstock.getProductionDate())) {
                        super.colNeedReInput(PROD_OR_EXP_DATE, ErrorConstants.PRO_BIGGER_NOW, accepterMap, ctx);
                        return;
                    }
                    HashMap<String,Object> param=new HashMap<String, Object>();
                    param.put(WmsConstants.KEY_INSTOCK_PARAM_SKUID,initInStockDetail.getSkuid());
                    param.put(WmsConstants.KEY_INSTOCK_PARAM_PURCHASENO,inStock.getPurchaseno());
                    param.put(WmsConstants.KEY_INSTOCK_PARAM_ASNINSTOCKID,inStock.getAsninstockId());
                    String proStr = DateTimeUtil.getStringWithSeparator(quickInstock.getProductionDate());
                    param.put(WmsConstants.KEY_INSTOCK_PARAM_PRODATE,proStr);
                    param.put(WmsConstants.KEY_INSTOCK_PARAM_ASNTYPE,inStock.getOrdertype());
                    //查询出库单中的生产日期，如果与当前的生产日期不同则进行提示
                    RemoteResult<String> result = instockRemoteService.queryProDateInOutOrder(getCredentialsVO(ctx), param);
                    if(result.isSuccess()){
                        printBeforeNextField(TipConstants.UN_RECEIVE_BU + (initInStockDetail.getExpectnumbu() - queryReceivedBU(initInStockDetail)), accepterMap, ctx);
                        rePrintCurColTip(accepterMap, ctx);
                    }else{
                        super.colNeedReInput(PROD_OR_EXP_DATE, result.getResultCode(), accepterMap, ctx);
                    }
                }
            } else {
                super.colNeedReInput(PROD_OR_EXP_DATE, ErrorConstants.ILLEGAL_DATE_PATTERN, accepterMap, ctx);
            }
        }
        if(CHECK_NUM.equals(lastCompleteColName)){
            //意向单可以0入库
            String patternReg = "^[1-9]\\d{0,9}$";
            String checkNum = quickInstock.getCheckNum();
            if (checkNum.matches(patternReg)) {
                Integer check = null;
                try {
                    check = Integer.parseInt(checkNum);
                } catch (NumberFormatException e) {
                    colNeedReInput(CHECK_NUM, ErrorConstants.BIGGER_THAN_EXPECT, accepterMap, ctx);
                    return;
                }
                String unitName = initInStockDetail.getUnitname();
                Integer receivedBu = queryReceivedBU(initInStockDetail);
                //如果包装单位选择的是箱，则实际收货数量为输入数量乘以箱规加上已经收货的数量
                if (unitName.equalsIgnoreCase(TipConstants.PK_LEVEL2_NAME)) {
                    receivedBu = receivedBu + (check * initInStockDetail.getPknum());
                } else {
                    receivedBu = receivedBu + check;
                }
                if (receivedBu > initInStockDetail.getExpectnumbu()||receivedBu < 0) {
                    colNeedReInput(CHECK_NUM, ErrorConstants.BIGGER_THAN_EXPECT, accepterMap, ctx);
                    return;
                }
                List<String> list = CheckReasonEnum.getNameList();
                //下一个字段以list切换的形式显示
                setNextColSwitchList(list, accepterMap, ctx);
            } else {
                colNeedReInput(CHECK_NUM, ErrorConstants.ILLEGAL_DATA, accepterMap, ctx);
            }
        }
        if(CHECK_RESULT.equals(lastCompleteColName)){//质检结果
            int type = CheckReasonEnum.getValueByName(quickInstock.getCheckResult());
            if (CheckReasonEnum.damaged.value.equals(type) || CheckReasonEnum.frozen.value.equals(type)) {//残品或者冻结，列出原因列表
                showReasons(ctx, accepterMap, type, Constants.PAGE_OFFSET_INIT);
            } else {//正品，跳过原因内容
                resetCurCol(PALLET_CODE, accepterMap, ctx);
            }
        }

        if (REASONID.equals(lastCompleteColName)) {
            String reasonid = quickInstock.getReasonid();
            PageModel<InstockReason> pageModle = (PageModel<InstockReason>) accepterMap.get(PageUtil.PAGE_MODEL);
            int pageSizeCurr = (Integer) accepterMap.get(PageUtil.LINES_NUM_CLEAN_KEY);//页面当前数据条数
            if (KeyEnum.N_78.value.equalsIgnoreCase(reasonid)) {//下一页
                PageUtil.changePageNext(ctx, accepterMap,WmsConstants.KEY_INSTOCKREASON_PARRAM, pageSizeCurr);
                showReasons(ctx, accepterMap, -1, Constants.PAGE_OFFSET_INIT);
                setColUnReceived(REASONID, accepterMap);
            } else if (KeyEnum.B_66.value.equalsIgnoreCase(reasonid)) {//上一页
                PageUtil.changePageUp(ctx, accepterMap,WmsConstants.KEY_INSTOCKREASON_PARRAM, pageSizeCurr);
                showReasons(ctx, accepterMap, -1, Constants.PAGE_OFFSET_INIT);
                setColUnReceived(REASONID, accepterMap);
            } else {//序号
                if (reasonid.matches(TipConstants.REG_SERIAL_NO)) {
                    List<InstockReason> instockReasons = pageModle.getDatas();
                    int maxIndex = instockReasons.size() - 1;
                    int index = PageUtil.getIndexFromSerialnoAndPageModle(reasonid, pageModle);//(pn-1)*size+index+1==serialno
                    if (index > maxIndex || index < 0) {
                        colNeedReInput(REASONID, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                    } else {
                        //正确选择原因内容，保存参数
                        accepterMap.remove(PageUtil.PARA_PAGE_MAP);
                        HandlerUtil.changeRow(ctx);
                        HandlerUtil.println(ctx, TipConstants.REASON_CONTENT + instockReasons.get(index).getRemark());
                        rePrintCurColTip(accepterMap, ctx);
                    }
                } else {
                    colNeedReInput(REASONID, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                }
            }
        }
        if (PALLET_CODE.equals(lastCompleteColName)) {
            String palletCode = quickInstock.getPalletCode();
            if (StringUtils.isNotEmpty(palletCode)) {
                BasePallet basePallet = instockRemoteService.getPalletByCode(getCredentialsVO(ctx), palletCode);
                if (basePallet == null) {
                    colNeedReInput(PALLET_CODE, "托盘编码不存在", accepterMap, ctx);
                }else if(basePallet.getPstatus().equals(WmsConstants.STATUS_DISABLE)){
                    colNeedReInput(PALLET_CODE, "托盘编码已失效", accepterMap, ctx);
                }else if(basePallet.getPstatus().equals(WmsConstants.STATUS_USED)){
                    colNeedReInput(PALLET_CODE, "托盘编码已使用", accepterMap, ctx);
                } else {
                    rePrintCurColTip(accepterMap, ctx);
                }
            } else {
                rePrintCurColTip(accepterMap, ctx);
            }
        }

        if (CONFIRM_IN.equalsIgnoreCase(lastCompleteColName)) {
            String confirmIn = quickInstock.getConfirmIn();
            if (Constants.CONFIRM_Y.equalsIgnoreCase(confirmIn)) {
                //将接收到的合法数据set到入库单明细中传到soa进行业务处理
                //日期
                initInStockDetail.setProductiondate(quickInstock.getProductionDate());
                initInStockDetail.setExpirationdate(quickInstock.getExpirationDate());
                initInStockDetail.setNearvaliddate(quickInstock.getNearValidDate());
                //托盘编码
                initInStockDetail.setPalletCode(quickInstock.getPalletCode());
                //实际收货数量BU
                initInStockDetail.setReceivenumbu(Integer.parseInt(quickInstock.getCheckNum()));
                //所输入的收货数量
                Integer checkNum = Integer.parseInt(quickInstock.getCheckNum());
                //箱规
                Integer pkNum = initInStockDetail.getPknum();
                String unitName = initInStockDetail.getUnitname();
                //实际收货包装数量，如果为箱，则为实际收货数量为所输入的收货后数量乘以箱规
                if (unitName.equalsIgnoreCase(TipConstants.PK_LEVEL2_NAME)) {
                    initInStockDetail.setReceivenumbu(checkNum * pkNum);
                    initInStockDetail.setReceivepknum(checkNum);
                } else {
                    initInStockDetail.setReceivenumbu(checkNum);
                    initInStockDetail.setReceivepknum(checkNum / pkNum);
                }
                //实际收货金额
                BigDecimal inStockMoney = initInStockDetail.getPrice().multiply(new BigDecimal(initInStockDetail.getReceivenumbu()));
                initInStockDetail.setInstockmoney(inStockMoney);
                //soa端主要业务处理
                //int[] orderType = {WmsConstants.INSTOCK_ORDER_TYPE_XIAOTUI, WmsConstants.INSTOCK_ORDER_TYPE_JUSHOU, WmsConstants.INSTOCK_ORDER_TYPE_HUANHUO};
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put(WmsConstants.KEY_INSTOCK_PARAM,inStock);
                map.put(WmsConstants.KEY_INSTOCKDETAIL_PARAM,initInStockDetail);
                initInStockDetail.setDateType(quickInstock.getDateType());//用户选择的日期类型，后端用来重新计算生产日期，失效日期
                //map.put(WmsConstants.KEY_INSTOCK_ORDERTYPE,orderType);
                int type = CheckReasonEnum.getValueByName(quickInstock.getCheckResult());
                map.put(WmsConstants.KEY_INSTOCK_CHECKSTATUS,type);
                if (!CheckReasonEnum.normal.value.equals(type)) {//非正品，设置原因id
                    PageModel pageModel = (PageModel) accepterMap.get(PageUtil.PAGE_MODEL);
                    List<InstockReason> instockReasons = pageModel.getDatas();
                    //根据序号获得原因id
                    int index = PageUtil.getIndexFromSerialnoAndPageModle(quickInstock.getReasonid(), pageModel);
                    InstockReason instockReason = instockReasons.get(index);
                    Long reasonid = instockReason.getId();
                    map.put(WmsConstants.KEY_INSTOCK_REASONID,reasonid);
                }
                RemoteResult<Boolean> inStockResult = this.instockRemoteService.confirmInStockFast(getCredentialsVO(ctx), map);
                if(inStockResult.isSuccess()){
                    Boolean isCanCheck = inStockResult.getT();
                    if(isCanCheck){
                        rePrintCurColTip(accepterMap, ctx);
                    }else{
                        HandlerUtil.write(ctx, Constants.BREAK_LINE +"收货成功，"+ ErrorConstants.ANY_KEY_CONTINUE);
                        accepterMap.put(GO_TO_FLAG, TO_SCAN_CODE);
                    }
                }else{
                    //true:Y继续，任意键继续。false：强制不能通过的，pda 任意键跳转日期录入。null,表示基础信息里的保质期无变化
                    Boolean suggestionMsg = inStockResult.getT();
                    String errMsg = inStockResult.getResultCode();
                    if (suggestionMsg != null) {
                        //基础信息里的保质期，预警时间发生变化。重新计算的生产日期，失效日期，再次校验，提示语
                        if (suggestionMsg) {
                            //日期校验后，提示用户输入Y继续，其他键跳到日期录入 ，重新查询保质期，预警时间
                            accepterMap.put(GO_TO_FLAG, TO_SUGGEST_PRODATE_CODE);
                            accepterMap.put(PARA_MAP, map);
                            HandlerUtil.changeRow(ctx);
                            HandlerUtil.write(ctx, errMsg + ",Y继续，其它键重新输入日期");
                            HandlerUtil.errorBeep(ctx);//系统错误，响铃
                        }else{
                            //跳转日期录入，重新查询保质期，预警时间
                            accepterMap.put(GO_TO_FLAG, TO_PRODATE_CODE);
                            HandlerUtil.changeRow(ctx);
                            HandlerUtil.write(ctx, errMsg + ErrorConstants.TIP_TO_CONTINUE);
                            HandlerUtil.errorBeep(ctx);//系统错误，响铃
                        }
                        return;
                    }else{
                        printErrorMsessage(ctx, accepterMap, errMsg);
                    }
                }

            } else if (Constants.CANCEL_N.equalsIgnoreCase(confirmIn)) {
                channelActive(ctx);
            } else {
                colNeedReInput(CONFIRM_IN, ErrorConstants.ONLY_YN, accepterMap, ctx);
            }
        }
        if (CONFIRM_CHECK.equalsIgnoreCase(lastCompleteColName)) {
            String confirmCheck = quickInstock.getConfirmCheck();
            if (Constants.CONFIRM_Y.equalsIgnoreCase(confirmCheck)) {
                RemoteResult<String> checkResult = instockRemoteService.confirmInstockPostingAndCheckFast(getCredentialsVO(ctx),inStock.getAsninstockcode());
                if(checkResult!=null && checkResult.isSuccess()){
                    shelfCode = (String)checkResult.getT();
                    HandlerUtil.println(ctx, Constants.BREAK_LINE + "上架单创建完成，整单分配完成，上架单已生效！");
                    rePrintCurColTip(accepterMap, ctx);
                }else{
                    printErrorMsessage(ctx,accepterMap,checkResult.getResultCode());
                }
            } else if (Constants.CANCEL_N.equalsIgnoreCase(confirmCheck)) {
                channelActive(ctx);
            } else {
                colNeedReInput(CONFIRM_CHECK, ErrorConstants.ONLY_YN, accepterMap, ctx);
            }
        }
        if (CONFIRM_SHELF.equalsIgnoreCase(lastCompleteColName)) {
            String confirmShelf = quickInstock.getConfirmShelf();
            if (Constants.CONFIRM_Y.equalsIgnoreCase(confirmShelf)) {
                if(StringUtils.isNotEmpty(shelfCode)){
                    QuickInstockParamManagerImpl quickInstockParamManager = new QuickInstockParamManagerImpl();
                    ShelfOrder shelfOrder = new ShelfOrder();
                    shelfOrder.setOrderCode(shelfCode);
                    quickInstockParamManager.setShelfOrder(shelfOrder);
                    ctx.pipeline().addAfter(Constants.ENCODE_HANDLER, "quickInstockParamManager", quickInstockParamManager);
                    forward("shelfOrderManager", ctx);
                }else {
                    printErrorMsessage(ctx,accepterMap,"上架单号为空");
                }
            } else {
                channelActive(ctx);
            }
        }
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
    /**
     * 重新设置保质期，预警时间，允收期
     *
     * @param ctx
     * @param accepterMap
     * @param tl_initInStockDetail
     */
    private boolean resetInstockDetail(ChannelHandlerContext ctx, Map<String, Object> accepterMap, InstockDetail tl_initInStockDetail) {
        BaseGoodsinfo goodsinfo = goodsinfoRemoteService.getEnableGoodsByBarCode(getCredentialsVO(ctx), tl_initInStockDetail.getBarcode());
        if (goodsinfo == null) {
            HandlerUtil.write(ctx, "商品" + tl_initInStockDetail.getBarcode() + "已经失效" + ErrorConstants.TIP_TO_CONTINUE);
            return false;
        } else {
            //更新明细里的保质期，预警时间，允许入库时间
            tl_initInStockDetail.setKeepdays(goodsinfo.getKeepdays());
            tl_initInStockDetail.setWarningday(goodsinfo.getWarningday());
            tl_initInStockDetail.setInstockdays(goodsinfo.getInstockdays());
            return true;
        }

    }

    /**
     * 从最后一步跳转到日期录入的地方
     *
     * @param accepterMap
     * @param ctx
     */
    private void toProDateFromLast(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        List<String> showStrings = CollectionUtil.newList(SCAN_CODE, BARCODE);
        List<String> clearStrings = CollectionUtil.newList(SELECT_PAGE, CHECK_NUM, CHECK_RESULT,REASONID, PALLET_CODE, CONFIRM_IN);
        printFieldsAndReceiveData(pageHeader, showStrings, PROD_OR_EXP_DATE, clearStrings, accepterMap, ctx);
        receivedBU = 0;
    }

    /**
     * 移除本地变量
     */
    private void removeLocals() {
        inStock = null;
        initInStockDetail = null;
        receivedBU = 0;
    }


    /**
     * 初始分页查询参数
     *
     * @param obj         查询列表的参数
     * @param accepterMap
     * @return 分页查询参数
     */
    private HashMap initPagePara(Object obj, Map<String, Object> accepterMap) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(WmsConstants.KEY_INSTOCKREASON_PARRAM, obj);
        accepterMap.put(PageUtil.PARA_PAGE_MAP, map);
        return map;
    }


    /**
     * 展示原因列表
     *
     * @param ctx
     * @param accepterMap
     * @param type        原因类型
     * @param pageOffSet  上一页 -1 下一页 1
     */
    private void showReasons(ChannelHandlerContext ctx, Map<String, Object> accepterMap, int type, int pageOffSet) {
        HashMap<String, Object> map = (HashMap<String, Object>) accepterMap.get(PageUtil.PARA_PAGE_MAP);
        if (map == null) {
            InstockReason instockReason = new InstockReason();
            instockReason.setPage(Constants.PAGE_START);
            instockReason.setRows(Constants.WHOLE_REASON_PAGE_SIZE);
            instockReason.setSidx("id");
            instockReason.setSord(Constants.PAGE_SORT_DESC);
            instockReason.setReasonType(type);
            instockReason.setReasonStatus(WmsConstants.STATUS_ENABLE);//生效
            map = initPagePara(instockReason, accepterMap);
        }
        RemoteResult<PageModel<InstockReason>> pageModelRemoteResult = instockReasonRemoteService.getInstockReasonPageList(getCredentialsVO(ctx), map);
        if (!pageModelRemoteResult.isSuccess()) {//输出错误信息（无数据或异常）
            accepterMap.remove(PageUtil.PARA_PAGE_MAP);
            List<String> list = CheckReasonEnum.getNameList();
            //下一个字段以list切换的形式显示
            setColReSwitchList(list, ErrorConstants.PLS_MAINTAION_REASON, accepterMap, ctx);
        } else {//展示原因内容列表，一定有数据
            PageModel<InstockReason> instockReasonPageModel = pageModelRemoteResult.getT();
            accepterMap.put(PageUtil.PAGE_MODEL, instockReasonPageModel);
            HandlerUtil.changeRow(ctx);
            int currPageLinesNum = PageUtil.showTable(ctx, instockReasonPageModel, TABLE_NAME, TABLE_COLUMN, true, true, null);//展示列表，带有序号
            accepterMap.put(PageUtil.LINES_NUM_CLEAN_KEY, currPageLinesNum);
            HandlerUtil.write(ctx, TipConstants.PLS_CHOSE_SERIALNO);
        }

    }
    private void showItemsPage(ChannelHandlerContext ctx) {
        Map<String, Object> accepterMap = getDataMap();
        //根据库位，商品条码，查询可操作数bu为大于0的，商品状态为正品的库存
        RemoteResult<PageModel<InstockDetail>> pageModelRemoteResult = instockRemoteService.queryInstockDetailsPage(getCredentialsVO(ctx), getParaMap());
        if (pageModelRemoteResult.isSuccess()) {
            //展示分页列表
            PageModel<InstockDetail> detailsPageModel = pageModelRemoteResult.getT();
            accepterMap.put(PageUtil.PAGE_MODEL, detailsPageModel);
            HandlerUtil.changeRow(ctx);
            List<InstockDetail> stockInfos = detailsPageModel.getDatas();
            String skuName = stockInfos.get(0).getSkuname();
            int currPageLinesNum = PageUtil.showTable(ctx, detailsPageModel, ITEM_TABLE_NAME, ITEM_TABLE_COLUMN, true, true, TipConstants.SKU_NAME + skuName);//展示列表，带有序号
            accepterMap.put(PageUtil.LINES_NUM_CLEAN_KEY, currPageLinesNum);
            HandlerUtil.moveUpN(ctx,1);//去掉空格
            resetCurCol(SELECT_PAGE, accepterMap, ctx);
        } else {//输出错误信息（无数据或异常）
            super.colNeedReInput(BARCODE, ErrorConstants.DATA_NOT_FOUNT_CONTINUE, accepterMap, ctx);
        }
    }
    /**
     * 分页查询map参数
     * @return
     */
    private  HashMap<String, Object> getParaMap() {
        Map<String, Object> accepterMap = getDataMap();
        HashMap<String, Object> map = (HashMap<String, Object>) accepterMap.get(PageUtil.PARA_PAGE_MAP);//缓存中的分页查询参数Map
        if (map == null) {
            final InstockDetail instockDetail = new InstockDetail();
            instockDetail.setPage(Constants.PAGE_START);
            instockDetail.setRows(Constants.STOCK_INFO_PAGE_SIZE);
            instockDetail.setSidx("ASNINSTOCK_DETAIL_ID");
            instockDetail.setSord(Constants.PAGE_SORT_DESC);
            instockDetail.setAsninstockId(inStock.getAsninstockId());//入库单id
            instockDetail.setSkuid(initInStockDetail.getSkuid());//商品id
            instockDetail.setDatatype(WmsConstants.DATATYPE_INIT);//初始数据
            map = new HashMap<String, Object>() {{
                put(WmsConstants.KEY_INSTOCKDETAILS_PARAM, instockDetail);
            }};
            accepterMap.put(PageUtil.PARA_PAGE_MAP, map);
            return map;
        }
        return map;
    }
    /**
     * 根据选择的日期类型及输入的日期进行校验是否可以收货
     *
     * @param quickInstock 业务对象，用于接收输入的数据
     * @param detail           查询到的instock_detail数据，包含对应的商品基本日期数据
     * @return 返回不同的校验结果，如果为空则说明校验通过
     */
    public QuickInstock countProdAndExpDate(QuickInstock quickInstock, InstockDetail detail) {
        String dataType = quickInstock.getDateType();//日期类型
        String prodOrExpDate = quickInstock.getProdOrExpDate();//接收的日期输入
        Integer keepdays = detail.getKeepdays();//商品保质期
        Integer warningday = detail.getWarningday(); //商品预警时间
        Integer instockdays = detail.getInstockdays(); //商品允收期

        Date productionDate;//生产日期
        Date expirationDate;//失效期
        Date nearValidDate;//近效期
        Date canInStockDate;//允收期
        if (Constants.batchRuleEnum.puTong.dateType.equals(dataType)) {
            productionDate = DateTimeUtil.parseSimpleStr(prodOrExpDate);
            expirationDate = DateTimeUtil.modifyDate(productionDate, keepdays);
        } else {
            expirationDate = DateTimeUtil.parseSimpleStr(prodOrExpDate);
            productionDate = DateTimeUtil.modifyDate(expirationDate, keepdays * -1);
        }
        nearValidDate = DateTimeUtil.modifyDate(productionDate, warningday);
        canInStockDate = DateTimeUtil.modifyDate(productionDate, instockdays);
        quickInstock.setProductionDate(productionDate);
        quickInstock.setExpirationDate(expirationDate);
        quickInstock.setNearValidDate(nearValidDate);
        quickInstock.setCanInStockDate(canInStockDate);
        return quickInstock;
    }
    /**
     * 获取主单内同一sku，同一行号明细的已收货数量
     * @param selectDetail 本次选择的明细数据
     * @return 已经收货的数量
     */
    private Integer queryReceivedBU(InstockDetail selectDetail) {
        List<InstockDetail> detailList = instockRemoteService.querySameSerialIDDetails(getCredentialsVO(ctx), selectDetail);
        Integer receivedBU = 0;
        for (InstockDetail detail : detailList) {
            receivedBU = receivedBU + detail.getReceivenumbu();
        }
        return receivedBU;
    }
    private void printErrorMsessage(ChannelHandlerContext ctx,Map<String, Object> accepterMap,String message){
        HandlerUtil.errorBeep(ctx);
        accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ACTIVE);
        HandlerUtil.println(ctx, Constants.BREAK_LINE + message + ErrorConstants.TIP_TO_CONTINUE);
    }
}
