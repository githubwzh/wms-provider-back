package com.womai.wms.rf.manager.window.menu;

import com.womai.common.tool.util.StringUtils;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.KeyEnum;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.ManagerLog;
import com.womai.wms.rf.common.util.WMSDebugManager;
import com.womai.wms.rf.manager.util.RFFatalException;
import com.womai.wms.rf.manager.util.RFMenuAuthException;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.manager.util.UserMenuAuthCache;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 菜单操作类，进行菜单跳转
 * User: zhangwei
 * Date: 2016-04-26
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component(Constants.MENU_SHELL_MANAGER)
public class MenuShellManagerImpl extends ReceiveManager {
    private List<MenuElement> menus;
    private ThreadLocal<String> tmstr = new ThreadLocal<String>();
    private MenuElement lastMenu;
    @Autowired
    UserMenuAuthCache userMenuAuthCache;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        showMenu(Constants.MENU_LEVEL_ONE_YES, Constants.MENU_ID_NOUSE, Constants.MENU_ERROR_MESS_NULL, ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        if(super.anyKeyToLogIn){//如果父类中的标识为true则跳转回登录，此标识在BaseShellManager捕获异常的方法exceptionCaught中设置
            forward(Constants.LOGIN_SHELL_MANAGER, ctx);
            return;
        }

        String msg = object.toString();
        String str = (null == tmstr.get() ? "" : tmstr.get());
        if (KeyEnum.CR_13.code == msg.getBytes()[0]) {
            String regx = "^[1-9]\\d{0,7}|[x]{2}|[X]{2}|[q]{2}|[Q]{2}$";
            if (str.matches(regx)) {
                if (forwardMainView(ctx, str)) {//返回判断
                    return;
                }
                int p = Integer.parseInt(str); // 选择的菜单序号
                if (p > menus.size()) { // 如果选择的序号超过菜单大小
                    if(lastMenu!=null){
                        showMenu(Constants.MENU_LEVEL_ONE_NO, lastMenu.getId(),  Constants.MENU_ERROR_MAX, ctx); // 展示下级菜单
                    }else{
                        showMenu(Constants.MENU_LEVEL_ONE_YES,  Constants.MENU_ID_NOUSE, Constants.MENU_ERROR_MAX, ctx);
                    }
                } else {
                    MenuElement menu = menus.get(p - 1);// 用户选择的菜单对象
                    if (StringUtils.isBlank(menu.getUrl())) {
                        lastMenu = menu;
                        showMenu(Constants.MENU_LEVEL_ONE_NO, menu.getId(), null, ctx); // 展示下级菜单
                    } else {
                        String view = menu.getUrl();
                        forward(view, ctx);// 跳转到下一个handler
                    }
                }
            } else {
                if(lastMenu!=null){
                    showMenu(Constants.MENU_LEVEL_ONE_NO, lastMenu.getId(),  Constants.MENU_NULL_ERROR, ctx); // 展示下级菜单
                }else{
                    showMenu(Constants.MENU_LEVEL_ONE_YES, Constants.MENU_ID_NOUSE, Constants.MENU_NULL_ERROR, ctx);
                }
            }
            tmstr.remove();
        } else {
            tmstr.set(inputStr(ctx, msg, str));
        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 将菜单打印到终端
     *
     * @param isLevelOne 是否是一级菜单
     * @param menuid     选择的菜单id，若是显示一级菜单，此参数没有作用
     * @param errorMess  菜单选择时错误提示
     * @param ctx        netty上下文
     */
    private void showMenu(boolean isLevelOne, String menuid, String errorMess, ChannelHandlerContext ctx) throws Exception {
        clearScreenAndPrintMenuTitle(ctx); // 清屏并打印标题
        // 如果不是一级菜单，检查权限是否发生了变化，若权限发生变化，刷新缓存，并且重新展示一级菜单。如果是一级菜单，载入之前都检查过，就跳过检查
        // 之所以不在权限发生变化时直接调用channelActive是因为channelActive还要检查一遍权限是否变化再调用showMenu这个方法，而这里没有这个必要
        if (!isLevelOne && userMenuAuthCache.isChangeUserMenu(getCurrentUserId(ctx), getCurrentSite(ctx))) {
            userMenuAuthCache.refreshUserMenuAuthCache(getCurrentUserId(ctx), getCurrentSite(ctx)); // 刷新缓存
            isLevelOne = true;// 让下面菜单的展示变成一级菜单
        }
        menus = userMenuAuthCache.getMenus(getCurrentUserId(ctx).toString(), getCurrentSite(ctx), isLevelOne, menuid);// 根据菜单等级、menuid等参数刷新菜单内容
        int i = 1;// 菜单序号，从1开始
        for (MenuElement menu : menus) { // 输出菜单选项
            String menuName = menu.getName();//菜单名称
            //通过枚举匹配菜单中文名称
            String menuDesc = MenuNameEnum.getMenuDesc(menuName);
            if(StringUtils.isNotBlank(menuDesc)){
                String mm = String.valueOf(i).concat(")").concat(menuDesc).concat(Constants.BREAK_LINE);// 菜单名
                HandlerUtil.write(ctx, mm); // 将菜单选项输出到屏幕
                i++;
            }else {
                ManagerLog.errorLog(menuName+"未查询到菜单名称");
            }
        }
        // 如果错误信息不为空，打印错误信息和换行符
        if (StringUtils.isNotBlank(errorMess)) {
            HandlerUtil.write(ctx, errorMess.concat(Constants.BREAK_LINE));
        }

        HandlerUtil.writeAndFlush(ctx, Constants.MENU_TIP);// 如果上面catch到异常了，这个页面就是菜单标题+请选择菜单：提示
    }

    /**
     * 清屏并且打印菜单标题
     */
    private void clearScreenAndPrintMenuTitle(ChannelHandlerContext ctx) {
        HandlerUtil.clearAll(ctx.channel());//清屏
        String[] outStr = {Constants.BREAK_LINE, Constants.MENU_TITLE, Constants.SPLIT, ""};
        HandlerUtil.writer(ctx, outStr, 1, 1);  // 将光标定位到左上角并输出outStr到终端

    }
}
