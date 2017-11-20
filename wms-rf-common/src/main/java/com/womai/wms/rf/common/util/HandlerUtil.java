package com.womai.wms.rf.common.util;

import com.google.common.collect.Lists;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.KeyEnum;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by xixiaochuan on 14-11-26.
 */
public class HandlerUtil {


    /**
     * 添加按行接收数据的编码handerl
     *
     * @param ctx hander 上下文
     */
    public static void addLineBasedDecoder(ChannelHandlerContext ctx) {
        removeLineBasedDecoder(ctx);
        ChannelPipeline pipeline = ctx.pipeline();
        pipeline.addLast(Constants.LINE_BASED_FRAME_DECODER, new LineBasedFrameDecoder(1024));
    }

    /**
     * 删除按行编码的handler
     *
     * @param ctx handler上下文
     */
    public static void removeLineBasedDecoder(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();
        if (pipeline.get(Constants.LINE_BASED_FRAME_DECODER) != null) {
            pipeline.remove(Constants.LINE_BASED_FRAME_DECODER);
        }
    }

    /**
     * 根据用户的当前输入获取keyCode值
     *
     * @param msg 用户当前输入
     * @return 返回keyCode以逗号分隔的字符串
     */
    public static String getKeyCode(String msg) {
        StringBuffer codeBuffer = new StringBuffer();
        for (int i = 0; i < msg.getBytes().length; i++) {
            if (i == 0) {
                codeBuffer.append(msg.getBytes()[i]);
            } else {
                codeBuffer.append(",").append(msg.getBytes()[i]);
            }
        }
        return codeBuffer.toString();
    }

    /**
     * 按照keyCode进行光标移动
     *
     * @param ctx
     * @param msg  接收到的用户输入
     */
    public static void positionMove(ChannelHandlerContext ctx, String msg) {
        boolean isMove = false;
//        PDA中普通字母getBytes的长度为1，上下左右为组合键，长度为3，
//        ESC[nA:光标上移n行。27,91,65
//        ESC[nB:光标下移n行。27,91,66
//        ESC[nC:光标右移n个字符。27,91,67
//        ESC[nD:光标左移n个字符。27,91,68
        String kyeCode = HandlerUtil.getKeyCode(msg);
        if (moveUp.equals(kyeCode)) {
            moveUpN(ctx, 1);
        } else if (moveDown.equals(kyeCode)) {
            moveDownN(ctx, 1);
        } else if (moveRight.equals(kyeCode)) {
            moveRightN(ctx, 1);
        } else if (moveLeft.equals(kyeCode)) {
            moveLeftN(ctx, 1);
        }
    }

    public final static String moveUp = "27,91,65";//光标上移的键盘数值
    public final static String moveDown = "27,91,66";//光标下移的键盘数值
    public final static String moveRight = "27,91,67";//光标右移的键盘数值
    public final static String moveLeft = "27,91,68";//光标左移的键盘数值

    /**
     * 光标移动
     * @param ctx ctx上下文
     * @param positionMoveKey 接收到的用户输入
     */
    public static void positionOppositeMove(ChannelHandlerContext ctx, String positionMoveKey){
        String kyeCode = positionMoveKey;
        if (kyeCode.equals(moveUp)) {
            moveUpN(ctx, 1);
        } else if (kyeCode.equals(moveDown)) {
            moveDownN(ctx, 1);
        } else if (kyeCode.equals(moveRight)) {
            moveRightN(ctx, 1);
        } else if (kyeCode.equals(moveLeft)) {
            moveLeftN(ctx, 1);
        }
    }


    /**
     * 设置红色字体，前景色为红色，背景色为客户端默认
     * 设置字体颜色后，一定要removeCustomStyle，清除一下，否则后续全都是红色
     * http://www.termsys.demon.co.uk/vtansi.htm
     * 参考以上网址最下边的Set Display Attributes目录
     * esc键+英文中括号[+对应的数字+小写字母m组成的命令
     *
     *
     * @param ctx
     */
    public static void setFontRed(ChannelHandlerContext ctx) {
        byte[] arrayOfByte = new byte[5];
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        arrayOfByte[2] = 51;//数字3
        arrayOfByte[3] = 49;//数字1
        arrayOfByte[4] = 109;
        ctx.channel().write(arrayOfByte);
    }

    /**
     * 清除自定义的样式，还原到设备的默认设置
     *
     * @param ctx
     */
    public static void removeCustomStyle(ChannelHandlerContext ctx) {
        byte[] arrayOfByte = new byte[4];
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        arrayOfByte[2] = 48;//0
        arrayOfByte[3] = 109;
        ctx.channel().write(arrayOfByte);
    }


    /**
     * 设置响铃
     * @param ctx
     */
    public static void errorBeep(ChannelHandlerContext ctx) {
        beep(ctx, Constants.BEEP_TIMES);
    }


    /**
     * 响铃一次
     *
     * @param ctx
     */
    public static void beepOnce(ChannelHandlerContext ctx) {
        beep(ctx, 1);
    }

    /**
     * 响铃
     *
     * @param ctx
     * @param times 响铃次数
     */
    public static void beep(ChannelHandlerContext ctx, Integer times) {
        byte[] arrayOfByte = new byte[times];
        for (int i = 0; i < times; i++) {
            arrayOfByte[i] = 7;
        }
        ctx.write(arrayOfByte);
    }


    /**
     * @param str        显示字符串
     * @param cursor     字符串所在起始坐标
     * @param ctx
     * @param startindex 默认{0,0}，flag操作起始位置
     * @param flag       0：默认(无操作) 1:先清屏 2：先清除光标右侧 3：清除光标右下方
     * @return
     */
    public static void getView(String[] str, int[][] cursor, ChannelHandlerContext ctx, int[] startindex, int flag) {
        List<byte[]> list = new ArrayList<byte[]>();
        for (int i = 0; i < str.length; i++) {
            byte[] vector = HandlerUtil.getCursorPositioningSequence(cursor[i]);
            list.add(vector);
            if (cursor[i][0] != 0 && cursor[i][1] != 0) {
                byte[] dest = str[i].getBytes();
                list.add(dest);
            }
        }
        getOperation(startindex, flag, ctx);
        for (int j = 0; j < list.size(); j++) {
            ctx.channel().write(list.get(j));
        }
    }

    public static byte[] getCursorPositioningSequence(int[] paramArrayOfInt) {
        byte[] arrayOfByte1 = null;
        if ((paramArrayOfInt[0] == 0) && (paramArrayOfInt[1] == 0)) {
            //27 91 50 75 ESC[2K:清除整行，光标不动。
            arrayOfByte1 = new byte[4];
            arrayOfByte1[0] = 27;
            arrayOfByte1[1] = 91;
            arrayOfByte1[2] = 50;
            arrayOfByte1[3] = 75;
        } else {
            //ESC[n;mH :光标定位到第n行m列(类似代码ESC[n;mf)。
            byte[] arrayOfByte2 = translateIntToDigitCodes(paramArrayOfInt[0]);
            byte[] arrayOfByte3 = translateIntToDigitCodes(paramArrayOfInt[1]);
            int i = 0;
            arrayOfByte1 = new byte[4 + arrayOfByte2.length
                    + arrayOfByte3.length];
            arrayOfByte1[0] = 27;
            arrayOfByte1[1] = 91;
            System.arraycopy(arrayOfByte2, 0, arrayOfByte1, 2,
                    arrayOfByte2.length);
            i = 2 + arrayOfByte2.length;
            arrayOfByte1[i] = 59;
            i++;
            System.arraycopy(arrayOfByte3, 0, arrayOfByte1, i,
                    arrayOfByte3.length);
            i += arrayOfByte3.length;
            arrayOfByte1[i] = 72;
        }
        return arrayOfByte1;
    }

    public static byte[] locationCursor(int n, int m) {
        byte[] arrayOfByte1 = null;
        //ESC[n;mH :光标定位到第n行m列(类似代码ESC[n;mf)。
        byte[] arrayOfByte2 = translateIntToDigitCodes(n);
        byte[] arrayOfByte3 = translateIntToDigitCodes(m);
        int i = 0;
        arrayOfByte1 = new byte[4 + arrayOfByte2.length
                + arrayOfByte3.length];
        arrayOfByte1[0] = 27;
        arrayOfByte1[1] = 91;
        System.arraycopy(arrayOfByte2, 0, arrayOfByte1, 2,
                arrayOfByte2.length);
        i = 2 + arrayOfByte2.length;
        arrayOfByte1[i] = 59;
        i++;
        System.arraycopy(arrayOfByte3, 0, arrayOfByte1, i,
                arrayOfByte3.length);
        i += arrayOfByte3.length;
        arrayOfByte1[i] = 72;
        return arrayOfByte1;
    }

    private static byte[] translateIntToDigitCodes(int paramInt) {
        return Integer.toString(paramInt).getBytes();
    }

    private static void getOperation(int[] startindex, int flag, ChannelHandlerContext ctx) {
        // 清屏ESC[2J
        byte[] arrayOfByte = new byte[4];
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        arrayOfByte[2] = 50;
        arrayOfByte[3] = 74;
        // 清除光标右侧ESC[K
        byte[] arrayOfByte1 = new byte[3];
        arrayOfByte1[0] = 27;
        arrayOfByte1[1] = 91;
        arrayOfByte1[2] = 75;
        // 清除光标右下方ESC[J
        byte[] arrayOfByte2 = new byte[3];
        arrayOfByte2[0] = 27;
        arrayOfByte2[1] = 91;
        arrayOfByte2[2] = 74;
        //0：默认 1:先清屏 2：先清除光标右侧 3：清除光标右下方
        byte[] vector = getCursorPositioningSequence(startindex);
        if (flag == 1) {
            ctx.channel().write(vector);
            ctx.channel().write(arrayOfByte);
        } else if (flag == 2) {
            ctx.channel().write(vector);
            ctx.channel().write(arrayOfByte1);
        } else if (flag == 3) {
            ctx.channel().write(vector);
            ctx.channel().write(arrayOfByte2);
        }
    }

    /**
     * ESC[K清除光标右侧,删除操作
     */
    public static void removeInput(ChannelHandlerContext ctx) {
        byte[] bytestr = new byte[3];
        bytestr[0] = 27;
        bytestr[1] = 91;
        bytestr[2] = 75;
        ctx.write(bytestr);
    }

    /**
     * 清除光标右下方ESC[J
     */
    public static void removeRightDown(ChannelHandlerContext ctx) {
        byte[] arrayOfByte2 = new byte[3];
        arrayOfByte2[0] = 27;
        arrayOfByte2[1] = 91;
        arrayOfByte2[2] = 74;
        ctx.write(arrayOfByte2);
    }

    /**
     * //ESC[1D 光标左移1,输出.密码操作
     */
    public static void passwordPrint(ChannelHandlerContext ctx) {
        byte[] bytestr = new byte[5];
        bytestr[0] = 27;
        bytestr[1] = 91;
        bytestr[2] = 49;
        bytestr[3] = 68;
        bytestr[4] = 46;
        ctx.write(bytestr);
    }

    /**
     * 输出点，用户显示用户密码输入
     *
     * @param ctx
     */
    public static void printPoint(ChannelHandlerContext ctx) {
        byte[] bytestr = new byte[1];
        bytestr[0] = 46;
        ctx.write(bytestr);
    }

    /**
     * ESC[1C光标右移一个字节
     */
    public static void rightMove(ChannelHandlerContext ctx) {
        byte[] bytestr = new byte[4];
        bytestr[0] = 27;
        bytestr[1] = 91;
        bytestr[2] = 49;
        bytestr[3] = 67;
        ctx.write(bytestr);
    }

    //==================清除指令=======================//

    /**
     * 清空屏幕
     *
     * @param channel
     */
    public static void clearAll(Channel channel) {
        // 清屏ESC[2J
        byte[] arrayOfByte = new byte[4];
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        arrayOfByte[2] = 50;
        arrayOfByte[3] = 74;
        channel.write(arrayOfByte);
    }

    /**
     * ESC[nX:清除光标右边n个字符，光标不动。
     */
    public static void delRightN(ChannelHandlerContext ctx, int num) {
        byte[] array = translateIntToDigitCodes(num);
        byte[] arrayOfByte = new byte[3 + array.length];
        int i = 0;
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        System.arraycopy(array, 0, arrayOfByte, 2,
                array.length);
        i = 2 + array.length;
        arrayOfByte[i] = 88;
        ctx.write(arrayOfByte);
    }

    /**
     * ESC[K;清除光标右边全部字符，光标不动。
     */
    public static void delRight(ChannelHandlerContext ctx) {
        byte[] arrayOfByte = new byte[3];
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        arrayOfByte[2] = 75;
        ctx.write(arrayOfByte);
    }

    /**
     * ESC[1K:清除光标左边全部字符，光标不动。
     */
    public static void delLeft(ChannelHandlerContext ctx) {
        byte[] arrayOfByte = new byte[4];
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        arrayOfByte[2] = 49;
        arrayOfByte[3] = 75;
        ctx.write(arrayOfByte);

    }

    /**
     * ESC[2K:清除整行，光标不动。
     */
    public static void delALL(ChannelHandlerContext ctx) {
        byte[] arrayOfByte = new byte[4];
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        arrayOfByte[2] = 50;
        arrayOfByte[3] = 75;
        ctx.write(arrayOfByte);
    }

    /**
     * ESC[J:清除光标右下屏所有字符，光标不动。
     */
    public static void clearRight(ChannelHandlerContext ctx) {
        byte[] arrayOfByte = new byte[3];
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        arrayOfByte[2] = 74;
        ctx.write(arrayOfByte);
    }

    /**
     * ESC[1J:清除光标左上屏所有字符，光标不动。
     */
    public static void clearLeft(ChannelHandlerContext ctx) {
        byte[] arrayOfByte = new byte[4];
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        arrayOfByte[2] = 49;
        arrayOfByte[3] = 74;
        ctx.write(arrayOfByte);
    }

    /**
     * ESC[nM:删除光标之下n行，剩下行往上移，光标不动。
     */
    public static void clearDownN(ChannelHandlerContext ctx, int num) {
        byte[] array = translateIntToDigitCodes(num);
        byte[] arrayOfByte = new byte[3 + array.length];
        int i = 0;
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        System.arraycopy(array, 0, arrayOfByte, 2,
                array.length);
        i = 2 + array.length;
        arrayOfByte[i] = 77;
        ctx.write(arrayOfByte);
    }

    /**
     * ESC[nP:删除光标右边n个字符，剩下部分左移，光标不动。
     */
    public static void clearRightN(ChannelHandlerContext ctx, int num) {
        byte[] array = translateIntToDigitCodes(num);
        byte[] arrayOfByte = new byte[3 + array.length];
        int i = 0;
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        System.arraycopy(array, 0, arrayOfByte, 2,
                array.length);
        i = 2 + array.length;
        arrayOfByte[i] = 80;
        ctx.write(arrayOfByte);
    }

    /**
     * 清空一行，光标靠左，先清空左右数据，上移一行，再写一个回车
     * @param ctx
     */
    public static void clearOneRow(ChannelHandlerContext ctx){
        HandlerUtil.delRight(ctx);
        HandlerUtil.delLeft(ctx);
        HandlerUtil.moveUpN(ctx, 1);
        byte[] arrayOfByte = new byte[1];
        arrayOfByte[0] = 13;
        ctx.write(arrayOfByte);
    }

    /**
     * 回车换行
     * @param ctx
     */
    public static void changeRow(ChannelHandlerContext ctx){
        byte[] arrayOfByte = new byte[2];
        arrayOfByte[0] = 13;
        arrayOfByte[1] = 10;
        ctx.write(arrayOfByte);
    }
    /**
     * 清空一行，光标靠左
     * @param ctx
     */
    public static void clearOneRowCursorLeft(ChannelHandlerContext ctx){
        HandlerUtil.delALL(ctx);
        HandlerUtil.moveUpN(ctx, 1);
    }

    //==================插入字符操作========================//

    /**
     * ESC[n@:在当前光标处插入n个字符。
     */
    public static void insertN(ChannelHandlerContext ctx, int num) {
        byte[] array = translateIntToDigitCodes(num);
        byte[] arrayOfByte = new byte[3 + array.length];
        int i = 0;
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        System.arraycopy(array, 0, arrayOfByte, 2,
                array.length);
        i = 2 + array.length;
        arrayOfByte[i] = 64;
        ctx.write(arrayOfByte);
    }

    /**
     * ESC[nL:在当前光标下插入n行。
     */
    public static void insertDownN(ChannelHandlerContext ctx, int num) {
        byte[] array = translateIntToDigitCodes(num);
        byte[] arrayOfByte = new byte[3 + array.length];
        int i = 0;
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        System.arraycopy(array, 0, arrayOfByte, 2,
                array.length);
        i = 2 + array.length;
        arrayOfByte[i] = 76;
        ctx.write(arrayOfByte);
    }

    //========================光标移动===========================//

    /**
     * ESC[nA:光标上移n行。
     */
    public static void moveUpN(ChannelHandlerContext ctx, int num) {
        byte[] array = translateIntToDigitCodes(num);
        byte[] arrayOfByte = new byte[3 + array.length];
        int i = 0;
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        System.arraycopy(array, 0, arrayOfByte, 2,
                array.length);
        i = 2 + array.length;
        arrayOfByte[i] = 65;
        ctx.write(arrayOfByte);
    }

    /**
     * ESC[nB:光标下移n行。
     */
    public static void moveDownN(ChannelHandlerContext ctx, int num) {
        byte[] array = translateIntToDigitCodes(num);
        byte[] arrayOfByte = new byte[3 + array.length];
        int i = 0;
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        System.arraycopy(array, 0, arrayOfByte, 2,
                array.length);
        i = 2 + array.length;
        arrayOfByte[i] = 66;
        ctx.write(arrayOfByte);
    }

    /**
     * ESC[nC:光标右移n个字符。
     */
    public static void moveRightN(ChannelHandlerContext ctx, int num) {
        byte[] array = translateIntToDigitCodes(num);
        byte[] arrayOfByte = new byte[3 + array.length];
        int i = 0;
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        System.arraycopy(array, 0, arrayOfByte, 2,
                array.length);
        i = 2 + array.length;
        arrayOfByte[i] = 67;
        ctx.write(arrayOfByte);
    }

    /**
     * ESC[nD:光标左移n个字符。
     */
    public static void moveLeftN(ChannelHandlerContext ctx, int num) {
        byte[] array = translateIntToDigitCodes(num);
        byte[] arrayOfByte = new byte[3 + array.length];
        int i = 0;
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        System.arraycopy(array, 0, arrayOfByte, 2,
                array.length);
        i = 2 + array.length;
        arrayOfByte[i] = 68;
        ctx.write(arrayOfByte);
    }

    /**
     * ESC[n;mH :光标定位到第n行m列(类似代码ESC[n;mf)。
     */
    public static void moveToNM(ChannelHandlerContext ctx, int n, int m) {
        byte[] array1 = translateIntToDigitCodes(n);
        byte[] array2 = translateIntToDigitCodes(m);
        byte[] arrayOfByte = new byte[4 + array1.length + array2.length];
        int i = 0;
        arrayOfByte[0] = 27;
        arrayOfByte[1] = 91;
        System.arraycopy(array1, 0, arrayOfByte, 2,
                array1.length);
        i = 2 + array1.length;
        arrayOfByte[i] = 59;
        i++;
        System.arraycopy(array2, 0, arrayOfByte, i,
                array2.length);
        i += array2.length;
        arrayOfByte[i] = 72;
        ctx.write(arrayOfByte);
    }

    /**
     * @param ctx
     * @param strs     要输出的汉字描述 数组元素独占一行
     * @param startRow 起始行
     * @param startCol 起始列
     * @throws Exception
     */
    public static void writer(ChannelHandlerContext ctx, String[] strs, int startRow, int startCol) {//startRow要输出的所在行
        Channel channel = ctx.channel();
        for (int r = 0; r < strs.length; r++) {
            locateCursor(channel, r + startRow, startCol);
            byte[] bytes = new byte[0];
            try {
                bytes = strs[r].getBytes(Constants.CHARSET);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            channel.write(bytes);
        }
        ctx.flush();
    }

    /**
     * 输出字符串数据到界面，需flush
     *
     * @param ctx
     * @param mess 待输出的字符串数据
     */
    public static void write(ChannelHandlerContext ctx, String mess) {
        try {
            ctx.write(mess.getBytes(Constants.CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 输出byte数据到界面，需flush
     *
     * @param ctx
     * @param bytes 待输出的byte数据
     */
    public static void write(ChannelHandlerContext ctx, byte[] bytes) {
        ctx.write(bytes);
    }

    /**
     * 输出并刷新字符串数据到界面
     *
     * @param ctx
     * @param mess 待输出字符串数据
     */
    public static void writeAndFlush(ChannelHandlerContext ctx, String mess) {
        try {
            ctx.write(mess.getBytes(Constants.CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ctx.flush();
    }

    /**
     * 输出并刷新byte[]数据到界面
     *
     * @param ctx
     * @param bytes 待输出byte数据
     */
    public static void writeAndFlush(ChannelHandlerContext ctx, byte[] bytes) {
        ctx.write(bytes);
        ctx.flush();
    }

    public static void locateCursor(Channel channel, int row, int col) {
        byte[] bytesCursor = HandlerUtil.locationCursor(row, col);
        channel.write(bytesCursor);
    }

    /**
     * 输出错误信息
     *
     * @param ctx
     * @param msg
     * @param row 显示错误信息后，光标定位的行
     * @param col 光标定位的列（光标的右下要清除）
     * @throws Exception
     */
    public static void errorMsg(ChannelHandlerContext ctx, String msg, int row, int col) {
        String[] errorMsg = {msg};
        writer(ctx, errorMsg, 1, 1);//输出到第1行第一列
        delRight(ctx);
        locateCursor(ctx.channel(), row, col);//定位光标
        removeRightDown(ctx);//删除右下方
    }

    /**
     * @param ctx
     * @param titleName 标题
     * @param row       所在行
     * @param col       所在列
     * @throws UnsupportedEncodingException
     */
    public static void printTitle(ChannelHandlerContext ctx, String titleName, int row, int col) {
        Channel channel = ctx.channel();
        clearAll(channel); //清屏
        locateCursor(channel, row, col);//光标定位
        drawString(channel, titleName);//输出标题
        drawString(channel, "\r\n");//回车换行
        channel.flush();
    }

    public static void println(ChannelHandlerContext ctx, String str) {
        Channel channel = ctx.channel();
        delRight(ctx);
        drawString(channel, str);//输出标题
        drawString(channel, "\r\n");//回车换行
        channel.flush();
    }

    public static void print(ChannelHandlerContext ctx, String str) {
        Channel channel = ctx.channel();
        delRight(ctx);
        drawString(channel, str);//输出标题
        channel.flush();
    }

    public static void drawString(Channel channel, String str) {
        byte[] bytes = new byte[0];
        try {
            bytes = str.getBytes(Constants.CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        channel.write(bytes);
    }

    public static void printTableHeader(ChannelHandlerContext ctx, String[] tableHeader, int[] colWidth) {
        Channel channel = ctx.channel();
        drawString(channel, Constants.SPLIT);//分割线
        drawString(channel, "\r\n");//换行
        for (int i = 0; i < tableHeader.length; i++) {
            String str = tableHeader[i];
            drawString(channel, str);
            int wordLenght = RFUtil.getWordCount(str);//字段对应的长度
            moveCursor(ctx, colWidth[i] - wordLenght);
            drawString(channel, "|");
        }
        drawString(channel, "\r\n");
    }

    public static void moveCursor(ChannelHandlerContext ctx, int offset) {
        moveRightN(ctx, offset);//offset光标右移动字符
    }

    /**
     * @param src 源字符串
     * @return 字符串，将src的第一个字母转换为大写，src为空时返回null
     */
    public static String change(String src) {
        if (src != null) {
            StringBuffer sb = new StringBuffer(src);
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
            return sb.toString();
        } else {
            return null;
        }
    }
}
