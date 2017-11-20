package com.womai.wms.rf.manager.window.inventory;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.CollectionUtil;
import com.womai.wms.rf.common.util.DateTimeUtil;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.ManagerLog;
import com.womai.wms.rf.domain.inventory.InventoryMP;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.base.GoodsinfoRemoteService;
import com.womai.wms.rf.remote.inventory.InventoryRemoteService;
import com.womai.zlwms.rfsoa.api.WMSErrorMess;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.base.BaseGoodsinfo;
import com.womai.zlwms.rfsoa.domain.base.BaseWarehouseinfo;
import com.womai.zlwms.rfsoa.domain.inventory.InventoryInfo;
import com.womai.zlwms.rfsoa.domain.inventory.InventoryItem;
import com.womai.zlwms.rfsoa.domain.inventory.InventoryRegistItem;
import com.womai.zlwms.rfsoa.domain.sys.SysSwitchinfo;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassDescribe:明盘业务处理
 * Author :zhangwei
 * Date: 2016-11-07
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component(TipConstants.INVENTORY_URL_MP_MANAGER)
public class InventoryManagerMPImpl extends ReceiveManager {
    @Autowired
    private InventoryRemoteService inventoryRemoteService;
    @Autowired
    private GoodsinfoRemoteService goodsinfoRemoteService;


    private final static String GO_TO_FLAG = "inventoryMP_goto";//跳转标志
    private final static Integer TO_CHANNEL_ROOT = -1;//跳转回到扫描库位的Handler
    private final static Integer TO_WH_CODE = 0;//跳转标志


    private final static String RECEIVED_WH_CODE = "receivedWhCode";//用于出发第一个接收，不显示字段提示
    private final static String EXIST_DIFFER = "existDiffer";//是否存在差异
    private final static String CONFIRM_BARCODE = "confirmBarCode";//选择存在差异时再次输入商品条码
    private final static String PROD_OR_EXP_DATE = "prodOrExpDate";//确认日期，根据批次规则的可能为生产日期或失效日期
    private final static String TOO_BIG_THAN_SYS_DATE = "tooBigThanSysDate";//与日期开关比较是否过大，左右选择，之前一行提示：与该库存系统日期相差过大
    private final static String CONFIRM_BU = "confirmBU";//盘点BU数
    private final static String EXIST_SURPLUS = "existSurplus";//是否存在多余库存


    public Map<String, Object> TL_BASE_DATA = new HashMap<String,Object>();//根据库位查询的基础数据
    private BaseWarehouseinfo TL_WARE_HOUSE_INFO ;//当前库位
    private InventoryInfo TL_INFO ;//盘点主单
    private InventoryItem TL_ITEM ;//盘点明细

    private SysSwitchinfo TL_MINPROSWITCH;//盘点最小生产日期
    private SysSwitchinfo TL_MAXBUSWITCH ;//盘点最大数量BU
    private SysSwitchinfo TL_MAXDIFFDAYSWITCH;//盘点日期差值提示开关

    private InventoryRegistItem TL_REGISTER ;//盘点登记明细
    private String TL_SYS_BARCODE ;//登记明细关联商品的条码
    private Date TL_SYS_PRODUCTION ;//查询到的系统生产日期
    private Date TL_SYS_EXPIRATION ;//查询到的系统失效日期
    private Integer TL_SYS_STOCK_NUM ;//查询到的系统库存数量BU
    private Integer TL_OPERATE_TYPE ;//操作类型，新增、更新、覆盖

    private BaseGoodsinfo TL_GOODS_INFO ;//存在差异时按照条码查询到的商品数据

    private final static List<String> YN_LIST = CollectionUtil.newList(TipConstants.NO, TipConstants.YES);//通用的是、否选择，默认显示否
    private List<String> dateTypeList = CollectionUtil.newList(Constants.batchRuleEnum.puTong.dateType, Constants.batchRuleEnum.xiHua.dateType);//日期类型列表
    private final static String DATE_TYPE = "dateType";//日期类型，选择生产日期或失效期
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InventoryParamManagerImpl inventoryParamManager = (InventoryParamManagerImpl) ctx.pipeline().get(TipConstants.INVENTORY_PARAM_MANAGER);
        Map<String, Object> baseMapData = inventoryParamManager.getBaseDataMap();
        TL_BASE_DATA.putAll(baseMapData);//暂存基础数据，如果需要跳转到存在多余实物的Handler将此数据带过去
        //初始化查询到的基础数据
        initBaseData(baseMapData);
        //清除参数并将参数Handler删除，避免重复放入
        inventoryParamManager.clearBaseDataMap();
        ctx.pipeline().remove(TipConstants.INVENTORY_PARAM_MANAGER);
        tipWhCode(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {

        Map<String, Object> accepterMap = getDataMap();
        if (goToHandler(accepterMap, ctx)) {
            return;
        }

        receiveDataAndNotPrintNext(ctx, object, accepterMap);
        InventoryMP inventoryMP = (InventoryMP) accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);

        if (RECEIVED_WH_CODE.equals(lastCompleteColName)) {
            RemoteResult<Map<String, Object>> unRegisterRemoteData =
                    inventoryRemoteService.getOneUnRegisteredDataForMP(getCredentialsVO(ctx), TL_WARE_HOUSE_INFO, TL_INFO, TL_ITEM);
            if (unRegisterRemoteData.isSuccess()) {
                Map<String, Object> registerDataMap = unRegisterRemoteData.getT();
                if (registerDataMap == null || registerDataMap.isEmpty()) {
                    setColSwitchList(EXIST_SURPLUS, YN_LIST, accepterMap, ctx);
                } else {
                    //如果存在可登记数据则提示是否存在差异
                    tipGoodsInfoForMp(registerDataMap, accepterMap, ctx);
                    setColSwitchList(EXIST_DIFFER, YN_LIST, accepterMap, ctx);
                }
            } else {
                accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ROOT);
                printBeforeNextField(unRegisterRemoteData.getResultCode(), accepterMap, ctx);
            }
        }

        if (EXIST_DIFFER.equals(lastCompleteColName)) {
            String existDiffer = inventoryMP.getExistDiffer();
            if (TipConstants.NO.equals(existDiffer)) {
                //如果选择不存在差异，则显示下一个可盘点商品
                TL_OPERATE_TYPE = WmsConstants.INVENTORY_REGISTER_NONE;
                //构造本次输入的登记数据参数
                InventoryRegistItem registerParam = new InventoryRegistItem();
                registerParam.setSkuid(TL_REGISTER.getSkuid());
                registerParam.setBatchrule(TL_REGISTER.getBatchrule());
               /* //洗化批次存失效
                if (Constants.batchRuleEnum.xiHua.code.equals(TL_REGISTER.getBatchrule())) {
                    registerParam.setStockProDate(TL_SYS_EXPIRATION);
                } else {
                    //其它批次存生效
                    registerParam.setStockProDate(TL_SYS_PRODUCTION);
                }*/
                registerParam.setStockProDate(TL_SYS_PRODUCTION);
                registerParam.setStockExpDate(TL_SYS_EXPIRATION);
                registerParam.setProductiondate(TL_SYS_PRODUCTION);
                registerParam.setExpirationdate(TL_SYS_EXPIRATION);
                registerParam.setRegisterbu(TL_SYS_STOCK_NUM);
                confirmRegister(registerParam, accepterMap, ctx);
            } else {
                resetCurCol(CONFIRM_BARCODE, accepterMap, ctx);
            }
        }

        if (EXIST_SURPLUS.equals(lastCompleteColName)) {
            String existSurplus = inventoryMP.getExistSurplus();
            if (TipConstants.NO.equals(existSurplus)) {
                //选择不存在多余库存，相当于输入00，结束本库位盘点
                endCmd(accepterMap, ctx);
            } else {
                //选择存在多余实物进入相应的Handler进行处理
                InventoryParamManagerImpl inventoryParamManager = new InventoryParamManagerImpl();
                inventoryParamManager.setBaseDataMap(TL_BASE_DATA);
                ctx.pipeline().addAfter(Constants.ENCODE_HANDLER, TipConstants.INVENTORY_PARAM_MANAGER, inventoryParamManager);

                forward(TipConstants.INVENTORY_URL_MP_SURPLUS_MANAGER, ctx);
            }
        }

        if (CONFIRM_BARCODE.equals(lastCompleteColName)) {
            String barCode = inventoryMP.getConfirmBarCode();
            if (TipConstants.END_CMD.equals(barCode)) {
                //商品条码处输入00，直接结束本库位盘点
                endCmd(accepterMap, ctx);
            } else {
                if (!barCode.equals(TL_SYS_BARCODE)) {
                    colNeedReInput(CONFIRM_BARCODE, "输入与上方一致的条码", accepterMap, ctx);
                    return;
                }
                RemoteResult<BaseGoodsinfo> remoteResult = goodsinfoRemoteService.getGoodsInfoByBarCode(getCredentialsVO(ctx), barCode);
                if (remoteResult.isSuccess()) {
                    BaseGoodsinfo goodsInfo = remoteResult.getT();
                    TL_GOODS_INFO = goodsInfo;//保存查询到的商品数据
                    List<String> tipList = CollectionUtil.newList(goodsInfo.getSkuname(), TipConstants.GOODSINFO_KEEPDAYS_LABEL);
                    List<String> valList = CollectionUtil.newList("", String.valueOf(goodsInfo.getKeepdays()));
                    printBeforeNextField(tipList, valList, accepterMap, ctx);
                    //如果主单需要盘生产日期且不是百货批次，则定位到日期的输入，否则直接输入数量BU
                    if (isNeedDate(TL_INFO.getIsbyproductiondate(), goodsInfo.getBatchrule())) {
                        changeDateTypeTip(accepterMap, ctx);//根据批次规则显示对应的日期提示
                        //resetCurCol(PROD_OR_EXP_DATE, accepterMap, ctx);
                    } else {
                        resetCurCol(CONFIRM_BU, accepterMap, ctx);
                    }
                } else {
                    colNeedReInput(CONFIRM_BARCODE, remoteResult.getResultCode(), accepterMap, ctx);
                }
            }
        }
        if (DATE_TYPE.equalsIgnoreCase(lastCompleteColName)) {
            String dateType = inventoryMP.getDateType();
            if (!dateTypeList.contains(dateType)) {
                setColReSwitchList(dateTypeList, ErrorConstants.DATE_TYPE_NOT_IN_LIST, accepterMap, ctx);
            } else {
                resetCurCol(PROD_OR_EXP_DATE, accepterMap, ctx);
            }
        }
        if (PROD_OR_EXP_DATE.equals(lastCompleteColName)) {
            String confirmDate = inventoryMP.getProdOrExpDate();
            Boolean dateReg = DateTimeUtil.isSimpleDate(confirmDate);//校验是否符合yyyyMMdd日期格式
            if (dateReg) {
                //计算生产、失效日期，根据输入的日期、商品是否已经登记、开关值判断下一步的接收
                countProdAndExpDate(inventoryMP, accepterMap, ctx);
            } else {
                colNeedReInput(PROD_OR_EXP_DATE, ErrorConstants.ILLEGAL_DATE_PATTERN, accepterMap, ctx);
            }
        }

        if (TOO_BIG_THAN_SYS_DATE.equals(lastCompleteColName)) {
            String tooBigThanSysDate = inventoryMP.getTooBigThanSysDate();
            if (TipConstants.NO.equals(tooBigThanSysDate)) {
                //如果选择否则需要重新输入日期
                HandlerUtil.moveUpN(ctx, 2);//从本接收行上移两行定位到输入条码的步骤|
                HandlerUtil.changeRow(ctx);//回车换行到输入日期的步骤且光标靠左
                HandlerUtil.clearRight(ctx);//清楚右下方全部显示数据重新接收日期
                //因为resetCurCol方法会多输出一行回车，所以此处需要再上移一行
                HandlerUtil.moveUpN(ctx, 1);
                resetCurCol("", PROD_OR_EXP_DATE, accepterMap, ctx);
            } else {
                //选择是，进入到最后一个的输入BU
                resetCurCol(CONFIRM_BU, accepterMap, ctx);
            }
        }

        if (CONFIRM_BU.equals(lastCompleteColName)) {
            String confirmBU = inventoryMP.getConfirmBU();
            //需要与盘点最大SKU数进行比较
            if (checkInventoryBU(confirmBU, accepterMap, ctx)) {
                TL_OPERATE_TYPE = WmsConstants.INVENTORY_REGISTER_NONE;
                //构造本次输入的登记数据参数
                InventoryRegistItem registerParam = new InventoryRegistItem();
                BaseGoodsinfo goodsInfo = TL_GOODS_INFO;
                registerParam.setSkuid(goodsInfo.getSkuid());
                registerParam.setBatchrule(goodsInfo.getBatchrule());
               /* //洗化批次存失效
                if (Constants.batchRuleEnum.xiHua.code.equals(TL_REGISTER.getBatchrule())) {
                    registerParam.setStockProDate(TL_SYS_EXPIRATION);
                } else {
                    //其它批次存生效
                    registerParam.setStockProDate(TL_SYS_PRODUCTION);
                }*/
                registerParam.setStockProDate(TL_SYS_PRODUCTION);
                registerParam.setStockExpDate(TL_SYS_EXPIRATION);
                registerParam.setProductiondate(inventoryMP.getProductionDate());
                registerParam.setExpirationdate(inventoryMP.getExpirationDate());
                registerParam.setRegisterbu(Integer.parseInt(confirmBU));
                registerParam.setKeepdays(goodsInfo.getKeepdays());//soa再次查询商品信息看保质期是否变更，如果变更则提示
                confirmRegister(registerParam, accepterMap, ctx);
            }
        }
    }

    /**
     * 确认提交登记数据
     *
     * @param registerParam 本次输入的登记数据
     * @param accepterMap   map数据容器
     * @param ctx           ctx上下文
     */
    private void confirmRegister(InventoryRegistItem registerParam, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        RemoteResult<Integer> remoteResult = inventoryRemoteService.confirmRegister(getCredentialsVO(ctx), TL_OPERATE_TYPE, registerParam, TL_ITEM, TL_INFO);
        String errorMess = remoteResult.getResultCode();
        //如果无异常则回到重新扫描商品条码的步骤
        if (remoteResult.isSuccess()) {
            errorMess = ErrorConstants.SUCCESS_CONTINUE;
            accepterMap.put(GO_TO_FLAG, TO_WH_CODE);
        } else {
            HandlerUtil.errorBeep(ctx);//系统错误，响铃
            if (WMSErrorMess.INVENTORY_DATA_EXPIRY.equals(errorMess)) {
                errorMess = ErrorConstants.INVENTORY_DATA_EXPIRY;
                accepterMap.put(GO_TO_FLAG, TO_WH_CODE);
            } else {
                errorMess = errorMess + ErrorConstants.TIP_TO_CONTINUE;
                accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ROOT);
            }
        }
        printBeforeNextField(errorMess, accepterMap, ctx);
    }


    /**
     * 跳转控制
     *
     * @param accepterMap map数据容器
     * @param ctx         ctx上下文
     * @return 返回true则已经跳转，channelRead方法中不再往下进行o
     * @throws Exception 抛出异常
     */
    public boolean goToHandler(Map<String, Object> accepterMap, ChannelHandlerContext ctx) throws Exception {
        if (accepterMap.get(GO_TO_FLAG) != null) {
            InventoryMP inventoryMP = (InventoryMP) accepterMap.get(DefaultKey.objectClass.keyName);
            Integer goToAim = (Integer) accepterMap.get(GO_TO_FLAG);
            accepterMap.remove(GO_TO_FLAG);//清空一下，避免重复调用
            // 根据不同的返回值回到不同的步骤
            if (TO_CHANNEL_ROOT.equals(goToAim)) {
                forward(TipConstants.INVENTORY_URL_MANAGER, ctx);
                return true;
            } else if (TO_WH_CODE.equals(goToAim)) {
                tipWhCode(ctx);
                return true;
            }
        }
        return false;
    }

    /**
     * 输入00，结束库位盘点
     *
     * @param accepterMap map数据容器
     * @param ctx         ctx上下文o
     */
    private void endCmd(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        RemoteResult<Map<String, Object>> endRemoteResult = inventoryRemoteService.endInventory(getCredentialsVO(ctx), TL_ITEM);
        String errorMess = ErrorConstants.OPERATE_SUCCESS;
        if (!endRemoteResult.isSuccess()) {
            errorMess = endRemoteResult.getResultCode();
        }
        accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ROOT);
        printBeforeNextField(errorMess + ErrorConstants.TIP_TO_CONTINUE, accepterMap, ctx);
    }

    /**
     * 输入的BU校验
     *
     * @param confirmBU   接收到的输入BU
     * @param accepterMap map数据容器
     * @param ctx         ctx上下文
     * @return 校验通过返回true
     */
    private boolean checkInventoryBU(String confirmBU, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        SysSwitchinfo maxBUSwitch = TL_MAXBUSWITCH;
        if (maxBUSwitch == null) {
            colNeedReInput(PROD_OR_EXP_DATE, "盘点最大BU数开关不存在", accepterMap, ctx);
            accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ROOT);//任意键后重新扫描库位
            return false;
        }
        if (!maxBUSwitch.getValue().matches(TipConstants.REG_SERIAL_NO)) {
            colNeedReInput(PROD_OR_EXP_DATE, "盘点最大BU数开关值错误" + maxBUSwitch.getValue(), accepterMap, ctx);
            accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ROOT);//任意键后重新扫描库位
            return false;
        }

        //需要输入0或大于0的正整数
        if (!confirmBU.matches(TipConstants.REG_ZERO_OR_POSITIVE) || confirmBU.length() > 9) {
            colNeedReInput(CONFIRM_BU, ErrorConstants.ILLEGAL_DATA, accepterMap, ctx);
            return false;
        }
        Integer inventoryBU = Integer.parseInt(confirmBU);
        Integer maxBU = Integer.parseInt(maxBUSwitch.getValue());

        if (inventoryBU.compareTo(maxBU) == 1) {
            colNeedReInput(CONFIRM_BU, "超过盘点最大BU数", accepterMap, ctx);
            return false;
        }
        return true;
    }

    /**
     * 计算生产、失效日期，根据输入的日期、商品是否已经登记、开关值判断下一步的接收
     *
     * @param inventoryMP 数据接收domain
     * @param accepterMap map数据容器
     * @param ctx         ctx上下文
     */
    private void countProdAndExpDate(InventoryMP inventoryMP, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        String prodOrExpDate = inventoryMP.getProdOrExpDate();
        BaseGoodsinfo goodsInfo = TL_GOODS_INFO;
        Integer keepdays = goodsInfo.getKeepdays();//商品保质期

        Date productionDate;//生产日期
        Date expirationDate;//失效期
        String datetype = inventoryMP.getDateType();
        if (!Constants.batchRuleEnum.xiHua.dateType.equals(datetype)) {
            productionDate = DateTimeUtil.parseSimpleStr(prodOrExpDate);
            expirationDate = DateTimeUtil.modifyDate(productionDate, keepdays);
        } else {
            expirationDate = DateTimeUtil.parseSimpleStr(prodOrExpDate);
            productionDate = DateTimeUtil.modifyDate(expirationDate, keepdays * -1);
        }
        inventoryMP.setProductionDate(productionDate);
        inventoryMP.setExpirationDate(expirationDate);


        //生产日期需要小于等于当前日期
        Boolean proBiggerNow = DateTimeUtil.compareDate(inventoryMP.getProductionDate(), new Date());
        if (proBiggerNow) {
            super.colNeedReInput(PROD_OR_EXP_DATE, ErrorConstants.PRO_BIGGER_NOW, accepterMap, ctx);
            return;
        }

        SysSwitchinfo minProSwitch = TL_MINPROSWITCH;
        if (minProSwitch == null) {
            colNeedReInput(PROD_OR_EXP_DATE, "盘点最小生产日期开关不存在", accepterMap, ctx);
            accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ROOT);//任意键后重新扫描库位
            return;
        }
        Date minProDate = DateTimeUtil.parseToDateWithShort(minProSwitch.getValue());
        if (minProDate == null) {
            colNeedReInput(PROD_OR_EXP_DATE, "盘点最小生产日期开关值错误" + minProSwitch.getValue(), accepterMap, ctx);
            accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ROOT);//任意键后重新扫描库位
            return;
        }

        SysSwitchinfo maxDiffDaySwitch = TL_MAXDIFFDAYSWITCH;
        if (maxDiffDaySwitch == null) {
            colNeedReInput(PROD_OR_EXP_DATE, "盘点日期差值开关不存在", accepterMap, ctx);
            accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ROOT);//任意键后重新扫描库位
            return;
        }
        if (!maxDiffDaySwitch.getValue().matches(TipConstants.REG_NUM)) {
            colNeedReInput(PROD_OR_EXP_DATE, "盘点日期差值开关值不正确" + maxDiffDaySwitch.getValue(), accepterMap, ctx);
            accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ROOT);//任意键后重新扫描库位
            return;
        }

        //如果输入的日期小于开关中的值，提示可输入的最小生产或失效日期，并重新输入
        String minDateTip = DateTimeUtil.getStringSimple(minProDate);
        if (Constants.batchRuleEnum.xiHua.dateType.equals(datetype)) {
            minDateTip = DateTimeUtil.getStringSimple(DateTimeUtil.modifyDate(minProDate, keepdays));
        }
        if (productionDate.compareTo(minProDate) == -1) {
            colNeedReInput(PROD_OR_EXP_DATE, "最小日期：" + minDateTip, accepterMap, ctx);
            return;
        }

        Date sysProductionDate = TL_SYS_PRODUCTION;
        BigInteger bigInteger = new BigInteger(maxDiffDaySwitch.getValue());
        //如果输入的生产日期与系统生产日期的差值的绝对值大于  日期差值开关 的值，则提示是否继续
        Integer differDays = DateTimeUtil.daysOfTwo(sysProductionDate, productionDate);
        if (Math.abs(differDays) > bigInteger.intValue()) {
            setColSwitchList(TOO_BIG_THAN_SYS_DATE, CollectionUtil.newList(TipConstants.YES, TipConstants.NO), accepterMap, ctx);
        } else {
            //如果在差值范围内则到输入BU的步骤
            resetCurCol(CONFIRM_BU, accepterMap, ctx);
        }
    }

    /**
     * 根据查询到的商品批次规则确定显示的日期类型
     *
     * @param accepterMap map数据容器
     * @param ctx         ctx上下文
     */
    private void changeDateTypeTip(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        BaseGoodsinfo goodsInfo = TL_GOODS_INFO;
        String DateTypeTip = Constants.batchRuleEnum.getDateTypeByCode(goodsInfo.getBatchrule());
        //changeFieldTip(PROD_OR_EXP_DATE, DateTypeTip, accepterMap, ctx);
        super.setNextColSwitchList(DateTypeTip,dateTypeList, accepterMap, ctx);
    }

    /**
     * 判断是否需要显示说输入日期，主单盘生产日期且商品批次规则不是百货批次
     *
     * @param isByProductionDate 主单数据是否盘生产日期
     * @param batchRule          商品批次规则
     * @return 返回true则显示生产日期或需要输入日期
     */
    private boolean isNeedDate(Integer isByProductionDate, Integer batchRule) {
        return !Constants.batchRuleEnum.baiHuo.code.equals(batchRule) && WmsConstants.INVENTORY_ISBYPROD_Y.equals(isByProductionDate);
    }


    /**
     * 明盘情况下，显示商品的条码、名称、保质期、日期类型、库存BU
     *
     * @param resultMap   包含查询的可登记数据（关联商品数据）,sum（库存数）及库存数对应的生产、失效日期.
     * @param accepterMap 数据容器
     * @param ctx         ctx上下文
     */
    private void tipGoodsInfoForMp(Map<String, Object> resultMap, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        InventoryInfo inventoryInfo = TL_INFO;
        //可操作的登记明细，包含商品条码、名称、保质期
        InventoryRegistItem registItem = (InventoryRegistItem) resultMap.get(WmsConstants.KEY_INVENTORY_REGIST);
        TL_REGISTER = registItem;//储存当前显示的登记数据
        List<String> tipList = CollectionUtil.newList(TipConstants.GOODSINFO_BAR_CODE_LABEL, registItem.getSkuname(), TipConstants.GOODSINFO_KEEPDAYS_LABEL);
        List<String> valueList = CollectionUtil.newList(registItem.getBarcode(), "", String.valueOf(registItem.getKeepdays()));
        //盘生产日期，则需要根据批次规则显示不同的日期类型
        if (isNeedDate(inventoryInfo.getIsbyproductiondate(), registItem.getBatchrule())) {
            if (Constants.batchRuleEnum.xiHua.code.equals(registItem.getBatchrule())) {
                //洗化批次规则显示失效日期
                tipList.add(Constants.batchRuleEnum.xiHua.dateType);
                String expirDate = DateTimeUtil.getStringWithSeparator((Date) resultMap.get(WmsConstants.KEY_EXPIRATIONDATE_PARAM));
                valueList.add(expirDate);
            } else {
                //普通批次规则显示生产日期
                tipList.add(Constants.batchRuleEnum.puTong.dateType);
                String proDate = DateTimeUtil.getStringWithSeparator((Date) resultMap.get(WmsConstants.KEY_PRODUCTIONDATE_PARAM));
                valueList.add(proDate);
            }
        }
        tipList.add(TipConstants.INVENTORY_SUM_STOCK_NUM);
        valueList.add(String.valueOf(resultMap.get(WmsConstants.KEY_INVENTORY_SUM_STOCKNUM)));

        TL_SYS_BARCODE = registItem.getBarcode();//查询到的当前显示的商品条码
        TL_SYS_PRODUCTION = (Date) resultMap.get(WmsConstants.KEY_PRODUCTIONDATE_PARAM);//查询到的系统生产日期
        TL_SYS_EXPIRATION = (Date) resultMap.get(WmsConstants.KEY_EXPIRATIONDATE_PARAM);//查询到的系统失效日期
        TL_SYS_STOCK_NUM = (Integer) resultMap.get(WmsConstants.KEY_INVENTORY_SUM_STOCKNUM);//查询到的系统库存数量BU

        printBeforeNextField(tipList, valueList, accepterMap, ctx);
    }

    /**
     * 将库位、盘点主单、盘点明细、三个开关的数据存到ThreadLocal中
     *
     * @param resultMap 查询的的数据集合
     */
    private void initBaseData(Map<String, Object> resultMap) {
        BaseWarehouseinfo wareHouseInfo = (BaseWarehouseinfo) resultMap.get(WmsConstants.KEY_BASEWAREHOUSEINFO_PARAM);
        InventoryInfo inventoryInfo = (InventoryInfo) resultMap.get(WmsConstants.KEY_INVENTORY_INFO);
        InventoryItem inventoryItem = (InventoryItem) resultMap.get(WmsConstants.KEY_INVENTORY_ITEM);
        //库位、盘点主单、明细数据
        TL_WARE_HOUSE_INFO = wareHouseInfo;
        TL_INFO = inventoryInfo;
        TL_ITEM = inventoryItem;
        //三个开关值，备用
        SysSwitchinfo minProSwitch = (SysSwitchinfo) resultMap.get(WmsConstants.SWITCH_INFO_INVENTOR_MINPRODUCTION);
        SysSwitchinfo maxBUSwitch = (SysSwitchinfo) resultMap.get(WmsConstants.SWITCH_INFO_INVENTOR_MAXBU);
        SysSwitchinfo maxDiffDaySwitch = (SysSwitchinfo) resultMap.get(WmsConstants.SWITCH_INFO_INVENTOR_MAXDIFFDAY);
        TL_MINPROSWITCH = minProSwitch;//盘点最小生产日期
        TL_MAXBUSWITCH = maxBUSwitch;//盘点最大数量BU
        TL_MAXDIFFDAYSWITCH = maxDiffDaySwitch;//盘点日期差值提示开关
    }

    /**
     * 初始化数据接收工具，显示已经接收到的库位编码
     *
     * @param ctx handler上下文
     * @throws Exception 抛出异常
     */
    private void tipWhCode(ChannelHandlerContext ctx) throws Exception {
        super.initBaseMap(InventoryMP.class, TipConstants.INVENTORY_PAGEHEADER, ctx);
        channelRead(ctx, TL_WARE_HOUSE_INFO.getWarehousecode() + Constants.BREAK_LINE);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
