package com.womai.wms.rf.common.constants;

import com.womai.zlwms.rfsoa.api.WMSErrorMess;
import com.womai.zlwms.rfsoa.api.WmsConstants;

/**
 * 用于放置各个模块的错误提示
 * User:zhangwei
 * Date: 2016-07-04
 * To change this template use File | Settings | File Templates.
 */
public class ErrorConstants {


    public final static String COMMON_PUNCTUATION = ",";//通用标点符号，逗号
    public final static String COMMON_COLON = ":";//通用标点符号，冒号
    public final static String OPERATE_SUCCESS = "操作完成";
    public final static String ILLEGAL_DATA = "数据不合法";
    public final static String POSITIVE_INTEGER = "请输入正整数";
    public final static String DATA_NOT_FOUNT = "数据不存在";
    public final static String SYS_ERROR = "系统内部错误";
    public final static String DATA_EXPIRED = "数据已过期";
    public final static String WORK_TYPE_ERROR = "操作方式错误";
    public final static String ANY_KEY_CONTINUE = "任意键继续";//提示按任意键继续
    public final static String ONLY_YN = "请输入Y/N";
    public final static String TIP_TO_CONTINUE = COMMON_PUNCTUATION + ANY_KEY_CONTINUE;//错误提示，按任意键继续
    public final static String ILLEGAL_DATACONTINUE = DATA_NOT_FOUNT + COMMON_PUNCTUATION + ANY_KEY_CONTINUE;//非法数据，按任意键继续
    public final static String DATA_NOT_FOUNT_CONTINUE = DATA_NOT_FOUNT + COMMON_PUNCTUATION + ANY_KEY_CONTINUE;//提示数据不存在，按任意键继续
    public final static String SYS_ERROR_CONTINUE = SYS_ERROR + COMMON_PUNCTUATION + ANY_KEY_CONTINUE;//系统错误,任意键继续
    public final static String DATA_EXPIRED_CONTINUE = DATA_EXPIRED + COMMON_PUNCTUATION + ANY_KEY_CONTINUE;//数据已过期,任意键继续
    public final static String SUCCESS_CONTINUE = OPERATE_SUCCESS + COMMON_PUNCTUATION + ANY_KEY_CONTINUE;//操作完成,任意键继续
    public final static String ONLY_YN_AND_OTHER = ONLY_YN;//请输入Y/N,
    public final static String INPUT_FORMAT_ERROR = "输入有误,请重新输入";//当输入的内容格式错误时的提示

    //---------------商品信息维护---------------//
    public final static String GOODSINFO_BARCODE_ERROR = "商品未维护,请重新输入";//当查询不到商品时的提示
    public final static String GOODSINFO_DISABLE_ERROR = "商品失效，不能新建二级包装";//商品失效
    public final static String GOODSINFO_PACKAGINGINFO_LEVEL1_ERROR = "该商品一级包装未维护，任意键继续";//当查询不到商品一级包装时的提示
    public final static String GOODSINFO_PACKAGINGINFO_LEVEL2_ERROR = "该商品二级包装未维护，任意键继续";//当查询不到商品时的提示
    public final static String GOODSINFO_PACKAGINGINFO_LEVEL1_NAME_ERROR = "一级包装名称为 箱 请通过页面修改名称，任意键继续";//当查询不到商品时的提示
    public final static String GOODSINFO_PACKAGINGINFO_LEVEL1_CUBAGE_ERROR = "一级包装体积不能大于二级包装，任意键重新输入";
    public final static String GOODSINFO_PACKAGINGINFO_LEVEL1_WEIGHT_ERROR = "一级包装重量不能大于二级包装，任意键重新输入";
    public final static String GOODSINFO_PACKAGINGINFO_LEVEL2_CUBAGE_ERROR = "二级包装体积不能小于一级包装，任意键重新输入";
    public final static String GOODSINFO_PACKAGINGINFO_LEVEL2_WEIGHT_ERROR = "二级包装重量不能小于一级包装，任意键重新输入";
    public final static String PACKAGINGINFO_LEVEL2_CUBAGE_ERROR = "二级包装体积不能小于一级包装，重新输入";
    public final static String PACKAGINGINFO_LEVEL2_WEIGHT_ERROR = "二级包装重量不能小于一级包装，重新输入";

    //---------------采购入库单登记---------------//
    public final static String UNIT_NAME_NOT_FOUNT = "包装单位未维护" + COMMON_PUNCTUATION;
    public final static String ASN_NOT_FOUND = "查询无ASN单号" + COMMON_PUNCTUATION;
    public final static String DATE_TYPE_NOT_IN_LIST = "无效日期类型" + COMMON_PUNCTUATION;
    public final static String ILLEGAL_DATE_PATTERN = "日期格式不合法";
    public final static String PRO_BIGGER_NOW = "生产日期不能大于当前日期";
    public final static String EXP_LESS_NOW = "失效日期不能小于等于当前日期";
    public final static String NEAR_DATE_BIGGER_NOW = "商品已过近效期";
    public final static String EXIST_DUPLICATE_DETAIL = "生产日期、SKUID、包装单位重复";
    public final static String BIGGER_THAN_EXPECT = "超过预期收货数量";
    public final static String PACK_LEVEL_TWO_EXPECT = "二级包装失效";
    public final static String MUST_BE_INTEGRAL_MULTIPLE = "输入数量必须为箱规的整数倍" + COMMON_PUNCTUATION;
    public final static String ILLEGAL_PALLET_CODE = "托盘编码不合法";

    //---------------意向入库单登记---------------//
    public final static String NOT_OUT_STOCK = "换货单未出库";

    //---------------意向单过账---------------//
    public final static String INTENTION_POSTING_KEY_ERROR = "查询无订单数据" + COMMON_PUNCTUATION + "请重新输入";

    //---------------质检---------------//
    public final static String DATA_ERROR_01 = "数量校验不通过" ;
    public final static String DATA_ERROR_02 = "请输入正整数" ;
    public final static String DATA_ERROR_03 = "数量超过待质检数量" ;
    public final static String PLS_MAINTAION_REASON = "请维护质检原因";
    public final static String PLS_INPUT_RIGHT_NUM = "请输入箱规的整数倍";

    //----------------上架----------------//
    public final static String PRO_DATE_NOT_LEGAL = "请输入该批次商品生产日期";
    public final static String EXP_DATE_NOT_LEGAL = "请输入该批次商品失效日期";
    public final static String TOO_MUCH_SHELF_NUM = "超过未上架数";
    public final static String NOT_MATCH_PK_NUM = "二级包装需要为箱规整数倍";


    //---------------移位管理---------------//
    public final static String PLS_INPUT_CAN_MOVE_NUM = "超出可移位BU";
    public final static String STOCK_MOVE_UNFIN_EXIST = "有未完成移位单:";
    public final static String STOCK_MOVE_UNFIN_EXIST_MORE = "有多个未完成的移位单";
    public final static String STOCK_MOVE_MORE_EXIST = "有多个移位单";
    public final static String STOCKINFO_EMPTY = "无可用移位库存";
    public final static String STOCKINFO_MOVE_NUM_NONEXISTENCE = "无可移位BU数";
    public final static String PLS_MAINTAION_MOVE_REASON = "请维护移位原因";
    public final static String STOCK_MOVE_ADD_ITEM_FAIL = "添加明细失败";
    public final static String STOCK_MOVE_SKUSTATUS_ERR = "商品状态错误";
    public final static String STOCK_MOVE_ALREDY_DISTR = "已经分配，请激活";
    public final static String STOCK_MOVE_CREATE_TYPE_ERR = "请扫描WEB创建移位单";
    public final static String STOCK_MOVE_WAREHOUSECODE_ERR = "不可移入原移出库位";
    public final static String STOCK_MOVE_SCAN_CREATE_TYPE_ERR = "扫描的移位单非WEB创建";
    //--------------------RF冻结、解冻------------------------------//
    public final static String STOCK_INFO_FREEZE_NUM_BEYOND = "超出可冻结BU";
    public final static String STOCK_INFO_FREEZE_EMPTY = "无可冻结库存";
    public final static String STOCK_INFO_UNFREEZE_NUM_BEYOND = "超出可解冻BU";
    public final static String STOCK_INFO_UNFREEZE_EMPTY = "无可解冻库存";
    public final static String BASE_WAREHOUSE_INFO_TYPE_ERR = "库位类型错误";
    public final static String BASE_WAREHOUSE_INFO_STATUS_ERR = "非生效库位";
    //---------------RF补货移出---------------//
    public final static String LAST_PAGE = "已至末页";
    public final static String FIRST_PAGE = "已至首页";
    //---------------RF补货移入---------------//
    public final static String REPLENISH_NO_DATA = "无可操作数据";
    public final static String REPLENISH_TOO_MUCH = "超过可移入数量";
    public final static String REPLENISH_NO_PACK= "无有效包装数据";

    //--------------库存查询-----------------------//
    public final static String QUERY_STOCKINFO_EMPTY = "无库存数据";
    //--------------盘点-----------------------//
    public final static String INVENTORY_DATA_EXPIRY = "日期已登记,不可重复"+TIP_TO_CONTINUE;



    //-----------RF拣货----------------------------//

    public final static String INPUT_ERROR = "输入不合法";
    public final static String OUTSTOCK_PICK_ORDER_USERNAME_ERROR = "开关管理中无此拣货账号";
    public final static String USERNAME_NOTEXSIT = "用户名不存在";
    public final static String PASSWORD_ERROR = "密码错误";
    public final static String WAREHOUSE_CODE_ERROR = "扫描库位错误";
    public final static String BARCODE_OR_PKBARCODE_ERROR = "扫描商品条码或外包装码错误";
    public final static String OUTSTOCK_PICK_ORDER_NUM_ERROR = "请输入正确的数量";

    public enum ErrorCodeParzer{
        dataExpiry(WMSErrorMess.DATA_EXPIRY_CODE,"数据过期"),
        worktypeError(WMSErrorMess.WORKTYPE_ERROR_CODE,"操作方式错误"),
        pickOrderTypeError(WMSErrorMess.OUTSTOCK_PICK_ORDER,"含有虚出标示的发货单"),
        statusError(WMSErrorMess.OUTSTOCK_PICK_ORDER_STATE_ERROR,"状态错误"),
        systemError(WMSErrorMess.ERROR_SYSTEM_CODE,"发生系统错误"),
        paraError(WMSErrorMess.ERROR_PARAMETER_CODE,"参数不合法"),
        queryError(WMSErrorMess.ERROR_QUERY_CODE,"查询不到结果"),
        quantityError(WMSErrorMess.OUTSTOCK_PICK_ORDER_ALL_ZERO_ERROR,"拣货不允许全部为0"),
        assignDataError(WMSErrorMess.OUTSTOCK_PICK_ORDER_ASSIGN_DATE_STOCKOUT_ERROR,"指定生产日期库存不足,请补货");
        private  String code;//错误代码
        private String value;//错误信息
        ErrorCodeParzer(String code,String value){
            this.code  = code;
            this.value = value;
        }
        /**
         * 解析错误代码，获得对应的错误信息
         * @param code 错误代码
         * @return 错误信息
         */
        public static String parzeErrorCode(String code) {
            for (ErrorCodeParzer t : ErrorCodeParzer.values()) {
                if (t.code.equals(code)) {
                    return t.value;
                }
            }
            return "";
        }

    }
}
