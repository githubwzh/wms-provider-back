package com.womai.wms.rf.domain.inventory;

import com.womai.wms.rf.common.annotation.Receiver;
import com.womai.wms.rf.common.constants.TipConstants;
import com.womai.wms.rf.domain.BaseDomain;

import java.util.Date;

/**
 * ClassDescribe:RF暗盘数据接收domain
 * Author :zhangwei
 * Date: 2016-11-07
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
public class InventoryAP extends BaseDomain {

    @Receiver(colTip = TipConstants.WH_CODE, topTip = true,canNull = true)
    private String receivedWhCode;//用于显示已经接收到的库位编码
    @Receiver(colTip = "商品条码:", topTip = true)
    private String barCode;//商品条码，用于盲盘的商品条码接收及明盘中的条码显示
    @Receiver(colTip = "选择日期类型:")
    private String dateType;//日期类型，选择生产日期或失效期
    @Receiver(colTip = "日期(yyyyMMdd格式):", topTip = true)
    private String prodOrExpDate;//确认日期，根据批次规则的可能为生产日期或失效日期
    @Receiver(colTip = "与库存系统日期相差过大,确认:", topTip = true)
    private String tooBigThanSysDate;//与日期开关比较是否过大，左右选择，之前一行提示：与该库存系统日期相差过大
    @Receiver(colTip = "库存数量BU:", topTip = true)
    private String confirmBU;//盘点BU数
    @Receiver(colTip = "已存在该条码，是否覆盖:", topTip = true)
    private String isCover;//提示是否覆盖，如果是混放库位则提示：已存在该日期，是否覆盖
    //    不混放：
//    如果登记的条码，是之前登记过的，那么提示是否覆盖。
//    提示‘已存在该条码，是否覆盖’
//    混放：
//    如果登记的条码，是之前登记过的且日期相同，那么提示是否覆盖。
//    提示‘已存在该日期，是否覆盖’
//    如果登记的条码，是之前登记过的且日期不同，那么提示是否覆盖或新增。
//    提示‘已存在该条码，是否新增或覆盖该条码的所有记录’
    @Receiver(colTip = "请输入:", topTip = true)
    private String addOrCover;//会根据不同的情况修改此字段的提示

    //输入日期后计算得到
    private Date productionDate;//生产日期
    private Date expirationDate;//失效日期

    public String getReceivedWhCode() {
        return receivedWhCode;
    }

    public void setReceivedWhCode(String receivedWhCode) {
        this.receivedWhCode = receivedWhCode;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
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

    public String getIsCover() {
        return isCover;
    }

    public void setIsCover(String isCover) {
        this.isCover = isCover;
    }

    public String getAddOrCover() {
        return addOrCover;
    }

    public void setAddOrCover(String addOrCover) {
        this.addOrCover = addOrCover;
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
        return "InventoryAP{" +
                "receivedWhCode='" + receivedWhCode + '\'' +
                ", barCode='" + barCode + '\'' +
                ", prodOrExpDate='" + prodOrExpDate + '\'' +
                ", tooBigThanSysDate='" + tooBigThanSysDate + '\'' +
                ", confirmBU='" + confirmBU + '\'' +
                ", isCover='" + isCover + '\'' +
                ", addOrCover='" + addOrCover + '\'' +
                ", productionDate=" + productionDate +
                ", expirationDate=" + expirationDate +
                '}';
    }
}
