package com.womai.wms.rf.manager.window.instock;

import com.google.common.collect.Lists;
import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.*;
import com.womai.wms.rf.domain.instock.IntentionInstock;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.base.GoodsinfoRemoteService;
import com.womai.wms.rf.remote.instock.InstockRemoteService;
import com.womai.zlwms.rfsoa.api.WMSErrorMess;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.base.BaseGoodsinfo;
import com.womai.zlwms.rfsoa.domain.base.BasePackaginginfo;
import com.womai.zlwms.rfsoa.domain.base.BasePallet;
import com.womai.zlwms.rfsoa.domain.instock.Instock;
import com.womai.zlwms.rfsoa.domain.instock.InstockDetail;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * 意向收货单
 * User:zhangwei
 * Date: 2016-06-20
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component("intentionInstockManager")
public class IntentionInstockManagerImpl extends ReceiveManager {

    @Autowired
    private InstockRemoteService instockRemoteService;
    private ChannelHandlerContext ctx;
    private final static String GO_TO_FLAG = "inStock_goto";//跳转标志
    private final static Integer TO_CHANNEL_ACTIVE = -1;//跳转标志
    private final static Integer TO_SCAN_CODE = -2;//跳转到扫描商品条码
    private final static Integer TO_PRODATE_CODE = -3;//跳转到日期录入
    private final static Integer TO_SUGGEST_PRODATE_CODE = -4;//Y继续，其他键跳转到日期录入

    private final static String SCAN_CODE = "scanCode";//扫描的ASN单号/网络订单号
    private final static String SELECT_ASN = "selectAsn";//选择ASN单号
    private final static String BARCODE = "barCode";//商品条码
    private final static String SELECT_PAGE = "selectPage";//翻页
    private final static String UNIT_NAME = "unitName";//包装单位
    private final static String PROD_OR_EXP_DATE = "prodOrExpDate";//生产日期或失效日期
    private final static String CHECK_NUM = "checkNum";//收货数量，实际质检数量
    private final static String PALLET_CODE = "palletCode";//托盘编码
    private final static String CONFIRM_IN = "confirmIn";//确认收货


    private final static String SMALLEST_DATE = "20000101";//所能输入的最小日期

    private List<Instock> TL_inStockList = new ArrayList<Instock>();//按照ASN单号或网络订单号查询到的多条数据
    private List<String> TL_ASNCodeList = new ArrayList<String>();//按照网络订单号查询到的多个ASN单号
    private Instock TL_inStock;//选择的一条主单数据
    private InstockDetail TL_initInStockDetail;//选择的入库明细
    private List<BasePackaginginfo> TL_packagingInfoList = new ArrayList<BasePackaginginfo>();//查询到的包装数据列表
    private List<String> TL_unitNameList = new ArrayList<String>();//与包装数据对应的包装名称
    private BasePackaginginfo TL_selectedPack;//选择的包装
    private Integer TL_ReceivedBU = 0;//未收货BU数量
    private static final String[] pageHeader = {"", TipConstants.INTENTION_INSTOCK, Constants.SPLIT, ""};
    private List<String> dateTypeList = CollectionUtil.newList(Constants.batchRuleEnum.
            puTong.dateType, Constants.batchRuleEnum.xiHua.dateType);//日期类型列表
    private final static String DATE_TYPE = "dateType";//日期类型，选择生产日期或失效期
    private final static String[] ITEM_TABLE_NAME = {"序号", "包装单位", "计划收货数量BU", "收货数量BU"};//明细表头
    private final static String[] ITEM_TABLE_COLUMN = {"unitname", "expectnumbu", "sumreceivenumbu"};//明细列名

    private final static String INSTOCK = "instock";
    private final static String INSTOCKDETAIL = "instockDetail";
    private final static String ORDERTYPE = "orderType";

    @Autowired
    private GoodsinfoRemoteService goodsinfoRemoteService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        removeLocals();
        this.ctx = ctx;
        super.initBaseMap(IntentionInstock.class, pageHeader, ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        Map<String, Object> accepterMap = getDataMap();
        if (accepterMap.get(GO_TO_FLAG) != null) {
            Integer goToAim = (Integer) accepterMap.get(GO_TO_FLAG);
            if (TO_PRODATE_CODE.equals(goToAim)) {
                //跳转到日期录入，重新查询商品信息获得新的保质期，预警时间
                boolean succ = resetInstockDetail(ctx, accepterMap, TL_initInStockDetail);
                if (succ) {
                    toProDateFromLast(accepterMap, ctx);
                    accepterMap.remove(GO_TO_FLAG);
                } else {
                    accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ACTIVE);
                }
                return;
            }

            if (TO_SUGGEST_PRODATE_CODE.equals(goToAim)) {
                //Y继续，其它键，跳转到日期录入，重新查询商品信息
                if (Constants.CONFIRM_Y.equalsIgnoreCase(object.toString())) {
                    boolean succ = resetInstockDetail(ctx, accepterMap, TL_initInStockDetail);
                    if(succ){
                        //获得提交参数，重新提交数据
                        Instock instock = (Instock) accepterMap.get(INSTOCK);
                        InstockDetail instockDetail = (InstockDetail) accepterMap.get(INSTOCKDETAIL);
                        int[] orderType = (int[]) accepterMap.get(ORDERTYPE);
                        RemoteResult<String> result = this.instockRemoteService.confirmInStock(getCredentialsVO(ctx), instock, instockDetail, orderType);
                        clearReCommiteParameter(accepterMap);
                        HandlerUtil.moveUpN(ctx,1);
                        HandlerUtil.changeRow(ctx);
                        HandlerUtil.clearRight(ctx);
                        disposeResult(accepterMap, result.getResultCode());
                    }else{
                        accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ACTIVE);
                    }

                } else {
                    clearReCommiteParameter(accepterMap);
                    boolean succ = resetInstockDetail(ctx, accepterMap, TL_initInStockDetail);
                    if (succ) {
                        toProDateFromLast(accepterMap, ctx);
                        accepterMap.remove(GO_TO_FLAG);
                    } else {
                        accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ACTIVE);
                    }
                }
                return;
            }


            if (TO_CHANNEL_ACTIVE.equals(goToAim)) {
                channelActive(ctx);
                return;
            }
            if (TO_SCAN_CODE.equals(goToAim)) {//add by wzh 20170302
                //跳转到扫描商品条码
                List<String> showStrings = CollectionUtil.newList(SCAN_CODE, SELECT_ASN);
                List<String> clearStrings = CollectionUtil.newList(SELECT_PAGE, UNIT_NAME, PROD_OR_EXP_DATE, CHECK_NUM, PALLET_CODE, CONFIRM_IN);
                printFieldsAndReceiveData(pageHeader, showStrings, BARCODE, clearStrings, accepterMap, ctx);
                accepterMap.remove(GO_TO_FLAG);
                TL_ReceivedBU = 0;
                return;
            }

        }
        receiveDataAndNotPrintNext(ctx, object, accepterMap);
        IntentionInstock intentionInstock = (IntentionInstock) accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (SCAN_CODE.equals(lastCompleteColName)) {
            String scanCode = intentionInstock.getScanCode();
            RemoteResult<List<Instock>> listRemoteResult = instockRemoteService.queryIntentionByASNCodeOrPurchaseCode(getCredentialsVO(ctx), scanCode);
            //未查询到数据则提示重新扫描
            List<Instock> instockList = listRemoteResult.getT();
            TL_inStockList = instockList;
            if (!listRemoteResult.isSuccess()) {
                colNeedReInput(lastCompleteColName, listRemoteResult.getResultCode(), accepterMap, ctx);
            } else if (instockList.size() == 1) {
                //只查询到一条数据则直接跳过选择ASN单号的步骤，直接到输入商品条码，并将选择的入库主单数据存入Thread变量
                Instock selectedInstock = instockList.get(0);
                //如果是换货意向单则需要验证是否已经出库
                if (selectedInstock.getOrdertype().equals(WmsConstants.INSTOCK_ORDER_TYPE_HUANHUO)) {
                    String serialNo = selectedInstock.getSerialno();
                    Boolean isOutStock = instockRemoteService.intentionIsOutStock(getCredentialsVO(ctx), serialNo);
                    if (!isOutStock) {
                        colNeedReInput(lastCompleteColName, ErrorConstants.NOT_OUT_STOCK, accepterMap, ctx);
                        return;
                    }
                }
                TL_inStock = selectedInstock;
                setDefaultValue(selectedInstock.getAsninstockcode(), SELECT_ASN, accepterMap, ctx);
                resetCurCol(BARCODE, accepterMap, ctx);
            } else if (instockList.size() > 1) {
                //如果存在多条数据，则需要切换选择ASN单号
                List<String> asnCodeList = Lists.newArrayList();
                for (Instock instock : instockList) {
                    asnCodeList.add(instock.getAsninstockcode());
                }
                TL_ASNCodeList = asnCodeList;
                setNextColSwitchList(asnCodeList, accepterMap, ctx);
            }
        }

        if (SELECT_ASN.equals(lastCompleteColName)) {
            String selectAsn = intentionInstock.getSelectAsn();
            List<String> asnCodeList = TL_ASNCodeList;//Thread中的单号List
            if (!asnCodeList.contains(selectAsn)) {
                setColReSwitchList(asnCodeList, ErrorConstants.DATA_NOT_FOUNT_CONTINUE, accepterMap, ctx);
            } else {
                Integer listIndex = (Integer) accepterMap.get(DefaultKey.listIndex.keyName);
                Instock selectedInstock = TL_inStockList.get(listIndex);
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
                //将选择的数据存入Thread变量
                TL_inStock = selectedInstock;
            }
        }

        List<String> unitNameList = Lists.newArrayList();
        List<BasePackaginginfo> packagingInfoList;
        InstockDetail initDetail = TL_initInStockDetail == null ? new InstockDetail() : TL_initInStockDetail;
        if (BARCODE.equals(lastCompleteColName)) {
            String barCode = intentionInstock.getBarCode();
            Long asnInStockId = TL_inStock.getAsninstockId();
            Map<String, Object> detailAndPackagingList = instockRemoteService.queryDetailAndGoodsAndPackaging(getCredentialsVO(ctx), asnInStockId, barCode);
            if (detailAndPackagingList == null || detailAndPackagingList.isEmpty()) {
//                super.printBeforeNextField(ErrorConstants.DATA_NOT_FOUNT_CONTINUE, accepterMap, ctx);
//                accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ACTIVE);
                //modify by wzh 20170202
                super.colNeedReInput(BARCODE, ErrorConstants.DATA_NOT_FOUNT_CONTINUE, accepterMap, ctx);
                return;
            } else {
                //v4.3 add by jjk 2017-07-26 扫描条码是先判断商品状态，失效时提示
                String errorMsg = (String) detailAndPackagingList.get(WmsConstants.KEY_ERROR_MSG);
                if (StringUtils.isNotEmpty(errorMsg)) {
                    super.colNeedReInput(BARCODE, errorMsg, accepterMap, ctx);
                    return;
                }
                initDetail = (InstockDetail) detailAndPackagingList.get(WmsConstants.KEY_INSTOCKDETAIL_PARAM);
                TL_initInStockDetail = initDetail;
                Integer initDetailSize = (Integer) detailAndPackagingList.get(WmsConstants.KEY_INSTOCK_DETAIL_INIT_SIZE);
                packagingInfoList = (List<BasePackaginginfo>) detailAndPackagingList.get(WmsConstants.KEY_BASEPACKAGINGINFOLIST_PARAM);
                TL_packagingInfoList = packagingInfoList;
                for (BasePackaginginfo packaginginfo : packagingInfoList) {
                    unitNameList.add(packaginginfo.getUnitname());
                }
                TL_unitNameList = unitNameList;

                if (initDetailSize != null && initDetailSize.intValue() == 1) {
                    String skuName = initDetail.getSkuname() == null ? "" : initDetail.getSkuname();
                    skuName = RFUtil.makeStrFitPda(skuName, TipConstants.GOODS_NAME, 2);
                    super.printBeforeNextField(TipConstants.GOODS_NAME + skuName, accepterMap, ctx);
                    resetCurColNoPrint(UNIT_NAME, accepterMap, ctx);//跳过分页查询明细
                    super.setNextColSwitchList(unitNameList, accepterMap, ctx);
                } else {
                    //add by wzh v3.8 20170324 同skuid所有收货的明细都显示出来
                    showItemsPage(ctx);
                }
            }
        }
        if (SELECT_PAGE.equals(lastCompleteColName)) {
            String pageNum = intentionInstock.getSelectPage();
            PageModel<InstockDetail> pageModle = (PageModel<InstockDetail>) accepterMap.get(PageUtil.PAGE_MODEL);
            int pageSizeCurr = (Integer) accepterMap.get(PageUtil.LINES_NUM_CLEAN_KEY);//页面当前数据条数
            if (KeyEnum.N_78.value.equalsIgnoreCase(pageNum)) {//下一页
                PageUtil.changePageNext(ctx, accepterMap, WmsConstants.KEY_INSTOCKDETAILS_PARAM, pageSizeCurr);
                setColUnReceived(SELECT_PAGE, accepterMap);
                showItemsPage(ctx);
                return;
            } else if (KeyEnum.B_66.value.equalsIgnoreCase(pageNum)) {//上一页
                PageUtil.changePageUp(ctx, accepterMap, WmsConstants.KEY_INSTOCKDETAILS_PARAM, pageSizeCurr);
                setColUnReceived(SELECT_PAGE, accepterMap);
                showItemsPage(ctx);
                return;
            } else {//序号
                if (pageNum.matches(TipConstants.REG_SERIAL_NO)) {
                    List<InstockDetail> items = pageModle.getDatas();
                    int maxIndex = items.size() - 1;
                    int index = PageUtil.getIndexFromSerialnoAndPageModle(pageNum, pageModle);//(pn-1)*size+index+1==serialno
                    if (index > maxIndex || index < 0) {
                        colNeedReInput(SELECT_PAGE, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                        return;
                    } else {
                        InstockDetail detail = items.get(index);
                        if (detail.getExpectnumbu().compareTo(detail.getSumreceivenumbu()) < 1) {//已经收货的大于，或等于期待收货数量bu
                            colNeedReInput(SELECT_PAGE, ErrorConstants.BIGGER_THAN_EXPECT, accepterMap, ctx);
                            return;
                        }
                        //正确选择原库存，保存参数
                        TL_initInStockDetail = detail;
                        //清空明细分页查询条件
                        accepterMap.remove(PageUtil.PARA_PAGE_MAP);
                        resetCurColNoPrint(UNIT_NAME, accepterMap, ctx);
                        super.setNextColSwitchList(TL_unitNameList, accepterMap, ctx);
                        return;
                    }
                } else {
                    colNeedReInput(SELECT_PAGE, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                    return;
                }
            }
        }
        if (UNIT_NAME.equals(lastCompleteColName)) {
            Integer batchRule = initDetail.getBatchrule();
            String unitName = intentionInstock.getUnitName();
            Integer listIndex = (Integer) accepterMap.get(DefaultKey.listIndex.keyName);
            packagingInfoList = TL_packagingInfoList;
            unitNameList = TL_unitNameList;
            if (!unitNameList.contains(unitName)) {
                setColReSwitchList(unitNameList, ErrorConstants.UNIT_NAME_NOT_FOUNT, accepterMap, ctx);
            } else {
                BasePackaginginfo selectedPack = packagingInfoList.get(listIndex);
                if (unitName.equalsIgnoreCase(TipConstants.PK_LEVEL2_NAME)) {
                    super.printBeforeNextField(TipConstants.GOODSINFO_PKNUM_LABEL + ErrorConstants.COMMON_COLON + selectedPack.getPknum(), accepterMap, ctx);
                }
                TL_selectedPack = selectedPack;
                //根据批次提示输入生产日期或失效日期
                String DateTypeTip = Constants.batchRuleEnum.getDateTypeByCode(batchRule);
                super.setNextColSwitchList(DateTypeTip, dateTypeList, accepterMap, ctx);
            }
        }
        if (lastCompleteColName.equalsIgnoreCase(DATE_TYPE)) {
            String dateType = intentionInstock.getDateType();
            if (!dateTypeList.contains(dateType)) {
                setColReSwitchList(dateTypeList, ErrorConstants.DATE_TYPE_NOT_IN_LIST, accepterMap, ctx);
            } else {
                resetCurCol(PROD_OR_EXP_DATE, accepterMap, ctx);
            }
        }
        if (PROD_OR_EXP_DATE.equalsIgnoreCase(lastCompleteColName)) {
            String prodOrExpDate = intentionInstock.getProdOrExpDate();
            Boolean dateReg = DateTimeUtil.isSimpleDate(prodOrExpDate);
            if (dateReg) {
                //根据选择的日期类型及输入的日期计算生产、近效期、允收期、失效期
                Date inputDate = DateTimeUtil.parseSimpleStr(prodOrExpDate);
                Date smallestDate = DateTimeUtil.parseSimpleStr(SMALLEST_DATE);
                Boolean isSmallest = DateTimeUtil.compareDate(smallestDate, inputDate);
                if (isSmallest) {
                    super.colNeedReInput(PROD_OR_EXP_DATE, ErrorConstants.ILLEGAL_DATE_PATTERN, accepterMap, ctx);
                } else {
                    intentionInstock = countProdAndExpDate(intentionInstock, initDetail);
                    //add by wangzhanhua 2017-02-16 校验生产日期不可以大于今天
                    if (DateTimeUtil.isDateAfterToday(intentionInstock.getProductionDate())) {
                        super.colNeedReInput(PROD_OR_EXP_DATE, ErrorConstants.PRO_BIGGER_NOW, accepterMap, ctx);
                        return;
                    }
                    HashMap<String, Object> param = new HashMap<String, Object>();
                    param.put(WmsConstants.KEY_INSTOCK_PARAM_SKUID, initDetail.getSkuid());
                    param.put(WmsConstants.KEY_INSTOCK_PARAM_PURCHASENO, TL_inStock.getPurchaseno());
                    param.put(WmsConstants.KEY_INSTOCK_PARAM_ASNINSTOCKID, TL_inStock.getAsninstockId());
                    String proStr = DateTimeUtil.getStringWithSeparator(intentionInstock.getProductionDate());
                    param.put(WmsConstants.KEY_INSTOCK_PARAM_PRODATE, proStr);
                    param.put(WmsConstants.KEY_INSTOCK_PARAM_ASNTYPE, TL_inStock.getOrdertype());
                    //查询出库单中的生产日期，如果与当前的生产日期不同则进行提示
                    RemoteResult<String> result = instockRemoteService.queryProDateInOutOrder(getCredentialsVO(ctx), param);
                    if (result.isSuccess()) {
                        printBeforeNextField(TipConstants.UN_RECEIVE_BU + (initDetail.getExpectnumbu() - queryReceivedBU(initDetail)), accepterMap, ctx);
                        rePrintCurColTip(accepterMap, ctx);
                    } else {
                        super.colNeedReInput(PROD_OR_EXP_DATE, result.getResultCode(), accepterMap, ctx);
                    }
                }
            } else {
                super.colNeedReInput(PROD_OR_EXP_DATE, ErrorConstants.ILLEGAL_DATE_PATTERN, accepterMap, ctx);
            }
        }

        if (CHECK_NUM.equals(lastCompleteColName)) {
            //输入“..”跳转到输入日期的地方
            if (Constants.ORDER_FOR_BACK_OPER.equals(intentionInstock.getCheckNum())) {
                toProDate(accepterMap, ctx);
                return;
            }
            //意向单可以0入库
            String patternReg = "^[0-9]\\d{0,9}$";
            String checkNum = intentionInstock.getCheckNum();
            if (checkNum.matches(patternReg)) {
                Integer check = null;
                try {
                    check = Integer.parseInt(checkNum);
                } catch (NumberFormatException e) {
                    colNeedReInput(CHECK_NUM, ErrorConstants.BIGGER_THAN_EXPECT, accepterMap, ctx);
                    return;
                }
                String unitName = intentionInstock.getUnitName();
                Integer receivedBu = queryReceivedBU(initDetail);
                //如果包装单位选择的是箱，则实际收货数量为输入数量乘以箱规加上已经收货的数量
                if (unitName.equalsIgnoreCase(TipConstants.PK_LEVEL2_NAME)) {
                    receivedBu = receivedBu + (check * initDetail.getPknum());
                } else {
                    receivedBu = receivedBu + check;
                }
                if (receivedBu > TL_initInStockDetail.getExpectnumbu() || receivedBu < 0) {
                    colNeedReInput(CHECK_NUM, ErrorConstants.BIGGER_THAN_EXPECT, accepterMap, ctx);
                    return;
                }
                rePrintCurColTip(accepterMap, ctx);
            } else {
                colNeedReInput(CHECK_NUM, ErrorConstants.ILLEGAL_DATA, accepterMap, ctx);
            }
        }

        if (PALLET_CODE.equals(lastCompleteColName)) {
            //输入“..”跳转到输入数量的地方
            if (Constants.ORDER_FOR_BACK_OPER.equals(intentionInstock.getPalletCode())) {
                List<String> showStrings = CollectionUtil.newList(SCAN_CODE, BARCODE, UNIT_NAME, PROD_OR_EXP_DATE);
                List<String> clearStrings = CollectionUtil.newList(SELECT_PAGE, PALLET_CODE);
                printFieldsAndReceiveData(pageHeader, UNIT_NAME, showStrings, CHECK_NUM, clearStrings, accepterMap, ctx);
                TL_ReceivedBU = 0;
                HandlerUtil.moveUpN(ctx, 1);
                printBeforeNextField(TipConstants.UN_RECEIVE_BU + (initDetail.getExpectnumbu() - queryReceivedBU(initDetail)), accepterMap, ctx);
                rePrintCurColTip(accepterMap, ctx);
                return;
            }
            String palletCode = intentionInstock.getPalletCode();
            if (StringUtils.isNotEmpty(palletCode)) {
                BasePallet basePallet = instockRemoteService.getPalletByCode(getCredentialsVO(ctx), palletCode);
                if (basePallet == null) {
                    colNeedReInput(PALLET_CODE, "托盘编码不存在", accepterMap, ctx);
                } else if (basePallet.getPstatus().equals(WmsConstants.STATUS_DISABLE)) {
                    colNeedReInput(PALLET_CODE, "托盘编码已失效", accepterMap, ctx);
                } else if (basePallet.getPstatus().equals(WmsConstants.STATUS_USED)) {
                    colNeedReInput(PALLET_CODE, "托盘编码已使用", accepterMap, ctx);
                } else {
                    rePrintCurColTip(accepterMap, ctx);
                }
            } else {
                rePrintCurColTip(accepterMap, ctx);
            }
        }

        if (CONFIRM_IN.equalsIgnoreCase(lastCompleteColName)) {
            //输入“..”跳转到输入数量的地方
            if (Constants.ORDER_FOR_BACK_OPER.equals(intentionInstock.getConfirmIn())) {
                List<String> showStrings = CollectionUtil.newList(SCAN_CODE, BARCODE, UNIT_NAME, PROD_OR_EXP_DATE);
                List<String> clearStrings = CollectionUtil.newList(SELECT_PAGE, PALLET_CODE, CONFIRM_IN);
                printFieldsAndReceiveData(pageHeader, UNIT_NAME, showStrings, CHECK_NUM, clearStrings, accepterMap, ctx);
                TL_ReceivedBU = 0;
                HandlerUtil.moveUpN(ctx, 1);
                printBeforeNextField(TipConstants.UN_RECEIVE_BU + (initDetail.getExpectnumbu() - queryReceivedBU(initDetail)), accepterMap, ctx);
                rePrintCurColTip(accepterMap, ctx);
                return;
            }
            String confirmIn = intentionInstock.getConfirmIn();
            if (Constants.CONFIRM_Y.equalsIgnoreCase(confirmIn)) {
                Instock instock = TL_inStock;
                InstockDetail instockDetail = TL_initInStockDetail;
                BasePackaginginfo basePackaginginfo = TL_selectedPack;

                //将接收到的合法数据set到入库单明细中传到soa进行业务处理
                instockDetail = setDetailValues(instockDetail, basePackaginginfo, intentionInstock);
                //所输入的收货数量
                Integer checkNum = Integer.parseInt(intentionInstock.getCheckNum());
                //箱规
                Integer pkNum = instockDetail.getPknum();
                String unitName = intentionInstock.getUnitName();
                //实际收货包装数量，如果为箱，则为实际收货数量为所输入的收货后数量乘以箱规
                if (unitName.equalsIgnoreCase(TipConstants.PK_LEVEL2_NAME)) {
                    instockDetail.setReceivenumbu(checkNum * pkNum);
                    instockDetail.setReceivepknum(checkNum);
                } else {
                    instockDetail.setReceivenumbu(checkNum);
                    instockDetail.setReceivepknum(checkNum / pkNum);
                }
                //实际收货金额
                BigDecimal inStockMoney = instockDetail.getPrice().multiply(new BigDecimal(instockDetail.getReceivenumbu()));
                instockDetail.setInstockmoney(inStockMoney);
                //soa端主要业务处理
                int[] orderType = {WmsConstants.INSTOCK_ORDER_TYPE_XIAOTUI, WmsConstants.INSTOCK_ORDER_TYPE_JUSHOU, WmsConstants.INSTOCK_ORDER_TYPE_HUANHUO};
                initDetail.setDateType(intentionInstock.getDateType());//用户选择的日期类型，后端用来重新计算生产日期，失效日期
                RemoteResult<String> stringRemoteResult = this.instockRemoteService.confirmInStock(getCredentialsVO(ctx), instock, instockDetail, orderType);
                String suggestionMsg = stringRemoteResult.getT();
                String inStockResult = stringRemoteResult.getResultCode();
                if (StringUtils.isNotBlank(suggestionMsg)) {
                    //基础信息里的保质期，预警时间发生变化。重新计算的生产日期，失效日期，再次校验，提示语
                    if (WMSErrorMess.VERIFY_FORCE_ERROR_CODE.equals(suggestionMsg)) {
                        //跳转日期录入，重新查询保质期，预警时间
                        accepterMap.put(GO_TO_FLAG, TO_PRODATE_CODE);
                        HandlerUtil.write(ctx, inStockResult + ErrorConstants.TIP_TO_CONTINUE);
                        HandlerUtil.errorBeep(ctx);//系统错误，响铃
                    }
                    if (WMSErrorMess.VERIFY_SUGGEST_ERROR_CODE.equals(suggestionMsg)) {
                        //日期校验后，提示用户输入Y继续，其他键跳到日期录入 ，重新查询保质期，预警时间
                        accepterMap.put(GO_TO_FLAG, TO_SUGGEST_PRODATE_CODE);
                        accepterMap.put(INSTOCK, instock);
                        accepterMap.put(INSTOCKDETAIL, initDetail);
                        accepterMap.put(ORDERTYPE, orderType);
                        HandlerUtil.write(ctx, inStockResult + ",Y继续，其它键重新输入日期");
                        HandlerUtil.errorBeep(ctx);//系统错误，响铃
                    }
                    return;
                }
                // modify by wzh 2017-03-02 登记完成后跳转到扫描商品条码，整单完成后跳转到扫描ASN单号
                disposeResult(accepterMap, inStockResult);
            } else if (Constants.CANCEL_N.equalsIgnoreCase(confirmIn)) {
                channelActive(ctx);
            } else {
                colNeedReInput(CONFIRM_IN, ErrorConstants.ONLY_YN, accepterMap, ctx);
            }
        }


    }

    private void clearReCommiteParameter(Map<String, Object> accepterMap) {
        accepterMap.remove(INSTOCKDETAIL);
        accepterMap.remove(INSTOCK);
        accepterMap.remove(ORDERTYPE);
    }

    /**
     * 从最后一步跳转到日期录入的地方
     *
     * @param accepterMap
     * @param ctx
     */
    private void toProDateFromLast(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        List<String> showStrings = CollectionUtil.newList(SCAN_CODE, BARCODE, UNIT_NAME);
        List<String> clearStrings = CollectionUtil.newList(SELECT_PAGE, CHECK_NUM, PALLET_CODE, CONFIRM_IN);
        printFieldsAndReceiveData(pageHeader, UNIT_NAME, showStrings, PROD_OR_EXP_DATE, clearStrings, accepterMap, ctx);
        TL_ReceivedBU = 0;
    }

    /**
     * 对返回结果的处理
     *
     * @param accepterMap
     * @param inStockResult 错误信息
     */
    private void disposeResult(Map<String, Object> accepterMap, String inStockResult) {
        HandlerUtil.write(ctx, inStockResult + ErrorConstants.TIP_TO_CONTINUE);
        if (WMSErrorMess.INSTOCK_SUCCESS.equals(inStockResult)) {//登记中，跳转到扫描商品条码
            accepterMap.put(GO_TO_FLAG, TO_SCAN_CODE);
        } else {//登记完成，或者有异常，跳转扫描ASN单号
            if (StringUtils.isNotBlank(inStockResult)) {
                HandlerUtil.errorBeep(ctx);//系统错误，响铃
            }
            accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ACTIVE);
        }
    }

    /**
     * 流程跳到日期录入的地方
     *
     * @param accepterMap
     * @param ctx
     */
    private void toProDate(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        List<String> showStrings = CollectionUtil.newList(SCAN_CODE, BARCODE, UNIT_NAME);
        List<String> clearStrings = CollectionUtil.newList(SELECT_PAGE, CHECK_NUM);
        printFieldsAndReceiveData(pageHeader, UNIT_NAME, showStrings, PROD_OR_EXP_DATE, clearStrings, accepterMap, ctx);
        TL_ReceivedBU = 0;
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

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void showItemsPage(ChannelHandlerContext ctx) {
        Map<String, Object> accepterMap = getDataMap();
        //根据库位，商品条码，查询可操作数bu为大于0的，商品状态为正品的库存
        RemoteResult<PageModel<InstockDetail>> pageModelRemoteResult = instockRemoteService.queryInstockDetailsPage(getCredentialsVO(ctx), getParaMap());
        if (pageModelRemoteResult.isSuccess()) {
            //展示库存分页列表，一定有数据
            PageModel<InstockDetail> detailsPageModel = pageModelRemoteResult.getT();
            accepterMap.put(PageUtil.PAGE_MODEL, detailsPageModel);
            HandlerUtil.changeRow(ctx);
            List<InstockDetail> stockInfos = detailsPageModel.getDatas();
            String skuName = stockInfos.get(0).getSkuname();
            int currPageLinesNum = PageUtil.showTable(ctx, detailsPageModel, ITEM_TABLE_NAME, ITEM_TABLE_COLUMN, true, true, TipConstants.SKU_NAME + skuName);//展示列表，带有序号
            accepterMap.put(PageUtil.LINES_NUM_CLEAN_KEY, currPageLinesNum);
            HandlerUtil.moveUpN(ctx, 1);//去掉空格
            resetCurCol(SELECT_PAGE, accepterMap, ctx);
        } else {//输出错误信息（无数据或异常）
            super.colNeedReInput(BARCODE, ErrorConstants.DATA_NOT_FOUNT_CONTINUE, accepterMap, ctx);
        }
    }

    /**
     * 分页查询map参数
     *
     * @return
     */
    private HashMap<String, Object> getParaMap() {
        Map<String, Object> accepterMap = getDataMap();
        HashMap<String, Object> map = (HashMap<String, Object>) accepterMap.get(PageUtil.PARA_PAGE_MAP);//缓存中的分页查询参数Map
        if (map == null) {
            final InstockDetail instockDetail = new InstockDetail();
            instockDetail.setPage(Constants.PAGE_START);
            instockDetail.setRows(Constants.STOCK_INFO_PAGE_SIZE);
            instockDetail.setSidx("ASNINSTOCK_DETAIL_ID");
            instockDetail.setSord(Constants.PAGE_SORT_DESC);
            instockDetail.setAsninstockId(TL_inStock.getAsninstockId());//入库单id
            instockDetail.setSkuid(TL_initInStockDetail.getSkuid());//商品id
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
     * 将接收到的合法用户输入set到入库单明细中
     * 需要在soa端进行金额计算，再次校验箱规等验证
     *
     * @param detail            入库单明细
     * @param basePackaginginfo 选择的包装数据
     * @param intentionInstock  数据接收对象
     * @return 返回设置值的入库单过明细数据
     */
    public InstockDetail setDetailValues(InstockDetail detail, BasePackaginginfo basePackaginginfo, IntentionInstock intentionInstock) {

        detail.setPkid(basePackaginginfo.getId());
        detail.setPklevel(basePackaginginfo.getPacklevel());
        detail.setUnitname(basePackaginginfo.getUnitname());
        detail.setPknum(basePackaginginfo.getPknum());
        //日期
        detail.setProductiondate(intentionInstock.getProductionDate());
        detail.setExpirationdate(intentionInstock.getExpirationDate());
        detail.setNearvaliddate(intentionInstock.getNearValidDate());
        //托盘编码
        detail.setPalletCode(intentionInstock.getPalletCode());
        //实际收货数量BU
        detail.setReceivenumbu(Integer.parseInt(intentionInstock.getCheckNum()));
        return detail;
    }


    /**
     * 根据选择的日期类型及输入的日期进行校验是否可以收货
     *
     * @param intentionInstock 业务对象，用于接收输入的数据
     * @param detail           查询到的instock_detail数据，包含对应的商品基本日期数据
     * @return 返回不同的校验结果，如果为空则说明校验通过
     */
    public IntentionInstock countProdAndExpDate(IntentionInstock intentionInstock, InstockDetail detail) {
        String dataType = intentionInstock.getDateType();//日期类型
        String prodOrExpDate = intentionInstock.getProdOrExpDate();//接收的日期输入
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
        intentionInstock.setProductionDate(productionDate);
        intentionInstock.setExpirationDate(expirationDate);
        intentionInstock.setNearValidDate(nearValidDate);
        intentionInstock.setCanInStockDate(canInStockDate);
        return intentionInstock;
    }


    /**
     * 获取主单内同一sku，同一行号明细的已收货数量
     *
     * @param selectDetail 本次选择的明细数据
     * @return 已经收货的数量
     */
    private Integer queryReceivedBU(InstockDetail selectDetail) {
        if (TL_ReceivedBU == null || TL_ReceivedBU == 0) {
            List<InstockDetail> detailList = instockRemoteService.querySameSerialIDDetails(getCredentialsVO(ctx), selectDetail);
            Integer receivedBU = 0;
            for (InstockDetail detail : detailList) {
                receivedBU = receivedBU + detail.getReceivenumbu();
            }
            TL_ReceivedBU = receivedBU;
        }
        return TL_ReceivedBU;
    }

    /**
     * 移除本地变量
     */
    private void removeLocals() {
        TL_inStockList.clear();
        TL_ASNCodeList.clear();
        TL_inStock = null;
        TL_initInStockDetail = null;
        TL_packagingInfoList.clear();
        TL_unitNameList.clear();
        TL_selectedPack = null;
        TL_ReceivedBU = 0;
    }


}
