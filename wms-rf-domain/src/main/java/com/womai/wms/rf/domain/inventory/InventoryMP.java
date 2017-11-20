package com.womai.wms.rf.domain.inventory;

import com.womai.wms.rf.common.annotation.Receiver;
import com.womai.wms.rf.common.constants.TipConstants;
import com.womai.wms.rf.domain.BaseDomain;

import java.util.Date;

/**
 * ClassDescribe:RF明盘数据接收domain
 * Author :zhangwei
 * Date: 2016-11-07
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
public class InventoryMP extends BaseDomain {

    @Receiver(colTip = TipConstants.WH_CODE, topTip = true,canNull = true)
    private String receivedWhCode;//用于显示已经接收到的库位编码
    @Receiver(colTip = "是否存在差异:", topTip = true)
    private String existDiffer;//是否存在差异
    @Receiver(colTip = "商品条码:", topTip = true)
    private String confirmBarCode;//选择存在差异时再次输入商品条码
    @Receiver(colTip = "选择日期类型:")
    private String dateType;//日期类型，选择生产日期或失效期
    @Receiver(colTip = "日期(yyyyMMdd格式):", topTip = true)
    private String prodOrExpDate;//确认日期，根据批次规则的可能为生产日期或失效日期
    @Receiver(colTip = "与库存系统日期相差过大,确认:", topTip = true)
    private String tooBigThanSysDate;//与日期开关比较是否过大，左右选择，之前一行提示：与该库存系统日期相差过大
    @Receiver(colTip = "库存数量BU:", topTip = true)
    private String confirmBU;//盘点BU数
    @Receiver(colTip = TipConstants.INVENTORY_TIP_SURPLUS, topTip = true)
    private String existSurplus;//是否存在多余库存


    //输入日期后计算得到
    private Date productionDate;//生产日期
    private Date expirationDate;//失效日期

    public String getReceivedWhCode() {
        return receivedWhCode;
    }

    public void setReceivedWhCode(String receivedWhCode) {
        this.receivedWhCode = receivedWhCode;
    }

    public String getExistDiffer() {
        return existDiffer;
    }

    public void setExistDiffer(String existDiffer) {
        this.existDiffer = existDiffer;
    }

    public String getConfirmBarCode() {
        return confirmBarCode;
    }

    public void setConfirmBarCode(String confirmBarCode) {
        this.confirmBarCode = confirmBarCode;
    }

    public String getProdOrExpDate() {
        return prodOrExpDate;
    }

    public void setProdOrExpDate(String prodOrExpDate) {
        this.prodOrExpDate = prodOrExpDate;
    }

    public String getTooBigThanSysDate() {
        return tooBigThanSysDate;
    }

    public void setTooBigThanSysDate(String tooBigThanSysDate) {
        this.tooBigThanSysDate = tooBigThanSysDate;
    }

    public String getConfirmBU() {
        return confirmBU;
    }

    public void setConfirmBU(String confirmBU) {
        this.confirmBU = confirmBU;
    }

    public String getExistSurplus() {
        return existSurplus;
    }

    public void setExistSurplus(String existSurplus) {
        this.existSurplus = existSurplus;
    }

    public Date getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(Date productionDate) {
        this.productionDate = productionDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getDateType() {
        return dateType;
    }

    public void setDateType(String dateType) {
        this.dateType = dateType;
    }
    @Override
    public String toString() {
        return "InventoryMP{" +
                "receivedWhCode='" + receivedWhCode + '\'' +
                ", existDiffer='" + existDiffer + '\'' +
                ", confirmBarCode='" + confirmBarCode + '\'' +
                ", prodOrExpDate='" + prodOrExpDate + '\'' +
                ", tooBigThanSysDate='" + tooBigThanSysDate + '\'' +
                ", confirmBU='" + confirmBU + '\'' +
                ", existSurplus='" + existSurplus + '\'' +
                ", productionDate=" + productionDate +
                ", expirationDate=" + expirationDate +
                '}';
    }
}
