package com.womai.wms.rf.domain.instock;

import com.womai.wms.rf.common.annotation.Receiver;

import java.util.Date;

/**
 * Created by keke on 17-8-28.
 */
public class QuickInstock {
    @Receiver(colTip = "ASN单号/网上订单号:",cursorDown = true,topTip = true)
    private String scanCode;//扫描的ASN单号
    @Receiver(colTip = "选择ASN单号:")
    private String selectAsn;
    @Receiver(colTip = "商品条码:",topTip = true)
    private String barCode;//商品条码
    @Receiver(colTip = "选择明细:",topTip = true)
    private String selectPage;//明细翻页
    @Receiver(colTip = "选择日期:")
    private String dateType;//日期类型，选择生产日期或失效期
    @Receiver(colTip = "输入日期(yyyyMMdd格式):",topTip = true)
    private String prodOrExpDate;//生产日期或失效日期
    @Receiver(colTip = "收货数量:",topTip = true)
    private String checkNum;//收货数量，实际质检数量
    @Receiver(colTip = "质检结果:",topTip = true)
    private String checkResult;//质检结果
    @Receiver(colTip = "选择序号:",topTip = true)
    private String reasonid;
    @Receiver(colTip = "托盘编码(留空请回车):",canNull = true,topTip = true)
    private String palletCode;//托盘编码
    @Receiver(colTip = "是否确认收货(Y/N):",topTip = true)
    private String confirmIn;
    @Receiver(colTip = "是否过账质检(Y/N):",topTip = true)
    private String confirmCheck;
    @Receiver(colTip = "过账质检完成！按Y键上架，任意键继续！",canNull = true,topTip = true)
    private String confirmShelf;

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

    public String getScanCode() {
        return scanCode;
    }

    public void setScanCode(String scanCode) {
        this.scanCode = scanCode;
    }

    public String getSelectAsn() {
        return selectAsn;
    }

    public void setSelectAsn(String selectAsn) {
        this.selectAsn = selectAsn;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

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

    public String getProdOrExpDate() {
        return prodOrExpDate;
    }

    public void setProdOrExpDate(String prodOrExpDate) {
        this.prodOrExpDate = prodOrExpDate;
    }

    public String getCheckNum() {
        return checkNum;
    }

    public void setCheckNum(String checkNum) {
        this.checkNum = checkNum;
    }

    public String getCheckResult() {
        return checkResult;
    }

    public void setCheckResult(String checkResult) {
        this.checkResult = checkResult;
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

    public String getConfirmCheck() {
        return confirmCheck;
    }

    public void setConfirmCheck(String confirmCheck) {
        this.confirmCheck = confirmCheck;
    }

    public String getConfirmShelf() {
        return confirmShelf;
    }

    public void setConfirmShelf(String confirmShelf) {
        this.confirmShelf = confirmShelf;
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

    public String getReasonid() {
        return reasonid;
    }

    public void setReasonid(String reasonid) {
        this.reasonid = reasonid;
    }
}
