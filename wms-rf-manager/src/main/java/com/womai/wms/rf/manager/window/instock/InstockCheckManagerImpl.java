package com.womai.wms.rf.manager.window.instock;


import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.PageUtil;
import com.womai.wms.rf.common.util.WMSDebugManager;
import com.womai.wms.rf.domain.instock.CheckDetail;
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
 * 整单质检
 * Created by wzh on 16-5-8.
 */
@Scope("prototype")
@Component("instockCheckManager")
public class InstockCheckManagerImpl extends ReceiveManager {
    @Autowired
    private InstockRemoteService instockRemoteService;
    @Autowired
    private InstockReasonRemoteService instockReasonRemoteService;
    @Autowired
    private InstockCheckRemoteService instockCheckRemoteService;
    public final static String ORDERCODE = "orderCode";//ASN单号或者网络订单号
    public final static String CHECKSTATUS = "checkStatus";//质检结果
    public final static String REASONID = "reasonid";//质检结果
    public final static String FLAGYN = "flagYN";//质检结果
    public final static String PAGEMODEL = "reasonPageModel";//原因列表集合
    public final static String INSTOCK_LIST = "instockList";//原因List
    public final static int ROW_FIXED = 5;//列表固定的行（表头+表尾）
    public final static String PARA_PAGE_MAP = "pageMap";//原因列表
    public final static String[] TABLE_NAME = {"序号", "原因内容                              "};//原因列表头
    public final static String[] TABLE_COLUMN = {"remark"};//原因列表头
    public final static String REG = "^[1-9]\\d{0,9}$";
    public final static String INSTOCK = "instock";//存放入库单对象的key
    private final static String SELECT_ASN = "selectAsn";//选择ASN单号
    public final static String SWITCH = "switch";//是否接受数据的开关
    public static final String LINES_NUM_CLEAN_KEY = "lines_num_clean_key";//翻页时，需要清除的数据行数

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
        CheckDetail checkDetail = (CheckDetail) accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (ORDERCODE.equals(lastCompleteColName)) {//扫描单号后验证
            //查询主表
            Instock instock = new Instock();
            instock.setPurchaseno(checkDetail.getOrderCode());//网络订单号
            instock.setAsninstockcode(checkDetail.getOrderCode());//入库单号
            RemoteResult<List<Instock>> result = instockRemoteService.getByASNCodeOrPurchaseNo(getCredentialsVO(ctx), instock);
            if (!result.isSuccess()) {//提示单据不存在
                colNeedReInput(ORDERCODE, result.getResultCode(), accepterMap, ctx);
                WMSDebugManager.debugLog("单号不存在，或者状态不对" + checkDetail);
            } else {
                List<Instock> instocks = result.getT();
                List<Instock> rmInstocks = new ArrayList<Instock>();
                for (Instock instockDB : instocks) {
                    if (WmsConstants.INSTOCKCHECK_STATUS_INIT != instockDB.getInstockStatus().intValue()) {//整单质检需要待质检状态
                        rmInstocks.add(instockDB);
                    }
                }
                if (rmInstocks.size() > 0) {
                    instocks.removeAll(rmInstocks);
                }
                if (instocks.size() > 0) {
                    processAsnCodes(ctx, accepterMap, result.getT());
                } else {
                    colNeedReInput(ORDERCODE, "状态不是待质检，不可以整单质检", accepterMap, ctx);
                }
            }
        }

        if (SELECT_ASN.equals(lastCompleteColName)) {//保存选择的入库单对象，输出接收字段
            List<Instock> instocks = (List<Instock>) accepterMap.get(INSTOCK_LIST);
            Integer listIndex = (Integer) accepterMap.get(DefaultKey.listIndex.keyName);
            accepterMap.put(INSTOCK, instocks.get(listIndex));
            List<String> list = CheckReasonEnum.getNameList();
            //下一个字段以list切换的形式显示
            setNextColSwitchList(list, accepterMap, ctx);
        }
        if (CHECKSTATUS.equals(lastCompleteColName)) {//质检结果
            int type = CheckReasonEnum.getValueByName(checkDetail.getCheckStatus());
            if (CheckReasonEnum.damaged.value.equals(type) || CheckReasonEnum.frozen.value.equals(type)) {//残品或者冻结，列出原因列表
                showReasons(ctx, accepterMap, type, Constants.PAGE_OFFSET_INIT);
            } else {//正品，跳过原因内容
                resetCurCol(FLAGYN, accepterMap, ctx);
            }
        }

        if (REASONID.equals(lastCompleteColName)) {
            String reasonid = checkDetail.getReasonid();
            PageModel<InstockReason> pageModle = (PageModel<InstockReason>) accepterMap.get(PAGEMODEL);
            int pageSizeCurr = (Integer) accepterMap.get(LINES_NUM_CLEAN_KEY);//页面当前数据条数
            if (KeyEnum.N_78.value.equalsIgnoreCase(reasonid)) {//下一页
                changePageNext(ctx, accepterMap, pageSizeCurr);
                setColUnReceived(REASONID, accepterMap);
            } else if (KeyEnum.B_66.value.equalsIgnoreCase(reasonid)) {//上一页
                changePageUp(ctx, accepterMap, pageSizeCurr);
                setColUnReceived(REASONID, accepterMap);
            } else {//序号
                if (reasonid.matches(REG)) {
                    List<InstockReason> instockReasons = pageModle.getDatas();
                    int maxIndex = instockReasons.size() - 1;
                    int index = PageUtil.getIndexFromSerialnoAndPageModle(reasonid, pageModle);//(pn-1)*size+index+1==serialno
                    if (index > maxIndex || index < 0) {
                        colNeedReInput(REASONID, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                    } else {
                        //正确选择原因内容，保存参数
                        HandlerUtil.changeRow(ctx);
                        HandlerUtil.println(ctx, TipConstants.REASON_CONTENT + instockReasons.get(index).getRemark());
                        rePrintCurColTip(accepterMap, ctx);
                    }
                } else {
                    colNeedReInput(REASONID, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                }
            }
        }

        if (RECEIVER_TYPE_FINISHED.equals(receiveResult)) {//接收数据完成，并且扫描交接单号后输入的为Y
            //组装数据
            String flag = checkDetail.getFlagYN();
            if (Constants.CONFIRM_Y.equalsIgnoreCase(flag)) {
                //接收参数完成
                WMSDebugManager.debugLog("整单质检，接收的参数:" + accepterMap);
                InstockCheckDetail instockCheckDetail = getInstockCheckDetail(accepterMap, checkDetail);
                Instock instock = (Instock) accepterMap.get(INSTOCK);
                RemoteResult<String> result = instockCheckRemoteService.processCheckDetails(getCredentialsVO(ctx), instock, instockCheckDetail);
                HandlerUtil.print(ctx, result.getT() + ErrorConstants.TIP_TO_CONTINUE);
                accepterMap.put(SWITCH, true);
            } else if (Constants.CANCEL_N.equalsIgnoreCase(flag)) {//取消操作，返回开始页面
                channelActive(ctx);
            } else {//提示输入错误
                colNeedReInput(FLAGYN, Constants.RF_MANAGER_ERROR_MSG_04, accepterMap, ctx);
            }
        }
        WMSDebugManager.debugLog("OutstockConfirmManagerImpl--Received:" + accepterMap);
    }

    /**
     * 对象转换
     *
     * @param checkDetail 接收质检明细参数的对象
     * @param map         接收参数的Map
     * @return 质检明细
     */
    private InstockCheckDetail getInstockCheckDetail(Map<String, Object> map, CheckDetail checkDetail) {
        Instock instock = (Instock) map.get(INSTOCK);
        InstockCheckDetail instockCheckDetail = new InstockCheckDetail();
        Integer checkStatus = CheckReasonEnum.getValueByName(checkDetail.getCheckStatus());//商品状态
        instockCheckDetail.setCheckStatus(checkStatus);
        instockCheckDetail.setAsninstockcode(instock.getAsninstockcode());
        if (!CheckReasonEnum.normal.value.equals(checkStatus)) {//非正品，设置原因id
            PageModel pageModel = (PageModel) map.get(PAGEMODEL);
            List<InstockReason> instockReasons = pageModel.getDatas();
            //根据序号获得原因id
            int index = PageUtil.getIndexFromSerialnoAndPageModle(checkDetail.getReasonid(), pageModel);
            InstockReason instockReason = instockReasons.get(index);
            Long reasonid = instockReason.getId();
            instockCheckDetail.setReasonid(reasonid);
            instockCheckDetail.setReasonContent(instockReason.getRemark());
        }
        return instockCheckDetail;
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
    private void processAsnCodes(ChannelHandlerContext ctx, Map<String, Object> accepterMap, List<Instock> instockList) throws Exception {
        accepterMap.put(INSTOCK_LIST, instockList);
        if (instockList.size() > 1) {//页面左右键，供用户选择入库单号。
            List<String> asnCodes = getAsnCodes(instockList);
            setNextColSwitchList(asnCodes, accepterMap, ctx);
        } else {
            //只查询到一条数据则直接跳过选择ASN单号的步骤
            Instock selectedInstock = instockList.get(0);
            accepterMap.put(INSTOCK, selectedInstock);
            setDefaultValue(selectedInstock.getAsninstockcode(), SELECT_ASN, accepterMap, ctx);
            channelRead(ctx, "\r\r");
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
     * 向上翻页
     *
     * @param ctx
     * @param accepterMap
     * @param pageSize
     */
    private void changePageUp(ChannelHandlerContext ctx, Map<String, Object> accepterMap, int pageSize) {
        changePage(ctx, accepterMap, pageSize, Constants.PAGE_OFFSET_PREV);
    }

    /**
     * 向下翻页
     *
     * @param ctx
     * @param accepterMap
     * @param pageSize
     */
    private void changePageNext(ChannelHandlerContext ctx, Map<String, Object> accepterMap, int pageSize) {
        changePage(ctx, accepterMap, pageSize, Constants.PAGE_OFFSET_NEXT);
    }

    /**
     * 翻页
     *
     * @param ctx
     * @param accepterMap
     * @param pageSize    一页显示多少条数据
     * @param offset      上一页或者下一页
     */
    private void changePage(ChannelHandlerContext ctx, Map<String, Object> accepterMap, int pageSize, int offset) {
        //清除原来的展示
        HandlerUtil.moveUpN(ctx, ROW_FIXED + pageSize);
        HandlerUtil.changeRow(ctx);//回车
        HandlerUtil.removeRightDown(ctx);
        HandlerUtil.moveUpN(ctx, 1);
        showReasons(ctx, accepterMap, -2, offset);//-2 占位符
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
        accepterMap.put(PARA_PAGE_MAP, map);
        return map;
    }

    /**
     * 翻页时，重新设置第几页
     *
     * @param accepterMap
     * @param pageOffSet  上一页或者下一页
     * @return 参数Map
     */
    private HashMap resetPagePara(Map<String, Object> accepterMap, int pageOffSet) {//翻页时，重新设定页码
        HashMap<String, Object> map = (HashMap<String, Object>) accepterMap.get(PARA_PAGE_MAP);
        PageModel pageModel = (PageModel) accepterMap.get(PAGEMODEL);
        int pageNum = pageModel.getPageNum() + pageOffSet;
        pageNum = pageNum < 1 ? 1 : pageNum;
        int maxPageNum = pageModel.getTotalPageNum();
        pageNum = pageNum > maxPageNum ? maxPageNum : pageNum;
        InstockReason instockReason = (InstockReason) map.get(WmsConstants.KEY_INSTOCKREASON_PARRAM);
        instockReason.setPage(pageNum);
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
        HashMap<String, Object> map = (HashMap<String, Object>) accepterMap.get(PARA_PAGE_MAP);
        if (map == null) {
            InstockReason instockReason = new InstockReason();
            instockReason.setPage(Constants.PAGE_START);
            instockReason.setRows(Constants.WHOLE_REASON_PAGE_SIZE);
            instockReason.setSidx("id");
            instockReason.setSord(Constants.PAGE_SORT_DESC);
            instockReason.setReasonType(type);
            instockReason.setReasonStatus(WmsConstants.STATUS_ENABLE);//生效
            map = initPagePara(instockReason, accepterMap);
        } else {
            map = resetPagePara(accepterMap, pageOffSet);
        }
        RemoteResult<PageModel<InstockReason>> pageModelRemoteResult = instockReasonRemoteService.getInstockReasonPageList(getCredentialsVO(ctx), map);
        if (!pageModelRemoteResult.isSuccess()) {//输出错误信息（无数据或异常）
            accepterMap.remove(PARA_PAGE_MAP);
            List<String> list = CheckReasonEnum.getNameList();
            //下一个字段以list切换的形式显示
            setColReSwitchList(list, ErrorConstants.PLS_MAINTAION_REASON, accepterMap, ctx);
        } else {//展示原因内容列表，一定有数据
            PageModel<InstockReason> instockReasonPageModel = pageModelRemoteResult.getT();
            accepterMap.put(PAGEMODEL, instockReasonPageModel);
            HandlerUtil.changeRow(ctx);
            int currPageLinesNum = PageUtil.showTable(ctx, instockReasonPageModel, TABLE_NAME, TABLE_COLUMN, true, true, null);//展示列表，带有序号
            accepterMap.put(LINES_NUM_CLEAN_KEY, currPageLinesNum);
            HandlerUtil.write(ctx, TipConstants.PLS_CHOSE_SERIALNO);
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 登录界面
        String[] pageHeader = {Constants.BREAK_LINE, Constants.INSTOCK_CHECK_ALL, Constants.SPLIT, ""};
        super.initBaseMap(CheckDetail.class, pageHeader, ctx);
    }
}
