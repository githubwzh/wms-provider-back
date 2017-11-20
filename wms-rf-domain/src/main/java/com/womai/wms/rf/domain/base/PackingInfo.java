package com.womai.wms.rf.domain.base;

import com.womai.wms.rf.common.annotation.Receiver;
import com.womai.wms.rf.common.constants.TipConstants;
import com.womai.wms.rf.domain.BaseDomain;

/**
 * ClassDescribe:
 * Author :wangzhanhua
 * Date: 2017-03-07
 * Since
 * To change this template use File | Settings | File Templates.
 */
public class PackingInfo extends BaseDomain {

    @Receiver(colTip = "包装单位:",topTip = true)
    private String unitname = TipConstants.PK_LEVEL2_NAME;//二级包装单位“箱”
    /**
     * 商品二级包装箱规
     */
    @Receiver(colTip = "箱规:",topTip = true)
    private String pknum;
    /**
     * 商品二级包装长度
     */
    @Receiver(colTip = "整箱 大边（米m）:",topTip = true)
    private String packageLevel2Length;
    /**
     * 商品二级包装宽度
     */
    @Receiver(colTip = "整箱 中边（米m）:",topTip = true)
    private String packageLevel2Width;
    /**
     * 商品二级包装高度
     */
    @Receiver(colTip = "整箱 小边（米m）:",topTip = true)
    private String packageLevel2Height;
    /**
     * 商品二级包装重量
     */
    @Receiver(colTip = "整箱 重量（公斤kg）:",topTip = true)
    private String packageLevel2Weight;
    /**
     * 商品二级包装是否维护码托
     */
    @Receiver(colTip = "是否维护码托（Y/N）:",topTip = true)
    private String packageLevel2Ismt;
    /**
     * 商品二级包装起码数量
     */
    @Receiver(colTip = "起码数量:",topTip = true)
    private String packageLevel2Startyardnm;
    /**
     * 商品二级包装托盘码放层数
     */
    @Receiver(colTip = "托盘码放层数:",topTip = true)
    private String packageLevel2Traylevel;
    /**
     * 商品二级包装单层码托数量
     */
    @Receiver(colTip = "单层码托数量:",topTip = true)
    private String packageLevel2Oneyardnum;

    public String getUnitname() {
        return unitname;
    }

    public void setUnitname(String unitname) {
        this.unitname = unitname;
    }

    public String getPknum() {
        return pknum;
    }

    public void setPknum(String pknum) {
        this.pknum = pknum;
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

    @Override
    public String toString() {
        return "PackingInfo{" +
                "unitname='" + unitname + '\'' +
                ", pknum='" + pknum + '\'' +
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
