package com.womai.wms.rf.manager.window.menu;

/**
 * ClassDescribe:菜单枚举，通过name匹配中文desc，页面上维护的desc字段不起作用
 * Author :zhangwei
 * Date: 2016-11-28
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
public enum MenuNameEnum {

    goodsInfoManager("RF-goodsInfo", "商品信息维护"),
    inStock("RF-instock", "入库管理"),
    purchaseInStockManager("RF-rfCginASN", "采购入库单"),
    intentionInStockManager("RF-rfCgoutASN", "意向收货单"),
    intentionPostingManager("RF-rfCgoutConfirm", "意向单过账"),
    quickInStockManager("RF-rfQuickInASN", "意向单快捷收货"),
    check("RF-instockCheckMenu", "质检管理"),
    inStockPartCheckManager("RF-singleCheck", "单一明细质检"),
    inStockCheckManager("RF-allCheck", "整单质检"),
    transfer("RF-transferMenu", "交接管理"),
    preTransferShell("RF-transfer", "RF交接管理"),
    outStockConfirmManager("RF-transferShip", "出库确认"),
    queryTransOrderManager("RF-searchTransfer", "查询交接单"),
    shelfOrderManager("RF-shelfOrder", "上架移位"),
    productShelfManager("RF-productShelf", "加工上架"),
    stockMove("RF-stockMove", "移位管理"),
    createStockMoveManager("RF-createStockMove", "创建移位计划"),
    stockMoveOutManager("RF-stockMoveOut", "RF移出"),
    stockMoveInManager("RF-stockMoveIn", "RF移入"),
    freeze("RF-stockFreeze", "RF冻结管理"),
    freezeStockInfoManager("RF-freezeStockInfo", "RF冻结"),
    unfreezeStockInfoManager("RF-unfreezeStockInfo", "RF解冻"),
    replenish("RF-stockReplenish", "RF补货管理"),
    replenishMoveOutManager("RF-replenishMoveOut", "补货移出"),
    replenishMoveInManager("RF-replenishMoveIn", "补货移入"),
    stockquery("RF-queryStockInfo", "库存查询"),
    queryStockInfoByBarcodeManager("RF-queryStockInfoByBarcode", "按商品查询"),
    queryStockInfoByWhsCodeManager("RF-queryStockInfoByWhsCode", "按库位查询"),
    outStockPickOrderManager("RF-outstockPickOrder", "拣货管理"),
    outStockZonePickManager("RF-outstockZonePick", "分区拣货"),
    outStockZonePickOrderManager("RF-outstockZonePickOrder", "分区拣货管理"),
    cancleZonePickManager("RF-cancleZonePickBind", "取消拣货任务绑定"),
    outstockCanclePackOrderManager("RF-outstockCanclePackOrder","RF退拣"),
    outstockPickupManager("RF-outstockPickup", "集货管理"),
    outstockPickupContainerManager("RF-pickupContainer", "集周转箱"),
    queryWhsCodeBySendsheetnoManager("RF-queryWhsCodeBySendsheetno", "查询集货库位"),
    querySendsheetnoByWhsCodeManager("RF-querySendsheetnoByWhsCode", "查询发货单"),
    inventoryManager("RF-inventory", "盘点管理"),
    modifyPwdShellManager("RF-modifyPwd", "密码修改"),
    switchWarehouseShellManager("RF-switchWarehouse", "仓库切换");

    public final String authName;//权限名称
    public final String authDesc;//权限中文名称


    MenuNameEnum(String authName, String authDesc) {
        this.authName = authName;
        this.authDesc = authDesc;
    }

    /**
     * 按照权限名称获取权限中文名称
     *
     * @param authName 权限英文名称
     * @return 权限中文名称
     */
    public static String getMenuDesc(String authName) {
        for (MenuNameEnum t : MenuNameEnum.values()) {
            //因为页面上的权限名称修改后会变为大写，所以此处匹配权限名称时需要忽略大小写
            if (t.authName.equalsIgnoreCase(authName)) {
                return t.authDesc;
            }
        }
        return null;
    }


}
