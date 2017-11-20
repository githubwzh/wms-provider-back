package com.womai.wms.rf.remote.inventory;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.base.BaseWarehouseinfo;
import com.womai.zlwms.rfsoa.domain.inventory.InventoryInfo;
import com.womai.zlwms.rfsoa.domain.inventory.InventoryItem;
import com.womai.zlwms.rfsoa.domain.inventory.InventoryRegistItem;

import java.util.List;
import java.util.Map;

/**
 * ClassDescribe:盘点远程调用接口
 * Author :zhangwei
 * Date: 2016-11-08
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
public interface InventoryRemoteService {

    /**
     * 按照itemid、whsid、skuid查询登记数据
     *
     * @param credentialsVO 基础数据对象
     * @param queryItem     包含itemid、whsid、skuid
     * @return 登记数据集合
     */
    RemoteResult<List<InventoryRegistItem>> queryRegisterItem(CredentialsVO credentialsVO, InventoryRegistItem queryItem);


    /**
     * 按照库位编码查询库位、盘点主单、明细、登记明细四种数据，所需三个开关
     *
     * @param credentialsVO 基础数据对象
     * @param whCode        库位编码
     * @return 包含查询的四种数据
     */
    RemoteResult<Map<String, Object>> queryInfoByWhCode(CredentialsVO credentialsVO, String whCode);

    /**
     * 明盘的情况，查询一条可登记的数据
     *
     * @param credentialsVO 基础数据对象
     * @param wareHouseInfo 库位数据
     * @param inventoryInfo 盘点主单数据
     * @param inventoryItem 盘点明细数据
     * @return 一条可对登记的数据
     */
    RemoteResult<Map<String, Object>> getOneUnRegisteredDataForMP(CredentialsVO credentialsVO, BaseWarehouseinfo wareHouseInfo, InventoryInfo inventoryInfo, InventoryItem inventoryItem);


    /**
     * 用于明盘输入条码，按照商品条码及外包装码查询商品，确认商品是否存在于盘点登记数据中
     *
     * @param credentialsVO 基本数据对象
     * @param barCode       商品条码或外包装码
     * @param inventoryItem 盘点明细数据
     * @return 包含商品数据、是否存在于登记数据中
     */
    RemoteResult<Map<String, Object>> getGoodsByBarCodeForMP(CredentialsVO credentialsVO, String barCode, InventoryItem inventoryItem) throws Exception;


    /**
     * 按照商品条码及外包装码查询商品，确认商品是否存在于盘点登记数据中
     *
     * @param credentialsVO 基本数据对象
     * @param barCode       商品条码或外包装码
     * @param baseWarehouseinfo 库位数据，用于盘点库位是否混放
     * @param inventoryItem 盘点明细数据
     * @return 包含商品数据、是否存在于登记数据中
     */
    RemoteResult<Map<String, Object>> getGoodsByBarCodeForAP(CredentialsVO credentialsVO, String barCode, BaseWarehouseinfo baseWarehouseinfo,InventoryItem inventoryItem);

    /**
     * @param credentialsVO 基础数据对象
     * @param operateType   操作类型 none(明盘无差异或有差异后的提交)、新增登记数据、更新登记数据、删除+更新登记数据
     * @param registerParam 输入的盘点登记数据，包括skuid、productionDate、bu
     * @param inventoryItem 盘点明细数据
     * @param inventoryInfo 盘点主单
     * @return 根据不同的返回值类型，前端定位到不同的输入
     */
    RemoteResult<Integer> confirmRegister(CredentialsVO credentialsVO, Integer operateType, InventoryRegistItem registerParam, InventoryItem inventoryItem, InventoryInfo inventoryInfo);


    /**
     * 输入00结束库位盘点
     *
     * @param credentialsVO 基本数据对象
     * @param inventoryItem 明细数据，包含主单id、明细id、库位id等数据
     * @return 发生系统异常则返回空
     */
    RemoteResult<Map<String, Object>> endInventory(CredentialsVO credentialsVO, InventoryItem inventoryItem);



}
