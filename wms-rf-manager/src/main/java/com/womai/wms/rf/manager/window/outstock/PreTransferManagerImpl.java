package com.womai.wms.rf.manager.window.outstock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.PageUtil;
import com.womai.wms.rf.common.util.RFUtil;
import com.womai.wms.rf.domain.outstock.PreTransfer;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.base.TransporterRemoteService;
import com.womai.wms.rf.remote.outstock.TransOrderRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.instock.InstockDetail;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockPackInfo;
import com.womai.zlwms.rfsoa.domain.stock.StockInfo;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交接管理界面
 * User: zhangwei
 * Date: 2016-05-06
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component("preTransferShell")
public class PreTransferManagerImpl extends ReceiveManager {
    @Autowired
    private TransporterRemoteService transporterRemoteService;
    @Autowired
    private TransOrderRemoteService transOrderRemoteService;

    private static final String TRANS_CODE = "transCode";//配送商号属性名称
    private static final String PACK_CODE = "packCode";//面单号属性名称
    private int scanPackNum;//当前扫描数量
    private int packageAmount;//应扫描数量
    private int allPackAmount;//交接单中应扫总箱数
    private int allScanPackNum;//交接单中已扫箱数
    private long transSheetId;//交接id
    private String transSheetCode;//交接编号单号
    private boolean onlyN;//是否只接受N键输入
    private boolean onlyY;//是否只接受Y键输入
    private final String[] OUTSTOCK_PACK_INFO_TABLE_NAME = {"序号", "        面单号      "};//明细表头;//库存表头
    private final String[] OUTSTOCK_PACK_INFO_TABLE_COLUMN = {"packcode"};//库存列名
    private final static String SELECT_PAGE = "selectPage";//翻页

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String[] pageHeader = {Constants.BREAK_LINE, Constants.PRETRANS_ADMIN, Constants.SPLIT, ""};
        super.initBaseMap(PreTransfer.class, pageHeader, ctx);
        scanPackNum = 0;
        packageAmount = 0;
        transSheetId = 0L;
        allPackAmount = 0;
        allScanPackNum = 0;
        onlyN = false;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        Map<String, Object> accepterMap = getDataMap();
        Integer receiveResult = receiveData(ctx, object, accepterMap);
        PreTransfer preTransfer = (PreTransfer) accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (TRANS_CODE.equals(lastCompleteColName)) {
            String transCode = preTransfer.getTransCode();
            RemoteResult<Map<String, Object>> remoteResult = transOrderRemoteService.validateTransporterAndQueryOutStockTransOrder(getCredentialsVO(ctx), transCode);
            if (remoteResult.isSuccess()) {
                Map<String, Object> validateResult = remoteResult.getT();
                transSheetCode = (String) validateResult.get("transSheetCode");
                allScanPackNum = (Integer) validateResult.get("allScanPackNum");
                allPackAmount = (Integer) validateResult.get("allPackAmount");
                removeErrMess(accepterMap);
                inputBoxCode(ctx, "", "", "");
            } else {
                colNeedReInput(TRANS_CODE, remoteResult.getResultCode(), accepterMap, ctx);
            }
        }
        if (PACK_CODE.equals(lastCompleteColName)) {
            String packCode = preTransfer.getPackCode();
            if (onlyN && packCode.equalsIgnoreCase(Constants.CANCEL_N)) {
                onlyN = false;
                colNeedReInput(PACK_CODE, "", accepterMap, ctx, false);
                inputBoxCode(ctx, "", "", "");
            } else if (onlyN && !packCode.equalsIgnoreCase(Constants.CANCEL_N)) {
                colNeedReInput(PACK_CODE, "", accepterMap, ctx);
                inputBoxCode(ctx, "", "只能输入N", "N");
            } else if (onlyY && packCode.equalsIgnoreCase(Constants.CONFIRM_Y)) {
                onlyY = false;
                colNeedReInput(PACK_CODE, "", accepterMap, ctx, false);
                inputBoxCode(ctx, "", "", "");
            } else if (onlyY && !packCode.equalsIgnoreCase(Constants.CONFIRM_Y)) {
                colNeedReInput(PACK_CODE, "", accepterMap, ctx);
                inputBoxCode(ctx, "", "只能输入Y", "Y");
            } else {
                onlyN = false;
                onlyY = false;
                if (packCode.equalsIgnoreCase(Constants.CONFIRM_Y)) {
                    //直接按照配送商进行查询与数据校验
                    RemoteResult<String> remoteResult = transOrderRemoteService.transOrderFinishScan(getCredentialsVO(ctx), preTransfer.getTransCode(), transSheetId);
                    if (!remoteResult.isSuccess()) {//确认失败则进行提示
                        {
                            HandlerUtil.clearAll(ctx.channel());
                            String[] pageHeader = {Constants.BREAK_LINE, Constants.PRETRANS_ADMIN + Constants.PRETRANS_CREATE_PRODUCTORDER, Constants.SPLIT, ""};
                            HandlerUtil.writer(ctx, pageHeader, 1, 1);
                            Map<String, Object> scmPackNumMap = new HashMap<String, Object>();
                            scmPackNumMap.put("scanPackNum", this.scanPackNum);
                            scmPackNumMap.put("packageAmount", this.packageAmount);
                            scmPackNumMap.put("allPackAmount", this.allPackAmount);
                            scmPackNumMap.put("allScanPackNum", this.allScanPackNum);
                            String scanInfo = RFUtil.composeMessage(Constants.PRETRANS_SCANPROCESSS, scmPackNumMap);
                            String transScanInfo = RFUtil.composeMessage(Constants.PRETRANS_TRANS_SCANPROCESSS, scmPackNumMap);
                            if (StringUtils.isNotBlank(transSheetCode)) {
                                HandlerUtil.write(ctx, "交接单号:" + transSheetCode + Constants.BREAK_LINE);
                            }
                            HandlerUtil.write(ctx, scanInfo + Constants.BREAK_LINE + transScanInfo + Constants.BREAK_LINE + "未扫描面单号列表：");
                            Object obj = accepterMap.get(PageUtil.PARA_PAGE_MAP);
                            if(obj != null){
                                //分页查询未扫描的面单号后，翻页到第二页，任意键退到面单号扫描的位置，再次“Y”进来后,从第二页展示，所以清空查询条件，从新获得
                                accepterMap.remove(PageUtil.PARA_PAGE_MAP);
                            }
                            showOutstockPackInfosPage(ctx);
                        }
                    } else {
                        ctx.pipeline().fireChannelActive();//确认成功,跳转回初始界面
                    }
                } else {
                    Map<String, Object> transResultMap = transOrderRemoteService.transOrderByPackCode(getCredentialsVO(ctx), packCode, preTransfer.getTransCode(), transSheetId);
                    String inputFlag = (String) transResultMap.get("inputFlag");
                    String transResult = (String) transResultMap.get("errorMess");
                    if (StringUtils.isBlank(transResult)) {
                        //分别对应扫数量、已扫数量、交接单号赋值
                        packageAmount = (Integer) transResultMap.get("packageAmount");
                        scanPackNum = (Integer) transResultMap.get("scanPackNum");
                        transSheetId = (Long) transResultMap.get("transSheetId");
                        transSheetCode = (String) transResultMap.get("transSheetCode");
                        allScanPackNum = (Integer) transResultMap.get("allScanPackNum");
                        allPackAmount = (Integer) transResultMap.get("allPackAmount");
                        colNeedReInput(PACK_CODE, transResult, accepterMap, ctx,false);
                    }else{
                        colNeedReInput(PACK_CODE, transResult, accepterMap, ctx);
                    }
                    inputBoxCode(ctx, packCode, transResult, inputFlag);
                }
            }
        }

        if (SELECT_PAGE.equals(lastCompleteColName)) {
            String pageNum = preTransfer.getSelectPage();
            int pageSizeCurr = (Integer) accepterMap.get(PageUtil.LINES_NUM_CLEAN_KEY) + 1;//页面当前数据条数
            if (KeyEnum.N_78.value.equalsIgnoreCase(pageNum)) {//下一页
                PageUtil.changePageNext(ctx, accepterMap, WmsConstants.KEY_OUTSTOCK_PARAM_PACK_INFO, pageSizeCurr);
                showOutstockPackInfosPage(ctx);
                setColUnReceived(SELECT_PAGE, accepterMap);
                return;
            } else if (KeyEnum.B_66.value.equalsIgnoreCase(pageNum)) {//上一页
                PageUtil.changePageUp(ctx, accepterMap, WmsConstants.KEY_OUTSTOCK_PARAM_PACK_INFO, pageSizeCurr);
                showOutstockPackInfosPage(ctx);
                setColUnReceived(SELECT_PAGE, accepterMap);
                return;
            } else {//序号
                onlyN = false;
                setColUnReceived(SELECT_PAGE, accepterMap);
                colNeedReInput(PACK_CODE, "", accepterMap, ctx);
                inputBoxCode(ctx, "", "", "");
            }
        }
    }

    /**
     * 分页查询未扫描面单
     *
     * @param ctx
     */
    private void showOutstockPackInfosPage(ChannelHandlerContext ctx) {
        Map<String, Object> accepterMap = getDataMap();
        RemoteResult<PageModel<OutstockPackInfo>> pageModelRemoteResult = transOrderRemoteService.getOutstockPackInfoPageList(getCredentialsVO(ctx), getParaMap());
        if (pageModelRemoteResult.isSuccess()) {
            //展示库存分页列表，一定有数据
            PageModel<OutstockPackInfo> detailsPageModel = pageModelRemoteResult.getT();
            accepterMap.put(PageUtil.PAGE_MODEL, detailsPageModel);
            HandlerUtil.changeRow(ctx);
            int currPageLinesNum = PageUtil.showTable(ctx, detailsPageModel, OUTSTOCK_PACK_INFO_TABLE_NAME, OUTSTOCK_PACK_INFO_TABLE_COLUMN, true, true, null);//展示列表，带有序号
            accepterMap.put(PageUtil.LINES_NUM_CLEAN_KEY, currPageLinesNum);
            resetCurCol(SELECT_PAGE, accepterMap, ctx);
            HandlerUtil.write(ctx,"翻页（B/N,其它键继续）:");
        } else {//输出错误信息（无数据或异常）
            super.colNeedReInput(PACK_CODE, ErrorConstants.DATA_NOT_FOUNT_CONTINUE, accepterMap, ctx);
        }
    }

    /**
     * 获取查询包裹分页条件
     *
     * @return
     */
    private HashMap<String, Object> getParaMap() {
        Map<String, Object> accepterMap = getDataMap();
        final PreTransfer preTransfer = (PreTransfer) accepterMap.get(DefaultKey.objectClass.keyName);
        HashMap<String, Object> map = (HashMap<String, Object>) accepterMap.get(PageUtil.PARA_PAGE_MAP);//缓存中的分页查询参数Map
        if (map == null) {
            final OutstockPackInfo outstockPackInfo = new OutstockPackInfo();
            outstockPackInfo.setPage(Constants.PAGE_START);
            outstockPackInfo.setRows(Constants.STOCK_INFO_PAGE_SIZE);
            outstockPackInfo.setSidx("ID");
            outstockPackInfo.setSord(Constants.PAGE_SORT_DESC);
            map = new HashMap<String, Object>() {{
                put(WmsConstants.KEY_OUTSTOCK_PARAM_PACK_INFO, outstockPackInfo);
                put(WmsConstants.KEY_OUTSTOCK_PARAM_TRANSCODE, preTransfer.getTransCode());
            }};
            accepterMap.put(PageUtil.PARA_PAGE_MAP, map);
            return map;
        }
        return map;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 输出第二屏，用于输入箱号
     *
     * @param ctx
     * @param packCode   当前扫描到的面单号
     * @param appendMess 需要额外显示的信息，错误提示等
     * @param inputFlag  只能输入Y或N的标识，如果值为Y只能输入Y，空或N，只能输入N
     */
    private void inputBoxCode(ChannelHandlerContext ctx, String packCode, String appendMess, String inputFlag) {
        HandlerUtil.clearAll(ctx.channel());
        String[] pageHeader = {Constants.BREAK_LINE, Constants.PRETRANS_ADMIN + Constants.PRETRANS_CREATE_PRODUCTORDER, Constants.SPLIT, ""};
        HandlerUtil.writer(ctx, pageHeader, 1, 1);
        Map<String, Object> scmPackNumMap = new HashMap<String, Object>();
        scmPackNumMap.put("scanPackNum", this.scanPackNum);
        scmPackNumMap.put("packageAmount", this.packageAmount);
        scmPackNumMap.put("allPackAmount", this.allPackAmount);
        scmPackNumMap.put("allScanPackNum", this.allScanPackNum);
        String scanInfo = RFUtil.composeMessage(Constants.PRETRANS_SCANPROCESSS, scmPackNumMap);
        String transScanInfo = RFUtil.composeMessage(Constants.PRETRANS_TRANS_SCANPROCESSS, scmPackNumMap);
//        HandlerUtil.print(ctx, scanInfo + Constants.BREAK_LINE+transScanInfo+Constants.BREAK_LINE
//                + appendMess +Constants.BREAK_LINE +Constants.PRETRANS_INPUT_BOXCODE);

        if (StringUtils.isNotBlank(transSheetCode)) {
            HandlerUtil.write(ctx, "交接单号:" + transSheetCode + Constants.BREAK_LINE);
        }
        HandlerUtil.write(ctx, scanInfo + Constants.BREAK_LINE + transScanInfo + Constants.BREAK_LINE);
        packCode = StringUtils.isNotBlank(appendMess) ? packCode : "";
        HandlerUtil.write(ctx, Constants.PRETRANS_INPUT_BOXCODE + packCode);
        if (StringUtils.isNotBlank(appendMess)) {
            String keyTip = "";
            if (StringUtils.isBlank(inputFlag) || Constants.CANCEL_N.equalsIgnoreCase(inputFlag)) {
                onlyN = true;
                keyTip = "输入 N 确认继续";
            } else if (Constants.CONFIRM_Y.equalsIgnoreCase(inputFlag)) {
                onlyY = true;
                keyTip = "输入 Y 确认继续";
            }
            HandlerUtil.write(ctx, Constants.BREAK_LINE + appendMess + Constants.BREAK_LINE + Constants.BREAK_LINE + keyTip + Constants.BREAK_LINE + "请输入：");
        }
    }

}
