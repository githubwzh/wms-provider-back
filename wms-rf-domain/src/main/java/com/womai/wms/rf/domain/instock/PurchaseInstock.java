package com.womai.wms.rf.domain.instock;

import com.womai.wms.rf.common.annotation.Receiver;
import com.womai.wms.rf.domain.BaseDomain;

import java.util.Date;

/**
 * 采购单入库业务domain
 * User:zhangwei
 * Date: 2016-06-20
 * To change this template use File | Settings | File Templates.
 */
public class PurchaseInstock extends BaseDomain {

    @Receiver(colTip = "ASN单号/ERP单号:",cursorDown = true,topTip = true)
    private String scanCode;//扫描的ASN单号
    @Receiver(colTip = "商品条码:",cursorDown = true,topTip = true)
    private String barCode;//商品条码
    @Receiver(colTip = "选择序号:",topTip = true)
    private String selectPage;//明细翻页
    @Receiver(colTip = "包装单位:")
    private String unitName;//包装单位
    @Receiver(colTip = "选择日期类型:")
    private String dateType;//日期类型，选择生产日期或失效期
    @Receiver(colTip = "输入日期(yyyyMMdd格式):",topTip = true)
    private String prodOrExpDate;//生产日期或失效日期
    @Receiver(colTip = "是否继续收货(Y/N):",topTip = true)
    private String isContinue;//当前日期超过允收期，提示是否继续收货
    @Receiver(colTip = "收货数量:",topTip = true)
    private String checkNum;//收货数量，实际质检数量
    @Receiver(colTip = "托盘编码(留空请回车):",canNull = true,topTip = true)
    private String palletCode;//托盘编码
    @Receiver(colTip = "确认收货(Y/N):",topTip = true)
    private String confirmIn;

    //根据选择的包装更新包装数据

    private Long pkId;//包装ID
    private Integer pkLevel;//包装级别
    private String newUnitName;//选择的包装单位
    private String spec;//包装规格
    private Integer pkNum;//箱规


    //根据选择的日期类型及输入的日期计算各个日期
    private Date productionDate;//生产日期
    private Date expirationDate;//失效期
    private Date nearValidDate;//近效期
    private Date canInStockDate;//允收期

    public String getSelectPage() {
        return selectPage;
    }

    public void setSelectPage(String selectPage) {
        this.selectPage = selectPage;
    }

    public String getDateType() {
        return dateType;
    }

    public void setDateType(String dateType) {
        this.dateType = dateType;
    }

    public String getScanCode() {
        return scanCode;
    }

    public void setScanCode(String scanCode) {
        this.scanCode = scanCode;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getProdOrExpDate() {
        return prodOrExpDate;
    }

    public void setProdOrExpDate(String prodOrExpDate) {
        this.prodOrExpDate = prodOrExpDate;
    }

    public String getIsContinue() {
        return isContinue;
    }

    public void setIsContinue(String isContinue) {
        this.isContinue = isContinue;
    }

    public String getCheckNum() {
        return checkNum;
    }

    public void setCheckNum(String checkNum) {
        this.checkNum = checkNum;
    }

    public String getPalletCode() {
        return palletCode;
    }

    public void setPalletCode(String palletCode) {
        this.palletCode = palletCode;
    }

    public String getConfirmIn() {
        return confirmIn;
    }

    public void setConfirmIn(String confirmIn) {
        this.confirmIn = confirmIn;
    }

    public Long getPkId() {
        return pkId;
    }

    public void setPkId(Long pkId) {
        this.pkId = pkId;
    }

    public Integer getPkLevel() {
        return pkLevel;
    }

    public void setPkLevel(Integer pkLevel) {
        this.pkLevel = pkLevel;
    }

    public String getNewUnitName() {
        return newUnitName;
    }

    public void setNewUnitName(String newUnitName) {
        this.newUnitName = newUnitName;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public Integer getPkNum() {
        return pkNum;
    }

    public void setPkNum(Integer pkNum) {
        this.pkNum = pkNum;
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

    public Date getNearValidDate() {
        return nearValidDate;
    }

    public void setNearValidDate(Date nearValidDate) {
        this.nearValidDate = nearValidDate;
    }

    public Date getCanInStockDate() {
        return canInStockDate;
    }

    public void setCanInStockDate(Date canInStockDate) {
        this.canInStockDate = canInStockDate;
    }

    @Override
    public String toString() {
        return "PurchaseInstock{" +
                "scanCode='" + scanCode + '\'' +
                ", barCode='" + barCode + '\'' +
                ", unitName='" + unitName + '\'' +
                ", prodOrExpDate='" + prodOrExpDate + '\'' +
                ", isContinue='" + isContinue + '\'' +
                ", checkNum='" + checkNum + '\'' +
                ", palletCode='" + palletCode + '\'' +
                ", confirmIn='" + confirmIn + '\'' +
                ", pkId=" + pkId +
                ", pkLevel=" + pkLevel +
                ", newUnitName='" + newUnitName + '\'' +
                ", spec='" + spec + '\'' +
                ", pkNum=" + pkNum +
                ", productionDate=" + productionDate +
                ", expirationDate=" + expirationDate +
                ", nearValidDate=" + nearValidDate +
                ", canInStockDate=" + canInStockDate +
                '}';
    }
}
