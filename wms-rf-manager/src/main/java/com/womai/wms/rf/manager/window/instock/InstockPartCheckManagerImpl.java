package com.womai.wms.rf.manager.window.instock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.*;
import com.womai.wms.rf.domain.instock.PartCheckDetail;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.instock.InstockCheckRemoteService;
import com.womai.wms.rf.remote.instock.InstockReasonRemoteService;
import com.womai.wms.rf.remote.instock.InstockRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.instock.Instock;
import com.womai.zlwms.rfsoa.domain.instock.InstockCheckDetail;
import com.womai.zlwms.rfsoa.domain.instock.InstockReason;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单一质检
 * Created by wzh on 16-5-8.
 */
@Scope("prototype")
@Component("instockPartCheckManager")
public class InstockPartCheckManagerImpl extends ReceiveManager {
    @Autowired
    private InstockRemoteService instockRemoteService;
    @Autowired
    private InstockReasonRemoteService instockReasonRemoteService;
    @Autowired
    private InstockCheckRemoteService instockCheckRemoteService;
    public final static String ORDERCODE = "orderCode";//ASN单号或者网络订单号
    private final static String SELECT_ASN = "selectAsn";//选择ASN单号
    public final static String BARCODE = "barcode";//商品条码
    public final static String CHECKBU = "checkBu";//商品条码
    public final static String CHECKSTATUS = "checkStatus";//质检结果
    public final static String REASONID = "reasonid";//质检结果
    public final static String FLAGYN = "flagYN";//质检结果
    public final static String INSTOCK_LIST = "instockList";//存放入库单List对应的key
    public final static String INSTOCK = "instock";//存放入库单对象的key
    public final static int ROW_FIXED = 5;//列表固定的行（表头+表尾）
    public final static String[] TABLE_NAME = {"序号", "原因内容                              "};//原因列表头
    public final static String[] TABLE_COLUMN = {"remark"};//原因列表头
    public final static String[] DETAIL_NAME_FIR = {"序号", "包装单位", "数量BU", " 生产日期 "};//原因列表名称
    public final static String[] DETAIL_NAME_SEC = {"序号", "包装单位", "数量BU", " 失效日期 "};//原因列表名称
    public final static String[] DETAIL_COLUMN_FIR = {"unitname", "planbu", "productiondate"};//原因列表字段
    public final static String[] DETAIL_COLUMN_SEC = {"unitname", "planbu", "expirationdate"};//原因列表字段
    private final static String OPERATION_TYPE = "operationType";//soa是否更新的同时，新增一条明细（拆明细）
    private final static String SERIALNO = "serialno";//选择质检明细的序号
    private final static String[] DETAIL_KEYS = {"detailMap", "detailPageModel"};// 质检明细分页查询参数Map,查询结果Page
    private final static String[] REASON_KEYS = {"reasonMap", "reasonPageModel"};// 原因分页查询参数Map,查询结果Page
    private final static String CHECK_DETAIL = "checkDetail";//选择的质检明细
    private final static String REG = "^[1-9]\\d{0,9}$";//十位以内的正整数
    public final static String SWITCH = "switch";//是否接受数据的开关
    public static final String REASON_LINES_NUM_CLEAN_KEY = "reason_lines_num_clean_key";//翻页时，需要清除的数据行数
    public static final String DETAIL_LINES_NUM_CLEAN_KEY = "detail_lines_num_clean_key";//翻页时，需要清除的数据行数
    public final static boolean IS_FINISH_DETAIL = true;//该商品所有明细质检完成

    /**
     * 接收用户输入
     *
     * @param ctx    上下文
     * @param object 每次接收到的字节数据
     * @throws Exception 抛出异常
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        Map<String, Object> accepterMap = getDataMap();
        Object switchFlag = accepterMap.get(SWITCH);
        if (switchFlag == null) {
            accepterMap.put(SWITCH, false);//正常接受参数
        } else if ((Boolean) switchFlag) {
            channelActive(ctx);
            return;
        }
        Integer receiveResult = receiveDataAndNotPrintNext(ctx, object, accepterMap);
        PartCheckDetail partCheckDetail = (PartCheckDetail) accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (ORDERCODE.equals(lastCompleteColName)) {//扫描单号后验证
            //查询主表
            Instock instock = new Instock();
            instock.setPurchaseno(partCheckDetail.getOrderCode());//网络订单号
            instock.setAsninstockcode(partCheckDetail.getOrderCode());//入库单号
            RemoteResult<List<Instock>> result = instockRemoteService.getByASNCodeOrPurchaseNo(getCredentialsVO(ctx), instock);
            if (!result.isSuccess()) {//提示单据不存在
                colNeedReInput(ORDERCODE, result.getResultCode(), accepterMap, ctx);
                WMSDebugManager.debugLog("单号不存在，或者状态不对" + partCheckDetail);
            } else {
                processAsnCodes(ctx, accepterMap, result.getT());
            }
        }
        if (SELECT_ASN.equals(lastCompleteColName)) {//保存选择的入库单对象，输出接收字段
            List<Instock> instocks = (List<Instock>) accepterMap.get(INSTOCK_LIST);
            Integer listIndex = (Integer) accepterMap.get(DefaultKey.listIndex.keyName);
            accepterMap.put(INSTOCK, instocks.get(listIndex));
            rePrintCurColTip(accepterMap, ctx);
        }

        if (BARCODE.equals(lastCompleteColName)) {//扫描商品条码后，展示商品明细
            WMSDebugManager.debugLog("扫描商品条码后验证" + accepterMap);
            showcheckDetails(ctx, !IS_FINISH_DETAIL, accepterMap, DETAIL_KEYS, 0);
        }
        if (SERIALNO.equals(lastCompleteColName)) {//选择一条质检明细
            String serialno = partCheckDetail.getSerialno();
            PageModel<InstockCheckDetail> checkDetailPageModel = (PageModel<InstockCheckDetail>) accepterMap.get(DETAIL_KEYS[1]);
            int pageSizeCurr = (Integer) accepterMap.get(DETAIL_LINES_NUM_CLEAN_KEY);//页面当前数据条数
            if (KeyEnum.N_78.value.equalsIgnoreCase(serialno)) {//下一页
                changePageNext(ctx, DETAIL_KEYS, accepterMap, pageSizeCurr);
                Reflections.invokeSetter(accepterMap.get(DefaultKey.objectClass.keyName), SERIALNO, "");
            } else if (KeyEnum.B_66.value.equalsIgnoreCase(serialno)) {//上一页
                changePageUp(ctx, DETAIL_KEYS, accepterMap, pageSizeCurr);
                Reflections.invokeSetter(accepterMap.get(DefaultKey.objectClass.keyName), SERIALNO, "");
            } else if (serialno.matches(REG)) {//选择号为正整数
                InstockCheckDetail instockCheckDetail = (InstockCheckDetail) PageUtil.getDataBySerialno(checkDetailPageModel, serialno);
                if (instockCheckDetail == null) {
                    colNeedReInput(SERIALNO, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                } else {
                    //正确选择原因内容，保存参数
                    accepterMap.put(CHECK_DETAIL, instockCheckDetail);
                    printBeforeCheckBu(accepterMap, ctx, instockCheckDetail);
                }
            } else {
                colNeedReInput(SERIALNO, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
            }
        }


        if (CHECKBU.equals(lastCompleteColName)) {//校验质检数量
            WMSDebugManager.debugLog("校验质检数量" + accepterMap);
            InstockCheckDetail instockCheckDetail = (InstockCheckDetail) accepterMap.get(CHECK_DETAIL);
            Integer nocheckBu = instockCheckDetail.getCheckbu();
            if (partCheckDetail.getCheckBu().matches(REG)) {
                Integer paraCheckBu = Integer.parseInt(partCheckDetail.getCheckBu());
                if (paraCheckBu.compareTo(nocheckBu) < 0) {
                    InstockCheckDetail currDetail = (InstockCheckDetail) accepterMap.get(CHECK_DETAIL);
                    if (paraCheckBu % currDetail.getPknum() != 0) {
                        colNeedReInput(CHECKBU, ErrorConstants.PLS_INPUT_RIGHT_NUM, accepterMap, ctx);
                        return;
                    }
                    accepterMap.put(OPERATION_TYPE, true);
                    List<String> list = CheckReasonEnum.getNameList();
                    //下一个字段以list切换的形式显示
                    setNextColSwitchList(list, accepterMap, ctx);
                } else if (paraCheckBu.compareTo(nocheckBu) == 0) {
                    //更新 明细为质检完成
                    List<String> list = CheckReasonEnum.getNameList();
                    //下一个字段以list切换的形式显示
                    setNextColSwitchList(list, accepterMap, ctx);
                    accepterMap.put(OPERATION_TYPE, false);
                } else {
                    colNeedReInput(CHECKBU, ErrorConstants.DATA_ERROR_03, accepterMap, ctx);
                }
            } else {
                colNeedReInput(CHECKBU, ErrorConstants.DATA_ERROR_02, accepterMap, ctx);
            }
        }
        if (CHECKSTATUS.equals(lastCompleteColName)) {//质检结果
            int type = CheckReasonEnum.getValueByName(partCheckDetail.getCheckStatus());
            if (CheckReasonEnum.damaged.value.equals(type) || CheckReasonEnum.frozen.value.equals(type)) {//残品或者冻结，列出原因列表
                showReasons(ctx, accepterMap, type, REASON_KEYS, Constants.PAGE_OFFSET_INIT);
            } else {//正品，跳过原因内容
                resetCurCol(FLAGYN, accepterMap, ctx);
            }
        }
        if (REASONID.equals(lastCompleteColName)) {
            String reasonid = partCheckDetail.getReasonid();
            PageModel<InstockReason> pageModle = (PageModel<InstockReason>) accepterMap.get(REASON_KEYS[1]);
            int pageSizeCurr = (Integer) accepterMap.get(REASON_LINES_NUM_CLEAN_KEY);//页面当前数据条数
            if (KeyEnum.N_78.value.equalsIgnoreCase(reasonid)) {//下一页
                changePageNext(ctx, REASON_KEYS, accepterMap, pageSizeCurr);
                Reflections.invokeSetter(accepterMap.get(DefaultKey.objectClass.keyName), REASONID, "");
            } else if (KeyEnum.B_66.value.equalsIgnoreCase(reasonid)) {//上一页
                changePageUp(ctx, REASON_KEYS, accepterMap, pageSizeCurr);
                Reflections.invokeSetter(accepterMap.get(DefaultKey.objectClass.keyName), REASONID, "");
            } else {//序号
                if (reasonid.matches(REG)) {
                    InstockReason instockReason = (InstockReason) PageUtil.getDataBySerialno(pageModle, reasonid);
                    if (instockReason == null) {
                        colNeedReInput(REASONID, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                    } else {
                        //正确选择原因内容，保存参数
                        HandlerUtil.changeRow(ctx);
                        HandlerUtil.print(ctx, TipConstants.REASON_CONTENT + instockReason.getRemark());
                        rePrintCurColTip(accepterMap, ctx);
                    }
                } else {
                    colNeedReInput(REASONID, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                }
            }
        }

        if (receiveResult == RECEIVER_TYPE_FINISHED) {//接收数据完成，并且扫描交接单号后输入的为Y
            //组装数据
            String flag = partCheckDetail.getFlagYN();
            if (Constants.CONFIRM_Y.equalsIgnoreCase(flag)) {//接收参数完成
                accepterMap.remove(REASON_KEYS[0]);// 清除查询原因列表的条件
                WMSDebugManager.debugLog("单一质检，接收的参数:" + accepterMap);
                Instock instock = (Instock) accepterMap.get(INSTOCK);
                InstockCheckDetail instockCheckDetail = (InstockCheckDetail) accepterMap.get(CHECK_DETAIL);
                instockCheckDetail = getInstockCheckDetail(accepterMap, instockCheckDetail, partCheckDetail);
                RemoteResult<Instock> result = instockCheckRemoteService.processPartCheckDetail(getCredentialsVO(ctx), instock, instockCheckDetail);
                if (result.isSuccess()) {
                    Instock instockDB = result.getT();
                    Instock instockCach = (Instock) accepterMap.get(INSTOCK);
                    if (WmsConstants.INSTOCKSHELF_STATUS_OPEN == instockDB.getInstockStatus()) {//上架打开状态
                        List<Instock> instocksCach = (List<Instock>) accepterMap.get(INSTOCK_LIST);
                        instocksCach.remove(instockCach);
                        if (instocksCach.isEmpty()) {//所有的入库单全部质检完成，跳转到初始状态
                            HandlerUtil.print(ctx, result.getT().getErrMsg() + ErrorConstants.TIP_TO_CONTINUE);
                            accepterMap.put(SWITCH, true);
                        } else {//网络订单号下面有多单,跳转到扫描商品条码
                            HandlerUtil.locateCursor(ctx.channel(), 6, 0); //定位光标到第8行
                            HandlerUtil.clearRight(ctx);
                            HandlerUtil.moveUpN(ctx, 1);
                            setReceivedToSelectAsn(accepterMap);
                            processAsnCodes(ctx, accepterMap, instocksCach);
                        }
                    } else {//某条明细质检完成，跳转到选择剩余质检明细分页列表
                        HandlerUtil.locateCursor(ctx.channel(), 8, 0); //定位光标到第8行
                        HandlerUtil.clearRight(ctx);
                        HandlerUtil.moveUpN(ctx, 1);
                        setReceivedToSerialno(accepterMap);
                        instockCach.setVersion(result.getT().getVersion());//更新主表的版本号
                        showcheckDetails(ctx, IS_FINISH_DETAIL, accepterMap, DETAIL_KEYS, 0);
                    }
                } else {//错误提示
                    HandlerUtil.print(ctx, result.getT().getErrMsg() + ErrorConstants.TIP_TO_CONTINUE);
                    accepterMap.put(SWITCH, true);
                }
            } else if (Constants.CANCEL_N.equalsIgnoreCase(flag)) {//取消操作，返回开始页面
                channelActive(ctx);
            } else {//提示输入错误
                colNeedReInput(FLAGYN, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
            }
        }
        WMSDebugManager.debugLog("InstockPartCheckManagerImpl--Received:" + accepterMap);
    }

    /**
     * 根据操作方式过滤
     *
     * @param instockCach 缓存的入库单
     */
    private List<Instock> validateInstocks(List<Instock> instockCach) {
        List<Instock> removeInstocks = new ArrayList<Instock>();
        for (Instock instock : instockCach) {
            Integer checkWorktype = instock.getCheckWorktype();
            if ((checkWorktype != null && WmsConstants.INSTOCK_WORKTYPE_RF != checkWorktype) ||
                    instock.getVirtualin() == WmsConstants.INSTOCK_VIRTUALIN_Y) {
                removeInstocks.add(instock);
            }
        }
        instockCach.removeAll(removeInstocks);
        return instockCach;
    }

    /**
     * 处理展示入库单号，供用户左右键选择
     *
     * @param accepterMap 存储页面，及查询数据的参数
     */
    private void processAsnCodes(ChannelHandlerContext ctx, Map<String, Object> accepterMap, List<Instock> instockList) {
        accepterMap.put(INSTOCK_LIST, instockList);
        if (instockList.size() > 1) {//页面左右键，供用户选择入库单号。
            List<String> asnCodes = getAsnCodes(instockList);
            setNextColSwitchList(asnCodes, accepterMap, ctx);
        } else {
            //只查询到一条数据则直接跳过选择ASN单号的步骤，直接到输入商品条码，并将选择的入库主单数据存入Thread变量
            Instock selectedInstock = instockList.get(0);
            accepterMap.put(INSTOCK, selectedInstock);
            setDefaultValue(selectedInstock.getAsninstockcode(), SELECT_ASN, accepterMap, ctx);
            resetCurCol(BARCODE, accepterMap, ctx);
        }
    }

    // 打印生产日期，和待质检数量
    private void printBeforeCheckBu(Map<String, Object> accepterMap, ChannelHandlerContext ctx, InstockCheckDetail instockCheckDetail) {
//        HandlerUtil.changeRow(ctx);
        String dateName = Constants.batchRuleEnum.xiHua.code.equals(instockCheckDetail.getBatchrule())?TipConstants.EXPIRATION_DATE:TipConstants.PRODUCTION_DATE;
        List<String> tipList = CollectionUtil.newList(dateName, TipConstants.EXPECT_CHECK_BU);
        List<String> valueList = CollectionUtil.newList(DateTimeUtil.getStringWithSeparator(instockCheckDetail.getProductiondate()), instockCheckDetail.getCheckbu() + "");
        printBeforeNextField(tipList, valueList, accepterMap, ctx);
        rePrintCurColTip(accepterMap, ctx);
    }

    /**
     * 跳转到选择ASN号
     *
     * @param accepterMap
     */
    private void setReceivedToSelectAsn(Map<String, Object> accepterMap) {
        String[] strings = {SELECT_ASN, BARCODE, SERIALNO, CHECKBU, CHECKSTATUS, REASONID, FLAGYN};//选择质检明细序号,要跳转到的属性名放第一个
        setReceivedBase(accepterMap, 1, strings);//1表示orderCode已经接收完成
    }

    /**
     * 跳转到选择质检明细
     *
     * @param accepterMap
     */
    private void setReceivedToSerialno(Map<String, Object> accepterMap) {
        String[] strings = {SERIALNO, CHECKBU, CHECKSTATUS, REASONID, FLAGYN};//选择质检明细序号,要跳转到的属性名放第一个
        setReceivedBase(accepterMap, 3, strings);//3 表示serialno上面已经有三个属性接收完成
    }

    /**
     * 属性值接收跳转时，初始后面的属性
     *
     * @param accepterMap
     * @param size        该属性上面的属性个数，即已经完成接收的个数
     * @param strings     要清空的属性名字
     */
    private void setReceivedBase(Map<String, Object> accepterMap, int size, String[] strings) {
        accepterMap.put(DefaultKey.curColName.keyName, strings[0]);//跳转到，将要接收数据的属性名
        accepterMap.put(DefaultKey.lastCompleteColName.keyName, "");
        accepterMap.put(DefaultKey.completeSize.keyName, size);
        for (String columnName : strings) {
            Reflections.invokeSetter(accepterMap.get(DefaultKey.objectClass.keyName), columnName, "");
        }
    }

    /**
     * 显示质检明细列表
     *
     * @param ctx
     * @param accepterMap 入库单号，打开状态
     * @param pageOffSet  上一页 -1 下一页 1
     * @param isFinish    商品质检完成，跳转到下个商品扫描（为了屏蔽提示语）
     * @param keys        0 分页查询条件map的key，1 查询结果pageModel的key
     */
    private void showcheckDetails(ChannelHandlerContext ctx, boolean isFinish, Map<String, Object> accepterMap, String[] keys, int pageOffSet) {
        HashMap<String, Object> map = (HashMap<String, Object>) accepterMap.get(keys[0]);
        if (map == null) {
            PartCheckDetail partCheckDetail = (PartCheckDetail) accepterMap.get(DefaultKey.objectClass.keyName);
            Instock instock = (Instock) accepterMap.get(INSTOCK);
            InstockCheckDetail instockCheckDetail = new InstockCheckDetail();
            instockCheckDetail.setPage(Constants.PAGE_START);
            instockCheckDetail.setRows(Constants.CHECK_DETAIL_PAGE_SIZE);
            instockCheckDetail.setAsninstockcode(instock.getAsninstockcode());//入库单号
            instockCheckDetail.setBarCode(partCheckDetail.getBarcode());
            instockCheckDetail.setDetailstatus(WmsConstants.INSTOCKDETAIL_STATUS_OPEN);//状态打开
            map = initPagePara(WmsConstants.KEY_INSTOCKCHECKDETAIL_PARAM, DETAIL_KEYS[0], instockCheckDetail, accepterMap);
        } else {
            map = resetPagePara(WmsConstants.KEY_INSTOCKCHECKDETAIL_PARAM, accepterMap, DETAIL_KEYS, pageOffSet);
        }
        RemoteResult<PageModel<InstockCheckDetail>> pageModelRemoteResult = instockCheckRemoteService.queryInstockCheckDetailPageList(getCredentialsVO(ctx), map);
        if (!pageModelRemoteResult.isSuccess()) {//输出错误信息
            accepterMap.remove(DETAIL_KEYS[0]);
            if (isFinish) {
                colNeedReInput(BARCODE, "", accepterMap, ctx);  //该品所有明细质检完成，光标停留在此处，等待扫描下个商品
            } else {
                colNeedReInput(BARCODE, pageModelRemoteResult.getResultCode(), accepterMap, ctx);
            }
        } else {//展示原质检明细列表
            PageModel<InstockCheckDetail> instockCheckDetailPageModel = pageModelRemoteResult.getT();
            accepterMap.put(keys[1], instockCheckDetailPageModel);
            HandlerUtil.changeRow(ctx);//回车
            InstockCheckDetail detail = instockCheckDetailPageModel.getDatas().get(0);
            String skuName = detail.getSkuname();
            skuName = RFUtil.makeStrFitPda(skuName, TipConstants.SKU_NAME, 5);
            String[] detailName;
            String[] detailColumn;
            if(Constants.batchRuleEnum.xiHua.code.equals(detail.getBatchrule())){//洗化，显示失效日期
                detailName = DETAIL_NAME_SEC;
                detailColumn = DETAIL_COLUMN_SEC;
            }else{
                detailName = DETAIL_NAME_FIR;
                detailColumn = DETAIL_COLUMN_FIR;
            }
            int currPageLinesNum = PageUtil.showTable(ctx, instockCheckDetailPageModel, detailName, detailColumn, true, false, TipConstants.SKU_NAME + skuName);//展示列表，带有序号
            accepterMap.put(DETAIL_LINES_NUM_CLEAN_KEY, currPageLinesNum);
            HandlerUtil.moveUpN(ctx, 1);//去掉空行
            resetCurCol(SERIALNO, accepterMap, ctx);
        }
    }

    /**
     * 获得入库单号集合
     *
     * @param instockList 入库主单集合
     * @return
     */
    private List<String> getAsnCodes(List<Instock> instockList) {
        List<String> strings = new ArrayList<String>();
        for (Instock instock : instockList) {
            strings.add(instock.getAsninstockcode());
        }
        return strings;
    }

    /**
     * 对象转换
     *
     * @param checkDetial 接收质检明细参数的对象
     * @param map         接收参数的Map
     * @return 质检明细
     */
    private InstockCheckDetail getInstockCheckDetail(Map<String, Object> map, InstockCheckDetail instockCheckDetail, PartCheckDetail checkDetial) {
        PageModel pageModel = (PageModel) map.get(REASON_KEYS[1]);
        boolean isInsert = (Boolean) map.get(OPERATION_TYPE);
        instockCheckDetail.setIsInsert(isInsert);
        Integer checkStatus = CheckReasonEnum.getValueByName(checkDetial.getCheckStatus());//商品状态
        instockCheckDetail.setCheckStatus(checkStatus);
        if (!CheckReasonEnum.normal.value.equals(checkStatus)) {//非正品，设置原因id，根据序号获得原因id
            InstockReason instockReason = (InstockReason) PageUtil.getDataBySerialno(pageModel, checkDetial.getReasonid());
            Long reasonid = instockReason.getId();
            instockCheckDetail.setReasonid(reasonid);
            instockCheckDetail.setReasonContent(instockReason.getRemark());
        }
        Integer checkbu = Integer.parseInt(checkDetial.getCheckBu());//质检数量bu
        instockCheckDetail.setExpectCheckbu(instockCheckDetail.getCheckbu());//拆分时，计算新插入数据的checkbu
        instockCheckDetail.setExpectChecknum(instockCheckDetail.getChecknum());//拆分时，计算新插入数据的checknum
        instockCheckDetail.setCheckbu(checkbu);
        instockCheckDetail.setChecknum(checkbu / instockCheckDetail.getPknum());
        return instockCheckDetail;
    }

    /**
     * 向上翻页
     *
     * @param ctx
     * @param accepterMap
     * @param pageSize
     */
    private void changePageUp(ChannelHandlerContext ctx, String[] keys, Map<String, Object> accepterMap, int pageSize) {
        changePage(ctx, keys, accepterMap, pageSize, Constants.PAGE_OFFSET_PREV);
    }

    /**
     * 向下翻页
     *
     * @param ctx
     * @param accepterMap
     * @param pageSize
     */
    private void changePageNext(ChannelHandlerContext ctx, String[] keys, Map<String, Object> accepterMap, int pageSize) {
        changePage(ctx, keys, accepterMap, pageSize, Constants.PAGE_OFFSET_NEXT);
    }

    /**
     * 翻页
     *
     * @param ctx
     * @param accepterMap
     * @param pageSize    一页显示多少条数据
     * @param offset      上一页或者下一页
     */
    private void changePage(ChannelHandlerContext ctx, String[] keys, Map<String, Object> accepterMap, int pageSize, int offset) {
        //清楚原来的展示
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        HandlerUtil.moveUpN(ctx, ROW_FIXED + pageSize);
        HandlerUtil.changeRow(ctx);//回车
        HandlerUtil.removeRightDown(ctx);
        HandlerUtil.moveUpN(ctx, 1);
        if (SERIALNO.equals(lastCompleteColName)) {
            showcheckDetails(ctx, !IS_FINISH_DETAIL, accepterMap, keys, offset);//质检明细列表
        } else {
            showReasons(ctx, accepterMap, -2, keys, offset);//-2 占位符，原因列表
        }
    }

    /**
     * 初始分页查询参数
     *
     * @param obj         查询列表的参数
     * @param accepterMap
     * @return 分页查询参数
     * @Param mapKey 分页查询条件Map存放对应的key
     */
    private HashMap initPagePara(String mapPara, String mapKey, Object obj, Map<String, Object> accepterMap) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(mapPara, obj);
        accepterMap.put(mapKey, map);
        return map;
    }

    /**
     * 翻页时，重新设置第几页
     *
     * @param accepterMap
     * @param pageOffSet  上一页或者下一页
     * @param key         map中获得对象的key
     * @return 参数Map
     */
    private HashMap resetPagePara(String key, Map<String, Object> accepterMap, String[] keys, int pageOffSet) {//翻页时，重新设定页码
        HashMap<String, Object> map = (HashMap<String, Object>) accepterMap.get(keys[0]);
        PageModel pageModel = (PageModel) accepterMap.get(keys[1]);
        int pageNum = pageModel.getPageNum() + pageOffSet;
        pageNum = pageNum < 1 ? 1 : pageNum;
        int maxPageNum = pageModel.getTotalPageNum();
        pageNum = pageNum > maxPageNum ? maxPageNum : pageNum;
        Object obj = map.get(key);
        Reflections.invokeSetter(obj, "page", pageNum);
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
    private void showReasons(ChannelHandlerContext ctx, Map<String, Object> accepterMap, int type, String[] keys, int pageOffSet) {
        HashMap<String, Object> map = (HashMap<String, Object>) accepterMap.get(keys[0]);
        InstockReason instockReason = new InstockReason();
        instockReason.setPage(Constants.PAGE_START);
        instockReason.setRows(Constants.REASON_PAGE_SIZE);
        instockReason.setSidx("id");
        instockReason.setSord(Constants.PAGE_SORT_DESC);
        instockReason.setReasonType(type);
        instockReason.setReasonStatus(WmsConstants.STATUS_ENABLE);//生效
        if (map == null) {
            map = initPagePara(WmsConstants.KEY_INSTOCKREASON_PARRAM, keys[0], instockReason, accepterMap);
        } else {
            map = resetPagePara(WmsConstants.KEY_INSTOCKREASON_PARRAM, accepterMap, keys, pageOffSet);
        }
        RemoteResult<PageModel<InstockReason>> pageModelRemoteResult = instockReasonRemoteService.getInstockReasonPageList(getCredentialsVO(ctx), map);
        if (!pageModelRemoteResult.isSuccess()) {//输出错误信息
            accepterMap.remove(keys[0]);
            List<String> list = CheckReasonEnum.getNameList();
            //下一个字段以list切换的形式显示
            setColReSwitchList(list, ErrorConstants.PLS_MAINTAION_REASON, accepterMap, ctx);
        } else {//展示原因内容列表
            PageModel<InstockReason> instockReasonPageModel = pageModelRemoteResult.getT();
            accepterMap.put(keys[1], instockReasonPageModel);
            HandlerUtil.changeRow(ctx);
            int currPageLinesNum = PageUtil.showTable(ctx, instockReasonPageModel, TABLE_NAME, TABLE_COLUMN, true, true, null);//展示列表，带有序号
            accepterMap.put(REASON_LINES_NUM_CLEAN_KEY, currPageLinesNum);
            HandlerUtil.moveUpN(ctx,1);
            resetCurCol(REASONID, accepterMap, ctx);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 登录界面
        String[] pageHeader = {Constants.BREAK_LINE, Constants.INSTOCK_CHECK_PART, Constants.SPLIT, ""};
        super.initBaseMap(PartCheckDetail.class, pageHeader, ctx);
    }
}
