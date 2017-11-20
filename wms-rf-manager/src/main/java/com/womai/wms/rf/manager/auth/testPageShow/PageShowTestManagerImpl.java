package com.womai.wms.rf.manager.auth.testPageShow;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.PageUtil;
import com.womai.wms.rf.common.util.WMSDebugManager;
import com.womai.wms.rf.domain.testPageShow.PageShowTest;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.instock.InstockReasonRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.instock.InstockReason;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分页测试类（需要修改menus.xml中的配置，可以将“修改密码”的location替换成“pageShowTestManager”，不要提交哦）
 * Created by wangzhanhua on 2016/7/21.
 */
@Scope("prototype")
@Component("pageShowTestManager")
public class PageShowTestManagerImpl extends ReceiveManager {
    public final static String[] TABLE_NAME = {"序号", "原因内容        ", "创建人创建人"};//原因列表头
    public final static String[] TABLE_COLUMN = {"remark", "creatorname"};//原因列表头
    private static final String PAGE_NUM = "pageNum";
    public final static String REG = "^[1-9]\\d{0,9}$";
    @Autowired
    private InstockReasonRemoteService instockReasonRemoteService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 登录界面
        String[] header = {"                分页测试                "};
        super.initBaseMap(PageShowTest.class, header, ctx);
        showReasonPage(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 原因列表分页
     *
     * @param ctx handler对象
     * @param msg 每次接收到的字节数据
     * @throws Exception 抛出异常
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Map<String, Object> accepterMap = getDataMap();
        Integer receiveResult = receiveDataAndNotPrintNext(ctx, msg, accepterMap);
        PageShowTest pageShowTest = (PageShowTest) accepterMap.get(DefaultKey.objectClass.keyName);
        //  String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (receiveResult == RECEIVER_TYPE_FINISHED) {
            //组装数据
            String pageNum = pageShowTest.getPageNum();
            PageModel<InstockReason> pageModel = (PageModel<InstockReason>) accepterMap.get(PageUtil.PAGE_MODEL);
            int pageSizeCurr = (Integer) accepterMap.get(PageUtil.LINES_NUM_CLEAN_KEY);//页面当前数据条数
            if (KeyEnum.N_78.value.equalsIgnoreCase(pageNum)) {//下一页
                PageUtil.changePageNext(ctx, accepterMap, WmsConstants.KEY_INSTOCKREASON_PARRAM, pageSizeCurr);
                showReasonPage(ctx);
                setColUnReceived(PAGE_NUM, accepterMap);
            } else if (KeyEnum.B_66.value.equalsIgnoreCase(pageNum)) {//上一页
                PageUtil.changePageUp(ctx, accepterMap, WmsConstants.KEY_INSTOCKREASON_PARRAM, pageSizeCurr);
                showReasonPage(ctx);
                setColUnReceived(PAGE_NUM, accepterMap);
            } else {//序号
                if (pageNum.matches(REG)) {
                    List<InstockReason> instockReasons = pageModel.getDatas();
                    int maxIndex = instockReasons.size() - 1;
                    int index = PageUtil.getIndexFromSerialnoAndPageModle(pageNum, pageModel);//(pn-1)*size+index+1==serialno
                    if (index > maxIndex || index < 0) {
                        colNeedReInput(PAGE_NUM, ErrorConstants.INPUT_FORMAT_ERROR + ErrorConstants.COMMON_PUNCTUATION, accepterMap, ctx);
                    } else {
                        //正确选择原因内容，保存参数
                        WMSDebugManager.debugLog("PageShowTestManagerImpl-Current-Detail-Received:" + instockReasons.get(index));
                        HandlerUtil.changeRow(ctx);
                        HandlerUtil.println(ctx, TipConstants.REASON_CONTENT + instockReasons.get(index).getRemark());
                        rePrintCurColTip(accepterMap, ctx);
                    }
                } else {
                    colNeedReInput(PAGE_NUM, ErrorConstants.INPUT_FORMAT_ERROR + ErrorConstants.COMMON_PUNCTUATION, accepterMap, ctx);
                }
            }
        }
        WMSDebugManager.debugLog("PageShowTestManagerImpl--Received:" + accepterMap);
    }

    /**
     * 显示原因内容分页列表
     *
     * @param ctx
     */
    private void showReasonPage(ChannelHandlerContext ctx) {
        Map<String, Object> accepterMap = getDataMap();
        RemoteResult<PageModel<InstockReason>> pageModelRemoteResult = instockReasonRemoteService.getInstockReasonPageList(getCredentialsVO(ctx), getParaMap());
        if (!pageModelRemoteResult.isSuccess()) {//输出错误信息（无数据或异常）
            accepterMap.remove(PageUtil.PARA_PAGE_MAP);
            colNeedReInput(PAGE_NUM, ErrorConstants.PLS_MAINTAION_REASON, accepterMap, ctx);
        } else {//展示原因内容列表，一定有数据
            PageModel<InstockReason> instockReasonPageModel = pageModelRemoteResult.getT();
            accepterMap.put(PageUtil.PAGE_MODEL, instockReasonPageModel);
            HandlerUtil.changeRow(ctx);
            int currPageLinesNum = PageUtil.showTable(ctx, instockReasonPageModel, TABLE_NAME, TABLE_COLUMN, true, true, null);//展示列表，带有序号
            accepterMap.put(PageUtil.LINES_NUM_CLEAN_KEY, currPageLinesNum);
            HandlerUtil.write(ctx, TipConstants.PLS_CHOSE_SERIALNO);
        }
    }

    /**
     * 获得分页查询条件
     */
    private HashMap<String, Object> getParaMap() {
        Map<String, Object> accepterMap = getDataMap();
        HashMap<String, Object> map = (HashMap<String, Object>) accepterMap.get(PageUtil.PARA_PAGE_MAP);//缓存中的分页查询参数Map
        if (map == null) {
            final InstockReason instockReason = new InstockReason();
            instockReason.setPage(Constants.PAGE_START);
            instockReason.setRows(Constants.REASON_PAGE_SIZE);
            instockReason.setSidx("id");
            instockReason.setSord(Constants.PAGE_SORT_DESC);
            instockReason.setReasonStatus(WmsConstants.STATUS_ENABLE);//生效
            map = new HashMap<String, Object>() {{
                put(WmsConstants.KEY_INSTOCKREASON_PARRAM, instockReason);
            }};
            accepterMap.put(PageUtil.PARA_PAGE_MAP, map);
        }
        return map;
    }
}
