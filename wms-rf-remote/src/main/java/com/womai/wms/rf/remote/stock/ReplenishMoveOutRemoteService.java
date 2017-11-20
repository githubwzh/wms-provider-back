package com.womai.wms.rf.remote.stock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.stock.StockReplenishItem;

import java.util.HashMap;

/**
 * ClassDescribe: RF补货移出调用soa接口
 * Author :Xiafei Qi
 * Date: 2016-09-28
 * Since
 * To change this template use File | Settings | File Templates.
 */
public interface ReplenishMoveOutRemoteService {

    /**
     * 验证补货单号正确性，若验证通过返回补货单id
     *
     * @param credentialsVO 登录用户信息对象
     * @param shelfCode     补货单号
     * @return 如果验证通过，getT()返回补货子单id，若验证不通过，getResultCode()返回错误提示信息，注意长度控制在34个英文字符之内
     */
    RemoteResult<Long> validShelfCodeAndReturnShelfId(CredentialsVO credentialsVO, String shelfCode);

    /**
     * 验证补货库位正确性，包括库位下是否还有可补货移出的明细数据，若没有是错误
     *
     * @param credentialsVO 登录用户信息对象
     * @param condition     查询条件，包括补货单id和库位编码
     * @return 如果验证通过，返回值无用，若验证不通过，getResultCode()返回错误提示信息，注意长度控制在34个英文字符之内
     */
    RemoteResult<String> validSrcWhsCode(CredentialsVO credentialsVO, StockReplenishItem condition);

    /**
     * 验证商品条码并返回在补货单id、库位编码、skuid下的可补货移出的明细分页数据，若查询不到明细是数据过期
     *
     * @param credentialsVO 登录用户信息对象
     * @param condition     查询条件补货单id、库位编码、分页信息
     * @param barCode       商品条码
     * @return 如果验证通过，getT() 返回分页数据，若验证不通过，getResultCode()返回错误提示信息，注意长度控制在34个英文字符之内
     */
    RemoteResult<PageModel<StockReplenishItem>> validBarCodeAndGetItemPage(CredentialsVO credentialsVO, HashMap<String, Object> condition, String barCode);

    /**
     * 查询补货单id、库位编码、skuid下某一页的可补货移出的明细分页数据
     *
     * @param credentialsVO 登录用户信息对象
     * @param condition     查询条件补货单id、库位编码、skuid 分页信息
     * @return getT()返回分页数据
     */
    RemoteResult<PageModel<StockReplenishItem>> getPage(CredentialsVO credentialsVO, HashMap<String, Object> condition);

    /**
     * RF补货移出提交
     *
     * @param credentialsVO              登录用户信息对象
     * @param selectedStockReplenishItem 移出明细
     * @param moveoutBu                  移出数量bu
     * @return 提交成功getT() 返回码
     * 1、若移出后当前补货单下全部移出，则提示“当前补货单已全部移出，任意键继续”，跳转到页面初始界面。
     * 2、若移出后当前补货单下未全部移出，但无可操作明细，则提示“操作成功，任意键继续”，跳转到页面初始界面。
     * 3、若当前库位全部移出，但是还存在其他货位未全部移出，则提示“当前库位已全部移出，任意键继续”，跳转到扫描库位。
     * 4、若当前库位未全部移出，但无可操作明细，则提示“操作成功，任意键继续”，跳转到扫描库位。
     * 5、若当前库位未全部移出，并且有可操作明细，则提示“操作成功，任意键继续”，跳转到扫描商品条码。
     * 提交失败getResultCode()返回错误提示信息，长度不限
     */
    RemoteResult<Integer> submitAndGetReturnCode(CredentialsVO credentialsVO, StockReplenishItem selectedStockReplenishItem, int moveoutBu);
}
