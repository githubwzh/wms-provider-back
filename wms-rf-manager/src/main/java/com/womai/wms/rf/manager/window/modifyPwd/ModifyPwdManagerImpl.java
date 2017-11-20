package com.womai.wms.rf.manager.window.modifyPwd;

import com.womai.person.api.domain.User;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.DefaultKey;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.MD5Util;
import com.womai.wms.rf.common.util.WMSDebugManager;
import com.womai.wms.rf.domain.auth.ModifyPassword;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.manager.util.UserCache;
import com.womai.wms.rf.remote.user.UserRemoteService;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author wangzhanhua
 * @version 1.0
 * @since 16-5-3 下午3:54
 */
@Scope("prototype")
@Component("modifyPwdShellManager")
public class ModifyPwdManagerImpl extends ReceiveManager {
    @Autowired
    private UserRemoteService userRemoteService;
    public final static Long SLEEP_TIME = 1000L;//页面跳转前，提示信息停留时间

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 登录界面
        String[] pageHeader = {Constants.BREAK_LINE, Constants.MODIFYPWD_TITLE, Constants.SPLIT, ""};
        super.initBaseMap(ModifyPassword.class, pageHeader, ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        //接收页面数据
        Map<String, Object> accepterMap = getDataMap();
        Integer receiveResult = receiveData(ctx, object, accepterMap);
        if (RECEIVER_TYPE_FINISHED == receiveResult) {
            processResetPassword(ctx, accepterMap);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


    /**
     * 处理修改密码
     *
     * @param ctx
     * @param accepterMap
     * @return
     * @throws Exception
     */
    private void processResetPassword(ChannelHandlerContext ctx, Map<String, Object> accepterMap) throws Exception {
        //获得缓存中的用户名（用户名唯一，且不可修改）
        User user = UserCache.getUser(getCurrentUserId(ctx));
        //验证旧密码是否正确
        User userDB = userRemoteService.findUser(user.getUserName());
        ModifyPassword modifyPassword = (ModifyPassword) accepterMap.get(DefaultKey.objectClass.keyName);
        String passOld = modifyPassword.getOldPwd();
        String passDB = userDB.getPassword();
        if (passDB.equals(MD5Util.encodeString(passOld))) {//旧密码两次MD5加密
            //旧密码比对正确，比较新密码两次输入是否一致
            String newPassFir = modifyPassword.getNewPwdFir();
            String newPassSec = modifyPassword.getNewPwdSec();
            if (newPassFir.length() < Constants.PASS_LENGTH) {//密码长度小于6
                HandlerUtil.errorBeep(ctx);//系统错误，响铃
                printErrorMsg(ctx, Constants.MODIFY_PWD_DATA_ERROR_1);
            } else if (!newPassFir.equals(newPassSec)) {
                HandlerUtil.errorBeep(ctx);//系统错误，响铃
                printErrorMsg(ctx, Constants.MODIFY_PWD_DATA_ERROR_0);
            } else {
                //处理修改密码
                int result = userRemoteService.resetPassword(userDB.getId(), newPassFir);
                if (result == 0) {
                    HandlerUtil.errorBeep(ctx);//系统错误，响铃
                    throw new RuntimeException("修改密码错误，用户id：" + userDB.getId());
                }
                printResultMsg(ctx, Constants.MODIFY_PWD_SECCESS);
                toMenuWindow(ctx);
            }
        } else {
            HandlerUtil.errorBeep(ctx);//系统错误，响铃
            //旧密码输入错误
            printErrorMsg(ctx, Constants.MODIFY_PWD_OLDPWD_ERROR);
        }
    }

    /**
     * 系统错误，或成功修改密码后，初始化数据
     *
     * @param ctx
     * @param msg 提示语（成功或系统错误）
     * @throws Exception
     */
    private void printResultMsg(ChannelHandlerContext ctx, String msg) throws Exception {
        HandlerUtil.clearAll(ctx.channel());//清屏
        HandlerUtil.print(ctx, msg);
        Thread.sleep(SLEEP_TIME);
    }

    /**
     * 错误信息提示
     *
     * @param ctx
     * @param msg 错误信息
     */
    private void printErrorMsg(ChannelHandlerContext ctx, String msg) throws Exception {//输出信息，0不跳转页面，1跳转到登陆页面
        printResultMsg(ctx, msg);
        channelActive(ctx);
    }

    /**
     * 回写到屏幕;
     *
     * @param ctx
     * @param msg 回写的内容
     */
    @Override
    protected void printBackToScreen(ChannelHandlerContext ctx, String msg, String key) {
        //密码写到屏幕为“.”;
        HandlerUtil.printPoint(ctx);
    }
}
