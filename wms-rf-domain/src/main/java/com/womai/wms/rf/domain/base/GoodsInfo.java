package com.womai.wms.rf.domain.base;

import com.womai.wms.rf.common.annotation.Receiver;
import com.womai.wms.rf.domain.BaseDomain;

/**
 * 描述: 商品信息维护功能
 * User:Qi Xiafei
 * Date: 2016-06-20
 * To change this template use File | Settings | File Templates.
 */
public class GoodsInfo extends BaseDomain {

    /**
     * 商品ID，来自ERP，全站唯一，不可修改 *
     */
    private Long skuid;
    /**
     * 商品名称，来自erp *
     */
    private String skuname;
    /**
     * 商品状态 0生效1失效 *
     */
    private Integer status;
    /**
     * 保质期 *
     */
    private Integer keepdays;
    /**
     * 商品条码  *
     */
    @Receiver(colTip = "商品条码:", cursorDown = true,topTip = true)
    private String barcode;

    /**
     * 批次规格 0：普通批次规则，1：百货批次规则，2：洗化批次规则*
     */
    @Receiver(colTip = "选择批次规则:",topTip = true)
    private String batchrule;

    @Receiver(colTip = "商品上架属性:",topTip = true)
    private String shelfFlag;// 商品上架属性
    /**
     * 虚拟列，要维护的包装级别
     */
    @Receiver(colTip = "要维护的包装级别:")
    private String packageLevelToMaintain;
    /**
     * 商品一级包装长度
     */
    @Receiver(colTip = "单品 大边（米m）:")
    private String packageLevel1Length;
    /**
     * 商品一级包装宽度
     */
    @Receiver(colTip = "单品 中边（米m）:")
    private String packageLevel1Width;
    /**
     * 商品一级包装高度
     */
    @Receiver(colTip = "单品 小边（米m）:")
    private String packageLevel1Height;
    /**
     * 商品一级包装重量
     */
    @Receiver(colTip = "单品 重量（公斤kg）:")
    private String packageLevel1Weight;
    /**
     * 商品一级包装是否维护码托
     */
    @Receiver(colTip = "是否维护码托（Y/N）:")
    private String packageLevel1Ismt;
    /**
     * 商品一级包装起码数量
     */
    @Receiver(colTip = "起码数量:")
    private String packageLevel1Startyardnm;
    /**
     * 商品一级包装托盘码放层数
     */
    @Receiver(colTip = "托盘码放层数:")
    private String packageLevel1Traylevel;
    /**
     * 商品一级包装单层码托数量
     */
    @Receiver(colTip = "单层码托数量:")
    private String packageLevel1Oneyardnum;
    /**
     * 商品二级包装长度
     */
    @Receiver(colTip = "整箱 大边（米m）:")
    private String packageLevel2Length;
    /**
     * 商品二级包装宽度
     */
    @Receiver(colTip = "整箱 中边（米m）:")
    private String packageLevel2Width;
    /**
     * 商品二级包装高度
     */
    @Receiver(colTip = "整箱 小边（米m）:")
    private String packageLevel2Height;
    /**
     * 商品二级包装重量
     */
    @Receiver(colTip = "整箱 重量（公斤kg）:")
    private String packageLevel2Weight;
    /**
     * 商品二级包装是否维护码托
     */
    @Receiver(colTip = "是否维护码托（Y/N）:")
    private String packageLevel2Ismt;
    /**
     * 商品二级包装起码数量
     */
    @Receiver(colTip = "起码数量:")
    private String packageLevel2Startyardnm;
    /**
     * 商品二级包装托盘码放层数
     */
    @Receiver(colTip = "托盘码放层数:")
    private String packageLevel2Traylevel;
    /**
     * 商品二级包装单层码托数量
     */
    @Receiver(colTip = "单层码托数量:")
    private String packageLevel2Oneyardnum;

    public String getPackageLevel1Length() {
        return packageLevel1Length;
    }

    public void setPackageLevel1Length(String packageLevel1Length) {
        this.packageLevel1Length = packageLevel1Length;
    }

    public String getPackageLevelToMaintain() {
        return packageLevelToMaintain;
    }

    public void setPackageLevelToMaintain(String packageLevelToMaintain) {
        this.packageLevelToMaintain = packageLevelToMaintain;
    }

    public String getPackageLevel1Width() {
        return packageLevel1Width;
    }

    public void setPackageLevel1Width(String packageLevel1Width) {
        this.packageLevel1Width = packageLevel1Width;
    }

    public String getPackageLevel1Height() {
        return packageLevel1Height;
    }

    public void setPackageLevel1Height(String packageLevel1Height) {
        this.packageLevel1Height = packageLevel1Height;
    }

    public String getPackageLevel1Weight() {
        return packageLevel1Weight;
    }

    public void setPackageLevel1Weight(String packageLevel1Weight) {
        this.packageLevel1Weight = packageLevel1Weight;
    }

    public String getPackageLevel1Ismt() {
        return packageLevel1Ismt;
    }

    public void setPackageLevel1Ismt(String packageLevel1Ismt) {
        this.packageLevel1Ismt = packageLevel1Ismt;
    }

    public String getPackageLevel1Startyardnm() {
        return packageLevel1Startyardnm;
    }

    public void setPackageLevel1Startyardnm(String packageLevel1Startyardnm) {
        this.packageLevel1Startyardnm = packageLevel1Startyardnm;
    }

    public String getPackageLevel1Traylevel() {
        return packageLevel1Traylevel;
    }

    public void setPackageLevel1Traylevel(String packageLevel1Traylevel) {
        this.packageLevel1Traylevel = packageLevel1Traylevel;
    }

    public String getPackageLevel1Oneyardnum() {
        return packageLevel1Oneyardnum;
    }

    public void setPackageLevel1Oneyardnum(String packageLevel1Oneyardnum) {
        this.packageLevel1Oneyardnum = packageLevel1Oneyardnum;
    }

    public String getPackageLevel2Length() {
        return packageLevel2Length;
    }

    public void setPackageLevel2Length(String packageLevel2Length) {
        this.packageLevel2Length = packageLevel2Length;
    }

    public String getPackageLevel2Width() {
        return packageLevel2Width;
    }

    public void setPackageLevel2Width(String packageLevel2Width) {
        this.packageLevel2Width = packageLevel2Width;
    }

    public String getPackageLevel2Height() {
        return packageLevel2Height;
    }

    public void setPackageLevel2Height(String packageLevel2Height) {
        this.packageLevel2Height = packageLevel2Height;
    }

    public String getPackageLevel2Weight() {
        return packageLevel2Weight;
    }

    public void setPackageLevel2Weight(String packageLevel2Weight) {
        this.packageLevel2Weight = packageLevel2Weight;
    }

    public String getPackageLevel2Ismt() {
        return packageLevel2Ismt;
    }

    public void setPackageLevel2Ismt(String packageLevel2Ismt) {
        this.packageLevel2Ismt = packageLevel2Ismt;
    }

    public String getPackageLevel2Startyardnm() {
        return packageLevel2Startyardnm;
    }

    public void setPackageLevel2Startyardnm(String packageLevel2Startyardnm) {
        this.packageLevel2Startyardnm = packageLevel2Startyardnm;
    }

    public String getPackageLevel2Traylevel() {
        return packageLevel2Traylevel;
    }

    public void setPackageLevel2Traylevel(String packageLevel2Traylevel) {
        this.packageLevel2Traylevel = packageLevel2Traylevel;
    }

    public String getPackageLevel2Oneyardnum() {
        return packageLevel2Oneyardnum;
    }

    public void setPackageLevel2Oneyardnum(String packageLevel2Oneyardnum) {
        this.packageLevel2Oneyardnum = packageLevel2Oneyardnum;
    }

    public Long getSkuid() {
        return skuid;
    }

    public void setSkuid(Long skuid) {
        this.skuid = skuid;
    }

    public String getSkuname() {
        return skuname;
    }

    public void setSkuname(String skuname) {
        this.skuname = skuname;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getBatchrule() {
        return batchrule;
    }

    public void setBatchrule(String batchrule) {
        this.batchrule = batchrule;
    }

    public Integer getKeepdays() {
        return keepdays;
    }

    public void setKeepdays(Integer keepdays) {
        this.keepdays = keepdays;
    }



    public String getShelfFlag() {
        return shelfFlag;
    }

    public void setShelfFlag(String shelfFlag) {
        this.shelfFlag = shelfFlag;
    }

    @Override
    public String toString() {
        return "GoodsInfo{" +
                "skuid=" + skuid +
                ", skuname='" + skuname + '\'' +
                ", status=" + status +
                ", keepdays=" + keepdays +
                ", barcode='" + barcode + '\'' +
                ", batchrule='" + batchrule + '\'' +
                ", shelfFlag='" + shelfFlag + '\'' +
                ", packageLevelToMaintain='" + packageLevelToMaintain + '\'' +
                ", packageLevel1Length='" + packageLevel1Length + '\'' +
                ", packageLevel1Width='" + packageLevel1Width + '\'' +
                ", packageLevel1Height='" + packageLevel1Height + '\'' +
                ", packageLevel1Weight='" + packageLevel1Weight + '\'' +
                ", packageLevel1Ismt='" + packageLevel1Ismt + '\'' +
                ", packageLevel1Startyardnm='" + packageLevel1Startyardnm + '\'' +
                ", packageLevel1Traylevel='" + packageLevel1Traylevel + '\'' +
                ", packageLevel1Oneyardnum='" + packageLevel1Oneyardnum + '\'' +
                ", packageLevel2Length='" + packageLevel2Length + '\'' +
                ", packageLevel2Width='" + packageLevel2Width + '\'' +
                ", packageLevel2Height='" + packageLevel2Height + '\'' +
                ", packageLevel2Weight='" + packageLevel2Weight + '\'' +
                ", packageLevel2Ismt='" + packageLevel2Ismt + '\'' +
                ", packageLevel2Startyardnm='" + packageLevel2Startyardnm + '\'' +
                ", packageLevel2Traylevel='" + packageLevel2Traylevel + '\'' +
                ", packageLevel2Oneyardnum='" + packageLevel2Oneyardnum + '\'' +
                '}';
    }
}
