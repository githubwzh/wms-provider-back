package com.womai.wms.rf.domain.workproduct;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * ClassDescribe: 加工上架业务类
 * Author :zhangwei
 * Date: 2017-03-07
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
public class ProductShelf {

    public final static String SHELF_TYPE = "上架方式:";
    public final static String SELECT_SERIAL_NO = "选择序号:";

    @Receiver(colTip = "加工单号:",cursorDown = true,topTip = true)
    private String orderCode;//上架单号或网络订单号
    @Receiver(colTip = SHELF_TYPE,topTip = true)
    private String shelfType;//上架方式，扫描商品条码或托盘码
    @Receiver(colTip = "商品条码:",topTip = true)
    private String barCode;//商品条码
    @Receiver(colTip = SELECT_SERIAL_NO,topTip = true)
    private String selectedDetail;//分页方式选择明细
    @Receiver(colTip = "托盘码:",topTip = true)
    private String palletCode;//托盘编码
    @Receiver(colTip = "上架库位:",topTip = true)
    private String scanWHCode;//扫描库位编码
    @Receiver(colTip = "确认日期(yyyyMMdd格式):",topTip = true)
    private String confirmDate;//输入生产日期进行确认
    @Receiver(colTip = "上架数量BU:",topTip = true)
    private String shelfNum;//上架数量
    @Receiver(colTip = "确认上架(Y/N):",topTip = true)
    private String confirmShelfNum;//托盘码上架方式，需要输入YN进行确认，一次性上架全部BU

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getShelfType() {
        return shelfType;
    }

    public void setShelfType(String shelfType) {
        this.shelfType = shelfType;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getSelectedDetail() {
        return selectedDetail;
    }

    public void setSelectedDetail(String selectedDetail) {
        this.selectedDetail = selectedDetail;
    }

    public String getPalletCode() {
        return palletCode;
    }

    public void setPalletCode(String palletCode) {
        this.palletCode = palletCode;
    }

    public String getScanWHCode() {
        return scanWHCode;
    }

    public void setScanWHCode(String scanWHCode) {
        this.scanWHCode = scanWHCode;
    }

    public String getConfirmDate() {
        return confirmDate;
    }

    public void setConfirmDate(String confirmDate) {
        this.confirmDate = confirmDate;
    }

    public String getShelfNum() {
        return shelfNum;
    }

    public void setShelfNum(String shelfNum) {
        this.shelfNum = shelfNum;
    }

    public String getConfirmShelfNum() {
        return confirmShelfNum;
    }

    public void setConfirmShelfNum(String confirmShelfNum) {
        this.confirmShelfNum = confirmShelfNum;
    }
}
