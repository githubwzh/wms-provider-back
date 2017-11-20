package com.womai.wms.rf.manager.auth.login;

import com.womai.person.api.domain.User;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.ManagerLog;
import com.womai.wms.rf.common.util.SpringContextHolder;
import com.womai.wms.rf.common.util.WMSDebugManager;
import com.womai.wms.rf.manager.util.RFFatalException;
import com.womai.wms.rf.manager.util.RFMenuAuthException;
import com.womai.wms.rf.manager.util.UserCache;
import com.womai.wms.rf.manager.window.UserAndSiteParamManager;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * 基础manager类
 * User: zhangwei
 * Date: 2016-04-26
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component("baseShellManager")
public class BaseShellManagerImpl extends SimpleChannelInboundHandler<Object> implements ShellManager {
    private static Logger logger = LoggerFactory.getLogger("manager-error");

    public boolean anyKeyToLogIn;//设置是否任意键回到登录界面
    private String nextShell;

    /**
     * 接收用户输入
     *
     * @param ctx handler对象
     * @param msg 每次接收到的字节数据
     * @throws Exception 抛出异常
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

    }

    /**
     * 因为此方法为protected类型，在aop拦截的时候比较麻烦，所以不要使用这个方法
     *
     * @param ctx handler对象
     * @param msg 每次接收到的字节数据
     * @throws Exception 抛出异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

    }

    /**
     * 整体异常处理，发生异常时，回到主菜单
     *
     * @param channelHandlerContext handler上下文
     * @param cause                 抛出异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
        cause.printStackTrace();
        if (cause.getClass().isInstance(new IOException())) {
            WMSDebugManager.debugLog("强制关闭客户端抛出错误：" + cause.getMessage(), cause);
            ManagerLog.errorLog(getCurrentSite(channelHandlerContext) + "强制关闭客户端抛出错误：" + cause.getMessage());
        } else if (cause.getClass().isInstance(new JedisConnectionException(""))) {
            WMSDebugManager.errorLog("Redis连接异常：" + cause.getMessage());
            ManagerLog.errorLog("Redis连接异常：" + cause.getMessage(), cause);
            HandlerUtil.clearAll(channelHandlerContext.channel());
            forward(Constants.LOGIN_SHELL_MANAGER, channelHandlerContext);
        } else if (cause.getClass().isInstance(new RFMenuAuthException("")) || cause.getClass().isInstance(new RFFatalException(""))) {
            WMSDebugManager.errorLog("权限异常：" + cause.getMessage());
            ManagerLog.errorLog("权限异常：" + cause.getMessage(), cause);
            HandlerUtil.clearAll(channelHandlerContext.channel());//清屏
            String[] outStr = {Constants.BREAK_LINE, Constants.MENU_TITLE, Constants.SPLIT, ""};// 菜单标题
            HandlerUtil.writer(channelHandlerContext, outStr, 1, 1);  // 将光标定位到左上角并输出菜单标题到终端
            HandlerUtil.println(channelHandlerContext, cause.getMessage()); // 打印错误信息
            HandlerUtil.println(channelHandlerContext,"任意键重新登录"); // 打印错误信息
            HandlerUtil.writeAndFlush(channelHandlerContext, Constants.MENU_TIP); // 选择菜单提示
            anyKeyToLogIn = true;
        } else { // 程序抛其他异常了，回主菜单
            WMSDebugManager.errorLog("抛出错误：" + cause);
            ManagerLog.errorLog("抛出错误：", cause);
            forward(Constants.MENU_SHELL_MANAGER, channelHandlerContext);
        }
//        cause.printStackTrace();
    }

    /**
     * 构造凭证数据，用于传递用户信息、切换数据库
     *
     * @return 返回构造的对象
     */
    public CredentialsVO getCredentialsVO(ChannelHandlerContext ctx) {
        User user = UserCache.getUser(getCurrentUserId(ctx));
        WMSDebugManager.debugLog("构建VO：" + user);
        if (user == null) {
            throw new RuntimeException("用户缓存为空，无法构建CredentialsVo");
        }
        CredentialsVO credentialsVO = new CredentialsVO();
        credentialsVO.setUserId(user.getId());
        credentialsVO.setLoginname(user.getUserName());
        credentialsVO.setUsername(user.getRealName());
        credentialsVO.setWorktype(WmsConstants.INSTOCK_WORKTYPE_RF);
        credentialsVO.setCurrentSite(getCurrentSite(ctx));
        return credentialsVO;
    }

    /**
     * 获取当前的用户ID
     *
     * @return 返回用户ID
     */
    public Long getCurrentUserId(ChannelHandlerContext ctx) {
        UserAndSiteParamManager us = (UserAndSiteParamManager) ctx.pipeline().first();
        return us.getUerId();
    }

    /**
     * 获取当前站点
     *
     * @return 返回当前站点
     */
    public String getCurrentSite(ChannelHandlerContext ctx) {
        UserAndSiteParamManager us = (UserAndSiteParamManager) ctx.pipeline().first();
        return us.getCurrentSite();
    }

    public void setNextShell(String shell) {
        this.nextShell = shell;
    }

    public String getNextShell() {
        return this.nextShell;
    }

    public ShellManager invokeNextShell() {
        ShellManager shell = (ShellManager) SpringContextHolder.getBean(nextShell);
        return shell;
    }

    /**
     * 通过配置的shellManager 中的compoent的name 属性进行跳转
     *
     * @param str                   compent中的name属性
     * @param channelHandlerContext handler上下文
     * @throws Exception 爬出异常
     */
    public void forward(String str, ChannelHandlerContext channelHandlerContext) {
        ChannelHandler handler = SpringContextHolder.getBean(str);// 这一步可能抛出异常
        ChannelPipeline p = channelHandlerContext.pipeline();
        p.removeLast();
        p.addLast(handler);
        p.fireChannelActive();
    }

    /**
     * 直接跳转到菜单界面
     *
     * @param channelHandlerContext
     */
    public void toMenuWindow(ChannelHandlerContext channelHandlerContext) {
        setNextShell(Constants.MENU_SHELL_MANAGER);
        ChannelPipeline p = channelHandlerContext.pipeline();
        p.removeLast();
        p.addLast(nextShell, (ChannelHandler) invokeNextShell());
        p.fireChannelActive();
    }

    /**
     * 自定义快捷键
     *
     * @param channelHandlerContext handler上下文
     * @param msg                   快捷键
     * @return 返回是否继续调用处接下来的业务逻辑
     * @throws Exception 抛出异常
     */
    public boolean forwardMainView(ChannelHandlerContext channelHandlerContext, String msg) {
        boolean flag = false;
        if ("XX".equalsIgnoreCase(msg)) {
            //如果在登录界面输入XX则检测是否存在参数Handler，不存在返回false，继续执行登录逻辑
            UserAndSiteParamManager userAndSiteParamManager = (UserAndSiteParamManager) channelHandlerContext.pipeline().get(Constants.USER_SITE_MANAGER);
            if (userAndSiteParamManager == null) {
                return false;
            }

            //输入XX返回主菜单
            HandlerUtil.clearAll(channelHandlerContext.channel());
            forward(Constants.MENU_SHELL_MANAGER, channelHandlerContext);
            flag = true;
        } else if ("QQ".equalsIgnoreCase(msg)) {
            //输入QQ返回登录界面
            HandlerUtil.clearAll(channelHandlerContext.channel());
            forward(Constants.LOGIN_SHELL_MANAGER, channelHandlerContext);
            flag = true;
        }
        return flag;
    }

    //输入操作
    public String inputStr(ChannelHandlerContext ctx, String msg, String str, String nstr) throws UnsupportedEncodingException {
        String str1 = "";
        if (8 == msg.getBytes()[0]) {
            if (str.length() > 0) {
                str1 = str.substring(0, str.length() - 1);
                HandlerUtil.moveLeftN(ctx, 1);
                HandlerUtil.clearRightN(ctx, 1);
            }
        } else if (msg.getBytes()[0] >= 32 && msg.getBytes()[0] <= 126) {//除特殊按键
            msg = msg.replaceAll("\r\n", "");
            str1 = str + msg;
            if (!StringUtils.isEmpty(nstr)) {//密码
                HandlerUtil.printPoint(ctx);
            } else {
                HandlerUtil.print(ctx, msg);
            }
        } else {
            str1 = str;
        }
        return str1;

    }

//    //输入操作
//    public String inputStr(ChannelHandlerContext ctx, String msg, String str, String nstr) {
//        String str1 = "";
//        if (8 == msg.getBytes()[0]) {
//            if (str.length() > 0) {//ESC[1D若删除操作，光标左移1,ESC[K清除光标右侧
//                str1 = str.substring(0, str.length() - 1);
//                HandlerUtil.removeInput(ctx);
//            } else {//若删除所有输入，再执行删除，光标右移一个字节
//                str1 = "";
//                HandlerUtil.rightMove(ctx);
//            }
//        } else if (msg.getBytes()[0] >= 32 && msg.getBytes()[0] <= 126) {//除特殊按键
//            msg = msg.replaceAll("\r\n", "");
//            str1 = str + msg;
//            if (!StringUtils.isEmpty(nstr)) {//密码
//                HandlerUtil.passwordPrint(ctx);
//            }
//        } else {
//            str1 = str;
//        }
//        return str1;
//    }

    //输入操作
    public String inputStr(ChannelHandlerContext ctx, String msg, String str) {
        String str1 = "";
        if (8 == msg.getBytes()[0]) {
            if (str.length() > 0) {
                HandlerUtil.moveLeftN(ctx, 1);
                HandlerUtil.clearRightN(ctx, 1);
                str1 = str.substring(0, str.length() - 1);
            }
        } else if (msg.getBytes()[0] >= 32 && msg.getBytes()[0] <= 126) {
            msg = msg.replaceAll("\r\n", "");
            str1 = str + msg;
            HandlerUtil.print(ctx, msg);
        } else {
            str1 = str;
        }
        return str1;
    }

//    public static void setApplicationContext(ApplicationContext applicationContext) {
//        BaseShellManagerImpl.context = applicationContext;
//    }

    @Override
    public void run() throws Exception {

    }
}
