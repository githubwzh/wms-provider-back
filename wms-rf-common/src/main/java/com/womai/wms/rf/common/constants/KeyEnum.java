package com.womai.wms.rf.common.constants;

import java.util.List;

/**
 * 十进制ASCII码与键盘值对照枚举
 * User: zhangwei
 * Date: 2016-04-28
 * To change this template use File | Settings | File Templates.
 */
public enum KeyEnum {

    NUT_0(0, "NUT"),//空字符
    SOH_1(1, "SOH"),//标题开始
    STX_2(2, "STX"),//本文开始
    ETX_3(3, "ETX"),//本文结束
    EOT_4(4, "EOT"),//传输结束
    ENQ_5(5, "ENQ"),//请求
    ACK_6(6, "ACK"),//确认回应
    BEL_7(7, "BEL"),//响铃
    BS_8(8, "BS"),//退格
    HT_9(9, "HT"),//水平定位符号
    LF_10(10, "LF"),//换行键
    VT_11(11, "VT"),//垂直定位符号
    FF_12(12, "FF"),//换页键
    CR_13(13, "CR"),//归位键，回车键
    SO_14(14, "SO"),//取消变换（Shift out）
    SI_15(15, "SI"),//启用变换（Shift in）
    DLE_16(16, "DLE"),//跳出数据通讯
    DCI_17(17, "DCI"),//设备控制一（XON 启用软件速度控制）
    DC2_18(18, "DC2"),//设备控制二
    DC3_19(19, "DC3"),//设备控制三（XOFF 停用软件速度控制）
    DC4_20(20, "DC4"),//设备控制四
    NAK_21(21, "NAK"),//确认失败回应
    SYN_22(22, "SYN"),//同步用暂停
    TB_23(23, "TB"),//区块传输结束
    CAN_24(24, "CAN"),//取消
    EM_25(25, "EM"),//连接介质中断
    SUB_26(26, "SUB"),//替换
    ESC_27(27, "ESC"),//跳出
    FS_28(28, "FS"),//文件分割符
    GS_29(29, "GS"),//组群分隔符
    RS_30(30, "RS"),//记录分隔符
    US_31(31, "US"),//单元分隔符
    space_32(32, " "),//空格
    tanHao_33(33, "!"),
    shuangYin_34(34, "\""),
    jingHao_35(35, "#"),
    dollar_36(36, "$"),
    percent_37(37, "%"),
    and_38(38, "&"),
    danYinHao_39(39, "'"),
    zuoXiaoKuoHao_40(40, "("),
    youXiaoKuoHao_41(41, ")"),
    xingHao_42(42, "*"),
    jiaHao_43(43, "+"),
    douHao_44(44, ","),
    duanHeng_45(45, "-"),
    dian_46(46, "."),
    youXieGang_47(47, "/"),
    Number0_48(48, "0"),
    Number1_49(49, "1"),
    Number2_50(50, "2"),
    Number3_51(51, "3"),
    Number4_52(52, "4"),
    Number5_53(53, "5"),
    Number6_54(54, "6"),
    Number7_55(55, "7"),
    Number8_56(56, "8"),
    Number9_57(57, "9"),
    maoHao_58(58, ":"),
    fenHao_59(59, ";"),
    xiaoYuHao_60(60, "<"),
    dengYuHao_61(61, "="),
    daYuHao_62(62, ">"),
    wenHao_63(63, "?"),
    AtFu_64(64, "@"),
    A_65(65, "A"),
    B_66(66, "B"),
    C_67(67, "C"),
    D_68(68, "D"),
    E_69(69, "E"),
    F_70(70, "F"),
    G_71(71, "G"),
    H_72(72, "H"),
    I_73(73, "I"),
    J_74(74, "J"),
    K_75(75, "K"),
    L_76(76, "L"),
    M_77(77, "M"),
    N_78(78, "N"),
    O_79(79, "O"),
    P_80(80, "P"),
    Q_81(81, "Q"),
    R_82(82, "R"),
    S_83(83, "S"),
    T_84(84, "T"),
    U_85(85, "U"),
    V_86(86, "V"),
    W_87(87, "W"),
    X_88(88, "X"),
    Y_89(89, "Y"),
    Z_90(90, "Z"),
    zuoZhongKuoHao_91(91, "["),
    zuoXieGang_92(92, "\\"),
    youZhongKuoHao_93(93, "]"),
    YiHuoFu_94(94, "^"),
    duanXiaHuaXian_95(95, "_"),
    dunHao_96(96, "、"),
    a_97(97, "a"),
    b_98(98, "b"),
    c_99(99, "c"),
    d_100(100, "d"),
    e_101(101, "e"),
    f_102(102, "f"),
    g_103(103, "g"),
    h_104(104, "h"),
    i_105(105, "i"),
    j_106(106, "j"),
    k_107(107, "k"),
    l_108(108, "l"),
    m_109(109, "m"),
    n_110(110, "n"),
    o_111(111, "o"),
    p_112(112, "p"),
    q_113(113, "q"),
    r_114(114, "r"),
    s_115(115, "s"),
    t_116(116, "t"),
    u_117(117, "u"),
    v_118(118, "v"),
    w_119(119, "w"),
    x_120(120, "x"),
    y_121(121, "y"),
    z_122(122, "z"),
    zuoDaKuoHao_123(123, "{"),
    huo_124(124, "|"),
    youDaKuoHao_125(125, "}"),
    shangDian_126(126, "`"),
    DEL_127(127, "DEL");//删除

    public final Integer code;
    public final String value;

    /**
     * ascii 码与键盘对应值
     *
     * @param code  ascii 码
     * @param value 键盘值
     */
    KeyEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    /**
     * 按照十进制字节码获取对应的值,特殊字符除外，大于等于32，小于等于126
     * @param queryCode 十进制字节码
     * @return 对应的值
     */
    public static String getValueByCode(Integer queryCode){
        for (KeyEnum t : KeyEnum.values()) {
            if(t.code.equals(queryCode) && (t.code>=space_32.code && t.code<=shangDian_126.code) ){
                return t.value;
            }
        }
        return null;
    }

    /**
     * 按照字节码集合获取对应的值
     * @param codeList 字节码集合
     * @return 对应的值
     */

    public static String getValuesByCodeList(List<Integer> codeList) {
        StringBuffer stringBuffer = new StringBuffer();
        for(Integer i:codeList){
            stringBuffer.append(getValueByCode(i));
        }
        return stringBuffer.toString();
    }


}
