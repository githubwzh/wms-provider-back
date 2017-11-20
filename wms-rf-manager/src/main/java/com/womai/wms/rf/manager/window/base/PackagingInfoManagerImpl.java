package com.womai.wms.rf.manager.window.base;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.DefaultKey;
import com.womai.wms.rf.common.constants.ErrorConstants;
import com.womai.wms.rf.common.constants.TipConstants;
import com.womai.wms.rf.common.util.CollectionUtil;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.RFUtil;
import com.womai.wms.rf.domain.base.PackingInfo;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.base.PackaginginfoRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.base.BaseGoodsinfo;
import com.womai.zlwms.rfsoa.domain.base.BasePackaginginfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * ClassDescribe:新建二级包装
 * Author :wangzhanhua
 * Date: 2017-03-07
 * Since
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component("packagingInfoManager")
public class PackagingInfoManagerImpl extends ReceiveManager {
    private ChannelHandlerContext ctx;
    private static final String[] pageHeader = {Constants.BREAK_LINE, TipConstants.GOODSINFO_ADD_PACKAGE_LEVEL2, Constants.SPLIT, ""};
    private final static String PKNUM = "pknum";//箱规
    private final static String PACKAGE_LEVEL2_LENGTH = "packageLevel2Length";//二级包装长度字段
    private final static String PACKAGE_LEVEL2_WIDTH = "packageLevel2Width";//二级包装宽度字段
    private final static String PACKAGE_LEVEL2_HEIGHT = "packageLevel2Height";//二级包装高度字段
    private final static String PACKAGE_LEVEL2_WEIGHT = "packageLevel2Weight";//二级包装重量字段
    private final static String PACKAGE_LEVEL2_ISMT = "packageLevel2Ismt";//二级包装是否维护码托字段
    private final static String PACKAGE_LEVEL2_STARTYARDNM = "packageLevel2Startyardnm";//二级包装起码数量字段
    private final static String PACKAGE_LEVEL2_TRAYLEVEL = "packageLevel2Traylevel";//二级包装托盘码放层数字段
    private final static String PACKAGE_LEVEL2_ONEYARDNUM = "packageLevel2Oneyardnum";//二级包装单层码托数量字段
    private final static String LENGTH_WIDTH_HEIGHT_REGX = "^([1-9]\\d*|[1-9]\\d*\\.\\d{0,2}[1-9]|0\\.[0-9]{0,2}[1-9])$";// 长宽高验证
    private final static String CUBAGE_REGX = "^([1-9]\\d*|[1-9]\\d*\\.\\d{0,8}[1-9]|0\\.[0-9]{0,8}[1-9])$";// 体积验证
    private final static String WEIGHT_REGX = "^([1-9]\\d*|[1-9]\\d*\\.\\d{0,5}[1-9]|0\\.[0-9]{0,5}[1-9])$";// 重量
    private final static String YN_REGX = "^[YNyn]$"; // 输入YN的验证
    private final static String STARTYARDNM_REGX = "^[1-9]\\d{0,3}$"; // 起码重量
    private final static String TRAYLEVEL_REGX = "^[1-9]\\d?$"; // 托盘码放层数
    private final static String ONEYARDNUM_REGX = "^[1-9]\\d{0,3}$"; // 单层码托数量
    private static final String NEXT_LOCATION = "nextLocation";//跳转的位置
    private GoodsInfoParamManagerImpl goodsInfoParamManager;//商品维护handler类
    private final BasePackaginginfo basePackaginginfo = new BasePackaginginfo();//新建的二级包装
    @Autowired
    private PackaginginfoRemoteService packaginginfoRemoteService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        // 新建二级包装
        super.initBaseMap(PackingInfo.class, pageHeader, ctx);
        Map<String, Object> accepterMap = getDataMap();
        //获得上个商品维护handler数据
        ChannelPipeline pipeline = ctx.pipeline();
        if (this.goodsInfoParamManager == null) {
            GoodsInfoParamManagerImpl goodsInfoParamManager = (GoodsInfoParamManagerImpl) pipeline.get(TipConstants.GOODSINFO_PARAM_MANAGER);
            if (goodsInfoParamManager != null) {
                this.goodsInfoParamManager = goodsInfoParamManager;
                pipeline.remove(goodsInfoParamManager);
            } else {
                colNeedReInput(PACKAGE_LEVEL2_HEIGHT, ErrorConstants.SYS_ERROR, accepterMap, ctx);
                return;
            }
        }
        BaseGoodsinfo baseGoodsinfo = this.goodsInfoParamManager.getBaseGoodsinfo();
        accepterMap.remove(NEXT_LOCATION);
        printBeforeNextField(CollectionUtil.newList("商品条码", "商品名称", "商品状态", "保质期", "包装单位"),
                CollectionUtil.newList(baseGoodsinfo.getBarcode(), baseGoodsinfo.getSkuname(),
                        Constants.getStatusLabel(baseGoodsinfo.getStatus()), baseGoodsinfo.getKeepdays() + "", TipConstants.PK_LEVEL2_NAME), accepterMap, ctx);
        this.basePackaginginfo.setUnitname(TipConstants.PK_LEVEL2_NAME);
        resetCurCol(PKNUM, accepterMap, ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Map<String, Object> accepterMap = getDataMap();
        if (accepterMap.get(NEXT_LOCATION) != null) {
            forward((String) accepterMap.get(NEXT_LOCATION), ctx);
            return;
        }
        receiveDataAndNotPrintNext(ctx, msg, accepterMap);
        PackingInfo packingInfo = getPackingInfo();
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);

        if (PKNUM.equals(lastCompleteColName)) {
            String pknum = packingInfo.getPknum();
            if (pknum.matches(TipConstants.REG_PKNUM)) {//正整数
                this.basePackaginginfo.setPknum(Integer.parseInt(pknum));
                //设置下一个待接收字段并设置默认值
                resetCurCol(PACKAGE_LEVEL2_LENGTH, accepterMap, ctx);
            } else {
                colNeedReInput(PKNUM, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
            }
        }
        if (PACKAGE_LEVEL2_LENGTH.equals(lastCompleteColName)) {
            String lengthStr = packingInfo.getPackageLevel2Length();
            if (lengthStr.matches(LENGTH_WIDTH_HEIGHT_REGX)) {
                //设置下一个待接收字段并设置默认值
                resetCurCol(PACKAGE_LEVEL2_WIDTH, accepterMap, ctx);
            } else {
                colNeedReInput(PACKAGE_LEVEL2_LENGTH, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
            }
        }
        if (PACKAGE_LEVEL2_WIDTH.equals(lastCompleteColName)) {
            String widthStr = packingInfo.getPackageLevel2Width();
            if (widthStr.matches(LENGTH_WIDTH_HEIGHT_REGX)) {
                //设置下一个待接收字段并设置默认值
                resetCurCol(PACKAGE_LEVEL2_HEIGHT, accepterMap, ctx);
            } else {
                colNeedReInput(PACKAGE_LEVEL2_WIDTH, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
            }
        }
        if (PACKAGE_LEVEL2_HEIGHT.equals(lastCompleteColName)) {
            String heightStr = packingInfo.getPackageLevel2Height();
            String widthStr = packingInfo.getPackageLevel2Width();
            String lengthStr = packingInfo.getPackageLevel2Length();
            BigDecimal length = new BigDecimal(lengthStr);
            BigDecimal width = new BigDecimal(widthStr);
            if (heightStr.matches(LENGTH_WIDTH_HEIGHT_REGX)) {
                BigDecimal height = new BigDecimal(heightStr);//二级包装高
                BigDecimal cubage = width.multiply(length.multiply(height)); //二级包装体积
                if (convertToNormalNumber(cubage.toString()).matches(CUBAGE_REGX)) {
                    int pknum = Integer.parseInt(getPackingInfo().getPknum());//录入的箱规
                    // 若一级包装体积乘以箱规大于二级包装体积，则提交失败
                    if (((this.goodsInfoParamManager.getBasePackagingInfoLevel1().getCubage()).multiply(new BigDecimal(pknum))).compareTo(cubage) == 1) {
                        colNeedReInput(PACKAGE_LEVEL2_HEIGHT, ErrorConstants.PACKAGINGINFO_LEVEL2_CUBAGE_ERROR, accepterMap, ctx);
                        return;
                    } else {
                        BigDecimal[] lengthOfSide = RFUtil.getLengthOfSide(new BigDecimal[]{height, width, length});
                        this.basePackaginginfo.setLength(lengthOfSide[2]);
                        this.basePackaginginfo.setWidth(lengthOfSide[1]);
                        this.basePackaginginfo.setHeight(lengthOfSide[0]);
                        this.basePackaginginfo.setCubage(cubage);
                        this.basePackaginginfo.setPacklevel(WmsConstants.PACK_LEVEL_TWO);//二级包装
                        this.basePackaginginfo.setPackstatus(WmsConstants.STATUS_ENABLE);//生效
                        this.basePackaginginfo.setSkuid(this.goodsInfoParamManager.getBaseGoodsinfo().getSkuid());//商品id
                        resetCurCol(PACKAGE_LEVEL2_WEIGHT, accepterMap, ctx);
                    }
                }
            } else {
                colNeedReInput(PACKAGE_LEVEL2_HEIGHT, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
            }
        }
        if (PACKAGE_LEVEL2_WEIGHT.equals(lastCompleteColName)) {
            String weightStr = packingInfo.getPackageLevel2Weight();
            if (weightStr.matches(WEIGHT_REGX)) {
                BigDecimal weight = new BigDecimal(weightStr);
                int pknum = Integer.parseInt(packingInfo.getPknum());
                // 若一级包装重量乘以箱规大于二级包装重量，则提交失败
                if (goodsInfoParamManager.getBasePackagingInfoLevel1().getWeight().multiply(new BigDecimal(pknum)).compareTo(weight) == 1) {
                    colNeedReInput(PACKAGE_LEVEL2_WEIGHT, ErrorConstants.PACKAGINGINFO_LEVEL2_WEIGHT_ERROR, accepterMap, ctx);
                    return;
                } else {
                    this.basePackaginginfo.setWeight(weight);
                    setNextColSwitchList(CollectionUtil.newGenericList(Constants.CONFIRM_Y, Constants.CANCEL_N), accepterMap, ctx);
                }
            } else {
                colNeedReInput(PACKAGE_LEVEL2_WEIGHT, ErrorConstants.INPUT_ERROR, accepterMap, ctx);
            }
        }
        if (PACKAGE_LEVEL2_ISMT.equals(lastCompleteColName)) {
            String ismt = packingInfo.getPackageLevel2Ismt();
            if (ismt.matches(YN_REGX)) {
                this.basePackaginginfo.setIsmt(ismt.equals(Constants.CONFIRM_Y) ? WmsConstants.IS_TRANSFER_Y : WmsConstants.IS_TRANSFER_N);
                if (Constants.CONFIRM_Y.equals(ismt)) {
                    //  forUpdate.setIsmt(ismt.equals(Constants.CONFIRM_Y) ? WmsConstants.IS_TRANSFER_Y : WmsConstants.IS_TRANSFER_N); TODO
                    //设置下一个待接收字段并设置默认值
                    resetCurCol(PACKAGE_LEVEL2_STARTYARDNM, accepterMap, ctx);
                } else {
                    //不码托
                    processInsertBasePackinginfo(accepterMap, ctx);
                }
            } else {
                colNeedReInput(PACKAGE_LEVEL2_ISMT, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                return;
            }
        }

        if (PACKAGE_LEVEL2_STARTYARDNM.equals(lastCompleteColName)) {
            String colValue = packingInfo.getPackageLevel2Startyardnm();
            if (colValue.matches(STARTYARDNM_REGX)) {
                this.basePackaginginfo.setStartyardnm(Integer.parseInt(colValue));
                resetCurCol(PACKAGE_LEVEL2_TRAYLEVEL, accepterMap, ctx);
            } else {
                colNeedReInput(PACKAGE_LEVEL2_STARTYARDNM, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                return;
            }
        }
        if (PACKAGE_LEVEL2_TRAYLEVEL.equals(lastCompleteColName)) {
            String colValue = packingInfo.getPackageLevel2Traylevel();
            if (colValue.matches(TRAYLEVEL_REGX)) {
                resetCurCol(PACKAGE_LEVEL2_ONEYARDNUM, accepterMap, ctx);
                this.basePackaginginfo.setTraylevel(Integer.parseInt(colValue));
            } else {
                colNeedReInput(PACKAGE_LEVEL2_TRAYLEVEL, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                return;
            }
        }
        if (PACKAGE_LEVEL2_ONEYARDNUM.equals(lastCompleteColName)) {
            String colValue = packingInfo.getPackageLevel2Oneyardnum();
            if (colValue.matches(ONEYARDNUM_REGX)) {
                this.basePackaginginfo.setOneyardnum(Integer.parseInt(colValue));
                processInsertBasePackinginfo(accepterMap, ctx);
            } else {
                colNeedReInput(PACKAGE_LEVEL2_ONEYARDNUM, ErrorConstants.INPUT_FORMAT_ERROR, accepterMap, ctx);
                return;
            }
        }
    }

    /**
     * 提交数据，新增商品二级包装
     *
     * @param accepterMap
     * @param ctx
     */
    private void processInsertBasePackinginfo(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        RemoteResult<String> result = packaginginfoRemoteService.insertBasePackaginginfo(this.getCredentialsVO(ctx), this.basePackaginginfo);
        HandlerUtil.changeRow(ctx);
        HandlerUtil.print(ctx, result.getResultCode() + ErrorConstants.TIP_TO_CONTINUE);//处理成功，或者失败提示语
        accepterMap.put(NEXT_LOCATION, TipConstants.GOODSINFO_MANAGER);//任意键后，跳转到商品维护
    }

    /**
     * 获得接受数据对象
     *
     * @return
     */
    private PackingInfo getPackingInfo() {
        return (PackingInfo) getDataMap().get(DefaultKey.objectClass.keyName);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
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

}
