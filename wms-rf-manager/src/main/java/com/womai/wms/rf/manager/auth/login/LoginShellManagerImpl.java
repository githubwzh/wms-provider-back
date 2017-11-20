package com.womai.wms.rf.manager.auth.login;

import com.womai.person.api.domain.User;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.DefaultKey;
import com.womai.wms.rf.common.constants.ErrorConstants;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.MD5Util;
import com.womai.wms.rf.common.util.ManagerLog;
import com.womai.wms.rf.domain.auth.Login;
import com.womai.wms.rf.manager.util.*;
import com.womai.wms.rf.manager.window.UserAndSiteParamManager;
import com.womai.wms.rf.remote.user.UserRemoteService;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Map;

/**
 * 用户登录处理
 * User: zhangwei
 * Date: 2016-04-26
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component(Constants.LOGIN_SHELL_MANAGER)
public class LoginShellManagerImpl extends ReceiveManager {

    @Autowired
    private UserRemoteService userRemoteService;
    @Autowired
    private UserMenuAuthCache userMenuAuthCache;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        removeParamHandler(ctx);//删除参数Handler
        initWinAndMap(ctx, "");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        Map<String, Object> accepterMap = getDataMap();
        Integer receiveResult = receiveData(ctx, object, accepterMap);
        Login login = (Login) accepterMap.get(DefaultKey.objectClass.keyName);
        if (receiveResult == RECEIVER_TYPE_FINISHED) {
            String reg = "^[0-9A-Za-z]{1,}$";
            String username = login.getUserName();
            String password = login.getPassword();
            User user = userRemoteService.findUser(username);
            if (!username.matches(reg)) {
                initWinAndMap(ctx, Constants.USERNAME_NOTMATCH);
            } else if (user == null) {
                initWinAndMap(ctx, Constants.USERNAME_NOTEXSIT);
            } else {
                if (MD5Util.encodeString(password).equals(user.getPassword())) {
                    //用户成功登陆，将用户信息放入缓存
                    if (StringUtils.isBlank(user.getSite())) {
                        initWinAndMap(ctx, Constants.USER_NOSITE);
                    } else {
                        Long userId = user.getId();
                        String userSite = "";//需要充用户站点列表中取得第一个有权限的站点
                        String[] siteArr = user.getSite().split(",");
                        for (int i = 0; i < siteArr.length; i++) {
                            String selectedSite = siteArr[i].trim();//站点列表是有空格的
                            boolean existAuthority = userMenuAuthCache.isExistAuthorityBySite(userId, selectedSite);// 根据菜单等级、menuid等参数刷新菜单内容
                            if (existAuthority) {
                                userSite = selectedSite;
                                break;
                            } else {
                                //如果没有权限且是用户中的最后一个站点，则提示用户无权限
                                if (i == (siteArr.length - 1)) {
                                    initWinAndMap(ctx, "用户所有站点无权限");
                                    return;
                                }
                            }
                        }

                        //用户id存入redis
                        UserCache.initUserCache(user);
                        //初始化用户参数Handler
                        removeParamHandler(ctx);//删除参数Handler
                        //增加专门的传参用Manager
                        UserAndSiteParamManager userAndSiteParamManager = new UserAndSiteParamManager();
                        userAndSiteParamManager.setUerId(userId);
                        userAndSiteParamManager.setCurrentSite(userSite);
                        ctx.pipeline().addFirst(Constants.USER_SITE_MANAGER, userAndSiteParamManager);

                        try {// 如果在登录时发生异常就不用进主菜单了，直接打印错误重新登录
                            userMenuAuthCache.refreshUserMenuAuthCache(userId, userSite); // 刷新用户菜单权限缓存
                        } catch (JedisConnectionException e) {
                            ManagerLog.errorLog("用户：".concat(login.toString()).concat("登录时发生Redis连接异常：").concat(e.getMessage()), e);
                            initWinAndMap(ctx, "缓存数据" + ErrorConstants.SYS_ERROR);
                            return;
                        } catch (RFMenuAuthException e) {
                            ManagerLog.errorLog("用户：".concat(login.toString()).concat("登录时发生权限系统异常：").concat(e.getMessage()), e);
                            initWinAndMap(ctx, e.getMessage());
                            return;
                        } catch (RFFatalException e) {
                            ManagerLog.errorLog("用户：".concat(login.toString()).concat("登录时发生权限严重异常：").concat(e.getMessage()), e);
                            e.printStackTrace();
                            initWinAndMap(ctx, e.getMessage());
                            return;
                        }
                        toMenuWindow(ctx); // 去主菜单
                    }
                } else {
                    initWinAndMap(ctx, Constants.PASSWORD_ERROR);
                }
            }
        }
    }


    /**
     * 删除参数Handler，避免重复添加
     *
     * @param ctx ctx上下文
     */
    private void removeParamHandler(ChannelHandlerContext ctx) throws Exception {
        UserAndSiteParamManager userAndSiteParamManager = (UserAndSiteParamManager) ctx.pipeline().get(Constants.USER_SITE_MANAGER);
        if (userAndSiteParamManager != null) {
            ctx.pipeline().remove(Constants.USER_SITE_MANAGER);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 初始化界面,清屏
     *
     * @param ctx
     * @param mess 需要显示的提示信息
     */
    public void initWinAndMap(ChannelHandlerContext ctx, String mess) throws Exception {
        if(StringUtils.isNotBlank(mess)){
            HandlerUtil.errorBeep(ctx);//系统错误，响铃
        }
        String[] pageHeader = {mess, Constants.LOGIN, Constants.SPLIT, ""};
        super.initBaseMap(Login.class, pageHeader, ctx);
    }
}
