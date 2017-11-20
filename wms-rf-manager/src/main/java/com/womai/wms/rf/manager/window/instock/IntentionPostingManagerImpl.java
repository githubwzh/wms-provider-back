package com.womai.wms.rf.manager.window.instock;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.DefaultKey;
import com.womai.wms.rf.common.constants.ErrorConstants;
import com.womai.wms.rf.common.constants.TipConstants;
import com.womai.wms.rf.common.util.CollectionUtil;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.RFUtil;
import com.womai.wms.rf.common.util.WMSDebugManager;
import com.womai.wms.rf.domain.instock.IntentionPosting;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.instock.InstockRemoteService;
import com.womai.zlwms.rfsoa.domain.instock.Instock;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 描述: 意向单过账Handler
 * User:Qi Xiafei
 * Date: 2016-06-30
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component("intentionPostingManager")
public class IntentionPostingManagerImpl extends ReceiveManager {

    @Autowired
    InstockRemoteService instockRemoteService;

    // 放入accepterMap中记录的Map键
    private final static String GOTO = "gotoColName";//标识是否跳转的keyName

    // 业务流程字段名
    private static final String INTENTION_POSTING_KEY = "intentionPostingKey"; // 意向单主键
    private static final String ASN_INSTOCK_CODE = "asnInstockCode"; // ASN单号
    private static final String IS_POSTING = "isPosting"; // 是否过账
    //字段输入校验
    private static final String YN_REGX = "^[YNyn]$"; // 输入y/n的场景校验
    // 本地变量
    private Instock inStockLocal; // 储存意向单信息，主要用于显示

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        resetLocals();// 初始化时清空本地变量内容
        // 界面标题:pageHeader中无论放的什么内容，每个数组元素占一行。若需要换行不要使用\r\n，使用一个新的数组元素。
        String[] pageHeader = {"", TipConstants.INTENTION_POSTING_TITLE, Constants.SPLIT, ""};
        super.initBaseMap(IntentionPosting.class, pageHeader, ctx);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        Map<String, Object> accepterMap = getDataMap(); // ReceiveManager中记录输入状态的Map
        // 处理跳转请求
        if (goToHandler(accepterMap, ctx)) {
            return;
        }

        receiveDataAndNotPrintNext(ctx, object, accepterMap);
        // 取到当前业务对象
        IntentionPosting intentionPosting = (IntentionPosting) accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);//上一个完成输入的字段

        if (INTENTION_POSTING_KEY.equals(lastCompleteColName)) {
            String intentionPostingKey = intentionPosting.getIntentionPostingKey();
            /**
             * 这里取到的收货单列表是符合以下条件的
             * 1、是意向单，即ordertype=21、22、23
             * 2、不能是取消状态CANCEFLAG=0，未取消状态
             * 3、收货中、收货登记状态instock_status=21、22 ！！！区别于Web端的逻辑，将主单收货中状态的收货单也放在合理的过账范围之内
             * 4、实际收货数量不能大于计划收货数量，EXPECTNUMBU>=RECEIVENUMBU
             * 5、明细表中不能包含失效的商品，按照SKUID查询Base_goodsinfo.status=1的商品
             * 6、仓库编码instock.warehouseno不为空并且等于credentialsVO.getCurrentSite()
             * 7、明细表中必须包含至少一条是收货登记状态detailStatus=1 ！！！区别于Web端的逻辑，因为允许收货中状态的收货单进行过账，所以只要明细表中存在一条收货登记即可，不需要所有明细都是收货登记状态
             * 8、明细表中不能存在记录isphcard=1并且cardstatuc=0的数据
             * 9、明细表中实际收货数量getReceivenumbu之和等于主单的实际收货数量。
             * 10、不能是虚入的VIRTUALIN==1
             */
            RemoteResult<List<Instock>> result = instockRemoteService.getIntentionListByASNCodeOrPurchaseNo(this.getCredentialsVO(ctx), intentionPostingKey);

            if (result.isSuccess()) {
                List<Instock> instockList = result.getT();
                if (instockList.size() == 1) {// 如果只能查询到一条结果，则跳过让用户选择ASN的步骤
                    Instock instock = instockList.get(0);
                    inStockLocal = instock;
                    // 在询问用户是否过账之前打印一些信息
                    printBeforeIsPosting(accepterMap, ctx);
                    // 直接跳转到是否过账的步骤
                    resetCurCol(IS_POSTING, accepterMap, ctx);
                } else {
                    List<String> valueList = new ArrayList<String>();
                    for (Instock e : instockList) {
                        valueList.add(e.getAsninstockcode());
                    }
                    // 若查出多条收货单则让用户根据ASN单号进行选择
                    setNextColSwitchList(valueList, accepterMap, ctx);
                }
            } else {
                // 若格式有误提示用户重新输入
                colNeedReInput(lastCompleteColName, ErrorConstants.INTENTION_POSTING_KEY_ERROR, accepterMap, ctx);
            }

        } else if (ASN_INSTOCK_CODE.equals(lastCompleteColName)) {
            String asnCode = intentionPosting.getAsnInstockCode();
            // 根据用户选择ASN单号查询收货单信息
            RemoteResult<Instock> result = instockRemoteService.getByAsnCode(this.getCredentialsVO(ctx), asnCode);
            if (result.isSuccess()) {
                inStockLocal = result.getT();
                // 在询问用户是否过账之前打印一些信息
                printBeforeIsPosting(accepterMap, ctx);
                rePrintCurColTip(accepterMap, ctx);
            } else {
                HandlerUtil.errorBeep(ctx);//系统错误，响铃
                exceptionJump(result.getResultCode(), INTENTION_POSTING_KEY, accepterMap, ctx);
            }

        } else if (IS_POSTING.equals(lastCompleteColName)) {
            String isPosting = intentionPosting.getIsPosting();
            if (isPosting.matches(YN_REGX)) {
                if (isPosting.equalsIgnoreCase(Constants.CANCEL_N)) {// 若用户选择不过账则跳转到初始界面重新扫码
                    channelActive(ctx);
                } else {
                    // 确认过账提交
                    RemoteResult<String> result = instockRemoteService.confirmInstockPosting(this.getCredentialsVO(ctx), inStockLocal.getAsninstockcode());
                    if (!result.isSuccess()) {
                        HandlerUtil.errorBeep(ctx);//系统错误，响铃
                        exceptionJump(result.getT(), INTENTION_POSTING_KEY, accepterMap, ctx);
                    } else {
                        exceptionJump(TipConstants.INTENTION_POSTING_SUCCESS, INTENTION_POSTING_KEY, accepterMap, ctx);
                    }
                }
            } else {
                colNeedReInput(lastCompleteColName, ErrorConstants.ONLY_YN_AND_OTHER, accepterMap, ctx);
            }

        }
        WMSDebugManager.debugLog("IntentionPostingManagerImpl--Received:".concat(accepterMap.toString()));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    // 清空本地变量
    private void resetLocals() {
        inStockLocal = null;
    }

    /**
     * 在输出是否过账前打印一些信息
     *
     * @param accepterMap 接收内容
     * @param ctx         netty上下文
     */
    private void printBeforeIsPosting(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        Instock instock = inStockLocal;
        List<String> tipList = CollectionUtil.newGenericList(TipConstants.INTENTION_POSTING_ASNCODE_LABEL, TipConstants.INTENTION_POSTING_RECEIVENUMBU_LABEL, TipConstants.INTENTION_POSTING_UNRECEIVENUMBU_LABEL);
        List<String> valueList = CollectionUtil.newGenericList(instock.getAsninstockcode(), instock.getReceivenumbu().toString(), (instock.getExpectnumbu() - instock.getReceivenumbu()) + "");
        printBeforeNextField(tipList, valueList, accepterMap, ctx);
    }

    // 直接跳转控制
    private boolean goToHandler(Map<String, Object> accepterMap, ChannelHandlerContext ctx) throws Exception {
        if (accepterMap.get(GOTO) != null) {
            String goToColName = String.valueOf(accepterMap.get(GOTO));
            if (goToColName.equals(INTENTION_POSTING_KEY)) {
                channelActive(ctx);
            }
            accepterMap.put(GOTO, null);
            return true;
        }
        return false;
    }


    /**
     * 发生异常后的跳转
     *
     * @param mess        提示信息
     * @param colName     跳转的目标字段名
     * @param accepterMap 记录输入参数的Map
     * @param ctx         ChannelHandlerContext
     */
    public void exceptionJump(String mess, String colName, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        printBeforeNextField(RFUtil.makeStrFitPda(mess, null, 3), accepterMap, ctx);
        accepterMap.put(GOTO, colName);
    }
}
