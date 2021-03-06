package com.womai.wms.rf.manager.window.inventory;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.DefaultKey;
import com.womai.wms.rf.common.constants.ErrorConstants;
import com.womai.wms.rf.common.constants.TipConstants;
import com.womai.wms.rf.common.util.CollectionUtil;
import com.womai.wms.rf.common.util.DateTimeUtil;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.domain.inventory.InventoryMPSurplus;
import com.womai.wms.rf.manager.util.ReceiveManager;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * ClassDescribe:明盘选择存在多余实物的业务处理
 * Author :zhangwei
 * Date: 2016-11-07
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component(TipConstants.INVENTORY_URL_MP_SURPLUS_MANAGER)
public class InventoryManagerMPSurplusImpl extends ReceiveManager {
    @Autowired
    private InventoryRemoteService inventoryRemoteService;

    private final static String GO_TO_FLAG = "inventoryMP_goto";//跳转标志
    private final static Integer TO_CHANNEL_ROOT = -1;//跳转回到扫描库位的Handler
    private final static Integer TO_WH_CODE = 0;//跳转标志


    private final static String RECEIVED_WH_CODE = "receivedWhCode";//用于显示已经接收到的库位编码
    private final static String CONFIRM_BARCODE = "confirmBarCode";//选择存在差异时再次输入商品条码
    private final static String IS_COVER = "isCover";//提示是否覆盖，如果是混放库位则提示：已存在该日期，是否覆盖
    private final static String PROD_OR_EXP_DATE = "prodOrExpDate";//确认日期，根据批次规则的可能为生产日期或失效日期
    private final static String ADD_OR_COVER = "addOrCover";//会根据不同的情况修改此字段的提示
    private final static String CONFIRM_BU = "confirmBU";//盘点BU数


    private BaseWarehouseinfo TL_WARE_HOUSE_INFO;//当前库位
    private InventoryInfo TL_INFO;//盘点主单
    private InventoryItem TL_ITEM;//盘点明细

    private SysSwitchinfo TL_MINPROSWITCH;//盘点最小生产日期
    private SysSwitchinfo TL_MAXBUSWITCH;//盘点最大数量BU
    private SysSwitchinfo TL_MAXDIFFDAYSWITCH;//盘点日期差值提示开关

    private BaseGoodsinfo TL_GOODS_INFO;//根据商品条码查询到的商品数据
    //已经登记的本库位、商品的数据
    private List<InventoryRegistItem> TL_REGISTER_LIST = new ArrayList<InventoryRegistItem>();
    private Integer TL_OPERATE_TYPE = 0;//操作类型，新增、更新、覆盖

    private final static List<String> YN_LIST = CollectionUtil.newList(TipConstants.NO, TipConstants.YES);//通用的是、否选择，默认显示否
    private List<String> dateTypeList = CollectionUtil.newList(Constants.batchRuleEnum.puTong.dateType, Constants.batchRuleEnum.xiHua.dateType);//日期类型列表
    private final static String DATE_TYPE = "dateType";//日期类型，选择生产日期或失效期

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InventoryParamManagerImpl inventoryParamManager = (InventoryParamManagerImpl) ctx.pipeline().get(TipConstants.INVENTORY_PARAM_MANAGER);
        //初始化查询到的基础数据
        initBaseData(inventoryParamManager.getBaseDataMap());
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
        InventoryMPSurplus inventoryMPSurplus = (InventoryMPSurplus) accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (RECEIVED_WH_CODE.equals(lastCompleteColName)) {
            //显示库位并自动回车后，显示选择的存在多余实物
            printBeforeNextField(TipConstants.INVENTORY_TIP_SURPLUS + TipConstants.YES, accepterMap, ctx);
            resetCurCol(CONFIRM_BARCODE, accepterMap, ctx);
        }
        if (CONFIRM_BARCODE.equals(lastCompleteColName)) {
            String confirmBarcode = inventoryMPSurplus.getConfirmBarCode();
            if (TipConstants.END_CMD.equals(confirmBarcode)) {
                //商品条码处输入00，直接结束本库位盘点
                endCmd(accepterMap, ctx);
            } else {
                RemoteResult<Map<String, Object>> remoteResult = inventoryRemoteService.getGoodsByBarCodeForMP(getCredentialsVO(ctx), confirmBarcode, TL_ITEM);
                if (remoteResult.isSuccess()) {
                    Map<String, Object> resultMap = remoteResult.getT();
                    List<InventoryRegistItem> registerItemList = (List<InventoryRegistItem>) resultMap.get(WmsConstants.KEY_INVENTORY_SKU_IN_REGISTER);
                    //如果此商品存在于登记过的数据中
                    if (registerItemList != null && registerItemList.size() > 0) {
                        TL_REGISTER_LIST = registerItemList;
                    }
                    //存储商品数据，需要批次规则、保质期数据
                    BaseGoodsinfo goodsInfo = (BaseGoodsinfo) resultMap.get(WmsConstants.KEY_BASEGOODSINFO_PARAM);
                    TL_GOODS_INFO = goodsInfo;
                    //输出商品名称、保质期
                    List<String> tipList = CollectionUtil.newList(goodsInfo.getSkuname(), TipConstants.GOODSINFO_KEEPDAYS_LABEL);
                    List<String> valList = CollectionUtil.newList("", String.valueOf(goodsInfo.getKeepdays()));
                    printBeforeNextField(tipList, valList, accepterMap, ctx);
                    //如果是百货批次且已经存在于登记表中，则直接输入BU然后提示是否覆盖，其它情况都需要输入日期
                    if (Constants.batchRuleEnum.baiHuo.code.equals(goodsInfo.getBatchrule()) && registerItemList.size() > 0) {
                        resetCurCol(CONFIRM_BU, accepterMap, ctx);
                    } else {
                        //resetCurCol(DATE_TYPE, accepterMap, ctx);
                        changeDateTypeTip(accepterMap, ctx);
                    }
                } else {
                    colNeedReInput(CONFIRM_BARCODE, remoteResult.getResultCode(), accepterMap, ctx);
                }
            }
        }
        if (DATE_TYPE.equalsIgnoreCase(lastCompleteColName)) {
            String dateType = inventoryMPSurplus.getDateType();
            if (!dateTypeList.contains(dateType)) {
                setColReSwitchList(dateTypeList, ErrorConstants.DATE_TYPE_NOT_IN_LIST, accepterMap, ctx);
            } else {
                resetCurCol(PROD_OR_EXP_DATE, accepterMap, ctx);
            }
        }
        if (PROD_OR_EXP_DATE.equals(lastCompleteColName)) {
            String prodOrExpDate = inventoryMPSurplus.getProdOrExpDate();
            Boolean dateReg = DateTimeUtil.isSimpleDate(prodOrExpDate);//校验是否符合yyyyMMdd日期格式
            if (dateReg) {
                //计算生产、失效日期，根据输入的日期、商品是否已经登记、开关值判断下一步的接收
                countProdAndExpDate(inventoryMPSurplus, accepterMap, ctx);
            } else {
                //changeDateTypeTip(accepterMap, ctx);
                colNeedReInput(PROD_OR_EXP_DATE, ErrorConstants.ILLEGAL_DATE_PATTERN, accepterMap, ctx);
            }
        }

        if (IS_COVER.equals(lastCompleteColName)) {
            String isCover = inventoryMPSurplus.getIsCover();
            if (TipConstants.NO.equals(isCover)) {
                //选择否重新输入条码
                tipWhCode(ctx);
            } else {
//                TL_OPERATE_TYPE = WmsConstants.INVENTORY_REGISTER_UPDATE;
                TL_OPERATE_TYPE = WmsConstants.INVENTORY_REGISTER_COVER;
                //构造本次输入的登记数据参数
                InventoryRegistItem registerParam = getRegisterParam(inventoryMPSurplus);
                //提交登记数据
                confirmRegister(registerParam, accepterMap, ctx);
            }
        }

        if (ADD_OR_COVER.equals(lastCompleteColName)) {
            String addOrCover = inventoryMPSurplus.getAddOrCover();
            //输入1是新增，输入2是覆盖
            if (TipConstants.INVENTORY_TIP_OPER_ADD.equals(addOrCover)) {
                TL_OPERATE_TYPE = WmsConstants.INVENTORY_REGISTER_ADD;
            } else if (TipConstants.INVENTORY_TIP_OPER_COVER.equals(addOrCover)) {
                TL_OPERATE_TYPE = WmsConstants.INVENTORY_REGISTER_COVER;
            }else{
                colNeedReInput(ADD_OR_COVER, "请输入1或2", accepterMap, ctx);
                return;
            }
            //构造本次输入的登记数据参数
            InventoryRegistItem registerParam = getRegisterParam(inventoryMPSurplus);
            //提交登记数据
            confirmRegister(registerParam, accepterMap, ctx);
        }


        if (CONFIRM_BU.equals(lastCompleteColName)) {
            String confirmBU = inventoryMPSurplus.getConfirmBU();
            if (checkInventoryBU(confirmBU, accepterMap, ctx)) {
                //BU校验通过后，再次校验此商品是否已经登记，如果已经登记则进行提示，根据提示后选择的操作类型， 确定soa端的数据操作方式
                InventoryItem item = TL_ITEM;
                InventoryRegistItem queryItem = new InventoryRegistItem();
                queryItem.setItemid(item.getId());
                queryItem.setWhsid(item.getWhsid());
                queryItem.setSkuid(TL_GOODS_INFO.getSkuid());
                RemoteResult<List<InventoryRegistItem>> remoteResult = inventoryRemoteService.queryRegisterItem(getCredentialsVO(ctx), queryItem);
                if (remoteResult.isSuccess()) {
                    if (checkInputRegisterDate(inventoryMPSurplus, remoteResult.getT(), accepterMap, ctx)) {
                        //构造本次输入的登记数据参数
                        InventoryRegistItem registerParam = getRegisterParam(inventoryMPSurplus);
                        //提交登记数据
                        confirmRegister(registerParam, accepterMap, ctx);
                    }
                } else {
                    colNeedReInput(CONFIRM_BU, remoteResult.getResultCode(), accepterMap, ctx);
                    accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ROOT);//任意键后重新扫描库位
                }
            }
        }
    }

    /**
     * 构造本次需要提交的登记数据
     *
     * @param inventoryMPSurplus 数据接收domain
     * @return 本次提交的register对象
     */
    private InventoryRegistItem getRegisterParam(InventoryMPSurplus inventoryMPSurplus) {
        InventoryRegistItem registerParam = new InventoryRegistItem();
        BaseGoodsinfo goodsInfo = TL_GOODS_INFO;
        registerParam.setSkuid(goodsInfo.getSkuid());
        registerParam.setBatchrule(goodsInfo.getBatchrule());
        //洗化批次存失效
       /* if (Constants.batchRuleEnum.xiHua.code.equals(TL_GOODS_INFO.getBatchrule())) {
            registerParam.setProductiondate(inventoryMPSurplus.getExpirationDate());
        } else {
            registerParam.setProductiondate(inventoryMPSurplus.getProductionDate());
        }*/
        registerParam.setProductiondate(inventoryMPSurplus.getProductionDate());
        registerParam.setExpirationdate(inventoryMPSurplus.getExpirationDate());
        registerParam.setRegisterbu(Integer.parseInt(inventoryMPSurplus.getConfirmBU()));
        registerParam.setKeepdays(goodsInfo.getKeepdays());//后端校验商品保质期是否发生变化
        return registerParam;
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
                errorMess = ErrorConstants.DATA_EXPIRED;
            }
            errorMess = errorMess + ErrorConstants.TIP_TO_CONTINUE;
            accepterMap.put(GO_TO_FLAG, TO_CHANNEL_ROOT);
        }
        printBeforeNextField(errorMess, accepterMap, ctx);
    }


    /**
     * 根据map中的数据控制跳转
     *
     * @param accepterMap map数据容器
     * @param ctx         ctx上下文
     * @return 返回true则在channelRead方法中跳转
     * @throws Exception 抛出异常
     */
    public boolean goToHandler(Map<String, Object> accepterMap, ChannelHandlerContext ctx) throws Exception {
        if (accepterMap.get(GO_TO_FLAG) != null) {
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
     * 校验本次输入的登记商品是否已经登记过，如果登记过则依据不同的条件跳转到不同的输入步骤
     *
     * @param inventoryMPSurplus 接收到的输入数据
     * @param registerItemList   按照itemid、whsid、skuid查询到登记数据
     * @param accepterMap        map数据容器
     * @param ctx                ctx上下文
     * @return 返回true则提交数据，返回false则跳转到了提示的步骤
     */
    private boolean checkInputRegisterDate(InventoryMPSurplus inventoryMPSurplus, List<InventoryRegistItem> registerItemList, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        //操作方式默认为NONE，适用于正常流程的登记操作，如果是已经登记过的数据则分别跳转到不同的提示步骤
        TL_OPERATE_TYPE = WmsConstants.INVENTORY_REGISTER_NONE;
        if (registerItemList == null || registerItemList.size() == 0) {
            return true;
        }
        List<InventoryRegistItem> registeredItemList = new ArrayList<InventoryRegistItem>();
        for (InventoryRegistItem item : registerItemList) {
            if (WmsConstants.INVENTORY_REGIST_STATUS_FINISH.equals(item.getStatus()) || WmsConstants.INVENTORY_REGIST_STATUS_INVENTORYING.equals(item.getStatus())) {
                registeredItemList.add(item);
            }
        }
        //如果不存在已经登记完成的数据
        if (registeredItemList == null || registeredItemList.size() == 0) {
            return true;
        }

        BaseWarehouseinfo wareHouseInfo = TL_WARE_HOUSE_INFO;
        //如果是混放库位
        if(WmsConstants.CON_YES == wareHouseInfo.getIsmixsku()){

            //如果是百货批次且已经存在于登记表中，则直接输入BU然后提示是否覆盖，其它情况都需要输入日期
            if (Constants.batchRuleEnum.baiHuo.code.equals(TL_GOODS_INFO.getBatchrule())){
                //如果混放库位不盘日期，直接提示是否覆盖
                changeFieldTip(IS_COVER, TipConstants.INVENTORY_TIP_COVER, accepterMap, ctx);
                setColSwitchList(IS_COVER, YN_LIST, accepterMap, ctx);
                return false;
            }else{
                //如果混放库位盘日期，则校验本次输入的日期是否登记过，如果登记过则提示是否覆盖，；未登记过则提示新增或覆盖
                Date inputDate = inventoryMPSurplus.getProductionDate();
                if (Constants.batchRuleEnum.xiHua.code.equals(TL_GOODS_INFO.getBatchrule())) {
                    inputDate = inventoryMPSurplus.getExpirationDate();
                }
                boolean sameDateRegistered = false;
                for (InventoryRegistItem registerItem : registeredItemList) {
                    if(registerItem.getProductiondate()!=null){
                        if (inputDate.compareTo(registerItem.getProductiondate()) == 0) {
                            sameDateRegistered = true;
                            break;
                        }
                    }
                }
                //与所输入日期相同的数据已经登记过，提示是否覆盖
                if (sameDateRegistered) {
                    changeFieldTip(IS_COVER, TipConstants.INVENTORY_TIP_MIX_COVER, accepterMap, ctx);
                    setColSwitchList(IS_COVER, YN_LIST, accepterMap, ctx);
                    return false;
                } else {
                    //日期不同的已经登记过，提示新增或覆盖
                    printBeforeNextField(TipConstants.INVENTORY_TIP_ADD_COVER, accepterMap, ctx);
                    resetCurCol(ADD_OR_COVER, accepterMap, ctx);
                    return false;
                }
            }
        }else {
            //如果是非混放库位，不管有没有输入日期，直接提示是否覆盖
            changeFieldTip(IS_COVER, TipConstants.INVENTORY_TIP_COVER, accepterMap, ctx);
            setColSwitchList(IS_COVER, YN_LIST, accepterMap, ctx);
            return false;
        }


//
//        //是否存在多余实物的步骤，一般情况都需要输入生产日期，此处校验主单是否盘日期，是为了与已经等过的数据进行比较
//        //如果主单盘日期且商品不是百货批次规则
//        if (isNeedDate(TL_INFO.getIsbyproductiondate(), TL_GOODS_INFO.getBatchrule())) {
//            Date inputDate = inventoryMPSurplus.getProductionDate();
//            if (Constants.batchRuleEnum.xiHua.code.equals(TL_GOODS_INFO.getBatchrule())) {
//                inputDate = inventoryMPSurplus.getExpirationDate();
//            }
//            boolean sameDateRegistered = false;
//            for (InventoryRegistItem registerItem : registeredItemList) {
//                if (inputDate.compareTo(registerItem.getProductiondate()) == 0) {
//                    sameDateRegistered = true;
//                    break;
//                }
//            }
//            //与所输入日期相同的数据已经登记过，提示是否覆盖
//            if (sameDateRegistered) {
//                changeFieldTip(IS_COVER, TipConstants.INVENTORY_TIP_MIX_COVER, accepterMap, ctx);
//                setColSwitchList(IS_COVER, YN_LIST, accepterMap, ctx);
//                return false;
//            } else {
//                //日期不同的已经登记过，提示新增或覆盖
//                printBeforeNextField(TipConstants.INVENTORY_TIP_ADD_COVER, accepterMap, ctx);
//                resetCurCol(ADD_OR_COVER, accepterMap, ctx);
//                return false;
//            }
//        } else {
//            //主单不盘日期且商品是洗化或普通批次
//            boolean registered = false;
//            for (InventoryRegistItem registerItem : registerItemList) {
//                if (WmsConstants.INVENTORY_REGIST_STATUS_FINISH.equals(registerItem.getStatus()) || WmsConstants.INVENTORY_REGIST_STATUS_INVENTORYING.equals(registerItem.getStatus())) {
//                    registered = true;
//                    break;
//                }
//            }
//            //不盘日期，条码已经登记过，提示是否覆盖
//            if (registered) {
//                changeFieldTip(IS_COVER, TipConstants.INVENTORY_TIP_COVER, accepterMap, ctx);
//                setColSwitchList(IS_COVER, YN_LIST, accepterMap, ctx);
//                return false;
//            }
//        }
//        return true;
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
     * 根据输入的日期计算生产、失效日期
     *
     * @param inventoryMPSurplus 数据接收domain
     * @param accepterMap        map数据容器
     * @param ctx                ctx上下文
     */
    private void countProdAndExpDate(InventoryMPSurplus inventoryMPSurplus, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        String prodOrExpDate = inventoryMPSurplus.getProdOrExpDate();
        BaseGoodsinfo goodsInfo = TL_GOODS_INFO;
        Integer batchRule = goodsInfo.getBatchrule();
        Integer keepdays = goodsInfo.getKeepdays();//商品保质期

        Date productionDate;//生产日期
        Date expirationDate;//失效期
        String datetype = inventoryMPSurplus.getDateType();
        if (!Constants.batchRuleEnum.xiHua.dateType.equals(datetype)) {
            productionDate = DateTimeUtil.parseSimpleStr(prodOrExpDate);
            expirationDate = DateTimeUtil.modifyDate(productionDate, keepdays);
        } else {
            expirationDate = DateTimeUtil.parseSimpleStr(prodOrExpDate);
            productionDate = DateTimeUtil.modifyDate(expirationDate, keepdays * -1);
        }
        inventoryMPSurplus.setProductionDate(productionDate);
        inventoryMPSurplus.setExpirationDate(expirationDate);

        //生产日期需要小于等于当前日期
        Boolean proBiggerNow = DateTimeUtil.compareDate(inventoryMPSurplus.getProductionDate(), new Date());
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
        //如果输入的日期小于开关中的值，提示可输入的最小生产或失效日期，并重新输入
        String minDateTip = DateTimeUtil.getStringSimple(minProDate);
        if (Constants.batchRuleEnum.xiHua.code.equals(batchRule)) {
            minDateTip = DateTimeUtil.getStringSimple(DateTimeUtil.modifyDate(minProDate, keepdays));
        }
        if (productionDate.compareTo(minProDate) == -1) {
            colNeedReInput(PROD_OR_EXP_DATE, "最小日期：" + minDateTip, accepterMap, ctx);
            return;
        }
        //日期校验通过后进入输入BU的步骤
        resetCurCol(CONFIRM_BU, accepterMap, ctx);
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
        super.initBaseMap(InventoryMPSurplus.class, TipConstants.INVENTORY_PAGEHEADER, ctx);
        channelRead(ctx, TL_WARE_HOUSE_INFO.getWarehousecode() + Constants.BREAK_LINE);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
