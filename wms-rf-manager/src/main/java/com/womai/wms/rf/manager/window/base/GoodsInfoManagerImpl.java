package com.womai.wms.rf.manager.window.base;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.CollectionUtil;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.RFUtil;
import com.womai.wms.rf.domain.base.GoodsInfo;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.base.GoodsinfoRemoteService;
import com.womai.wms.rf.remote.base.PackaginginfoRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.base.BaseDictionary;
import com.womai.zlwms.rfsoa.domain.base.BaseGoodsinfo;
import com.womai.zlwms.rfsoa.domain.base.BasePackaginginfo;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 描述: 商品信息维护 Handler
 * User:Qi Xiafei
 * Date: 2016-06-20
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component(TipConstants.GOODSINFO_MANAGER)
public class GoodsInfoManagerImpl extends ReceiveManager {

    @Autowired
    private GoodsinfoRemoteService goodsinfoRemoteService;
    @Autowired
    private PackaginginfoRemoteService packaginginfoRemoteService;
    private ChannelHandlerContext ctx;

    // 放入accepterMap中记录的Map键
    private final static String GOTO = "gotoColName";//标识是否跳转的keyName
    private final static String GOTO_MESS = "gotoMessage";//标识跳转之前的错误提示文本
    private final static String FROM_ANYKEY = "anykeyColName";//标识任意键跳转从哪个字段发起的
    private final static String DEL_LINE = "delLine";//标识删除多少行内容

    // 控制流程的domain的各字段名
    private final static String BAR_CODE = "barcode";//商品信息条形码字段
    private final static String BATCH_RULE = "batchrule";//批次规则字段
    private final static String SHELF_FLAG = "shelfFlag";// 商品上架属性
    private final static String PACKAGELEVEL_TO_MAINTAIN = "packageLevelToMaintain";//要维护的包装级别字段
    private final static String PACKAGE_LEVEL1_LENGTH = "packageLevel1Length";//一级包装长度字段
    private final static String PACKAGE_LEVEL1_WIDTH = "packageLevel1Width";//一级包装宽度字段
    private final static String PACKAGE_LEVEL1_HEIGHT = "packageLevel1Height";//一级包装高度字段
    private final static String PACKAGE_LEVEL1_WEIGHT = "packageLevel1Weight";//一级包装重量字段
    private final static String PACKAGE_LEVEL1_ISMT = "packageLevel1Ismt";//一级包装是否维护码托字段
    private final static String PACKAGE_LEVEL1_STARTYARDNM = "packageLevel1Startyardnm";//一级包装起码数量字段
    private final static String PACKAGE_LEVEL1_TRAYLEVEL = "packageLevel1Traylevel";//一级包装托盘码放层数字段
    private final static String PACKAGE_LEVEL1_ONEYARDNUM = "packageLevel1Oneyardnum";//一级包装单层码托数量字段
    private final static String PACKAGE_LEVEL2_LENGTH = "packageLevel2Length";//二级包装长度字段
    private final static String PACKAGE_LEVEL2_WIDTH = "packageLevel2Width";//二级包装宽度字段
    private final static String PACKAGE_LEVEL2_HEIGHT = "packageLevel2Height";//二级包装高度字段
    private final static String PACKAGE_LEVEL2_WEIGHT = "packageLevel2Weight";//二级包装重量字段
    private final static String PACKAGE_LEVEL2_ISMT = "packageLevel2Ismt";//二级包装是否维护码托字段
    private final static String PACKAGE_LEVEL2_STARTYARDNM = "packageLevel2Startyardnm";//二级包装起码数量字段
    private final static String PACKAGE_LEVEL2_TRAYLEVEL = "packageLevel2Traylevel";//二级包装托盘码放层数字段
    private final static String PACKAGE_LEVEL2_ONEYARDNUM = "packageLevel2Oneyardnum";//二级包装单层码托数量字段

    // 线程本地变量
    private BaseGoodsinfo baseGoodsInfoLocal;//存储数据库中查询到的商品信息
    private BasePackaginginfo basePackagingInfoLevel1;//存储数据库中查询到的商品一级包装信息
    private BasePackaginginfo basePackagingInfoLevel1Condition;//存储数据库中查询到的商品一级包装信息
    private BasePackaginginfo basePackagingInfoLevel2Local;//存储数据库中查询到的商品二级包装信息
    private BasePackaginginfo basePackagingInfoLevel2Condition;//存储数据库中查询到的商品二级包装信息

    // 字段格式正则验证
    private final static String BAR_CODE_REGX = "^[a-zA-Z0-9]{1,20}$"; // 商品条码
    private final static List<String> PACKAGELEVEL_TO_MAINTAIN_REGX = CollectionUtil.newGenericList(TipConstants.GOODSINFO_PACKAGE_LEVEL_ONE, TipConstants.GOODSINFO_PACKAGE_LEVEL_TWO, TipConstants.GOODSINFO_PACKAGE_LEVEL_ADD_TWO); // 选择包装级别
    private final static String LENGTH_WIDTH_HEIGHT_REGX = "^([1-9]\\d*|[1-9]\\d*\\.\\d{0,2}[1-9]|0\\.[0-9]{0,2}[1-9])$";// 长宽高验证
    private final static String CUBAGE_REGX = "^([1-9]\\d*|[1-9]\\d*\\.\\d{0,8}[1-9]|0\\.[0-9]{0,8}[1-9])$";// 体积验证
    private final static String WEIGHT_REGX = "^([1-9]\\d*|[1-9]\\d*\\.\\d{0,5}[1-9]|0\\.[0-9]{0,5}[1-9])$";// 重量
    private final static String YN_REGX = "^[YNyn]$"; // 输入YN的验证
    private final static String STARTYARDNM_REGX = "^[1-9]\\d{0,3}$"; // 起码重量
    private final static String TRAYLEVEL_REGX = "^[1-9]\\d?$"; // 托盘码放层数
    private final static String ONEYARDNUM_REGX = "^[1-9]\\d{0,3}$"; // 单层码托数量
    private List<BaseDictionary> shelfFlagDictList;// 商品上架属性字典列表
    private List<String> shelfFlagDescList;// 商品上架描述属性列表

    // 用户控制左右选择list
    private final static List<String> ynList = CollectionUtil.newGenericList(Constants.CANCEL_N, Constants.CONFIRM_Y);

    public BaseGoodsinfo getBaseGoodsInfoLocal() {
        return baseGoodsInfoLocal;
    }

    public void setBaseGoodsInfoLocal(BaseGoodsinfo baseGoodsInfoLocal) {
        this.baseGoodsInfoLocal = baseGoodsInfoLocal;
    }

    public BasePackaginginfo getBasePackagingInfoLevel1() {
        return basePackagingInfoLevel1;
    }

    public void setBasePackagingInfoLevel1(BasePackaginginfo basePackagingInfoLevel1) {
        this.basePackagingInfoLevel1 = basePackagingInfoLevel1;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 初始化时清空本地变量内容
        resetLocals();
        this.ctx = ctx;
        // 界面标题
        String[] pageHeader = {Constants.BREAK_LINE, TipConstants.GOODSINFO_TITLE, Constants.SPLIT, ""};
        super.initBaseMap(GoodsInfo.class, pageHeader, ctx);

        if (shelfFlagDictList == null || shelfFlagDictList.size() == 0) {
            shelfFlagDictList = goodsinfoRemoteService.getShelfFlagList(getCredentialsVO(ctx));
            shelfFlagDescList = new ArrayList<String>() {{
                for (BaseDictionary value : shelfFlagDictList) {
                    add(value.getBilltypecn());
                }
            }};
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {

        String msg = object.toString();//本次输入
        Map<String, Object> accepterMap = getDataMap();
        if (goToHandler(msg, accepterMap, ctx)) {
            return;
        } // 处理跳转请求
        receiveData(ctx, object, accepterMap); // 接收用户输入的处理
        GoodsInfo goodsInfo = (GoodsInfo) accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);//上一个完成输入的字段

        if (BAR_CODE.equals(lastCompleteColName)) {

            String barcode = goodsInfo.getBarcode();
            if (barcode.matches(BAR_CODE_REGX)) {
                if (refreshGoodsinfo(barcode)) {
                    // 打印一些提示信息
                    // 标签信息列表
                    List<String> tipList = CollectionUtil.newGenericList(TipConstants.GOODSINFO_BAR_CODE_LABEL, TipConstants.GOODSINFO_SKUNAME_LABEL, TipConstants.GOODSINFO_STATUS_LABEL, TipConstants.GOODSINFO_KEEPDAYS_LABEL);
                    // 值信息列表
                    String skuName = baseGoodsInfoLocal.getSkuname();
                    List<String> valueList = CollectionUtil.newGenericList(baseGoodsInfoLocal.getBarcode(), RFUtil.makeStrFitPda(skuName, TipConstants.GOODSINFO_SKUNAME_LABEL, 2), Constants.getStatusLabel(baseGoodsInfoLocal.getStatus()), baseGoodsInfoLocal.getKeepdays().toString());
                    //输出多行自定义内容
                    printBeforeNextField(tipList, valueList, accepterMap, ctx);
                    setNextColSwitchList(getBatchRuleList(), accepterMap, ctx);
                } else {
                    exceptionJump(ErrorConstants.GOODSINFO_BARCODE_ERROR, BAR_CODE, 0, null, accepterMap, ctx);
                    return;
                }
            } else {
                exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, lastCompleteColName, 1, null, accepterMap, ctx);
                return;
            }

        } else if (BATCH_RULE.equals(lastCompleteColName)) {

            String batchrule = goodsInfo.getBatchrule();
            if (Constants.batchRuleEnum.nameExist(batchrule)) {
                //保存对批次规则的修改
                BaseGoodsinfo forUpdate = new BaseGoodsinfo();
                forUpdate.setBatchrule(Constants.batchRuleEnum.getCodeByName(batchrule));// 0 普通批次规则，1 百货批次规则 2 洗化批次
                forUpdate.setSkuid(baseGoodsInfoLocal.getSkuid());
                RemoteResult<Integer> result = goodsinfoRemoteService.updateColBatchRuleBySkuId(this.getCredentialsVO(ctx), forUpdate);
                if (result.isSuccess()) {
                    if (refreshGoodsinfo(null)) {
                        //下一个字段以list切换的形式显示
                        setNextColSwitchList(getShelfFlagList(), accepterMap, ctx);
                    } else {
                        exceptionJump(ErrorConstants.GOODSINFO_BARCODE_ERROR, BAR_CODE, 0, null, accepterMap, ctx);
                        return;
                    }
                } else {
                    exceptionJump(ErrorConstants.SYS_ERROR_CONTINUE, BAR_CODE, 0, null, accepterMap, ctx);
                    return;
                }
            } else {
                // 当前字段以list切换的形式显示,并显示错误提示
                setColReSwitchList(getBatchRuleList(), ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
            }

        } else if (SHELF_FLAG.equals(lastCompleteColName)) {
            String shelfFlag = goodsInfo.getShelfFlag();
            int index = shelfFlagDescList.indexOf(shelfFlag);// 用户选择的商品上架属性在列表中的位置
            if (index >= 0) {
                //保存对商品上架属性的修改
                BaseGoodsinfo forUpdate = new BaseGoodsinfo();
                forUpdate.setShelfflag(Integer.valueOf(shelfFlagDictList.get(index).getBilltypecode()));
                forUpdate.setSkuid(baseGoodsInfoLocal.getSkuid());
                RemoteResult<Integer> result = goodsinfoRemoteService.updateShelfFlagBySkuId(getCredentialsVO(ctx), forUpdate);
                if (result.isSuccess()) {
                    if (refreshPackaginginfoLevel1()) {//modify by wzh 20170307 存在一级，二级则维护。只存在一级，可以维护一级，或者新建二级
                        if (refreshPackaginginfoLevel2()) {
                            setNextColSwitchList(getPackLevelList(WmsConstants.PACK_LEVEL_ONE), accepterMap, ctx);
                            return;
                        } else {
                            setNextColSwitchList(CollectionUtil.newGenericList(TipConstants.GOODSINFO_PACKAGE_LEVEL_ONE, TipConstants.GOODSINFO_PACKAGE_LEVEL_ADD_TWO), accepterMap, ctx);
                        }
                    } else {
                        exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL1_ERROR, BAR_CODE, 0, null, accepterMap, ctx);
                        return;
                    }
                } else {
                    exceptionJump(ErrorConstants.SYS_ERROR_CONTINUE, BAR_CODE, 0, null, accepterMap, ctx);
                    return;
                }
            } else {
                // 当前字段以list切换的形式显示,并显示错误提示
                setColReSwitchList(getShelfFlagList(), ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
            }
        } else if (PACKAGELEVEL_TO_MAINTAIN.equals(lastCompleteColName)) {

            String packageLevelToMaintain = goodsInfo.getPackageLevelToMaintain();
            if (PACKAGELEVEL_TO_MAINTAIN_REGX.contains(packageLevelToMaintain)) {
                if (packageLevelToMaintain.equals(TipConstants.GOODSINFO_PACKAGE_LEVEL_ONE)) {
                    if (refreshPackaginginfoLevel1()) {
                        if (basePackagingInfoLevel1.getUnitname().equals(TipConstants.PK_LEVEL2_NAME)) {
                            exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL1_NAME_ERROR, BAR_CODE, 0, null, accepterMap, ctx);
                            return;
                        }
                        printBeforeLevel1Length(accepterMap, ctx);
                        //设置下一个待接收字段
                        resetCurCol(getNullIsNullLabel(basePackagingInfoLevel1.getLength()), PACKAGE_LEVEL1_LENGTH, accepterMap, ctx);
                    } else {
                        exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL1_ERROR, PACKAGELEVEL_TO_MAINTAIN, 1, PACKAGELEVEL_TO_MAINTAIN, accepterMap, ctx);
                        return;
                    }
                } else if (packageLevelToMaintain.equals(TipConstants.GOODSINFO_PACKAGE_LEVEL_TWO)) {
                    if (refreshPackaginginfoLevel2()) {
                        printBeforeLevel2Length(accepterMap, ctx);
                        resetCurCol(getNullIsNullLabel(basePackagingInfoLevel2Local.getLength()), PACKAGE_LEVEL2_LENGTH, accepterMap, ctx);
                    } else {
                        exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL2_ERROR, PACKAGELEVEL_TO_MAINTAIN, 1, PACKAGELEVEL_TO_MAINTAIN, accepterMap, ctx);
                        return;
                    }
                } else {//新增二级包装
                    if(this.baseGoodsInfoLocal.getStatus().equals(WmsConstants.STATUS_DISABLE)){
                        exceptionJump(ErrorConstants.GOODSINFO_DISABLE_ERROR, PACKAGELEVEL_TO_MAINTAIN, 1, PACKAGELEVEL_TO_MAINTAIN, accepterMap, ctx);
                        return;
                    }
                    GoodsInfoParamManagerImpl goodsInfoParamManager = new GoodsInfoParamManagerImpl();
                    goodsInfoParamManager.setBaseGoodsinfo(this.baseGoodsInfoLocal);
                    goodsInfoParamManager.setBasePackagingInfoLevel1(this.basePackagingInfoLevel1);
                    ctx.pipeline().addAfter(Constants.ENCODE_HANDLER, TipConstants.GOODSINFO_PARAM_MANAGER, goodsInfoParamManager);
                    forward(TipConstants.PACKINGINFO_MANAGER, ctx);
                }
            } else {
                setColReSwitchList(getPackLevelList(WmsConstants.PACK_LEVEL_ONE), ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
            }
        } else if (PACKAGE_LEVEL1_LENGTH.equals(lastCompleteColName)) {

            String lengthStr = goodsInfo.getPackageLevel1Length();
            if (lengthStr.matches(LENGTH_WIDTH_HEIGHT_REGX)) {
                if (refreshPackaginginfoLevel1()) {
                    //设置下一个待接收字段并设置默认值
                    resetCurCol(getNullIsNullLabel(basePackagingInfoLevel1.getWidth()), PACKAGE_LEVEL1_WIDTH, accepterMap, ctx);
                } else {
                    exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL1_ERROR, PACKAGELEVEL_TO_MAINTAIN, 7, PACKAGE_LEVEL1_LENGTH, accepterMap, ctx);
                    return;
                }
            } else {
                exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, lastCompleteColName, 1, null, accepterMap, ctx);
                return;
            }
        } else if (PACKAGE_LEVEL1_WIDTH.equals(lastCompleteColName)) {

            String widthStr = goodsInfo.getPackageLevel1Width();
            if (widthStr.matches(LENGTH_WIDTH_HEIGHT_REGX)) {
                if (refreshPackaginginfoLevel1()) {
                    //设置下一个待接收字段并设置默认值
                    resetCurCol(getNullIsNullLabel(basePackagingInfoLevel1.getHeight()), PACKAGE_LEVEL1_HEIGHT, accepterMap, ctx);
                } else {
                    exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL1_ERROR, PACKAGELEVEL_TO_MAINTAIN, 8, PACKAGE_LEVEL1_WIDTH, accepterMap, ctx);
                    return;
                }
            } else {
                exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, lastCompleteColName, 1, null, accepterMap, ctx);
                return;
            }
        } else if (PACKAGE_LEVEL1_HEIGHT.equals(lastCompleteColName)) {
            //modify by wangzhanhua 长宽高，从大到小排序后，给大边（长），中边（宽），小边（高）
            String heightStr = goodsInfo.getPackageLevel1Height();
            if (heightStr.matches(LENGTH_WIDTH_HEIGHT_REGX)) {
                BigDecimal length = new BigDecimal(goodsInfo.getPackageLevel1Length()); //一级包装长
                BigDecimal width = new BigDecimal(goodsInfo.getPackageLevel1Width());//一级包装宽
                BigDecimal height = new BigDecimal(heightStr);//一级包装高
                BigDecimal[] lens = RFUtil.getLengthOfSide(new BigDecimal[]{length, width, height});

                BigDecimal cubage = length.multiply(width.multiply(height)); //一级包装体积

                if (convertToNormalNumber(cubage.toString()).matches(CUBAGE_REGX)) {
                    if (refreshPackaginginfoLevel2()) {// 如果存在生效的二级包装
                        Integer pknum = basePackagingInfoLevel2Local.getPknum();// 箱规
                        // 若一级包装体积乘以箱规大于二级包装的体积，则不允许提交
                        if ((cubage.multiply(new BigDecimal(pknum))).compareTo(basePackagingInfoLevel2Local.getCubage()) == 1) {
                            exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL1_CUBAGE_ERROR, PACKAGE_LEVEL1_LENGTH, 3, PACKAGE_LEVEL1_HEIGHT, accepterMap, ctx);
                            return;
                        }
                    }
                    BasePackaginginfo forUpdate = new BasePackaginginfo();
                    forUpdate.setLength(lens[2]);//大边
                    forUpdate.setWidth(lens[1]);//中边
                    forUpdate.setHeight(lens[0]);//小边
                    forUpdate.setCubage(cubage);
                    forUpdate.setId(basePackagingInfoLevel1.getId());
                    forUpdate.setSkuid(basePackagingInfoLevel1.getSkuid());
                    //去更新长宽高体积
                    RemoteResult<Integer> result = packaginginfoRemoteService.updateLenWidHeiCubageById(this.getCredentialsVO(ctx), forUpdate);
                    //根据更新的结果处理
                    if (result.isSuccess()) {
                        //更新成功的方法
                        if (refreshPackaginginfoLevel1()) {
                            //设置下一个待接收字段并设置默认值
                            resetCurCol(getNullIsNullLabel(basePackagingInfoLevel1.getWeight()), PACKAGE_LEVEL1_WEIGHT, accepterMap, ctx);
                        } else {
                            exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL1_ERROR, PACKAGELEVEL_TO_MAINTAIN, 9, PACKAGE_LEVEL1_HEIGHT, accepterMap, ctx);
                            return;
                        }
                    } else {
                        exceptionJump(ErrorConstants.SYS_ERROR_CONTINUE, BAR_CODE, 0, null, accepterMap, ctx);
                        return;
                    }
                } else {
                    exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, lastCompleteColName, 1, null, accepterMap, ctx);
                    return;
                }
            } else {
                exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, lastCompleteColName, 1, null, accepterMap, ctx);
                return;
            }
        } else if (PACKAGE_LEVEL1_WEIGHT.equals(lastCompleteColName)) {

            String weightStr = goodsInfo.getPackageLevel1Weight();
            if (weightStr.matches(WEIGHT_REGX)) {
                BigDecimal weight = new BigDecimal(weightStr);
                if (refreshPackaginginfoLevel2()) {
                    Integer pknum = basePackagingInfoLevel2Local.getPknum();
                    // 若一级包装重量乘以箱规大于二级包装，则不允许提交
                    if (weight.multiply(new BigDecimal(pknum)).compareTo(basePackagingInfoLevel2Local.getWeight()) == 1) {
                        exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL1_WEIGHT_ERROR, PACKAGE_LEVEL1_WEIGHT, 1, null, accepterMap, ctx);
                        return;
                    }
                }
                BasePackaginginfo forUpdate = new BasePackaginginfo();
                forUpdate.setWeight(weight);
                forUpdate.setId(basePackagingInfoLevel1.getId());
                forUpdate.setSkuid(basePackagingInfoLevel1.getSkuid());
                forUpdate.setPacklevel(WmsConstants.PACK_LEVEL_ONE);
                // 去更新重量
                RemoteResult<Integer> result = packaginginfoRemoteService.updateWeightById(this.getCredentialsVO(ctx), forUpdate);
                // 根据更新的结果处理
                if (result.isSuccess()) {
                    // 更新成功的方法
                    if (refreshPackaginginfoLevel1()) {
                        if (refreshGoodsinfo(null)) {
                            if (baseGoodsInfoLocal.getIsmix() == WmsConstants.CON_YES) {// 如果是整箱才询问用户是否维护码托，如果不是整箱就不问了一级包装维护完成
                                // 设置下一个待接收字段
                                resetCurCol(PACKAGE_LEVEL1_ISMT, accepterMap, ctx);
                                setNextColSwitchList(basePackagingInfoLevel1.getIsmt() == WmsConstants.CON_NO ? Constants.CANCEL_N : Constants.CONFIRM_Y, ynList, accepterMap, ctx);
                            } else {
                                successJump(TipConstants.GOODSINFO_CONTINUETO_PACKAGE_LEVEL2, PACKAGE_LEVEL2_LENGTH, -1, PACKAGE_LEVEL1_WEIGHT, accepterMap, ctx);
                                return;
                            }
                        } else {
                            exceptionJump(ErrorConstants.GOODSINFO_BARCODE_ERROR, BAR_CODE, 0, null, accepterMap, ctx);
                            return;
                        }
                    } else {
                        exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL1_ERROR, PACKAGELEVEL_TO_MAINTAIN, 10, PACKAGE_LEVEL1_WEIGHT, accepterMap, ctx);
                        return;
                    }
                } else {
                    exceptionJump(ErrorConstants.SYS_ERROR_CONTINUE, BAR_CODE, 0, null, accepterMap, ctx);
                    return;
                }
            } else {
                exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, lastCompleteColName, 1, null, accepterMap, ctx);
                return;
            }
        } else if (PACKAGE_LEVEL1_ISMT.equals(lastCompleteColName)) {
            String ismt = goodsInfo.getPackageLevel1Ismt();
            if (ismt.matches(YN_REGX)) {
                if (Constants.CONFIRM_Y.equals(ismt)) {
                    BasePackaginginfo forUpdate = new BasePackaginginfo();
                    forUpdate.setId(basePackagingInfoLevel1.getId());
                    forUpdate.setIsmt(ismt.equals(Constants.CONFIRM_Y) ? WmsConstants.IS_TRANSFER_Y : WmsConstants.IS_TRANSFER_N);
                    RemoteResult<Integer> result = packaginginfoRemoteService.updateIsMtById(this.getCredentialsVO(ctx), forUpdate);
                    if (result.isSuccess()) {
                        if (refreshPackaginginfoLevel1()) {
                            //设置下一个待接收字段并设置默认值
                            resetCurCol(getNullIsNullLabel(basePackagingInfoLevel1.getStartyardnm()), PACKAGE_LEVEL1_STARTYARDNM, accepterMap, ctx);
                        } else {
                            exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL1_ERROR, PACKAGELEVEL_TO_MAINTAIN, 11, PACKAGE_LEVEL1_ISMT, accepterMap, ctx);
                            return;
                        }
                    } else {
                        exceptionJump(ErrorConstants.SYS_ERROR_CONTINUE, BAR_CODE, 0, null, accepterMap, ctx);
                        return;
                    }
                } else {
                    BasePackaginginfo forUpdate = new BasePackaginginfo();
                    forUpdate.setId(basePackagingInfoLevel1.getId());
                    forUpdate.setIsmt(ismt.equals(Constants.CONFIRM_Y) ? WmsConstants.IS_TRANSFER_Y : WmsConstants.IS_TRANSFER_N);
                    RemoteResult<Integer> result = packaginginfoRemoteService.updateIsMtById(this.getCredentialsVO(ctx), forUpdate);
                    if (result.isSuccess()) {
                        exceptionJump(TipConstants.GOODSINFO_CONTINUETO_PACKAGE_LEVEL2, PACKAGE_LEVEL2_LENGTH, -1, PACKAGE_LEVEL1_ISMT, accepterMap, ctx);
                        return;
                    } else {
                        exceptionJump(ErrorConstants.SYS_ERROR_CONTINUE, BAR_CODE, 0, null, accepterMap, ctx);
                        return;
                    }
                }
            } else {
                exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, lastCompleteColName, 1, null, accepterMap, ctx);
                return;
            }
        } else if (PACKAGE_LEVEL1_STARTYARDNM.equals(lastCompleteColName)) {
            String colValue = goodsInfo.getPackageLevel1Startyardnm();
            if (colValue.matches(STARTYARDNM_REGX)) {
                BasePackaginginfo forUpdate = new BasePackaginginfo();
                forUpdate.setId(basePackagingInfoLevel1.getId());
                forUpdate.setStartyardnm(Integer.parseInt(colValue));
                RemoteResult<Integer> result = packaginginfoRemoteService.updateStartyardnmById(this.getCredentialsVO(ctx), forUpdate);
                if (result.isSuccess()) {
                    if (refreshPackaginginfoLevel1()) {
                        //设置下一个待接收字段并设置默认值
                        resetCurCol(getNullIsNullLabel(basePackagingInfoLevel1.getTraylevel()), PACKAGE_LEVEL1_TRAYLEVEL, accepterMap, ctx);
                    } else {
                        exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL1_ERROR, PACKAGELEVEL_TO_MAINTAIN, 12, PACKAGE_LEVEL1_STARTYARDNM, accepterMap, ctx);
                        return;
                    }
                } else {
                    exceptionJump(ErrorConstants.SYS_ERROR_CONTINUE, BAR_CODE, 0, null, accepterMap, ctx);
                    return;
                }
            } else {
                exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, lastCompleteColName, 1, null, accepterMap, ctx);
                return;
            }
        } else if (PACKAGE_LEVEL1_TRAYLEVEL.equals(lastCompleteColName)) {
            String colValue = goodsInfo.getPackageLevel1Traylevel();
            if (colValue.matches(TRAYLEVEL_REGX)) {
                BasePackaginginfo forUpdate = new BasePackaginginfo();
                forUpdate.setId(basePackagingInfoLevel1.getId());
                forUpdate.setTraylevel(Integer.parseInt(colValue));
                RemoteResult<Integer> result = packaginginfoRemoteService.updateTraylevelById(this.getCredentialsVO(ctx), forUpdate);
                if (result.isSuccess()) {
                    if (refreshPackaginginfoLevel1()) {
                        //设置下一个待接收字段并设置默认值
                        resetCurCol(getNullIsNullLabel(basePackagingInfoLevel1.getOneyardnum()), PACKAGE_LEVEL1_ONEYARDNUM, accepterMap, ctx);
                    } else {
                        exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL1_ERROR, PACKAGELEVEL_TO_MAINTAIN, 13, PACKAGE_LEVEL1_TRAYLEVEL, accepterMap, ctx);
                        return;
                    }
                } else {
                    exceptionJump(ErrorConstants.SYS_ERROR_CONTINUE, BAR_CODE, 0, null, accepterMap, ctx);
                    return;
                }
            } else {
                exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, lastCompleteColName, 1, null, accepterMap, ctx);
                return;
            }
        } else if (PACKAGE_LEVEL1_ONEYARDNUM.equals(lastCompleteColName)) {
            String colValue = goodsInfo.getPackageLevel1Oneyardnum();
            if (colValue.matches(ONEYARDNUM_REGX)) {
                BasePackaginginfo forUpdate = new BasePackaginginfo();
                forUpdate.setId(basePackagingInfoLevel1.getId());
                forUpdate.setOneyardnum(Integer.parseInt(colValue));
                RemoteResult<Integer> result = packaginginfoRemoteService.updateOneyardnumById(this.getCredentialsVO(ctx), forUpdate);
                if (result.isSuccess()) {
                    if (refreshPackaginginfoLevel1()) {
                        exceptionJump(TipConstants.GOODSINFO_CONTINUETO_PACKAGE_LEVEL2, PACKAGE_LEVEL2_LENGTH, -1, PACKAGE_LEVEL1_ONEYARDNUM, accepterMap, ctx);
                        return;
                    } else {
                        exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL1_ERROR, PACKAGELEVEL_TO_MAINTAIN, 14, PACKAGE_LEVEL1_ONEYARDNUM, accepterMap, ctx);
                        return;
                    }
                } else {
                    exceptionJump(ErrorConstants.SYS_ERROR_CONTINUE, BAR_CODE, 0, null, accepterMap, ctx);
                    return;
                }
            } else {
                exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, lastCompleteColName, 1, null, accepterMap, ctx);
                return;
            }
        } else if (PACKAGE_LEVEL2_LENGTH.equals(lastCompleteColName)) {
            String lengthStr = goodsInfo.getPackageLevel2Length();
            if (lengthStr.matches(LENGTH_WIDTH_HEIGHT_REGX)) {
                if (refreshPackaginginfoLevel2()) {
                    //设置下一个待接收字段并设置默认值
                    resetCurCol(getNullIsNullLabel(basePackagingInfoLevel2Local.getWidth()), PACKAGE_LEVEL2_WIDTH, accepterMap, ctx);
                } else {
                    exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL2_ERROR, PACKAGELEVEL_TO_MAINTAIN, 3, null, accepterMap, ctx);
                    return;
                }
            } else {
                exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, lastCompleteColName, 1, null, accepterMap, ctx);
                return;
            }
        } else if (PACKAGE_LEVEL2_WIDTH.equals(lastCompleteColName)) {
            String widthStr = goodsInfo.getPackageLevel2Width();
            if (widthStr.matches(LENGTH_WIDTH_HEIGHT_REGX)) {
                if (refreshPackaginginfoLevel2()) {
                    //设置下一个待接收字段并设置默认值
                    resetCurCol(getNullIsNullLabel(basePackagingInfoLevel2Local.getHeight()), PACKAGE_LEVEL2_HEIGHT, accepterMap, ctx);
                } else {
                    exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL2_ERROR, PACKAGELEVEL_TO_MAINTAIN, 4, null, accepterMap, ctx);
                    return;
                }
            } else {
                exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, lastCompleteColName, 1, null, accepterMap, ctx);
                return;
            }
        } else if (PACKAGE_LEVEL2_HEIGHT.equals(lastCompleteColName)) {
            String heightStr = goodsInfo.getPackageLevel2Height();
            if (heightStr.matches(LENGTH_WIDTH_HEIGHT_REGX)) {
                BigDecimal length = new BigDecimal(goodsInfo.getPackageLevel2Length()); //二级包装长
                BigDecimal width = new BigDecimal(goodsInfo.getPackageLevel2Width());//二级包装宽
                BigDecimal height = new BigDecimal(heightStr);//二级包装高
                BigDecimal[] lengthOfSide = RFUtil.getLengthOfSide(new BigDecimal[]{length, width, height});
                BigDecimal cubage = length.multiply(width.multiply(height)); //二级包装体积
                if (convertToNormalNumber(cubage.toString()).matches(CUBAGE_REGX)) {
                    if (refreshPackaginginfoLevel1()) {
                        Integer pknum = basePackagingInfoLevel2Local.getPknum();
                        // 若一级包装体积乘以箱规大于二级包装体积，则提交失败
                        if (((basePackagingInfoLevel1.getCubage()).multiply(new BigDecimal(pknum))).compareTo(cubage) == 1) {
                            exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL2_CUBAGE_ERROR, PACKAGE_LEVEL2_LENGTH, 3, null, accepterMap, ctx);
                            return;
                        }
                    }
                    BasePackaginginfo forUpdate = new BasePackaginginfo();
                    forUpdate.setLength(lengthOfSide[2]);
                    forUpdate.setWidth(lengthOfSide[1]);
                    forUpdate.setHeight(lengthOfSide[0]);
                    forUpdate.setCubage(cubage);
                    forUpdate.setId(basePackagingInfoLevel2Local.getId());
                    forUpdate.setSkuid(basePackagingInfoLevel2Local.getSkuid());
                    //去更新长宽高体积
                    RemoteResult<Integer> result = packaginginfoRemoteService.updateLenWidHeiCubageById(this.getCredentialsVO(ctx), forUpdate);
                    //根据更新的结果处理
                    if (result.isSuccess()) {
                        //更新成功的方法
                        if (refreshPackaginginfoLevel2()) {
                            //设置下一个待接收字段并设置默认值
                            resetCurCol(getNullIsNullLabel(basePackagingInfoLevel2Local.getWeight()), PACKAGE_LEVEL2_WEIGHT, accepterMap, ctx);
                        } else {
                            exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL2_ERROR, PACKAGELEVEL_TO_MAINTAIN, 5, null, accepterMap, ctx);
                            return;
                        }
                    } else {
                        exceptionJump(ErrorConstants.SYS_ERROR_CONTINUE, BAR_CODE, 0, null, accepterMap, ctx);
                        return;
                    }
                } else {
                    exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, lastCompleteColName, 1, null, accepterMap, ctx);
                    return;
                }
            } else {
                exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, lastCompleteColName, 1, null, accepterMap, ctx);
                return;
            }
        } else if (PACKAGE_LEVEL2_WEIGHT.equals(lastCompleteColName)) {
            String weightStr = goodsInfo.getPackageLevel2Weight();
            if (weightStr.matches(WEIGHT_REGX)) {
                BigDecimal weight = new BigDecimal(weightStr);
                if (refreshPackaginginfoLevel1()) {
                    Integer pknum = basePackagingInfoLevel2Local.getPknum();
                    // 若一级包装重量乘以箱规大于二级包装重量，则提交失败
                    if (basePackagingInfoLevel1.getWeight().multiply(new BigDecimal(pknum)).compareTo(weight) == 1) {
                        exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL2_WEIGHT_ERROR, PACKAGE_LEVEL2_WEIGHT, 1, null, accepterMap, ctx);
                        return;
                    }
                }
                BasePackaginginfo forUpdate = new BasePackaginginfo();
                forUpdate.setWeight(weight);
                forUpdate.setId(basePackagingInfoLevel2Local.getId());
                forUpdate.setSkuid(basePackagingInfoLevel2Local.getSkuid());
                forUpdate.setPacklevel(WmsConstants.PACK_LEVEL_TWO);
                // 去更新重量
                RemoteResult<Integer> result = packaginginfoRemoteService.updateWeightById(this.getCredentialsVO(ctx), forUpdate);
                // 根据更新的结果处理
                if (result.isSuccess()) {
                    // 更新成功的方法
                    if (refreshPackaginginfoLevel2()) {
                        // 设置下一个待接收字段
                        resetCurCol(PACKAGE_LEVEL2_ISMT, accepterMap, ctx);
                        setNextColSwitchList(basePackagingInfoLevel2Local.getIsmt() == WmsConstants.CON_NO ? Constants.CANCEL_N : Constants.CONFIRM_Y, ynList, accepterMap, ctx);
                    } else {
                        exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL2_ERROR, PACKAGELEVEL_TO_MAINTAIN, 6, null, accepterMap, ctx);
                        return;
                    }
                } else {
                    exceptionJump(ErrorConstants.SYS_ERROR_CONTINUE, BAR_CODE, 0, null, accepterMap, ctx);
                    return;
                }
            } else {
                exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, PACKAGE_LEVEL2_WEIGHT, 1, null, accepterMap, ctx);
                return;
            }
        } else if (PACKAGE_LEVEL2_ISMT.equals(lastCompleteColName)) {
            String ismt = goodsInfo.getPackageLevel2Ismt();
            if (ismt.matches(YN_REGX)) {
                if (Constants.CONFIRM_Y.equals(ismt)) {
                    BasePackaginginfo forUpdate = new BasePackaginginfo();
                    forUpdate.setId(basePackagingInfoLevel2Local.getId());
                    forUpdate.setIsmt(ismt.equals(Constants.CONFIRM_Y) ? WmsConstants.IS_TRANSFER_Y : WmsConstants.IS_TRANSFER_N);
                    RemoteResult<Integer> result = packaginginfoRemoteService.updateIsMtById(this.getCredentialsVO(ctx), forUpdate);
                    if (result.isSuccess()) {
                        if (refreshPackaginginfoLevel2()) {
                            //设置下一个待接收字段并设置默认值
                            resetCurCol(getNullIsNullLabel(basePackagingInfoLevel2Local.getStartyardnm()), PACKAGE_LEVEL2_STARTYARDNM, accepterMap, ctx);
                        } else {
                            exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL2_ERROR, PACKAGELEVEL_TO_MAINTAIN, 7, null, accepterMap, ctx);
                            return;
                        }
                    } else {
                        exceptionJump(ErrorConstants.SYS_ERROR_CONTINUE, BAR_CODE, 0, null, accepterMap, ctx);
                        return;
                    }
                } else {
                    BasePackaginginfo forUpdate = new BasePackaginginfo();
                    forUpdate.setId(basePackagingInfoLevel2Local.getId());
                    forUpdate.setIsmt(ismt.equals(Constants.CONFIRM_Y) ? WmsConstants.IS_TRANSFER_Y : WmsConstants.IS_TRANSFER_N);
                    RemoteResult<Integer> result = packaginginfoRemoteService.updateIsMtById(this.getCredentialsVO(ctx), forUpdate);
                    if (result.isSuccess()) {
                        exceptionJump(TipConstants.GOODSINFO_CONTINUETO_PACKAGE_LEVEL1, PACKAGE_LEVEL1_LENGTH, -1, PACKAGE_LEVEL2_ISMT, accepterMap, ctx);
                    } else {
                        exceptionJump(ErrorConstants.SYS_ERROR_CONTINUE, BAR_CODE, 0, null, accepterMap, ctx);
                    }
                    return;
                }
            } else {
                exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, lastCompleteColName, 1, null, accepterMap, ctx);
                return;
            }
        } else if (PACKAGE_LEVEL2_STARTYARDNM.equals(lastCompleteColName)) {
            String colValue = goodsInfo.getPackageLevel2Startyardnm();
            if (colValue.matches(STARTYARDNM_REGX)) {
                BasePackaginginfo forUpdate = new BasePackaginginfo();
                forUpdate.setId(basePackagingInfoLevel2Local.getId());
                forUpdate.setStartyardnm(Integer.parseInt(colValue));
                RemoteResult<Integer> result = packaginginfoRemoteService.updateStartyardnmById(this.getCredentialsVO(ctx), forUpdate);
                if (result.isSuccess()) {
                    if (refreshPackaginginfoLevel2()) {
                        //设置下一个待接收字段并设置默认值
                        resetCurCol(getNullIsNullLabel(basePackagingInfoLevel2Local.getTraylevel()), PACKAGE_LEVEL2_TRAYLEVEL, accepterMap, ctx);
                    } else {
                        exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL2_ERROR, PACKAGELEVEL_TO_MAINTAIN, 8, null, accepterMap, ctx);
                        return;
                    }
                } else {
                    exceptionJump(ErrorConstants.SYS_ERROR_CONTINUE, BAR_CODE, 0, null, accepterMap, ctx);
                    return;
                }
            } else {
                exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, lastCompleteColName, 1, null, accepterMap, ctx);
                return;
            }
        } else if (PACKAGE_LEVEL2_TRAYLEVEL.equals(lastCompleteColName)) {
            String colValue = goodsInfo.getPackageLevel2Traylevel();
            if (colValue.matches(TRAYLEVEL_REGX)) {
                BasePackaginginfo forUpdate = new BasePackaginginfo();
                forUpdate.setId(basePackagingInfoLevel2Local.getId());
                forUpdate.setTraylevel(Integer.parseInt(colValue));
                RemoteResult<Integer> result = packaginginfoRemoteService.updateTraylevelById(this.getCredentialsVO(ctx), forUpdate);
                if (result.isSuccess()) {
                    if (refreshPackaginginfoLevel2()) {
                        //设置下一个待接收字段并设置默认值
                        resetCurCol(getNullIsNullLabel(basePackagingInfoLevel2Local.getOneyardnum()), PACKAGE_LEVEL2_ONEYARDNUM, accepterMap, ctx);
                    } else {
                        exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL2_ERROR, PACKAGELEVEL_TO_MAINTAIN, 9, null, accepterMap, ctx);
                        return;
                    }
                } else {
                    exceptionJump(ErrorConstants.SYS_ERROR_CONTINUE, BAR_CODE, 0, null, accepterMap, ctx);
                    return;
                }
            } else {
                exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, lastCompleteColName, 1, null, accepterMap, ctx);
                return;
            }
        } else if (PACKAGE_LEVEL2_ONEYARDNUM.equals(lastCompleteColName)) {
            String colValue = goodsInfo.getPackageLevel2Oneyardnum();
            if (colValue.matches(ONEYARDNUM_REGX)) {
                BasePackaginginfo forUpdate = new BasePackaginginfo();
                forUpdate.setId(basePackagingInfoLevel2Local.getId());
                forUpdate.setOneyardnum(Integer.parseInt(colValue));
                RemoteResult<Integer> result = packaginginfoRemoteService.updateOneyardnumById(this.getCredentialsVO(ctx), forUpdate);
                if (result.isSuccess()) {
                    if (refreshPackaginginfoLevel2()) {
                        successJump(TipConstants.GOODSINFO_CONTINUETO_PACKAGE_LEVEL1, PACKAGE_LEVEL1_LENGTH, -1, PACKAGE_LEVEL2_ONEYARDNUM, accepterMap, ctx);
                        return;
                    } else {
                        exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL2_ERROR, PACKAGELEVEL_TO_MAINTAIN, 10, null, accepterMap, ctx);
                        return;
                    }
                } else {
                    exceptionJump(ErrorConstants.SYS_ERROR_CONTINUE, BAR_CODE, 0, null, accepterMap, ctx);
                    return;
                }
            } else {
                exceptionJump(ErrorConstants.INPUT_FORMAT_ERROR, lastCompleteColName, 1, null, accepterMap, ctx);
                return;
            }
        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 刷新本地商品信息变量
     *
     * @param barcode 商品条码
     * @return 能否查询到商品信息
     */
    private boolean refreshGoodsinfo(String barcode) {
        if (barcode == null) {
            RemoteResult<BaseGoodsinfo> result = goodsinfoRemoteService.getGoodsInfoByBarCode(this.getCredentialsVO(ctx), baseGoodsInfoLocal.getBarcode());
            if (result.isSuccess()) {
                baseGoodsInfoLocal = result.getT();
                return true;
            } else {
                baseGoodsInfoLocal = null;
                return false;
            }
        } else {
            // 只有第一次扫描条形码时会进入这个判断
            RemoteResult<BaseGoodsinfo> result = goodsinfoRemoteService.getGoodsInfoByBarCode(this.getCredentialsVO(ctx), barcode);
            if (result.isSuccess()) {
                //初始化本次操作的基础变量
                baseGoodsInfoLocal = result.getT();
                BasePackaginginfo condition = new BasePackaginginfo();
                condition.setSkuid(result.getT().getSkuid());
                condition.setPacklevel(WmsConstants.PACK_LEVEL_ONE);
                condition.setPackstatus(WmsConstants.STATUS_ENABLE);
                basePackagingInfoLevel1Condition = condition;
                BasePackaginginfo condition2 = new BasePackaginginfo();
                condition2.setSkuid(result.getT().getSkuid());
                condition2.setPacklevel(WmsConstants.PACK_LEVEL_TWO);
                condition2.setPackstatus(WmsConstants.STATUS_ENABLE);
                basePackagingInfoLevel2Condition = condition2;
                return true;
            } else {
                baseGoodsInfoLocal = null;
                return false;
            }
        }
    }

    /**
     * 刷新本地一级包装信息
     *
     * @return 能否查询到一级包装信息
     */
    private boolean refreshPackaginginfoLevel1() {
        RemoteResult<BasePackaginginfo> result = packaginginfoRemoteService.getPackagingInfoByCondition(this.getCredentialsVO(ctx), basePackagingInfoLevel1Condition);
        if (result.isSuccess()) {
            basePackagingInfoLevel1 = result.getT();
            return true;
        }
        return false;
    }

    /**
     * 刷新本地二级包装信息
     *
     * @return 能否查询到一级包装信息
     */
    private boolean refreshPackaginginfoLevel2() {
        RemoteResult<BasePackaginginfo> result = packaginginfoRemoteService.getPackagingInfoByCondition(this.getCredentialsVO(ctx), basePackagingInfoLevel2Condition);
        if (result.isSuccess()) {
            basePackagingInfoLevel2Local = result.getT();
            return true;
        }
        return false;
    }

    /**
     * 重新设定所有local变量
     */
    private void resetLocals() {
        basePackagingInfoLevel1 = null;
        basePackagingInfoLevel2Local = null;
        basePackagingInfoLevel1Condition = null;
        basePackagingInfoLevel2Condition = null;
        baseGoodsInfoLocal = null;
    }

    /**
     * 重置goto相关的map键值
     *
     * @param accepterMap 接收内容
     */
    private void resetGotoKeys(Map<String, Object> accepterMap) {
        accepterMap.put(DEL_LINE, 0);
        accepterMap.put(FROM_ANYKEY, null);
        accepterMap.put(GOTO, null);
        accepterMap.put(GOTO_MESS, null);
    }

    // 是否贵品List
    private List<String> getValuableflagList() {
        Integer valuableflag = baseGoodsInfoLocal.getValuableflag();
        if (valuableflag == null || valuableflag.equals(WmsConstants.IS_TRANSFER_Y)) {
            return CollectionUtil.newGenericList(TipConstants.GOODSINFO_VALUABLEFLAG_Y, TipConstants.GOODSINFO_VALUABLEFLAG_N);
        } else {
            return CollectionUtil.newGenericList(TipConstants.GOODSINFO_VALUABLEFLAG_N, TipConstants.GOODSINFO_VALUABLEFLAG_Y);
        }
    }

    // 包装级别List
    private List<String> getPackLevelList(Integer packlevel) {
        if (packlevel == null || packlevel.equals(WmsConstants.PACK_LEVEL_ONE)) {
            return CollectionUtil.newGenericList(TipConstants.GOODSINFO_PACKAGE_LEVEL_ONE, TipConstants.GOODSINFO_PACKAGE_LEVEL_TWO);
        } else {
            return CollectionUtil.newGenericList(TipConstants.GOODSINFO_PACKAGE_LEVEL_TWO, TipConstants.GOODSINFO_PACKAGE_LEVEL_ONE);
        }
    }

    // 批次规则List
    private List<String> getBatchRuleList() {
        return Constants.batchRuleEnum.getNameSwichList(baseGoodsInfoLocal.getBatchrule());
    }

    /**
     * 获取商品上架属性左右切换列表，如果商品信息不为空，将商品信息中的那个值放在第一个
     *
     * @return 拼好的商品上架属性左右切换列表
     */
    private List<String> getShelfFlagList() {
        List<String> switchList = CollectionUtil.newGenericList(shelfFlagDescList.toArray(new String[shelfFlagDescList.size()]));
        if (baseGoodsInfoLocal.getShelfflag() != null) {
            for (int i = 0, len = shelfFlagDictList.size(); i < len; i++) {
                if (Objects.equals(shelfFlagDictList.get(i).getBilltypecode(), baseGoodsInfoLocal.getShelfflag().toString())) {
                    switchList.remove(i);
                    switchList.add(0, shelfFlagDictList.get(i).getBilltypecn());
                    break;
                }
            }
        }
        return switchList;
    }

    // 将数据库中长宽高、层数等为空就打印0的字段的值做下空字符串的处理
    private String getNullIs0Label(Object measure) {
        if (measure == null) {
            return "0";
        } else {
            return measure.toString();
        }
    }

    // 将数据库中长宽高、层数等为空就打印空字符串的字段的值做下处理
    private String getNullIsNullLabel(Object measure) {
        if (measure == null) {
            return "";
        } else {
            return measure.toString();
        }
    }

    /**
     * 转换成正常数字
     *
     * @param numStr 待转换数字字符串
     * @return 处理过的数字字符串
     */
    private String convertToNormalNumber(String numStr) throws Exception {
        if (StringUtils.isBlank(numStr)) {
            return "";
        }
        BigDecimal num = new BigDecimal(Double.parseDouble(numStr));
        String result = num.setScale(9, BigDecimal.ROUND_HALF_UP).toString();
        while (result.lastIndexOf("0") == (result.length() - 1) || result.lastIndexOf(".") == (result.length() - 1)) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    // 将数据库中重量字段的值加上KG单位
    private String getWeightLabel(BigDecimal weight) {
        if (weight == null) {
            return "0".concat(Constants.WEIGHT_UNIT);
        }
        return weight.toString().concat(Constants.WEIGHT_UNIT);
    }

    // 在打印一级包装长度之前打印的一些信息
    private void printBeforeLevel1Length(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        BasePackaginginfo pack = basePackagingInfoLevel1;
        List<String> tipList = CollectionUtil.newGenericList(TipConstants.GOODSINFO_PACKAGELEVEL1_WEIGHT_LABEL
                , TipConstants.GOODSINFO_LENGTH_LABEL.concat(getNullIs0Label(pack.getLength())).
                concat(TipConstants.GOODSINFO_WIDTH_LABEL).concat(getNullIs0Label(pack.getWidth())).
                concat(TipConstants.GOODSINFO_HEIGHT_LABEL).concat(getNullIs0Label(pack.getHeight())).
                concat(TipConstants.GOODSINFO_CUBAGE_LABEL).concat(getNullIs0Label(pack.getCubage())).concat("（米）")
                , TipConstants.GOODSINFO_PACKAGELEVEL1_MT_LABEL
                , TipConstants.GOODSINFO_ONEYARDNUM_LABEL.concat(getNullIs0Label(pack.getOneyardnum())).
                concat(TipConstants.GOODSINFO_TRAYLEVEL_LABEL).concat(getNullIs0Label(pack.getTraylevel())).
                concat(TipConstants.GOODSINFO_STARTYARDNM_LABEL).concat(getNullIs0Label(pack.getStartyardnm())));
        List<String> valueList = CollectionUtil.newGenericList(getWeightLabel(pack.getWeight()), "", "", "");
        printBeforeNextField(tipList, valueList, accepterMap, ctx);
    }

    // 在打印二级包装长度之前打印的一些信息
    private void printBeforeLevel2Length(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        BasePackaginginfo pack = basePackagingInfoLevel2Local;
        List<String> tipList = CollectionUtil.newGenericList(TipConstants.GOODSINFO_PACKAGELEVEL2_WEIGHT_LABEL
                , TipConstants.GOODSINFO_LENGTH_LABEL.concat(getNullIs0Label(pack.getLength())).
                concat(TipConstants.GOODSINFO_WIDTH_LABEL).concat(getNullIs0Label(pack.getWidth())).
                concat(TipConstants.GOODSINFO_HEIGHT_LABEL).concat(getNullIs0Label(pack.getHeight())).
                concat(TipConstants.GOODSINFO_CUBAGE_LABEL).concat(getNullIs0Label(pack.getCubage())).concat("（米）")
                , TipConstants.GOODSINFO_PACKAGELEVEL2_MT_LABEL
                , TipConstants.GOODSINFO_ONEYARDNUM_LABEL.concat(getNullIs0Label(pack.getOneyardnum())).
                concat(TipConstants.GOODSINFO_TRAYLEVEL_LABEL).concat(getNullIs0Label(pack.getTraylevel())).
                concat(TipConstants.GOODSINFO_STARTYARDNM_LABEL).concat(getNullIs0Label(pack.getStartyardnm()))
                , TipConstants.GOODSINFO_PKNUM_LABEL);
        List<String> valueList = CollectionUtil.newGenericList(getWeightLabel(pack.getWeight()), "", "", "", pack.getPknum().toString());
        printBeforeNextField(tipList, valueList, accepterMap, ctx);
    }

    // 直接跳转控制
    private boolean goToHandler(String msg, Map<String, Object> accepterMap, ChannelHandlerContext ctx) throws Exception {
        if (accepterMap.get(GOTO) != null) {
            String kyeCode = HandlerUtil.getKeyCode(msg);
            if (kyeCode.equals("27,91,65")) {//如果是上移则将光标移动到下一行，屏幕上显示光标不动
                HandlerUtil.moveDownN(ctx, 1);
            } else if (kyeCode.equals("27,91,66")) {//如果是下移，增将上移相反
                HandlerUtil.moveUpN(ctx, 1);
            }
            int messLength = String.valueOf(accepterMap.get(GOTO_MESS)).split("\r\n").length;
            int delLine = Integer.parseInt(String.valueOf(accepterMap.get(DEL_LINE)));
            String goToColName = String.valueOf(accepterMap.get(GOTO));
            String fromColName = String.valueOf(accepterMap.get(FROM_ANYKEY));
            if (goToColName.equals(BAR_CODE)) {
                channelActive(ctx);
            } else if (goToColName.equals(PACKAGELEVEL_TO_MAINTAIN)) {
                delMessAndLines(messLength, delLine, ctx);
                resetCurCol(goToColName, accepterMap, ctx);
                if (refreshPackaginginfoLevel2()) {
                    setNextColSwitchList(getPackLevelList(WmsConstants.PACK_LEVEL_ONE), accepterMap, ctx);
                } else {
                    setNextColSwitchList(CollectionUtil.newGenericList(TipConstants.GOODSINFO_PACKAGE_LEVEL_ONE, TipConstants.GOODSINFO_PACKAGE_LEVEL_ADD_TWO), accepterMap, ctx);
                }
            } else if (goToColName.equals(PACKAGE_LEVEL1_LENGTH)) {
                if ((fromColName.equals(PACKAGE_LEVEL2_ISMT) || fromColName.equals(PACKAGE_LEVEL2_ONEYARDNUM)) && msg.getBytes()[0] != KeyEnum.CR_13.code) {
                    channelActive(ctx);
                } else if (fromColName.equals(PACKAGE_LEVEL2_ISMT) || fromColName.equals(PACKAGE_LEVEL2_ONEYARDNUM)) {
                    delMessAndLines(messLength, delLine, ctx);
                    if (refreshPackaginginfoLevel1()) {
                        printBeforeLevel1Length(accepterMap, ctx);
                        resetCurCol(getNullIsNullLabel(basePackagingInfoLevel1.getLength()), goToColName, accepterMap, ctx);
                    } else {
                        resetGotoKeys(accepterMap);
                        exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL1_ERROR, PACKAGELEVEL_TO_MAINTAIN, 0, PACKAGELEVEL_TO_MAINTAIN, accepterMap, ctx);
                        return true;
                    }
                } else {
                    delMessAndLines(messLength, delLine, ctx);
                    if (refreshPackaginginfoLevel1()) {
                        resetCurCol(goToColName, accepterMap, ctx);
                    } else {
                        resetGotoKeys(accepterMap);
                        exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL1_ERROR, PACKAGELEVEL_TO_MAINTAIN, 7, PACKAGELEVEL_TO_MAINTAIN, accepterMap, ctx);
                        return true;
                    }
                }
            } else if (goToColName.equals(PACKAGE_LEVEL2_LENGTH)) {
                if ((fromColName.equals(PACKAGE_LEVEL1_ISMT) || fromColName.equals(PACKAGE_LEVEL1_ONEYARDNUM) | fromColName.equals(PACKAGE_LEVEL1_WEIGHT)) && msg.getBytes()[0] != KeyEnum.CR_13.code) {
                    channelActive(ctx);
                } else if (fromColName.equals(PACKAGE_LEVEL1_ISMT) || fromColName.equals(PACKAGE_LEVEL1_ONEYARDNUM) | fromColName.equals(PACKAGE_LEVEL1_WEIGHT)) {
                    delMessAndLines(messLength, delLine, ctx);
                    if (refreshPackaginginfoLevel2()) {
                        printBeforeLevel2Length(accepterMap, ctx);
                        resetCurCol(getNullIsNullLabel(basePackagingInfoLevel2Local.getLength()), goToColName, accepterMap, ctx);
                    } else {
                        resetGotoKeys(accepterMap);
                        exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL2_ERROR, PACKAGELEVEL_TO_MAINTAIN, 0, PACKAGELEVEL_TO_MAINTAIN, accepterMap, ctx);
                        return true;
                    }
                } else {
                    delMessAndLines(messLength, delLine, ctx);
                    if (refreshPackaginginfoLevel2()) {
                        resetCurCol(goToColName, accepterMap, ctx);
                    } else {
                        resetGotoKeys(accepterMap);
                        exceptionJump(ErrorConstants.GOODSINFO_PACKAGINGINFO_LEVEL2_ERROR, PACKAGELEVEL_TO_MAINTAIN, 3, PACKAGELEVEL_TO_MAINTAIN, accepterMap, ctx);
                        return true;
                    }
                }
            } else {
                delMessAndLines(messLength, delLine, ctx);
                resetCurCol(goToColName, accepterMap, ctx);
            }
            resetGotoKeys(accepterMap);
            return true;
        }
        return false;
    }

    // 删除错误提示并且回退N行
    private void delMessAndLines(int messLength, int delLine, ChannelHandlerContext ctx) {
        if (messLength > 0) {
            for (int i = 0; i < messLength; i++) {
                HandlerUtil.delALL(ctx);//删除错误提示一行
                HandlerUtil.moveUpN(ctx, 1);
            }
        }
        if (delLine > 0) {
            for (int i = 0; i < delLine; i++) {
                HandlerUtil.delALL(ctx);
                HandlerUtil.moveUpN(ctx, 1);
            }
        } else if (delLine < 0) {
            for (int i = 0; i > delLine; i--) {
                ctx.write(Constants.BREAK_LINE.getBytes());
            }
        }
        ctx.write(Constants.BREAK_LINE.getBytes());
    }

    /**
     * 发生异常后的跳转
     *
     * @param mess        提示信息
     * @param colName     跳转的目标字段名
     * @param delLine     向上删除多少行，如果是负数则添加多少个回车
     * @param fromCol     从哪个字段发起的跳转
     * @param accepterMap 记录输入参数的Map
     * @param ctx         ChannelHandlerContext
     */
    public void exceptionJump(String mess, String colName, int delLine, String fromCol, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        mess = RFUtil.makeStrFitPda(mess, "", 3);
        setColUnReceived(colName, accepterMap);
        HandlerUtil.delALL(ctx);// 删除下一个已打印出的字段信息
        HandlerUtil.moveUpN(ctx, 1); // 上移到有内容的一行
        ctx.write(Constants.BREAK_LINE.getBytes()); // 另起一行
        HandlerUtil.errorBeep(ctx);//响铃
        HandlerUtil.print(ctx, mess); // 打印错误信息
        accepterMap.put(DEL_LINE, delLine);
        accepterMap.put(FROM_ANYKEY, fromCol);
        accepterMap.put(GOTO, colName);//标识接下来会跳转到哪步输入
        accepterMap.put(GOTO_MESS, mess);//标识错误提示文本
    }
    /**
     * 成功后的跳转
     *
     * @param mess        提示信息
     * @param colName     跳转的目标字段名
     * @param delLine     向上删除多少行，如果是负数则添加多少个回车
     * @param fromCol     从哪个字段发起的跳转
     * @param accepterMap 记录输入参数的Map
     * @param ctx         ChannelHandlerContext
     */
    public void successJump(String mess, String colName, int delLine, String fromCol, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        mess = RFUtil.makeStrFitPda(mess, "", 3);
        setColUnReceived(colName, accepterMap);
        HandlerUtil.delALL(ctx);// 删除下一个已打印出的字段信息
        HandlerUtil.moveUpN(ctx, 1); // 上移到有内容的一行
        ctx.write(Constants.BREAK_LINE.getBytes()); // 另起一行
        HandlerUtil.print(ctx, mess); // 打印错误信息
        accepterMap.put(DEL_LINE, delLine);
        accepterMap.put(FROM_ANYKEY, fromCol);
        accepterMap.put(GOTO, colName);//标识接下来会跳转到哪步输入
        accepterMap.put(GOTO_MESS, mess);//标识错误提示文本
    }

}
