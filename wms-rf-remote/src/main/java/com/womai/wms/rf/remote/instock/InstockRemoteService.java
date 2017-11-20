package com.womai.wms.rf.remote.instock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.base.BasePallet;
import com.womai.zlwms.rfsoa.domain.instock.Instock;
import com.womai.zlwms.rfsoa.domain.instock.InstockDetail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User:zhangwei
 * Date: 2016-06-24
 * To change this template use File | Settings | File Templates.
 */
public interface InstockRemoteService {

    /**
     * 按照ASN单号查询
     *
     * @param credentialsVO 基础数据
     * @param asnCode       ASN单号
     * @return 查询到的主单数据，如果未查询到数据则返回空
     */
    RemoteResult<Instock> getPurchaseByASNCode(CredentialsVO credentialsVO, String asnCode);

    /**
     * 按照前台流水号查询换货意向单是否已经出库
     *
     * @param credentialsVO
     * @param serialNo      前台流水号
     * @return 已出库返回true，否则返回false
     */
    Boolean intentionIsOutStock(CredentialsVO credentialsVO, String serialNo);

    /**
     * 按照ASN单号或网络订单号查询意向单
     *
     * @param credentialsVO
     * @param ASNOrPurchaseCode asn或网络订单号
     * @return 返回查询的入库单主表数据
     */
    RemoteResult<List<Instock>> queryIntentionByASNCodeOrPurchaseCode(CredentialsVO credentialsVO, String ASNOrPurchaseCode);

    /**
     * 按照入库单主表ID及条码查询一条明细及一二级包装数据List
     *
     * @param credentialsVO 基础数据
     * @param asnInStockId  入库单主表ID
     * @param barCode       商品条码
     * @return 返回查询的map数据，按照key获取数据
     */
    Map<String, Object> queryDetailAndGoodsAndPackaging(CredentialsVO credentialsVO, Long asnInStockId, String barCode);

    /**
     * 按照operatorid、sku、pkid、生产日期检测是否存在重复数据
     *
     * @param credentialsVO
     * @param instockDetail 包含四个检测条件的数据对象
     * @return true：存在重复数据；false：不存在重复数据
     */
    Boolean existDuplicateDetail(CredentialsVO credentialsVO, InstockDetail instockDetail);

    /**
     * 查询同一个入库单下同一skuid、同一行号的明细数据
     *
     * @param credentialsVO
     * @param instockDetail 包含入库主单id、skuid、行号
     * @return 明细列表
     */
    List<InstockDetail> querySameSerialIDDetails(CredentialsVO credentialsVO, InstockDetail instockDetail);

    /**
     * 按照托盘编码查询托盘
     *
     * @param credentialsVO
     * @param palletCode    托盘编码
     * @return 查询到的托盘数据
     */
    BasePallet getPalletByCode(CredentialsVO credentialsVO, String palletCode);

    /**
     * 入库单主业务处理
     *
     * @param credentialsVO
     * @param instock       入库主单
     * @param instockDetail 入库单明细
     * @param orderType 需要处理的入库单类型
     * @return 返回处理结果
     */
    RemoteResult<String> confirmInStock(CredentialsVO credentialsVO, Instock instock, InstockDetail instockDetail,int[] orderType);


    /**
     * 根据入库单号或者网络订单号，查询主表
     *
     * @param instock 查询参数
     * @return 查询的主表集合
     */
    RemoteResult<List<Instock>> getByASNCodeOrPurchaseNo(CredentialsVO credentialsVO, Instock instock);

    /**
     * 根据网络订单号或ASN单号查询意向单列表
     * 1、是意向单，即ordertype=21、22、23
     * 2、不能是取消状态CANCEFLAG=0，未取消状态
     * 3、收货中、收货登记状态instock_status=21、22 ！！！区别于Web端的逻辑，将主单收货中状态的收货单也放在合理的过账范围之内
     * 4、实际收货数量不能大于计划收货数量，EXPECTNUMBU>=RECEIVENUMBU
     * 5、明细表中不能包含失效的商品，按照SKUID查询Base_goodsinfo.status=1的商品
     * 6、仓库编码instock.warehouseno不为空并且等于credentialsVO.getCurrentSite()
     * 7、明细表中必须包含至少一条是收货登记状态detailStatus=1 ！！！区别于Web端的逻辑，因为允许收货中状态的收货单进行过账，所以只要明细表中存在一条收货登记即可，不需要所有明细都是收货登记状态
     * 8、明细表中不能存在记录isphcard=1并且cardstatuc=0的数据
     * 9、明细表中实际收货数量getReceivenumbu之和等于主单的实际收货数量。
     * 10、不能是虚入的VIRTUALIN==1
     *
     * @param credentialsVO
     * @param key           网路订单号或ASN单号
     * @return 符合条件的意向单数据
     */
    RemoteResult<List<Instock>> getIntentionListByASNCodeOrPurchaseNo(CredentialsVO credentialsVO, String key);

    /**
     * 确认意向单过账
     * @param credentialsVO
     * @param asnInstockCode 意向单的ASNCode
     * @return 执行结果和执行错误提示信息
     */
    RemoteResult<String> confirmInstockPosting(CredentialsVO credentialsVO,String asnInstockCode);

    /**
     * 根据ASNCode查询入库单数据
     * @param credentialsVO
     * @param asnInstockCode ASN单号
     * @return 入库单数据
     */
    RemoteResult<Instock> getByAsnCode(CredentialsVO credentialsVO,String asnInstockCode);

    /**
     * 按照skuid及网络订单号查询出库的商品的生产日期
     * @param credentialsVO 基础数据对象
     * @param hashMap  商品ID，生产日期，网络单号，单据类型，单据id
     * @return 生产日期 RemoteResult 为true的话，校验通过允许入库，否则提示不符合条件的RemoteResult.T的值（生产日期）
     */
    RemoteResult<String> queryProDateInOutOrder(CredentialsVO credentialsVO,HashMap<String,Object> hashMap);

    /**
     * 分页查询入库明细信息
     * @param credentialsVO
     * @param paraMap 第几页，一页多少数据，入库主单id,商品id
     * @return 如果有异常,查询结果无数据，或者参数不对返回RemoteResult.success 为false
     */

    RemoteResult<PageModel<InstockDetail>> queryInstockDetailsPage(CredentialsVO credentialsVO, HashMap<String, Object> paraMap);

    /**
     * 意向单快捷收货主业务处理
     * @param credentialsVO
     * @param hashMap instock    入库主单 instockDetail 入库单明细 orderType[]  需要处理的入库单类型 check_status ,reasonid ,remark
     * @return 返回处理结果，如果全部收货true，否则false
     */
    RemoteResult<Boolean> confirmInStockFast(CredentialsVO credentialsVO, Map<String, Object> hashMap);

    /**
     * 意向单快捷收货-过账
     * @param credentialsVO
     * @param asnInstockCode ASN单号
     */
    RemoteResult<String> confirmInstockPostingAndCheckFast(CredentialsVO credentialsVO, String asnInstockCode);

    /**
     * 意向单快捷收货查询意向单
     * @param credentialsVO
     * @param scanCode
     * @return
     */
    RemoteResult<List<Instock>> queryIntentionByASNCodeOrPurchaseCodeFast(CredentialsVO credentialsVO, String scanCode);
}
