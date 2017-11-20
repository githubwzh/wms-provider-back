package com.womai.wms.rf.common.constants;

/**
 * 常量类，用于放置各个模块需要的提示，
 * 例如当前界面位置等
 * User:zhangwei
 * Date: 2016-07-04
 * To change this template use File | Settings | File Templates.
 */
public class TipConstants {


    //----------------通用----------------//

    public final static String NAME = "名称";//商品名称
    public final static String PK_LEVEL2_NAME = "箱";//二级包装默认单位
    public final static String GOODS_NAME = "名称:";//商品名称
    public final static String WH_NAME = "库位编码";//商品名称
    public final static String WH_CODE = "库位编码:";//商品名称
    public final static String WORK_TYPE_ERR = "操作方式错误";
    public final static String STATUS_ERR = "状态错误";
    public final static String PAGEHEADER_SERIAL_NO = "序号"; // 分页列表序号列的表头
    public final static String REG_PKNUM = "^([1-9]\\d+)|[2-9]$";//大于1的正整数（箱规）
    public final static String REG_SERIAL_NO = "^[1-9]\\d{0,9}$";// 分页选择序号正则表达式，大于0的整数
    public final static String REG_ZERO_OR_POSITIVE = "^0|[1-9]\\d{0,9}$";// 验证零和非零开头的数字,最多十位
    public final static String REG_NUM = "^[0-9]*$";//最小值为0的无限大的整数
    public final static String REG_YN = "^[YNyn]{1}$"; // 输入y/n的场景校验
    public final static String REG_REASON_FOR_PICK = "^1|2$"; // 输入y/n的场景校验
    public final static String REG_ZONE_PICKBU_CHANGE_CONTAINER ="^0(0|[1-9]\\d{0,9}$)"; // 四期拣货，周转箱满"0*",输入拣货数量bu
    public final static String REG_ZONE_PICKBU_NOT_ENOUGH = "^0{2}(0|[1-9]\\d{0,9}$)"; // 四期拣货，缺货"00*",输入拣货数量bu
    public final static String DATE_TIP = "(yyyyMMdd格式):";//日期类型提示的后半截，前半截从批次规则的枚举中获取
    public final static String YES = "是";//
    public final static String NO = "否";//
    public final static String IS_SAME_SKU = "推荐库位存在相同商品!";


    //----------------商品信息维护----------------//

    public final static String GOODSINFO_TITLE = "商品信息维护";//商品信息维护标题
    public final static String GOODSINFO_CONTINUETO_PACKAGE_LEVEL2 = "商品一级包装维护完成，按回车键维护二级包装，或按其他键继续扫描条码";
    public final static String GOODSINFO_CONTINUETO_PACKAGE_LEVEL1 = "商品二级包装维护完成，按回车键维护一级包装，或按其他键继续扫描条码";
    public final static String GOODSINFO_ADD_PACKAGE_LEVEL2 = "新建商品二级包装";
    public final static String GOODSINFO_MANAGER = "goodsInfoManager";//商品信息维护handler名称
    public final static String PACKINGINFO_MANAGER = "packagingInfoManager";//新增二级包装的handler名称
    public final static String GOODSINFO_PARAM_MANAGER = "goodsInfoParamManager";//商品维护参数handler的名称

    // 字段
    public final static String GOODSINFO_BATCH_RULE_NORMAL = "普通批次规则";
    public final static String GOODSINFO_BATCH_RULE_GENERAL_MERCHANDISE = "百货批次规则";
    public final static String GOODSINFO_PACKAGE_LEVEL_ONE = "1级";
    public final static String GOODSINFO_PACKAGE_LEVEL_TWO = "2级";
    public final static String GOODSINFO_PACKAGE_LEVEL_ADD_TWO = "新建2级";
    public final static String GOODSINFO_VALUABLEFLAG_Y = "是";
    public final static String GOODSINFO_VALUABLEFLAG_N = "否";
    public final static String GOODSINFO_BAR_CODE_LABEL = "商品条码";//Goodsinfo的barcode字段中文
    public final static String GOODSINFO_SKUNAME_LABEL = "商品名称";
    public final static String GOODSINFO_STATUS_LABEL = "商品状态";
    public final static String GOODSINFO_KEEPDAYS_LABEL = "保质期";
    public final static String GOODSINFO_PACKAGELEVEL1_WEIGHT_LABEL = "一级包装重量";
    public final static String GOODSINFO_LENGTH_LABEL = "大边:";
    public final static String GOODSINFO_WIDTH_LABEL = "中边:";
    public final static String GOODSINFO_HEIGHT_LABEL = "小边:";
    public final static String GOODSINFO_CUBAGE_LABEL = "体积:";
    public final static String GOODSINFO_PACKAGELEVEL1_MT_LABEL = "一级码托规则如下:";
    public final static String GOODSINFO_ONEYARDNUM_LABEL = "单层数量:";
    public final static String GOODSINFO_TRAYLEVEL_LABEL = "层数:";
    public final static String GOODSINFO_STARTYARDNM_LABEL = "起码:";
    public final static String GOODSINFO_PACKAGELEVEL2_WEIGHT_LABEL = "二级包装重量";
    public final static String GOODSINFO_PACKAGELEVEL2_MT_LABEL = "二级码托规则如下:";
    public final static String GOODSINFO_PKNUM_LABEL = "箱规";


    //----------------采购入库单登记----------------//
    public final static String PURCHASE_INSTOCK = "采购入库单收货";
    public final static String PRODUCT_DATE_NAME = "生产日期";
    public final static String EXP_DATE_NAME = "失效日期";
    public final static String UN_RECEIVE_BU = "未收货数量BU:";

    //----------------意向入库单登记----------------//
    public final static String INTENTION_INSTOCK = "意向单收货";


    //----------------意向单过账----------------//
    public final static String INTENTION_POSTING_TITLE = "意向单过账";
    public final static String INTENTION_POSTING_ASNCODE_LABEL = "ASN单号";
    public final static String INTENTION_POSTING_RECEIVENUMBU_LABEL = "实际收货数量BU";
    public final static String INTENTION_POSTING_UNRECEIVENUMBU_LABEL = "未收货数量BU";
    public final static String INTENTION_POSTING_SUCCESS = "操作成功，按任意键返回";

    //---------------质检--------------------------//
    public final static String PRODUCTION_DATE = "生产日期";
    public final static String EXPIRATION_DATE = "失效日期";
    public final static String EXPECT_CHECK_BU = "待质检数量BU";
    public final static String REASON_CONTENT = "原因内容:";
    public final static String PLS_CHOSE_SERIALNO = "请选择序号:";
    public final static String SKU_NAME = "商品名称:";
    public final static String UNIT_NAME = "包装单位:";


    //----------------上架----------------//
    public final static String SHELF_TITLE = "上架移位";
    public final static String RECOM_WHCODE = "推荐上架库位:";
    public final static String PKNUM_LEVEL_TWO = "箱规:";
    public final static String GOODS_SKU = "商品代码:";
    public final static String GOODS_BARCODE = "商品条码:";
    public final static String GOODS_PRODUCTION_DATE = "生产日期:";
    public final static String GOODS_EXPIRATION_DATE = "失效日期:";
    public final static String SKU_STATUS = "商品状态:";
    public final static String SHELF_DETAIL_PLANBU = "计划上架数BU:";
    public final static String SHELF_DETAIL_DISTRBU = "分配上架数BU:";
    public final static String SHELF_DETAIL_UN_SHELF_BU = "当前批次该商品未上架数量BU:";
    public final static String SHELF_ALL_COMPLETE = "上架单操作完成";//上架主单完成
    public final static String PALLET_COMPLETE = "本批次操作完成";//上架主单完成
    public final static String SELECT_OTHER_DETAIL = "操作完成" + ErrorConstants.TIP_TO_CONTINUE + "选择批次";//上架主单完成
    public final static String CONTINUE_INPUT_NUM = "操作完成" + ErrorConstants.TIP_TO_CONTINUE + "上架";//上架主单完成


    //----------------创建移位单----------------//
    public final static String STOCK_MOVE_CREATE = "创建移位计划";
    public final static String STOCK_MOVE_UNDONE_TASK = "有未完成创建的任务,移位单号:";
    public final static String STOCK_MOVE_DAMAGE_REASON = "转残原因:";
    public final static String STOCK_MOVE_QUERY_NO_DATA = "未查询到移位单";
    //-------------RF移入--------------------//
    public final static String STOCK_MOVE_IN = "RF移入";
    public final static String STOCK_MOVE_IN_UNDONE_NUM = "待移入数量BU:";
    public final static String STOCK_MOVE_IN_CURR_BARCODE_COMPLETE = "该商品全部移入完成";
    public final static String STOCK_MOVE_IN_ALL_BARCODE_COMPLETE = "所有商品移位完成";
    public final static String STOCK_MOVE_IN_SELECT = "选择序号:";


    //--------------RF移出----------------------//
    public final static String STOCK_MOVEOUT_TITLE = "RF移出确认";
    public final static String STOCK_MOVEOUT_SHELFCODE = "移位单号";
    public final static String STOCK_MOVEOUT_TIP_BEFORE_MOVEOUTALLDETAIL_PREFIX = "当前库位该商品未移出数量BU:"; // 移出确认移出全部明细时提示的前缀
    public final static String STOCK_MOVEOUT_SPLIT_SELECTUNIT = "/";// 选择单位的分隔符
    public final static String STOCK_MOVEOUT_SRCWHSCODE_ALLOUT = "当前货位已经全部移出" + ErrorConstants.TIP_TO_CONTINUE;
    public final static String STOCK_MOVEOUT_SHELFCODE_ALLOUT = "当前移位单下商品已全部移出" + ErrorConstants.TIP_TO_CONTINUE;
    public final static String STOCK_MOVEOUT_BARCODE_ALLOUT = "当前商品已全部移出" + ErrorConstants.TIP_TO_CONTINUE;
    public final static String STOCK_MOVEOUT_UNITNAME_PRODUCTIONDATE_ALLOUT = "当前单位/生产日期商品已全部移出" + ErrorConstants.TIP_TO_CONTINUE;
    public final static String STOCK_MOVEOUT_NO_ITEM_OUT = "请先移出";

    //------------RF冻结----------------------------------------//
    public final static String STOCK_INFO_FREEZE = "RF冻结";
    public final static String STOCK_INFO_UNFREEZE = "RF解冻";
    public final static String STOCK_INFO_FREEZE_REASON = "冻结原因:";

    //------------RF补货移出----------------------------------------//
    public final static String REPLENISH_MOVEOUT_TITLE = "RF补货移出";// RF补货移出页面标题
    public final static String REPLENISH_MOVEOUT_DISTRNUM_LABEL = "分配数量"; // RF补货移出 分配数量标签
    public final static String REPLENISH_MOVEOUT_DISTRBU_LABEL = "分配BU"; // RF补货移出 分配数量表头
    public final static String REPLENISH_MOVEOUT_UNITNAME_LABEL = "单位"; // RF补货移出 分配数量表头
    public final static String REPLENISH_MOVEOUT_PRODUCTDATE_LABEL = "生产日期  "; // RF补货移出 分配数量表头
    public final static String REPLENISH_MOVEOUT_EXPIRATIONDATE_LABEL = "失效日期  "; // RF补货移出 分配数量表头
    public final static String REPLENISH_MOVEOUT_BARCODE_LABEL = "商品条码     "; // RF补货移出 商品条码表头
    public final static String REPLENISH_MOVEOUT_SINGLEITEM_CONFIRM_HINT = "当前商品未移出数量BU";// 只有一条明细确认前的提示信息
    public final static String REPLENISH_MOVEOUT_SELECTITEM_CONFIRM_HINT = "当前明细未移出数量BU:";// 只有一条明细确认前的提示信息
    public final static String REPLENISH_MOVEOUT_SRCWHSCODE_ALLOUT = "当前库位已全部移出" + ErrorConstants.TIP_TO_CONTINUE;
    public final static String REPLENISH_MOVEOUT_SHELFCODE_ALLOUT = "当前补货单已全部移出" + ErrorConstants.TIP_TO_CONTINUE;
    public final static String REPLENISH_MOVEOUT_SUCCESS = "操作成功" + ErrorConstants.TIP_TO_CONTINUE;

    //------------RF补货移入----------------------------------------//
    public final static String REPLENISH_MOVEIN_TITLE = "RF补货移入";// RF补货移入页面标题
    public final static String REPLENISH_MOVEIN_UNINNUM_LABEL = "未移入数量"; // RF补货移出 分配数量标签
    public final static String REPLENISH_MOVEIN_UNINBU_LABEL = "未移入数量BU"; // RF补货移出 分配数量表头
    public final static String REPLENISH_UN_MOVE_IN_BU = "当前批次该商品未移入BU数" + ErrorConstants.COMMON_COLON;
    public final static String REPLENISH_RECOMMEND_WH = "推荐移入库位" + ErrorConstants.COMMON_COLON;

    public final static String REPLENISH_ALL_COMPLETE = "操作完成,已无可移入数据" + ErrorConstants.TIP_TO_CONTINUE;//补货单已无可移入数据
    public final static String RE_INPUT_BAR_CODE = "操作完成" + ErrorConstants.TIP_TO_CONTINUE + "扫描商品";//上架主单完成
    public final static String CONTINUE_INPUT_MOVE_IN = "操作完成" + ErrorConstants.TIP_TO_CONTINUE + "移入";//上架主单完成


    //---------------------------------冻结/解冻-------------------------//
    public final static String STOCK_INFO_FREEZE_SUCCESS = "冻结成功";
    public final static String STOCK_INFO_UNFREEZE_SUCCESS = "解冻成功";
    //--------------库存查询-------------------------------//
    public final static String QUERY_STOCKINFO = "库存记录";

    //---------------------拣货管理--------------------------------------//
    public final static String OUTSTOCK_INFO_PICK_ORDER_MANAGER = "拣货";
    public final static String OUTSTOCK_INFO_PICK_FINISHED = "拣货完成";

    //---------------------盘点管理--------------------------------------//
    public final static String[] INVENTORY_PAGEHEADER = {"", TipConstants.INVENTORY_MANAGER, Constants.SPLIT, ""};
    public final static String END_CMD = "00";//结束命令00，结束一个库位的盘点
    public final static String INVENTORY_PARAM_MANAGER = "inventoryParamManager";//参数handler的名称
    public final static String INVENTORY_URL_MANAGER = "inventoryManager";//盘点扫描库位的Handler的名称
    public final static String INVENTORY_URL_MP_MANAGER = "inventoryManagerMP";//明盘Handler的名称
    public final static String INVENTORY_URL_MP_SURPLUS_MANAGER = "inventoryManagerSurplusMP";//明盘Handler的名称
    public final static String INVENTORY_URL_AP_MANAGER = "inventoryManagerAP";//暗盘Handler的名称
    public final static String INVENTORY_MANAGER = "盘点";


    public final static String INVENTORY_TIP_OPER_ADD = "1";
    public final static String INVENTORY_TIP_OPER_COVER = "2";
    public final static String INVENTORY_TIP_SURPLUS = "存在多余实物?";
    public final static String INVENTORY_TIP_COVER = "已存在该条码,是否覆盖:";
    public final static String INVENTORY_TIP_MIX_COVER = "已存在该日期,是否覆盖:";
    public final static String INVENTORY_TIP_ADD_COVER = "已登记，1:新增；2:覆盖条码全部记录";
    public final static String INVENTORY_SUM_STOCK_NUM = "数量BU";


    //----------------加工上架----------------//
    public final static String PROCESS_SHELF_TITLE = "加工上架";

    //--------------四期拣货------------------//
    public final static String OUTSTOCK_ZONE_PICK_ORDER_MANAGER = "分区拣货（换周转箱，请输入1）";
    public final static String OUTSTOCK_ZONE_PICK_FINISHED = "拣货完成";
    public final static String OUTSTOCK_ZONE_CANCLE_PICK_TASK_BIND= "取消拣货任务绑定";


    //--------------四期集货------------------//
    public final static String OUTSTOCK_PICKUP_MANAGER = "集周转箱";
    public final static String OUTSTOCK_PICKUP_SENDSHEETNO = "查询发货单";
    public final static String OUTSTOCK_PICKUP_STOREWHSCODE = "查询集货库位";

    //---------------RF-------------------//
    public final  static  String OUTSTOCK_CANCLE_ORDER = "退拣";

    //---------------意向单快捷收货-------------------//
    public final  static  String QUICK_INSTOCK = "意向单快捷收货";

}
