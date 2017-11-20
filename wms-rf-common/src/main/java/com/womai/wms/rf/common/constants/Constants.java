package com.womai.wms.rf.common.constants;

import com.womai.wms.rf.common.util.CollectionUtil;
import com.womai.wms.rf.common.util.SpringContextHolder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 用于放置各模块通用的常量
 * User:xixiaochuan
 * Date: 2014-11-26
 * To change this template use File | Settings | File Templates.
 */
public class Constants {
    public final static String MD5 = "md5";
    //符号
    public final static String SPLIT = "----------------------------------------------";
    public final static String SPLIT_LINE = "|";
    public final static long SLEEP_TIME = 3000L;//移出时，提示语显示时间
    public final static String ORDER_FOR_BACK_OPER = "..";//用户输入“..”跳转到之前的某个操作


    public final static String DECODE_HANDLER = "decodeHandler";//解码handler名称
    public final static String ENCODE_HANDLER = "encodeHandler";//编码handler名称

    public final static String LINE_BASED_FRAME_DECODER = "LineBasedFrameDecoder";//编码类型

    public final static String USER_SITE_MANAGER = "userAndSiteParamManager";//储存用户id及当前站点的manager
    public final static String MENU_SHELL_MANAGER = "menuShellManager";//菜单界面路径
    public final static String LOGIN_SHELL_MANAGER = "loginShellManager";//登录界面路径
    public final static String STOCK_MOVE_IN_MANAGER = "stockMoveInManager";//移入的handler名字
    public final static String STOCK_MOVE_CREATE_MANAGER = "createStockMoveManager";//创建移位单handler名字
    public final static String STOCK_MOVE_OUT_MANAGER = "stockMoveOutManager";//RF移位移出路径，用于创建移位单时的跳转
    public final static String REPLENISH_MOVEOUT_MANAGER = "replenishMoveOutManager"; // RF补货移出beanid


    public final static Integer BEEP_TIMES = 2;//默认的响铃次数

    //针对不同客户端软件的字符串编码设置不同的字符集值，PDA上的Telnet软件为UTF-8，windows的cmd窗口为GBK
    public final static String CHARSET = SpringContextHolder.getBean("charset");
    //每页显示数目
    public final static int PAGE_SIZE = 10;
    //原因列表每页显示数目
    public final static int REASON_PAGE_SIZE = 2;
    public final static int PAGE_SIZE_THR = 3;
    //整单质检，原因列表每页显示数目
    public final static int WHOLE_REASON_PAGE_SIZE = 5;
    //质检明细列表每页显示数目
    public final static int CHECK_DETAIL_PAGE_SIZE = 2;
    //质检明细列表每页显示数目
    public final static int STOCK_MOVE_DETAIL_PAGE_SIZE = 2;
    //分页开始页码
    public final static int PAGE_START = 1;
    //排序规则
    public final static String PAGE_SORT_DESC = "DESC";
    public final static String PAGE_STOCK_INFO_SIDX = "stockid";//库存默认排序字段
    //添加移位明细时，库存列表每页显示数目
    public final static int STOCK_INFO_PAGE_SIZE = 2;
    //库存查询模块，库存列表每页显示数目
    public final static int QUERY_STOCK_INFO_PAGE_SIZE = 5;
    public final static String PAGE_REASON_SIDX = "id";
    public final static int PAGE_OFFSET_INIT = 0;//分页展示初始
    public final static int PAGE_OFFSET_NEXT = 1;//向后翻页
    public final static int PAGE_OFFSET_PREV = -1;//向前翻页
    public final static String PAGE_COLNAME_CUT_SUF = "..";//分页列表字段显示的字节数，超过截取
    public final static String PAGE_SORT_ASC = "ASC";
    //换行符
    public final static String BREAK_LINE = "\r\n";
    //确认及取消
    public final static String CONFIRM_Y = "Y";
    public final static String CANCEL_N = "N";
    public final static String WEIGHT_UNIT = "KG";//通用重量单位

    //提示语
    public final static String PAGE_START_ERROR = "已经是第一页！";
    public final static String PAGE_END_ERROR = "已经最后一页！";
    public final static String PAGE_INFO = "第${currentPage}/${totalPage}页,总数：${totalCount}";
    public final static String PAGE_TURING_TIP = "B:上一页  N:下一页  ";
    public final static String QUIT_BY_ANYKEY = "按任意键退出：";


    public final static String SYSTEM_ERROR = "系统异常";

    // ====一些字段输入00回退到上一个字段===//
    public static final String KEY_BACK = "00";// 回退的关键字

    //----------------登录-----------------//
    //-------------权限----------//
    public final static Integer APPTYPE = 2;// 权限相关
    public final static Integer MENU_LEVEL_ONE = new Integer(1);
    public final static boolean MENU_LEVEL_ONE_YES = true;// 是一级菜单
    public final static boolean MENU_LEVEL_ONE_NO = false;// 不是一级菜单
    public final static String MENU_ID_NOUSE = null;// 未使用的menuid
    public final static String MENU_ERROR_MESS_NULL = null;// 空的菜单错误提示
    public final static String MENU_IS_NULL_TIP_SUFFIX ="无权限";// 菜单是空的提示
    public final static String SITE_ERROR = "用户站点异常";
    public final static String AUTH_SYSTEM_ERROR = "权限系统异常";

    //登录页面
    public final static String LOGIN_STATUS = "status";
    public final static String LOGIN = "登录";
    public final static String LOGIN_USERNAME = "用户名：";
    public final static String LOGIN_PASSWORD = "密  码：";
    //提示语
    public final static String USERNAME_NOTMATCH = "用户名不符合规范！";
    public final static String USERNAME_NOTEXSIT = "用户名不存在！";
    public final static String PASSWORD_ERROR = "密码错误，请重新输入！";
    public final static String USER_NOSITE = "用户无站点信息,请联系管理员！";
    //------------修改密码--------------------//
    public final static String MODIFYPWD_TITLE = "密码修改";
    public final static int PASS_LENGTH = 6;//密码长度小于6
    //提示语
    public final static String MODIFYPWD_MSG = "请输入旧密码：";
    public final static String MODIFYPWD_MSG_FIR = "请输入新密码：";
    public final static String MODIFYPWD_MSG_SEC = "请确认新密码：";

    //----------------主菜单-----------------//
    //主菜单
    public final static String MENU_TITLE = "菜单管理";
    public final static String MENU_TIP = "菜单选择：";
    //提示语
    public final static String MENU_NUM_ERROR = "输入的菜单序号不在当前合法范围内！";
    public final static String MENU_NULL_ERROR = "未识别菜单序列号！";
    public final static String MENU_ERROR_MAX = "超过菜单最大序列号！";
    //----------------密码修改-----------------//
    public final static String MODIFY_PWD_SYSTEM_ERROR = "系统错误，请重新登陆！";
    public final static String MODIFY_PWD_DATA_ERROR_0 = "新密码不一致，请重新输入！";
    public final static String MODIFY_PWD_DATA_ERROR_1 = "新密码长度需不小于6位，请重新输入！";
    public final static String MODIFY_PWD_OLDPWD_ERROR = "密码错误，请重新输入！";
    public final static String MODIFY_PWD_SECCESS = "密码修改成功！";
    //----------------仓库切换-----------------//
    public final static String SWITCH_WAREHOUSE = "切换仓库";
    public final static String SELECT_WAREHOUSE = "(当前:${currentSiteName})\r\n选择仓库(输入序号):";
    public final static String SELECT_WAREHOUSE_ERROR = "无效站点序号";
    public final static String SELECT_WAREHOUSE_TIP = "选择仓库(输入序号):";
    //----------------交接管理-----------------//
    public final static String PRETRANS_ADMIN = "交接";
    public final static String PRETRANS_CREATE_PRODUCTORDER = "(创建交接单输入'Y')";
    public final static String PRETRANS_INPUT_TRANSCODE = "配送商编号：";
    public final static String PRETRANS_TRANSCODE_NULL = "配送商未维护";
    public final static String PRETRANS_TRANSSHEET_NULL = "单号为空";
    public final static String PRETRANS_SCANPROCESSS = "当前出库单已扫描：${scanPackNum}/${packageAmount}";
    public final static String PRETRANS_TRANS_SCANPROCESSS = "当前交接单已扫描：${allScanPackNum}/${allPackAmount}";
    public final static String PRETRANS_INPUT_BOXCODE = "面单号：";

    public final static String RF_MANAGER_CONFIRM = "出库确认";
    public final static String RF_MANAGER_TRANSFER_CODE = "交接单号：";
    public final static String RF_MANAGER_CONFIRM_YN = "是否出库确认（Y/N）?";
    public final static String RF_MANAGER_INPUT = "请输入：";
    public final static String RF_MANAGER_ERROR_MSG_01 = "交接单号扫描错误！";
    public final static String RF_MANAGER_ERROR_MSG_02 = "按任意键继续";
    public final static String RF_MANAGER_ERROR_MSG_03 = "请输入:";
    public final static String RF_MANAGER_ERROR_MSG_04 = "未识别，请重新输入";
    public final static String RF_MANAGER_ERROR_MSG_05 = "提交失败，按任意键继续";
    public final static String RF_MANAGER_ERROR_MSG_06 = "未识别,";

    public final static String RF_MANAGER_QUERY = "查询交接单";
    public final static String RF_MANAGER_PACKCODE = "面单号：";
    public final static String RF_MANAGER_SCANNER_NAME = "交接扫描人：";
    public final static String RF_MANAGER_TRANSCODE = "对应交接单号：";

    //----------------集货管理----------------//
    public final static String RF_MANAGER_CONTAINERNO   = "周转箱：";
    public final static String RF_MANAGER_SENDSHEETNO   = "发货单号：";
    public final static String RF_MANAGER_WHSCODE   = "推荐库位：";
    public final static String RF_MANAGER_REALWHSCODE   = "集货库位：";



    //----------------质检----------------//

    public final static String INSTOCK_CHECK_ALL = "整单质检";
    public final static String INSTOCK_CHECK_PART = "单一质检";
    public final static String INSTOCK_REASON_CONTENT = "          原因列表";


    //----------------库存查询----------------//
    public final static String STOCK_ADMIN = "库存记录";
    public final static String STOCK_BARCODE = "货品条码：";
    public final static String STOCK_WHCODE = "库位号：";
    //货品条码查询
    public final static String STOCK_INFO_1 = "库位";
    public final static String STOCK_INFO_2 = "库存数量BU";
    public final static String STOCK_INFO_3 = "单位";
    public final static String STOCK_INFO_4 = "规格";
    public final static String STOCK_INFO_5 = "上架数量BU";
    public final static String STOCK_INFO_6 = "拣货数量BU";
    public final static String STOCK_INFO_7 = "生产日期";
    //库位查询
    public final static String STOCK_INFO_8 = "货品条码";
    public final static String STOCK_INFO_9 = "货品名称";

    //提示语
    public final static String FINDSKU_ERROR = "系统中没有维护此货品信息！";
    public final static String STOCK_NULL_BARCODE = "输入的货品条码不能为空！";
    public final static String FINDWH_ERROR = "系统中没有该库位！";
    public final static String STOCK_NULL_WHCODE = "输入的库位信息不能为空！";

    //-------------拣货管理------------------------
    public final static String REASON_ONE = "1";//RF拣货缺货（用户输入值）
    public final static String REASON_TWO = "2";//RF拣货残品（用户输入值）
    public final static Integer REASON_TYPE_FOR_INPUT_ONE = 9002;//RF拣货缺货的类型
    public final static Integer REASON_TYPE_FOR_INPUT_TWO = 9003;//RF拣货残品的类型
    //库存类型
    public final static String CUNHUO = "存货";
    public final static String CIPIN = "次品";
    public final static String ZHIJIAN = "质检";
    public final static String TUIJIAN = "退拣";
    public final static String DAIFA = "待发";
    public final static String BAOSUN = "报损";
    public final static String FAHUO = "发货";
    public final static String DABAO = "打包";

    /**
     * 库位类型
     */
    public static String getWhTypeName(Integer whtype) {
        switch (whtype) {
            case 0:
                return CUNHUO;
            case 2:
                return CIPIN;
            case 3:
                return ZHIJIAN;
            case 4:
                return TUIJIAN;
            case 5:
                return DAIFA;
            case 6:
                return BAOSUN;
            case 7:
                return FAHUO;
            case 8:
                return DABAO;
            default:
                return "";
        }
    }


    //----------------商品信息维护----------------//
    //商品状态
    public final static String GOODSINFO_STATUS_ENABLE_LABEL = "生效";
    public final static String GOODSINFO_STATUS_DISABLE_LABEL = "失效";
    public final static String GOODSINFO_STATUS_ILLEGAL_LABEL = "异常状态代码";

    // 将数据库中状态字段的值翻译成中文
    public static String getStatusLabel(Integer statusCode) {
        switch (statusCode) {
            case 0:
                return GOODSINFO_STATUS_ENABLE_LABEL;
            case 1:
                return GOODSINFO_STATUS_DISABLE_LABEL;
            default:
                return GOODSINFO_STATUS_ILLEGAL_LABEL;
        }
    }


    public enum batchRuleEnum {
        puTong(0, "普通批次", "生产日期"),//普通批次规则默认显示生产日期
        baiHuo(1, "百货批次", "生产日期"),//百货批次规则改为显示生产日期
        xiHua(2, "洗化批次", "失效日期");//洗化批次规则显示失效日期

        public Integer code;//值
        public String name;//批次名称
        public String dateType;//对应的需要显示的日期类型名称

        batchRuleEnum(Integer code, String name, String dateType) {
            this.code = code;
            this.name = name;
            this.dateType = dateType;
        }

        /**
         * 根据默认的类型显示不同的日期类型
         *
         * @param defaulCode 默认值
         * @return 构造日期类型list
         */
        public static List<String> makeDateTypeList(Integer defaulCode) {
            if (puTong.code.equals(defaulCode) || baiHuo.code.equals(defaulCode)) {
                //普通、百货批次规则默认显示生产日期
                return CollectionUtil.newGenericList(puTong.dateType, baiHuo.dateType);
            } else {
                //洗化批次规则默认显示失效日期
                return CollectionUtil.newGenericList(xiHua.dateType, puTong.dateType);
            }
        }

        /**
         * 根据批次规则编码值获取日期类型
         *
         * @param code 批次规则编码
         * @return 批次规则名称
         */
        public static String getDateTypeByCode(Integer code) {
            for (batchRuleEnum t : batchRuleEnum.values()) {
                if (t.code.equals(code)) {
                    return t.dateType;
                }
            }
            return "";
        }

        /**
         * 根据批次规则编码值获取名称
         *
         * @param code 批次规则编码
         * @return 批次规则名称
         */
        public static String getNameByCode(Integer code) {
            for (batchRuleEnum t : batchRuleEnum.values()) {
                if (t.code.equals(code)) {
                    return t.name;
                }
            }
            return "";
        }

        /**
         * 根据批次规则名称获取代码
         *
         * @param name 批次规则名称
         * @return 批次规则编码
         */
        public static Integer getCodeByName(String name) {
            for (batchRuleEnum t : batchRuleEnum.values()) {
                if (t.name.equals(name)) {
                    return t.code;
                }
            }
            return null;
        }

        /**
         * 根据传入的批次规则编码，将等于批次规则编码的批次规则名字放在第一个，重新排列获取批次规则名字切换列表
         *
         * @param code 批次规则编码
         * @return 重新排列后的批次规则名字切换列表
         */
        public static List<String> getNameSwichList(Integer code) {
            List<String> nameList = new LinkedList<String>();
            for (batchRuleEnum t : batchRuleEnum.values()) {
                if (t.code.equals(code)) {
                    nameList.add(0, t.name);// 等于传入的code，放在list的第一个位置
                } else {
                    nameList.add(t.name);// 不等于传入的code，保持原顺序往里放就行了
                }
            }
            return nameList;
        }

        /**
         * 校验批次规则名字是否存在
         *
         * @param name 批次规则名字
         * @return true 存在 false 不存在
         */
        public static boolean nameExist(String name) {
            for (batchRuleEnum t : batchRuleEnum.values()) {
                if (t.name.equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static enum StockMoveOrderTypeEnum {
        normal(0, "普通移位"),
        normalToDamage(1, "正转残"),
        DamageToNormal(2, "残转正");
        public Integer value;
        public String name;

        StockMoveOrderTypeEnum(Integer value, String name) {
            this.value = value;
            this.name = name;
        }

        public static List<String> getNameList() {
            List<String> strings = new ArrayList<String>();
            for (StockMoveOrderTypeEnum enumType : StockMoveOrderTypeEnum.values()) {
                strings.add(enumType.name);
            }
            return strings;
        }

        /**
         * 通过名字获得值，如果不存在返回-1
         *
         * @param name 枚举中的name
         * @return 枚举中的value
         */
        public static Integer getValueByName(String name) {
            for (StockMoveOrderTypeEnum t : StockMoveOrderTypeEnum.values()) {
                if (t.name.equals(name)) {
                    return t.value;
                }
            }
            return -1;
        }

        /**
         * 通过名字获得值，如果不存在返回-1
         *
         * @param value 枚举中的value
         * @return 枚举中的name
         */
        public static String getNameByValue(Integer value) {
            for (StockMoveOrderTypeEnum t : StockMoveOrderTypeEnum.values()) {
                if (t.value.equals(value)) {
                    return t.name;
                }
            }
            return "";
        }
    }


}
