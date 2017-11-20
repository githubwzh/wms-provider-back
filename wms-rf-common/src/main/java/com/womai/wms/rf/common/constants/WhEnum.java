package com.womai.wms.rf.common.constants;

import java.util.ArrayList;
import java.util.List;

/**
 * 仓库编码及名称枚举
 * User: zhangwei
 * Date: 2016-05-02
 * To change this template use File | Settings | File Templates.
 */
public enum WhEnum {
    WU_HAN_W01("W01", "武汉生鲜仓"),
    TI_YAN_W02("W02", "体验店"),
    DONG_CHENG_W03("W03", "东城仓"),
    HE_PING_LI_W04("W04", "和平里仓"),
    XU_ZHUANG_W05("W05", "徐庄仓"),
    XI_CHENG_W06("W06", "西城仓"),
    SHAO_YAO_JU_W07("W07", "芍药居仓"),
    WU_DAO_KOU_W08("W08", "五道口仓"),
    XIN_JIE_KOU_W09("W09", "新街口仓"),
    JIU_XIAN_QIAO_W10("W10", "酒仙桥仓"),
    BAI_ZI_WAN_W11("W11", "百子湾仓"),
    JIAN_XIANG_QIAO_W12("W12", "健翔桥仓"),
    HB_CHANG_WEN_W27("W27", "北京常温仓"),
    HB_SHENG_XIAN_W28("W28", "北京生鲜仓"),
    HD_CHANG_WEN_W29("W29", "上海常温仓"),
    HD_SHENG_XIAN_W30("W30", "上海生鲜仓"),
    HN_CHANG_WEN_W31("W31", "广州常温仓"),
    HN_SHENG_XIAN_W32("W32", "广州生鲜仓"),
    ZHENG_ZHOU_W33("W33", "郑州生鲜仓"),
    SHEN_YANG_W34("W34", "沈阳生鲜仓"),
    NAN_JING_W35("W35", "南京生鲜仓"),
    FU_ZHOU_W36("W36", "福州生鲜仓"),
    CHENG_DOU_W37("W37", "成都生鲜仓"),
    SCM_VIR_W38("W38", "SCM虚拟仓"),
    //20160607 新开仓库
    DA_LIAN_SX_W40("W40", "大连生鲜仓"),
    QING_DAO_SX_W41("W41", "青岛生鲜仓"),
    JI_NAN_SX_W42("W42", "济南生鲜仓"),
    HANG_ZHOU_SX_W43("W43", "杭州生鲜仓"),
    CHONG_QING_SX_W44("W44", "重庆生鲜仓"),
    CHANG_SHA_SX_W45("W45", "长沙生鲜仓"),
    CHENG_DU_CW_W46("W46", "成都常温仓");

    public final String code;
    public final String name;

    WhEnum(String whCode, String whName) {
        this.code = whCode;
        this.name = whName;
    }

    /**
     * 按照仓库编码获取仓库名称
     *
     * @param whCode 仓库编码
     * @return 仓库名称
     */
    public static String getWhName(String whCode) {
        for (WhEnum t : WhEnum.values()) {
            if (t.code.equals(whCode)) {
                return t.name;
            }
        }
        return null;
    }

    /**
     * 按照仓库编码集合获取对应的名称集合
     *
     * @param whCodeList 仓库编码集合
     * @return 返回对应的名称集合
     */
    public static List<String> getWhNameList(List<String> whCodeList) {
        List<String> whNameList = new ArrayList<String>();
        for (String whCode : whCodeList) {
            whNameList.add(getWhName(whCode));
        }
        return whNameList;
    }

    /**
     * 获取所有的仓库名称
     * @return 返回仓库名称集合
     */
    public static List<String> getALLWhNames() {
        List<String> whNameList = new ArrayList<String>();
        for (WhEnum t : WhEnum.values()) {
            whNameList.add(t.name);
        }
        return whNameList;
    }

    /**
     * 获取所有的仓库编码
     * @return 返回仓库编码集合
     */
    public static List<String> getALLWhCodes() {
        List<String> whCodeList = new ArrayList<String>();
        for (WhEnum t : WhEnum.values()) {
            whCodeList.add(t.code);
        }
        return whCodeList;
    }

}
