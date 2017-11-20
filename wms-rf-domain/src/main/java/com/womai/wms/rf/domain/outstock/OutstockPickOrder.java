package com.womai.wms.rf.domain.outstock;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * ClassDescribe:拣货管理
 * Author :wangzhanhua
 * Date: 2016-11-05
 * Since
 * To change this template use File | Settings | File Templates.
 */
public class OutstockPickOrder {
    @Receiver(colTip = "拣货单号:",topTip = true)
    private String workSheetNo;
    @Receiver(colTip = "库位编码:",topTip = true)
    private String warehouseCode;
    @Receiver(colTip = "商品条码:",topTip = true)
    private String barcode;
    @Receiver(colTip = "拣货数量BU:",topTip = true)
    private String pickBu;
    @Receiver(colTip = "重新分配拣货库位,是否确认请按Y/N:",topTip = true)
    private String selectNoFir;
    @Receiver(colTip = "重新分配拣货库位,是否确认请按Y/N:",topTip = true)
    private String selectNoSec;
    @Receiver(colTip = "账号:",topTip = true)
    private String username;
    @Receiver(colTip = "密码:",topTip = true,encrypt = "*")
    private String password;
    @Receiver(colTip = "请选择缺货类型,1【缺货】,2【残品】：",topTip = true)
    private String reasonContent;//原因

    @Override
    public String toString() {
        return "OutstockPickOrder{" +
                "workSheetNo='" + workSheetNo + '\'' +
                ", warehouseCode='" + warehouseCode + '\'' +
                ", barcode='" + barcode + '\'' +
                ", pickBu='" + pickBu + '\'' +
                ", selectNoFir='" + selectNoFir + '\'' +
                ", selectNoSec='" + selectNoSec + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", reasonContent='" + reasonContent + '\'' +
                '}';
    }

    public String getSelectNoSec() {
        return selectNoSec;
    }

    public void setSelectNoSec(String selectNoSec) {
        this.selectNoSec = selectNoSec;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getReasonContent() {
        return reasonContent;
    }

    public void setReasonContent(String reasonContent) {
        this.reasonContent = reasonContent;
    }

    public String getSelectNoFir() {
        return selectNoFir;
    }

    public void setSelectNoFir(String selectNoFir) {
        this.selectNoFir = selectNoFir;
    }

    public String getWorkSheetNo() {
        return workSheetNo;
    }

    public void setWorkSheetNo(String workSheetNo) {
        this.workSheetNo = workSheetNo;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getPickBu() {
        return pickBu;
    }

    public void setPickBu(String pickBu) {
        this.pickBu = pickBu;
    }

}
