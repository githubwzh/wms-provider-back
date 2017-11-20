package com.womai.wms.rf.manager.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.DefaultKey;
import com.womai.wms.rf.common.constants.KeyEnum;
import com.womai.wms.rf.common.util.*;
import com.womai.wms.rf.manager.auth.login.BaseShellManagerImpl;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据接收工具
 * User:zhangwei
 * Date: 2016-05-25
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component("receiveManager")
public class ReceiveManager extends BaseShellManagerImpl {
    public final static Integer RECEIVER_TYPE_FINISHED = 1;//接收数据完成
    public final static Integer RECEIVER_TYPE_NOFINISHED = 0;//接收数据未完成
    public final static Integer RECEIVER_TYPE_FORWARD = -1;//页面跳转
    private List<FieldObject> fieldObjectList;
    public Map<String, Object> dataMap = new HashMap<String, Object>();

    /**
     * 用于子类获取map数据
     *
     * @return map容器
     */
    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    /**
     * 用于设置dataMap值
     *
     * @param dataMap map容器
     */
    public void setDataMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

    /**
     * 用于清空dataMap
     */
    public void resetDataMap() {
        dataMap.clear();
    }

    /**
     * 初始化map数据及对象注解
     *
     * @param objectClass handler中的业务对象
     * @param pageHeader  初始化屏幕，数组中的每个元素独占一行进行输出，每个元素中不能包含\r\n换行操作符，如需换行，请在数组中增加单独\r\n元素
     * @param ctx         handler上下文
     */
    public void initBaseMap(Class objectClass, String[] pageHeader, ChannelHandlerContext ctx) throws Exception {
        resetDataMap();
        fieldObjectList = AnnotationUtil.getObjectFields(objectClass);
        if (fieldObjectList.size() == 0) {
            throw new RuntimeException(objectClass.getName() + " 未找到Receiver注解的属性");
        }
        Map<String, Object> accepterMap = Maps.newHashMap();
        //当前接收中的字段名称默认为属性列表第一个
        FieldObject fieldObject = fieldObjectList.get(0);
        accepterMap = setBaseKVInMap(fieldObject.getFieldName(), objectClass, accepterMap);
        if (pageHeader != null && pageHeader.length > 0) {
            HandlerUtil.clearAll(ctx.channel());
            HandlerUtil.writer(ctx, pageHeader, 1, 1);
        }
        String fieldTip = fieldObject.getFieldTip();
        fieldTip = fieldObject.isCursorDown() ? fieldTip + Constants.BREAK_LINE : fieldTip;
        HandlerUtil.writeAndFlush(ctx, fieldTip);
        setDataMap(accepterMap);
    }

    /**
     * 设置map中的基本key-value数据
     *
     * @param curColName  当前带接收的字段名称
     * @param objectClass 待接收的类对象
     * @param accepterMap map数据容器
     * @return 返回设置默认值的map容器
     */
    private Map<String, Object> setBaseKVInMap(String curColName, Class objectClass, Map<String, Object> accepterMap) throws Exception {
        accepterMap.put(DefaultKey.curColName.keyName, curColName);
        accepterMap.put(DefaultKey.lastCompleteColName.keyName, DefaultKey.lastCompleteColName.defaultVal);
        accepterMap.put(DefaultKey.completeSize.keyName, DefaultKey.completeSize.defaultVal);
        accepterMap.put(DefaultKey.rePage.keyName, DefaultKey.rePage.defaultVal);
        accepterMap.put(DefaultKey.clearDate.keyName, DefaultKey.clearDate.defaultVal);
        accepterMap.put(DefaultKey.curColErrMess.keyName, DefaultKey.curColErrMess.defaultVal);
        accepterMap.put(DefaultKey.switchList.keyName, DefaultKey.switchList.defaultVal);
        accepterMap.put(DefaultKey.listIndex.keyName, DefaultKey.listIndex.defaultVal);
        accepterMap.put(DefaultKey.autoPrintNextCol.keyName, DefaultKey.autoPrintNextCol.defaultVal);
        accepterMap.put(DefaultKey.canMoveCursor.keyName, DefaultKey.canMoveCursor.defaultVal);
        accepterMap = setObjectClassInMap(objectClass, accepterMap);
        return accepterMap;
    }

    /**
     * 设置map中的待接收数据对象
     *
     * @param objectClass 待接收数据对象类
     * @param accepterMap map数据容器
     * @return 返回设置对象的map数据容器
     */
    private Map<String, Object> setObjectClassInMap(Class objectClass, Map<String, Object> accepterMap) throws Exception {
        accepterMap.put(DefaultKey.objectClass.keyName, objectClass.newInstance());
        return accepterMap;
    }

    /**
     * 接收数据，不自动显示下一个待接收字段，需要显示下一个字段的话调用rePrintCurColTip方法
     *
     * @param ctx         handler上下文
     * @param object      接收到的数据
     * @param accepterMap map数据容器
     * @return 返回接收的状态
     * @throws Exception 抛出异常
     */
    protected int receiveDataAndNotPrintNext(ChannelHandlerContext ctx, Object object, Map<String, Object> accepterMap) throws Exception {
        Integer result = dealReceivedData(object, false, accepterMap, ctx);
        accepterMap.put(DefaultKey.autoPrintNextCol.keyName, false);
        return result;
    }

    /**
     * 接收数据，自动显示下一个待接收字段无需特殊处理
     * <p/>
     * 不建议使用
     *
     * @param ctx         handler上下文
     * @param object      接收到的数据
     * @param accepterMap map数据容器
     * @return 返回接收的状态
     * @throws Exception 抛出异常
     */
    protected int receiveData(ChannelHandlerContext ctx, Object object, Map<String, Object> accepterMap) throws Exception {
        return dealReceivedData(object, true, accepterMap, ctx);
    }


    private List<Integer> keyList = new ArrayList<Integer>();

    private static String list2Str(List<Integer> list) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        for (Integer integer : list) {
            if (i == 0) {
                stringBuilder.append(integer);
            } else {
                stringBuilder.append(",").append(integer);
            }
            i++;
        }
        return stringBuilder.toString();
    }


    /**
     * 处理接收到的数据
     *
     * @param object      接收到的字节数据
     * @param printNext   是否自动打印下一个待接收字段
     * @param accepterMap map数据容器
     * @param ctx         handler上下文
     * @return 返回是否接收完成、是否跳转、是否退格
     */
    protected int dealReceivedData(Object object, boolean printNext, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        if (super.anyKeyToLogIn) {//如果父类中的标识为true则跳转回登录，此标识在BaseShellManager捕获异常的方法exceptionCaught中设置
            forward(Constants.LOGIN_SHELL_MANAGER, ctx);
            return RECEIVER_TYPE_NOFINISHED;
        }
        //首先清空一下上次接收字段，避免字段重新执行的问题，直到enter后重新赋值
        accepterMap.put(DefaultKey.lastCompleteColName.keyName, "");

        String msg = object.toString();
        Object objectClass = accepterMap.get(DefaultKey.objectClass.keyName);
        String fieldName = (String) accepterMap.get(DefaultKey.curColName.keyName);//属性名称
        String fieldValue = (String) Reflections.invokeGetter(objectClass, fieldName);//当前已接收的属性值
        if (StringUtils.isEmpty(fieldValue)) {
            fieldValue = "";
        }
        //定长接收器长度为1，方向键的输入为3，所以需要拼装一下，如果接收到27，情况以前的接收
        if (msg.getBytes()[0] == KeyEnum.ESC_27.code) {
            keyList.clear();
        }
        keyList.add(Integer.valueOf(msg.getBytes()[0]));
        //如果包含27，size小于3，说明是按了方向键后的三个值未全部接收完成
        if (keyList.contains(KeyEnum.ESC_27.code) && keyList.size() < 3) {
            return RECEIVER_TYPE_NOFINISHED;
        }
        //拼装的方向移动的keyCode，与KeyEnum中的code对应
        String positionMoveKey = list2Str(keyList);
        if (isBackSpace(ctx, msg, fieldName, fieldValue, objectClass)) {
            return RECEIVER_TYPE_NOFINISHED;//回退键
        } else if (switchList(msg, positionMoveKey, accepterMap, ctx)) {
            return RECEIVER_TYPE_NOFINISHED;
        } else {
            //判断是否可以移动光标
            Boolean canMoveCursor = (Boolean) accepterMap.get(DefaultKey.canMoveCursor.keyName);
            //判断是否可以切换光标，因为拦截器早于此接收器执行，需要将已经移动的光标给反向移动一下
            cursorPosition(canMoveCursor, positionMoveKey, ctx);

            //如果包含27，size又等于3，说明是在非list切换的地方按的方向键，需要直接返回不进行赋值处理
            if (keyList.indexOf(KeyEnum.ESC_27.code) == 0 && keyList.size() == 3) {
                return RECEIVER_TYPE_NOFINISHED;
            }

            List<Integer> receivedBytes = byteList(msg);
            Integer enterIndex = receivedBytes.indexOf(KeyEnum.CR_13.code);
            if (msg.getBytes()[0] >= KeyEnum.space_32.code && msg.getBytes()[0] <= KeyEnum.shangDian_126.code) {
                List<String> switchList = (List<String>) accepterMap.get(DefaultKey.switchList.keyName);
                if (switchList.size() == 0) {
                    List<Integer> newList = receivedBytes;
                    if (enterIndex >= 0) {
                        newList = newListBeforeIndex(enterIndex, receivedBytes);
                    }
                    msg = KeyEnum.getValuesByCodeList(newList);
                    Reflections.invokeSetter(objectClass, fieldName, fieldValue + msg);
                    //判断是否为密码，密码输出“.”
                    printBackToScreen(ctx, msg, fieldName);
                }
            }
            if (enterIndex >= 0) {
                //回车后，清空接收到的拼装数据
                keyList.clear();
                //判断有无输入，防止误回车
                FieldObject currObject = this.getFieldObjectColName(fieldName);
                if (StringUtils.isEmpty(fieldValue) && !currObject.isCanNull()) {
                    return RECEIVER_TYPE_NOFINISHED;//连续回车
                } else if (forwardMainView(ctx, fieldValue)) {
                    accepterMap.put(DefaultKey.rePage.keyName, true);
                    return RECEIVER_TYPE_FORWARD;
                }
                //如果已完成的数量大于等于获取到的注解属性数量则表示输入全部完成
                Integer receivedIndex = (Integer) accepterMap.get(DefaultKey.completeSize.keyName);
                if (receivedIndex >= fieldObjectList.size()) {
                    HandlerUtil.write(ctx, Constants.BREAK_LINE);
                    return RECEIVER_TYPE_FINISHED;
                } else {
                    Integer completeSize = (Integer) accepterMap.get(DefaultKey.completeSize.keyName);
                    accepterMap.put(DefaultKey.completeSize.keyName, completeSize + 1);
                    //设置上次接收字段为当前接收字段
                    accepterMap.put(DefaultKey.lastCompleteColName.keyName, accepterMap.get(DefaultKey.curColName.keyName));
                    //在显示下一个字段之前，清除可能存在的校验错误提示数据及已经显示的错误提示
                    //如果字段有加密类型则显示对应的密文
                    if (StringUtils.isNotBlank(currObject.getEncrypt())) {
                        fieldValue = fieldValue.replaceAll(".", currObject.getEncrypt());
                    }
                    cleanTipAndErrorInWin(completeSize, fieldValue + msg, accepterMap, ctx);
                    //如果完成接收的属性数量+1后等于获取到的注解属性数量，则表示接收完成，返回接收完成状态
                    if ((completeSize + 1) >= fieldObjectList.size()) {
                        HandlerUtil.write(ctx, Constants.BREAK_LINE);
                        return RECEIVER_TYPE_FINISHED;
                    }

                    accepterMap.put(DefaultKey.curColName.keyName, fieldObjectList.get(completeSize + 1).getFieldName());
                    if (printNext) {
                        FieldObject fieldObject = getFieldObjectByIndex(completeSize + 1);
                        String fieldTip = fieldObject.getFieldTip();
                        fieldTip = fieldObject.isCursorDown() ? fieldTip + Constants.BREAK_LINE : fieldTip;
                        printMessage(ctx, accepterMap, fieldTip);//回车后，页面显示的下一个字段提示信息
                    }
                    return RECEIVER_TYPE_NOFINISHED;
                }
            }
            return RECEIVER_TYPE_NOFINISHED;
        }
    }

    /**
     * 判断是否可以移动光标
     *
     * @param canMoveCursor   true：可以移动；false：不可以移动
     * @param positionMoveKey 接收到的输入
     * @param ctx             handler上下文
     */
    private void cursorPosition(Boolean canMoveCursor, String positionMoveKey, ChannelHandlerContext ctx) {
        //如果设置可以移动光标，则进行移动
        if (canMoveCursor && positionMoveKey != null) {
            HandlerUtil.positionOppositeMove(ctx, positionMoveKey);
        }
    }

    /**
     * @param ctx         handler上下文
     * @param msg         当前接收数据
     * @param fieldName   属性名称，用于去掉一个字符后再次赋值
     * @param fieldValue  属性值，用于减少一个字符
     * @param objectClass handler中的具体业务对象
     * @return 如果是退格则返回true，否则返回false
     */
    private boolean isBackSpace(ChannelHandlerContext ctx, String msg, String fieldName, String fieldValue, Object objectClass) {
        List<String> switchList = (List<String>) getDataMap().get(DefaultKey.switchList.keyName);
        if (msg.getBytes()[0] == KeyEnum.BS_8.code && switchList.size() == 0) {
            if (StringUtils.isNotEmpty(fieldValue) && fieldValue.length() > 0) {
                String strNew = fieldValue.substring(0, fieldValue.length() - 1);
                Reflections.invokeSetter(objectClass, fieldName, strNew);
                HandlerUtil.moveLeftN(ctx, 1);
                HandlerUtil.clearRightN(ctx, 1);
            }
            return true;
        }
        return false;
    }

    /**
     * 是否切换列表
     *
     * @param msg         接收到的键盘相应
     * @param accepterMap map数据容器
     * @param ctx         handler上下文
     * @return 切换列表返回true，否则返回false
     */
    private boolean switchList(String msg, String keyCode, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
//        String kyeCode = HandlerUtil.getKeyCode(msg);
        if (msg.getBytes()[0] != KeyEnum.CR_13.code) {
            List<String> switchList = (List<String>) accepterMap.get(DefaultKey.switchList.keyName);
            if (switchList.size() > 0 && (keyCode.equals("27,91,67") || keyCode.equals("27,91,68"))) {
                String fieldName = (String) accepterMap.get(DefaultKey.curColName.keyName);//属性名称
                Integer listIndex = (Integer) accepterMap.get(DefaultKey.listIndex.keyName);
                Integer newIndex = 0;

                FieldObject fieldObject = getFieldObjectColName(fieldName);
                boolean cursorDown = fieldObject.isCursorDown();
                if (cursorDown) {
                    clearCurColTip(ctx);//清除当前行的显示数据
                    HandlerUtil.write(ctx, Constants.BREAK_LINE);
                } else {
                    clearCurColTip(ctx);//清除当前字段提示行及默认列表值的显示
                    rePrintCurColTip(accepterMap, ctx);
                }
                String defaultValue = "";
                if (HandlerUtil.moveRight.equals(keyCode)) {//右移
                    if (listIndex >= switchList.size()) {
                        newIndex = switchList.size() - 1;
                        defaultValue = switchList.get(newIndex);
                        setAndPrintDefaultValue(defaultValue, accepterMap, ctx);
                    } else {
                        newIndex = listIndex + 1;
                        newIndex = newIndex >= switchList.size() ? switchList.size() - 1 : newIndex;
                        defaultValue = switchList.get(newIndex);
                        setAndPrintDefaultValue(defaultValue, accepterMap, ctx);
                    }
                } else if (HandlerUtil.moveLeft.equals(keyCode)) {
                    if (listIndex == 0) {
                        defaultValue = switchList.get(0);
                        setAndPrintDefaultValue(defaultValue, accepterMap, ctx);
                    } else {
                        newIndex = listIndex - 1;
                        newIndex = newIndex < 0 ? 0 : newIndex;
                        defaultValue = switchList.get(newIndex);
                        setAndPrintDefaultValue(defaultValue, accepterMap, ctx);
                    }
                }
                accepterMap.put(DefaultKey.listIndex.keyName, newIndex);
                return true;
            }
//            else {
//                Boolean canMoveCursor = (Boolean) accepterMap.get(DefaultKey.canMoveCursor.keyName);
//                //如果可以移动光标的话，因为拦截器早与接收器执行，在list切换的时候需要将光标移动回来
//                if (canMoveCursor) {
//                    if (keyCode.equals("27,91,65")) {//如果是上移则将光标移动到下一行，屏幕上显示光标不动
//                        HandlerUtil.moveDownN(ctx, 1);
//                    } else if (keyCode.equals("27,91,66")) {//如果是下移，增将上移相反
//                        HandlerUtil.moveUpN(ctx, 1);
//                    }
//                }
//            }
        } else {
            accepterMap.put(DefaultKey.switchList.keyName, DefaultKey.switchList.defaultVal);
        }
        return false;
    }

    /**
     * 根据设置的光标及错误提示语位置，动态进行调整
     *
     * @param completeSize 已经接收完整的字段数量
     * @param fieldValue   字段值
     * @param ctx          handler上下文
     * @param accepterMap  map数据容器
     */
    public void cleanTipAndErrorInWin(Integer completeSize, String fieldValue, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        //在显示下一个字段之前，清除可能存在的校验错误提示数据及已经显示的错误提示
        String errorMess = (String) accepterMap.get(DefaultKey.curColErrMess.keyName);
        FieldObject curFieldObject = getFieldObjectByIndex(completeSize);
        if (StringUtils.isNotBlank(errorMess)) {
            accepterMap.put(DefaultKey.curColErrMess.keyName, "");//清空map中的错误提示数据
            boolean cursorDown = curFieldObject.isCursorDown();
            boolean tipTop = curFieldObject.isTopTip();
            if (cursorDown && tipTop) {//光标在字段提示下边，错误提示在字段提示上边
                HandlerUtil.delALL(ctx);//删除当前输入行
                HandlerUtil.moveUpN(ctx, 1);//从输入行移动到字段提示行
                HandlerUtil.delALL(ctx);//删除当前字段提示行
                HandlerUtil.moveUpN(ctx, 1);//从字段提示行上移到错误提示行
                HandlerUtil.delALL(ctx);//删除错误提示行
                HandlerUtil.moveUpN(ctx, 1);//多上移一行，并输出回车，光标会回到左边
                HandlerUtil.write(ctx, Constants.BREAK_LINE + curFieldObject.getFieldTip() + Constants.BREAK_LINE + fieldValue);//重新输出字段提示和输入的数据
            }
            if (!cursorDown && !tipTop) {//光标在字段提示右边，错误提示在左边
                HandlerUtil.delALL(ctx);//删除当前错误提示、字段提示及输入
                HandlerUtil.moveUpN(ctx, 1);//多上移一行，并输出回车，光标会回到左边
                HandlerUtil.write(ctx, Constants.BREAK_LINE + curFieldObject.getFieldTip() + fieldValue);//重新输出字段提示和输入的数据
            }
            if (cursorDown && !tipTop) {//光标在字段提示下边，错误提示在字段提示左边
                HandlerUtil.delALL(ctx);//删除当前输入行
                HandlerUtil.moveUpN(ctx, 1);//从输入行移动到字段提示行
                HandlerUtil.delALL(ctx);//删除当前字段提示+错误提示行
                HandlerUtil.moveUpN(ctx, 1);//多上移一行，并输出回车，光标会回到左边
                HandlerUtil.write(ctx, Constants.BREAK_LINE + curFieldObject.getFieldTip() + Constants.BREAK_LINE + fieldValue);//重新输出字段提示和输入的数据
            }
            if (!cursorDown && tipTop) {//光标在字段提示右边，错误提示在上边
                HandlerUtil.delALL(ctx);//删除当前字段提示及输入
                HandlerUtil.moveUpN(ctx, 1);//多上移一行，定位到错误提示行
                HandlerUtil.delALL(ctx);//删除当前错误提示行

                HandlerUtil.moveUpN(ctx, 1);//多上移一行
                HandlerUtil.write(ctx, Constants.BREAK_LINE + curFieldObject.getFieldTip() + fieldValue);//重新输出字段提示和输入的数据
            }

        }
    }


    /**
     * 获取接收到的数据字节码
     *
     * @param msg 接收到的数据
     * @return 转换为字节码数组
     */
    public List<Integer> byteList(String msg) {
        List<Integer> result = Lists.newArrayList();
        byte[] bytes = msg.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            result.add(Integer.valueOf(msg.getBytes()[i]));
        }
        return result;
    }

    /**
     * 将list中某个位置之前的数组放到 new List中
     *
     * @param index list序列号
     * @param list  数据来源list
     * @return 返回新list
     */
    public static List<Integer> newListBeforeIndex(Integer index, List<Integer> list) {
        List<Integer> newList = Lists.newArrayList();
        for (int i = 0; i < index; i++) {
            newList.add(list.get(i));
        }
        return newList;
    }

    /**
     * 按照序列号获取属性对象
     *
     * @param fieldIndex 属性列表中的序列号
     * @return 返回获取到的field对象
     */
    public FieldObject getFieldObjectByIndex(Integer fieldIndex) {
        return fieldObjectList.get(fieldIndex);
    }

    /**
     * 按照属性名称获取属性名称的反射对象
     *
     * @param colName 列名称
     * @return 返回获取的到的field对象
     */
    public FieldObject getFieldObjectColName(String colName) {
        for (FieldObject fieldObject : fieldObjectList) {
            if (fieldObject.getFieldName().equals(colName)) {
                return fieldObject;
            }
        }
        return new FieldObject();
    }

    /**
     * 获取所有的字段名称
     *
     * @return 字段名称数据集合
     */
    private List<String> getFieldNameList() {
        List<String> getFieldNameList = new ArrayList<String>();
        for (FieldObject fieldObject : fieldObjectList) {
            getFieldNameList.add(fieldObject.getFieldName());
        }
        return getFieldNameList;
    }


    /**
     * 按照字段名称获取下一个字段的属性对象
     *
     * @param colName 字段名称
     * @return 下一个字段的属性对象
     */
    public FieldObject getNextFieldObjectByColName(String colName) {
        List<String> colNameList = Lists.newArrayList();
        for (FieldObject fieldObject : fieldObjectList) {
            colNameList.add(fieldObject.getFieldName());
        }
        Integer index = colNameList.indexOf(colName) + 1;
        index = index >= fieldObjectList.size() ? fieldObjectList.size() - 1 : index;
        return getFieldObjectByIndex(index);
    }

    /**
     * 按照字段名称获取字段在List中的序列号
     *
     * @param colName 字段名称
     * @return 字段在fieldObjectList中的序列号
     */
    public Integer getColIndexByColName(String colName) {
        Integer index = 0;
        for (FieldObject fieldObject : fieldObjectList) {
            if (fieldObject.getFieldName().equals(colName)) {
                break;
            }
            index++;
        }
        return index;
    }


    /**
     * 回写到屏幕
     * 如果有密码，在页面不显示出来，可以重写该方法   HandlerUtil.printPoint(ctx);
     *
     * @param ctx       handler上下文
     * @param msg       回写的内容
     * @param fieldName 当前字段名称 重写方法时可依据fieldName进行判断，在界面输出不同的内容
     */
    protected void printBackToScreen(ChannelHandlerContext ctx, String msg, String fieldName) {
        FieldObject fieldObject = this.getFieldObjectColName(fieldName);
        if (StringUtils.isNotBlank(fieldObject.getEncrypt())) {
            HandlerUtil.print(ctx, fieldObject.getEncrypt());
        } else {
            HandlerUtil.print(ctx, msg);
        }
    }

    /*
     * 回车后，输出的提示信息
     *
     * @param ctx     handler上下文
     * @param accepterMap 数据容器
     * @param colTip  字段的输出提示
     */
    public void printMessage(ChannelHandlerContext ctx, Map<String, Object> accepterMap, String colTip) {
        HandlerUtil.drawString(ctx.channel(), Constants.BREAK_LINE);//回车换行;
        HandlerUtil.print(ctx, colTip);
    }

    /**
     * 清空当前需要输入的字段提示，光标上移一行
     *
     * @param ctx handler上下文对象
     */
    public void clearCurColTip(ChannelHandlerContext ctx) {
        HandlerUtil.delALL(ctx);//清除整行
        HandlerUtil.moveUpN(ctx, 1);//光标上移一行
    }


/**
 * 以下功能类方法可以在业务handler中灵活调用，实现不同的显示及接收
 *
 * 具体使用方法可参考每个方法的注释
 *
 * 可以在ReceiveTestManagerImpl中进行测试，
 *
 * 测试前需要将ServerInitializer类的业务入库改为此handler
 *
 * pipeline.addLast("handler", receiveTestManager);
 *
 */

    /**
     * 重新设置某个字段的提示
     *
     * @param colName     字段名称
     * @param newTip      新的提示数据
     * @param accepterMap map数据容器
     * @param ctx         ctx上下文
     */
    public void changeFieldTip(String colName, String newTip, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        for (FieldObject fieldObject : fieldObjectList) {
            if (fieldObject.getFieldName().equals(colName)) {
                fieldObject.setFieldTip(newTip);
                break;
            }
        }
    }


    /**
     * 重新打印当前字段的输入提示
     * 建议使用resetCurCol方法，明确指示待接收字段。去除字段接收顺序与业务domain中属性顺序的绑定
     *
     * @param accepterMap map数据容器
     * @param ctx         handler上下文对象
     */
    @Deprecated
    public void rePrintCurColTip(Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        String fieldName = (String) accepterMap.get(DefaultKey.curColName.keyName);//属性名称
        FieldObject fieldObject = getFieldObjectColName(fieldName);
        String fieldTip = fieldObject.getFieldTip();
        fieldTip = fieldObject.isCursorDown() ? fieldTip + Constants.BREAK_LINE : fieldTip;
        printMessage(ctx, accepterMap, fieldTip);//重新输出当前应该输入的字段提示
    }

    /**
     * 设置某字段需要再次输入，用于校验不通过或需要循环输入的情况(箱号不存在、扫描多个箱号)
     *
     * @param colName     字段属性名称
     * @param errorMess   校验提示信息
     * @param accepterMap 数据容器
     * @param ctx         handler上下文
     * @param notBeep         只要有值就不响铃
     */
    public void colNeedReInput(String colName, String errorMess, Map<String, Object> accepterMap, ChannelHandlerContext ctx,boolean... notBeep) {
        //重新输入字段的时候，需要响铃两次
        if(notBeep==null || notBeep.length==0){
            HandlerUtil.errorBeep(ctx);
        }

        setColUnReceived(colName, accepterMap);
        accepterMap.put(DefaultKey.curColErrMess.keyName, errorMess);
        FieldObject nextFieldObject = getNextFieldObjectByColName(colName);
        Integer completeSize = (Integer) accepterMap.get(DefaultKey.completeSize.keyName);
        Boolean isLastCol = completeSize >= fieldObjectList.size() - 1;
        Boolean nextCursorDown = nextFieldObject.isCursorDown();
        Boolean autoPrintNextCol = (Boolean) accepterMap.get(DefaultKey.autoPrintNextCol.keyName);
        if (autoPrintNextCol) {
            if (isLastCol) {
                HandlerUtil.moveUpN(ctx, 1);//光标上移一行
            } else {
                if (nextCursorDown) {
                    HandlerUtil.moveUpN(ctx, 1);//光标上移一行
                }
                HandlerUtil.delALL(ctx);//因为已经输出下一个待接收字段提示了，先清除掉
                HandlerUtil.moveUpN(ctx, 1);//光标上移一行，定位到上一次的数据输入行
            }
        } else {
            if (isLastCol) {
                HandlerUtil.delALL(ctx);//因为已经输出下一个待接收字段提示了，先清除掉
                HandlerUtil.moveUpN(ctx, 1);//光标上移一行，定位到上一次的数据输入行
            }
        }

        FieldObject fieldObject = getFieldObjectColName(colName);
        boolean cursorDown = fieldObject.isCursorDown();
        boolean tipTop = fieldObject.isTopTip();

        String fieldTip = fieldObject.getFieldTip();

        if (cursorDown && tipTop) {
            //光标在字段提示的下边，且错误提示在字段提示的上边
            HandlerUtil.delALL(ctx);//清除输入的数据
            HandlerUtil.moveUpN(ctx, 1);//光标上移一行到字段提示行
            HandlerUtil.delALL(ctx);//清除字段提示行
            HandlerUtil.moveUpN(ctx, 1);//光标上移一行

            if (StringUtils.isNotBlank(errorMess)) {//如果错误提示不为空则输出，否则将错误提示拼装一个换行符
                errorMess = errorMess + Constants.BREAK_LINE;
            }
            printMessage(ctx, accepterMap, errorMess + fieldTip + Constants.BREAK_LINE);//多输出一个回车，光标定位到字段提示的下边一行
        }

        if (!cursorDown && !tipTop) {
            HandlerUtil.delALL(ctx);//清除当前行的字段提示及所输入的值
            HandlerUtil.moveUpN(ctx, 1);//光标上移一行
            printMessage(ctx, accepterMap, errorMess + fieldTip);
        }
        if (cursorDown && !tipTop) {
            //如果光标在字段提示的下边，但是错误提示在左边，需要多清除一行
            HandlerUtil.delALL(ctx);//清除当前输入
            HandlerUtil.moveUpN(ctx, 1);//光标上移一行
            HandlerUtil.delALL(ctx);//清除当前字段提示及可能存在的左边的错误提示
            HandlerUtil.moveUpN(ctx, 1);//光标上移一行
            printMessage(ctx, accepterMap, errorMess + fieldTip);//输出错误提示
            HandlerUtil.write(ctx, Constants.BREAK_LINE);//输出回车，光标定位到字段提示的下边一行
        }

        if (!cursorDown && tipTop) {
            //如果光标在字段提示的同一行，但是错误提示在上边
            HandlerUtil.delALL(ctx);//清除当前行及输入的数据
            HandlerUtil.moveUpN(ctx, 1);//光标上移一行到字段提示行
            if (StringUtils.isNotBlank(errorMess)) {
                //如果错误提示不为空则输出，否则将错误提示拼装一个换行符
                errorMess = errorMess + Constants.BREAK_LINE;
            }
            printMessage(ctx, accepterMap, errorMess + fieldTip);
        }

    }

    /**
     * 按照字段名称设置某个字段为未输入，且需要清空对象中已经接收到的数据
     *
     * @param colName     字段属性名称
     * @param accepterMap 数据容器
     */
    public void setColUnReceived(String colName, Map<String, Object> accepterMap) {
        accepterMap.put(DefaultKey.curColName.keyName, colName);
        accepterMap.put(DefaultKey.lastCompleteColName.keyName, "");
        Integer completeSize = (Integer) accepterMap.get(DefaultKey.completeSize.keyName) - 1;
        completeSize = completeSize <= 0 ? 0 : completeSize;
        accepterMap.put(DefaultKey.completeSize.keyName, completeSize);
        Reflections.invokeSetter(accepterMap.get(DefaultKey.objectClass.keyName), colName, "");
    }

    /**
     * 清除map中的错误提示
     *
     * @param accepterMap map数据容器
     */
    public void removeErrMess(Map<String, Object> accepterMap) {
        accepterMap.remove(DefaultKey.curColErrMess.keyName);
        accepterMap.put(DefaultKey.lastCompleteColName.keyName, "");
    }

    /**
     * 在显示下一个字段的输入提示的上边一行，显示额外的输出内容
     *
     * @param forPrint    需要输出的额外的内容
     * @param accepterMap map数据容器
     * @param ctx         handler上下文
     */
    public void printBeforeNextField(String forPrint, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {

        String fieldName = (String) accepterMap.get(DefaultKey.curColName.keyName);//属性名称

        FieldObject fieldObject = getFieldObjectColName(fieldName);
        Boolean autoPrintNextCol = (Boolean) accepterMap.get(DefaultKey.autoPrintNextCol.keyName);
        //如果自动打印了下一个字段，先清除
        if (autoPrintNextCol) {
            if (fieldObject.isCursorDown()) {
                clearCurColTip(ctx);//如果光标在下边，则先清除光标行
            }
            clearCurColTip(ctx);//清除提示行
        }
        HandlerUtil.write(ctx, Constants.BREAK_LINE + forPrint);//打印需要输出的内容
        if (autoPrintNextCol) {
            rePrintCurColTip(accepterMap, ctx);//如果是自动打印下一个字段，则重新打印字段提示
        }
    }

    /**
     * 在显示下一个字段的输入提示的上边一行，显示额外的输出内容，其中tipList与valueList需要对应
     *
     * @param tipList     提示信息集合
     * @param valueList   提示信息对应的数据集合
     * @param accepterMap map数据容器
     * @param ctx         handler上下文
     */
    public void printBeforeNextField(List<String> tipList, List<String> valueList, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        if (tipList != null && valueList != null && tipList.size() == valueList.size()) {
            String fieldName = (String) accepterMap.get(DefaultKey.curColName.keyName);//属性名称
            Boolean autoPrintNextCol = (Boolean) accepterMap.get(DefaultKey.autoPrintNextCol.keyName);
            FieldObject fieldObject = getFieldObjectColName(fieldName);
            //如果自动打印了下一个字段，先清除
            if (autoPrintNextCol) {
                if (fieldObject.isCursorDown()) {
                    clearCurColTip(ctx);//如果光标在下边，则先清除光标行
                }
                clearCurColTip(ctx);//清除提示行
            }
            HandlerUtil.write(ctx, Constants.BREAK_LINE);
            for (int i = 0; i < tipList.size(); i++) {
                String tip = tipList.get(i);
                String value = valueList.get(i);
                //如果值不为空则为默认的冒号，如果值为空则不添加分割符号，只显示tip，
                // 用于拼装将多个需要显示的数据拼装为tip后显示一行，否则末尾会多一个分割符号
                String split = StringUtils.isNotBlank(value) ? ":" : "";
                String breakLine = (i + 1) >= tipList.size() ? "" : Constants.BREAK_LINE;//最后一个不换行
                HandlerUtil.write(ctx, tip + split + value + breakLine);
            }
            if (autoPrintNextCol) {
                rePrintCurColTip(accepterMap, ctx);
            }
        } else {
            throw new RuntimeException("输出提示与输出值的数量不匹配");
        }
    }

    /**
     * 在显示下一个字段的输入提示的上边一行，显示额外的输出内容，其中tipList与valueList需要对应
     *
     * @param tipList     提示信息集合
     * @param valueList   提示信息对应的数据集合
     * @param accepterMap map数据容器
     * @param isChangeRow 对应valueList 是否换行，0 不换行，1换行
     * @param ctx         handler上下文
     */
    public void printBeforeNextField(List<String> tipList, List<String> valueList,int[] isChangeRow, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        if (tipList != null && valueList != null && tipList.size() == valueList.size()) {
            String fieldName = (String) accepterMap.get(DefaultKey.curColName.keyName);//属性名称
            Boolean autoPrintNextCol = (Boolean) accepterMap.get(DefaultKey.autoPrintNextCol.keyName);
            FieldObject fieldObject = getFieldObjectColName(fieldName);
            //如果自动打印了下一个字段，先清除
            if (autoPrintNextCol) {
                if (fieldObject.isCursorDown()) {
                    clearCurColTip(ctx);//如果光标在下边，则先清除光标行
                }
                clearCurColTip(ctx);//清除提示行
            }
            HandlerUtil.write(ctx, Constants.BREAK_LINE);
            for (int i = 0; i < tipList.size(); i++) {
                String tip = tipList.get(i);
                String value = valueList.get(i);
                //如果值不为空则为默认的冒号，如果值为空则不添加分割符号，只显示tip，
                // 用于拼装将多个需要显示的数据拼装为tip后显示一行，否则末尾会多一个分割符号
                String split = StringUtils.isNotBlank(value) ? ":" : "";
                String breakLine;
                if(isChangeRow[i]==0){
                    breakLine = "";
                }else{
                    breakLine = Constants.BREAK_LINE;
                }
                HandlerUtil.write(ctx, tip + split + value + breakLine);
            }
            if (autoPrintNextCol) {
                rePrintCurColTip(accepterMap, ctx);
            }
        } else {
            throw new RuntimeException("输出提示与输出值的数量不匹配");
        }
    }
    /**
     * 设置某一个字段为列表切换
     *
     * @param colName     需要切换list的字段名称
     * @param list        需要切换的list数据
     * @param accepterMap map数据容器
     * @param ctx         handler上下文
     */
    public void setColSwitchList(String colName, List<String> list, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        String curColName = (String) accepterMap.get(DefaultKey.curColName.keyName);
        Boolean autoPrintNextCol = (Boolean) accepterMap.get(DefaultKey.autoPrintNextCol.keyName);
        if (!curColName.equals(colName) && autoPrintNextCol) {
            FieldObject fieldObject = getFieldObjectColName(curColName);
            if (fieldObject.isCursorDown()) {
                HandlerUtil.moveUpN(ctx, 1);
            }
            HandlerUtil.delALL(ctx);
            HandlerUtil.moveUpN(ctx, 1);
            FieldObject newFieldObject = getFieldObjectColName(colName);
            String fieldTip = newFieldObject.getFieldTip();
            HandlerUtil.write(ctx, Constants.BREAK_LINE + fieldTip);
        }
        accepterMap.put(DefaultKey.curColName.keyName, colName);
        setNextColSwitchList(list, accepterMap, ctx);
    }

    /**
     * 在一个字段接收完成之后，设置下一个为需要切换的list
     *
     * @param list        需要切换的list数据
     * @param accepterMap map数据容器
     * @param ctx         handler上下文
     */
    public void setNextColSwitchList(List<String> list, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        setNextColSwitchList(list.get(0), list, accepterMap, ctx);
    }

    /**
     * 在一个字段接收完成之后，设置下一个为需要切换的list
     *
     * @param defaultVal  默认值
     * @param list        需要切换的list数据
     * @param accepterMap map数据容器
     * @param ctx         handler上下文
     */
    public void setNextColSwitchList(String defaultVal, List<String> list, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        if (CollectionUtil.isNotEmpty(list)) {
            accepterMap.put(DefaultKey.switchList.keyName, list);
            accepterMap.put(DefaultKey.lastCompleteColName.keyName, "");
            Boolean autoPrintNextCol = (Boolean) accepterMap.get(DefaultKey.autoPrintNextCol.keyName);
            if (!autoPrintNextCol) {//如果下一个字段不是自动显示的
                rePrintCurColTip(accepterMap, ctx);
            }
            setAndPrintDefaultValue(defaultVal, accepterMap, ctx);
            Integer listIndex = list.indexOf(defaultVal);
            listIndex = listIndex < 0 ? 0 : listIndex;
            accepterMap.put(DefaultKey.listIndex.keyName, listIndex);
        }
    }

    /**
     * 设置字段为重新切换列表
     *
     * @param list        需要切换显示的list数据
     * @param mess        需要显示的提示信息
     * @param accepterMap map数据容器
     * @param ctx         handler上下文
     */
    public void setColReSwitchList(List<String> list, String mess, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        setColReSwitchList(list.get(0), list, mess, accepterMap, ctx);
    }

    /**
     * 设置字段为重新切换列表
     *
     * @param defaultVal  默认值
     * @param list        需要切换显示的list数据
     * @param mess        需要显示的提示信息
     * @param accepterMap map数据容器
     * @param ctx         handler上下文
     */
    public void setColReSwitchList(String defaultVal, List<String> list, String mess, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        if (CollectionUtil.isNotEmpty(list)) {
            String curColNameBeforeReset = (String) accepterMap.get(DefaultKey.curColName.keyName);//上一个待接收字段
            String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);//当前列表字段
            accepterMap.put(DefaultKey.curColName.keyName, lastCompleteColName);
            accepterMap.put(DefaultKey.lastCompleteColName.keyName, "");
            accepterMap.put(DefaultKey.switchList.keyName, list);

            Boolean autoPrintNextCol = (Boolean) accepterMap.get(DefaultKey.autoPrintNextCol.keyName);
            if (autoPrintNextCol) {//如果自动打印下一个字段
                FieldObject fieldObject = this.getFieldObjectColName(curColNameBeforeReset);
                if (fieldObject.isCursorDown()) {
                    clearCurColTip(ctx);//如果下一个字段的光标是在下边就先清除下一个字段的光标行
                }
                clearCurColTip(ctx);//清除下一个字段提示行
            }
            FieldObject lastCompleteField = this.getFieldObjectColName(lastCompleteColName);
            if (lastCompleteField.isCursorDown()) {
                clearCurColTip(ctx);//如果当前列表接收字段的光标是在下边就先清除当前字段的光标行
            }
            clearCurColTip(ctx);//清除当前列表显示行

            Object objectClass = accepterMap.get(DefaultKey.objectClass.keyName);
            String fieldName = (String) accepterMap.get(DefaultKey.curColName.keyName);//属性名称
            Reflections.invokeSetter(objectClass, fieldName, defaultVal);

            Boolean isCursorDown = lastCompleteField.isCursorDown();
            String cursorPosition = isCursorDown ? Constants.BREAK_LINE : "";
            if (lastCompleteField.isTopTip()) {
                printMessage(ctx, accepterMap, mess + Constants.BREAK_LINE + lastCompleteField.getFieldTip() + cursorPosition);//重新输出当前应该输入的字段提示
            } else {
                printMessage(ctx, accepterMap, mess + lastCompleteField.getFieldTip() + cursorPosition);
            }
            HandlerUtil.write(ctx, defaultVal);


            Integer listIndex = list.indexOf(defaultVal);
            listIndex = listIndex < 0 ? 0 : listIndex;
            accepterMap.put(DefaultKey.listIndex.keyName, listIndex);
            Integer completeSize = (Integer) accepterMap.get(DefaultKey.completeSize.keyName) - 1;
            completeSize = completeSize <= 0 ? 0 : completeSize;
            accepterMap.put(DefaultKey.completeSize.keyName, completeSize);
            accepterMap.put(DefaultKey.curColErrMess.keyName, mess);
        }
    }

    /**
     * 重新设置当前接收字段，可实现字段接收之间的灵活跳转
     * 会重置completeSize数据，下次接收将会从colName的下一个字段开始，未接收字段将会忽略
     *
     * @param colName     字段名称
     * @param accepterMap map数据容器
     * @param ctx         handler上下文
     */
    public void resetCurCol(String colName, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        Boolean autoPrintNextCol = (Boolean) accepterMap.get(DefaultKey.autoPrintNextCol.keyName);
        String curColNameBeforeReset = (String) accepterMap.get(DefaultKey.curColName.keyName);
        accepterMap.put(DefaultKey.curColName.keyName, colName);
        accepterMap.put(DefaultKey.lastCompleteColName.keyName, "");
        Integer index = getColIndexByColName(colName);
        accepterMap.put(DefaultKey.completeSize.keyName, index);
        if (autoPrintNextCol) {
            clearCurColTip(ctx);
            FieldObject fieldObject = this.getFieldObjectColName(curColNameBeforeReset);
            if (fieldObject.isCursorDown()) {
                clearCurColTip(ctx);
            }
        }
        rePrintCurColTip(accepterMap, ctx);
    }
    /**
     * 重新设置当前接收字段，可实现字段接收之间的灵活跳转(不打印当前字段提示)
     * 会重置completeSize数据，下次接收将会从colName的下一个字段开始，未接收字段将会忽略
     *
     * @param colName     字段名称
     * @param accepterMap map数据容器
     * @param ctx         handler上下文
     */
    public void resetCurColNoPrint(String colName, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        Boolean autoPrintNextCol = (Boolean) accepterMap.get(DefaultKey.autoPrintNextCol.keyName);
        String curColNameBeforeReset = (String) accepterMap.get(DefaultKey.curColName.keyName);
        accepterMap.put(DefaultKey.curColName.keyName, colName);
        accepterMap.put(DefaultKey.lastCompleteColName.keyName, "");
        Integer index = getColIndexByColName(colName);
        accepterMap.put(DefaultKey.completeSize.keyName, index);
        if (autoPrintNextCol) {
            clearCurColTip(ctx);
            FieldObject fieldObject = this.getFieldObjectColName(curColNameBeforeReset);
            if (fieldObject.isCursorDown()) {
                clearCurColTip(ctx);
            }
        }
    }
    /**
     * 重新设置当前接收字段，可实现字段接收之间的灵活跳转
     * 会重置completeSize数据，下次接收将会从colName的下一个字段开始，未接收字段将会忽略
     *
     * @param defaultVal  默认值
     * @param colName     字段名称
     * @param accepterMap map数据容器
     * @param ctx         handler上下文
     */
    public void resetCurCol(String defaultVal, String colName, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        resetCurCol(colName, accepterMap, ctx);
        setAndPrintDefaultValue(defaultVal, accepterMap, ctx);
    }

    /**
     * 从当前光标开始显示
     *
     * @param defaultValue 需要显示的数据
     * @param accepterMap  map数据容器
     * @param ctx          handler上下文
     */
    public void setAndPrintDefaultValue(String defaultValue, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        Object objectClass = accepterMap.get(DefaultKey.objectClass.keyName);
        String fieldName = (String) accepterMap.get(DefaultKey.curColName.keyName);//属性名称
        Reflections.invokeSetter(objectClass, fieldName, defaultValue);
        HandlerUtil.write(ctx, defaultValue);
    }

    /**
     * 设置某个字段的默认值
     *
     * @param defaultValue 默认值
     * @param colName      制定的字段属性名称
     * @param accepterMap  map数据容器
     * @param ctx          handler上下文
     */
    public void setDefaultValue(String defaultValue, String colName, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        Object objectClass = accepterMap.get(DefaultKey.objectClass.keyName);
        Reflections.invokeSetter(objectClass, colName, defaultValue);
    }


    /**
     * 重新打印指定字段及已经接收的数据，并设置某个字段为待接收
     * 此方法会将fieldNameList之外的已经接收字段的数据清空
     *
     * @param pageHeader    界面头部提示，如果不为空则清屏并重新输出
     * @param fieldNameList 待重新显示的字段名称
     * @param toReceiveCol  待重新接收的字段名称
     * @param accepterMap   map数据容器
     * @param ctx           handler上下文对象
     */
    public void printFieldsAndReceiveData(String[] pageHeader, List<String> fieldNameList, String toReceiveCol, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        if (pageHeader != null && pageHeader.length > 0) {
            HandlerUtil.clearAll(ctx.channel());
            HandlerUtil.writer(ctx, pageHeader, 1, 1);
            HandlerUtil.moveUpN(ctx, 1);//因为printMessage方法输出了回车，所以多上移一行
        }
        Object objectClass = accepterMap.get(DefaultKey.objectClass.keyName);
        for (String fieldName : fieldNameList) {
            String defaultVal = (String) Reflections.invokeGetter(objectClass, fieldName);
            resetCurCol(defaultVal, fieldName, accepterMap, ctx);
        }
        for (String fieldName : getFieldNameList()) {
            if (!fieldNameList.contains(fieldName)) {
                Reflections.invokeSetter(objectClass, fieldName, "");
            }
        }
        if (StringUtils.isNotEmpty(toReceiveCol)) {
            setColUnReceived(toReceiveCol, accepterMap);
            resetCurCol(toReceiveCol, accepterMap, ctx);
        }

    }

    /**
     * 重新打印指定字段及已经接收的数据，并设置某个字段为待接收
     * 此方法会将fieldNameList之外的已经接收字段的数据清空
     *
     * @param pageHeader    界面头部提示，如果不为空则清屏并重新输出
     * @param fieldNameList 待重新显示的字段名称
     * @param switchList    如果待接收字段需要切换列表，请提供数据
     * @param toReceiveCol  待重新接收的字段名称
     * @param accepterMap   map数据容器
     * @param ctx           handler上下文对象
     */
    public void printFieldsAndReceiveData(String[] pageHeader, List<String> fieldNameList, List<String> switchList, String toReceiveCol, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        if (pageHeader != null && pageHeader.length > 0) {
            HandlerUtil.clearAll(ctx.channel());
            HandlerUtil.writer(ctx, pageHeader, 1, 1);
            HandlerUtil.moveUpN(ctx, 1);//因为printMessage方法输出了回车，所以多上移一行
        }
        Object objectClass = accepterMap.get(DefaultKey.objectClass.keyName);
        for (String fieldName : fieldNameList) {
            String defaultVal = (String) Reflections.invokeGetter(objectClass, fieldName);
            resetCurCol(defaultVal, fieldName, accepterMap, ctx);
        }
        for (String fieldName : getFieldNameList()) {
            if (!fieldNameList.contains(fieldName)) {
                Reflections.invokeSetter(objectClass, fieldName, "");
            }
        }
        if (StringUtils.isNotEmpty(toReceiveCol)) {
            if (switchList != null && switchList.size() > 0) {
                setColSwitchList(toReceiveCol, switchList, accepterMap, ctx);
            } else {
                setColUnReceived(toReceiveCol, accepterMap);
                resetCurCol(toReceiveCol, accepterMap, ctx);
            }
        }
    }


    /**
     * 重新打印指定字段及已经接收的数据，并设置某个字段为待接收
     *
     * @param pageHeader    界面头部提示，如果不为空则清屏并重新输出
     * @param fieldNameList 待重新显示的字段名称
     * @param toReceiveCol  待重新接收的字段名称
     * @param clearAllData  是否清空所有已经接收的数据，如果不清空的话，会存在重复接收数据的问题
     * @param accepterMap   map数据容器
     * @param ctx           handler上下文对象
     */
    public void printFieldsAndReceiveData(String[] pageHeader, List<String> fieldNameList, String toReceiveCol, boolean clearAllData, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        if (pageHeader != null && pageHeader.length > 0) {
            HandlerUtil.clearAll(ctx.channel());
            HandlerUtil.writer(ctx, pageHeader, 1, 1);
            HandlerUtil.moveUpN(ctx, 1);//因为printMessage方法输出了回车，所以多上移一行
        }
        Object objectClass = accepterMap.get(DefaultKey.objectClass.keyName);
        for (String fieldName : fieldNameList) {
            String defaultVal = (String) Reflections.invokeGetter(objectClass, fieldName);
            resetCurCol(defaultVal, fieldName, accepterMap, ctx);
        }
        if (clearAllData) {
            for (String fieldName : getFieldNameList()) {
                Reflections.invokeSetter(objectClass, fieldName, "");
            }
        }
        if (StringUtils.isNotEmpty(toReceiveCol)) {
            setColUnReceived(toReceiveCol, accepterMap);
            resetCurCol(toReceiveCol, accepterMap, ctx);
        }

    }

    /**
     * @param pageHeader    界面头部提示，如果不为空则清屏并重新输出
     * @param fieldNameList 待重新显示的字段名称
     * @param toReceiveCol  待重新接收的字段名称
     * @param fieldToClear  需要清空数据的字段集合
     * @param accepterMap   map数据容器
     * @param ctx           handler上下文对象
     */
    public void printFieldsAndReceiveData(String[] pageHeader, List<String> fieldNameList, String toReceiveCol, List<String> fieldToClear, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        if (pageHeader != null && pageHeader.length > 0) {
            HandlerUtil.clearAll(ctx.channel());
            HandlerUtil.writer(ctx, pageHeader, 1, 1);
            HandlerUtil.moveUpN(ctx, 1);//因为printMessage方法输出了回车，所以多上移一行
        }
        Object objectClass = accepterMap.get(DefaultKey.objectClass.keyName);
        for (String fieldName : fieldNameList) {
            String defaultVal = (String) Reflections.invokeGetter(objectClass, fieldName);
            resetCurCol(defaultVal, fieldName, accepterMap, ctx);
        }
        if (fieldToClear != null && fieldToClear.size() > 0) {
            for (String fieldName : fieldToClear) {
                Reflections.invokeSetter(objectClass, fieldName, "");
            }
        }
        if (StringUtils.isNotEmpty(toReceiveCol)) {
            setColUnReceived(toReceiveCol, accepterMap);
            resetCurCol(toReceiveCol, accepterMap, ctx);
        }
    }

    /**
     * @param pageHeader        界面头部提示，如果不为空则清屏并重新输出
     * @param printMsgBeforeCol 该字段名称前，输出额外的提示信息
     * @param fieldNameList     待重新显示的字段名称
     * @param toReceiveCol      待重新接收的字段名称
     * @param fieldToClear      需要清空数据的字段集合
     * @param accepterMap       map数据容器
     * @param ctx               Context上下文
     */
    public void printFieldsAndReceiveData(String[] pageHeader, String printMsgBeforeCol, List<String> fieldNameList, String toReceiveCol, List<String> fieldToClear, Map<String, Object> accepterMap, ChannelHandlerContext ctx) {
        if (pageHeader != null && pageHeader.length > 0) {
            HandlerUtil.clearAll(ctx.channel());
            HandlerUtil.writer(ctx, pageHeader, 1, 1);
            HandlerUtil.moveUpN(ctx, 1);//因为printMessage方法输出了回车，所以多上移一行
        }
        Object objectClass = accepterMap.get(DefaultKey.objectClass.keyName);
        for (String fieldName : fieldNameList) {
            if (fieldName.equals(printMsgBeforeCol)) {
                printMsg(objectClass, accepterMap);//额外的提示信息
            }
            String defaultVal = (String) Reflections.invokeGetter(objectClass, fieldName);
            resetCurCol(defaultVal, fieldName, accepterMap, ctx);
        }
        if (fieldToClear != null && fieldToClear.size() > 0) {
            for (String fieldName : fieldToClear) {
                Reflections.invokeSetter(objectClass, fieldName, "");
            }
        }
        if (StringUtils.isNotEmpty(toReceiveCol)) {
            setColUnReceived(toReceiveCol, accepterMap);
            resetCurCol(toReceiveCol, accepterMap, ctx);
        }
    }

    /**
     * 输出相应的信息
     *
     * @param objectClass 当前业务类
     * @param accepterMap map数据容器
     */
    protected void printMsg(Object objectClass, Map<String, Object> accepterMap) {
        //子类复写，该方法
    }

    /**
     * 完全重置map中的数据，清屏并重新从类对象的第一个字段开始接收输入，相当于调用channelActive方法
     *
     * @param objectClass 待接收的对象
     * @param pageHeader  初始化屏幕显示提示
     * @param cleanWin    是否清屏，如果为true，则pageHeader不能为空
     * @param ctx         handler上下文
     */
    public void resetBaseMap(Class objectClass, String[] pageHeader, boolean cleanWin, ChannelHandlerContext ctx) throws Exception {
        if (cleanWin && (pageHeader == null || pageHeader.length == 0)) {
            throw new RuntimeException("如需清屏，必须提供pageHeader");
        }
        if (!cleanWin) {
            pageHeader = null;
        }
        this.initBaseMap(objectClass, pageHeader, ctx);
    }

    /**
     * 以当前map中的类对象为基础，重新加载待接收的字段，忽略掉已经加载的字段
     *
     * @param fieldNameList 待接收的字段名称列表
     * @param pageHeader    初始化屏幕显示提示
     * @param cleanWin      是否清屏，如果为true，则pageHeader不能为空
     * @param ctx           handler上下文
     */
    public void resetBaseMap(List<String> fieldNameList, String[] pageHeader, boolean cleanWin, ChannelHandlerContext ctx) throws Exception {
        if (cleanWin && (pageHeader == null || pageHeader.length == 0)) {
            throw new RuntimeException("如需清屏，必须提供pageHeader");
        }
        if (cleanWin && pageHeader.length > 0) {
            HandlerUtil.clearAll(ctx.channel());
            HandlerUtil.writer(ctx, pageHeader, 1, 1);
        } else {
            HandlerUtil.write(ctx, Constants.BREAK_LINE);//如果不需要清屏，则输出一个空行
        }

        Map<String, Object> accepterMap = getDataMap();
        if (accepterMap == null || accepterMap.isEmpty()) {
            throw new RuntimeException("mapThread未初始化");
        }
        Class objectClass = (Class) accepterMap.get(DefaultKey.objectClass.keyName);
        fieldObjectList.clear();
        fieldObjectList = AnnotationUtil.getObjectFieldsByList(objectClass, fieldNameList);

        FieldObject fieldObject = fieldObjectList.get(0);
        accepterMap = setBaseKVInMap(fieldObject.getFieldName(), objectClass, accepterMap);

        String fieldTip = fieldObject.getFieldTip();
        fieldTip = fieldObject.isCursorDown() ? fieldTip + Constants.BREAK_LINE : fieldTip;
        HandlerUtil.writeAndFlush(ctx, fieldTip);
        setDataMap(accepterMap);

    }

    /**
     * 重新设置待接收的数据对象，重新加载待接收的字段，忽略掉已经加载的字段
     *
     * @param objectClass   新的待接收对象
     * @param fieldNameList 待接收的字段名称列表
     * @param pageHeader    初始化屏幕显示提示
     * @param cleanWin      是否清屏，如果为true，则pageHeader不能为空
     * @param ctx           handler上下文
     */
    public void resetBaseMap(Class objectClass, List<String> fieldNameList, String[] pageHeader, boolean cleanWin, ChannelHandlerContext ctx) throws Exception {
        Map<String, Object> accepterMap = getDataMap();
        if (accepterMap == null || accepterMap.isEmpty()) {
            throw new RuntimeException("mapThread未初始化");
        }
        setObjectClassInMap(objectClass, accepterMap);
        resetBaseMap(fieldNameList, pageHeader, cleanWin, ctx);
    }

    /**
     * 设置可移动光标，默认为不可移动光标
     *
     * @param accepterMap map数据容器
     */
    public void openCursorMove(Map<String, Object> accepterMap) {
        accepterMap.put(DefaultKey.canMoveCursor.keyName, true);
    }

    /**
     * 关闭光标的移动，默认为不可移动光标
     *
     * @param accepterMap map数据容器
     */
    public void closeCursorMove(Map<String, Object> accepterMap) {
        accepterMap.put(DefaultKey.canMoveCursor.keyName, DefaultKey.canMoveCursor.defaultVal);
    }
    public boolean toChannelActive(ChannelHandlerContext ctx) throws Exception {
        Object obj = getDataMap().get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) getDataMap().get(DefaultKey.lastCompleteColName.keyName);
        Object fieldValue = Reflections.getFieldValue(obj, lastCompleteColName);
        if(fieldValue.equals(Constants.ORDER_FOR_BACK_OPER)){
            channelActive(ctx);
            return true;
        }else {
            return false;
        }
    }

}
