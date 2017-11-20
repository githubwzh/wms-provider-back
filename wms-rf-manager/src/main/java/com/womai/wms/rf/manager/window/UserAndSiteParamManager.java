package com.womai.wms.rf.manager.window;

import com.womai.wms.rf.common.constants.Constants;
import io.netty.channel.ChannelHandlerAdapter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 储存用户id及当前站点的manager
 * User:zhangwei
 * Date: 2016-05-25
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component(Constants.USER_SITE_MANAGER)
public class UserAndSiteParamManager extends ChannelHandlerAdapter {
    private Long uerId;//用户id
    private String currentSite;//当前站点

    public Long getUerId() {
        return uerId;
    }

    public void setUerId(Long uerId) {
        this.uerId = uerId;
    }

    public String getCurrentSite() {
        return currentSite;
    }

    public void setCurrentSite(String currentSite) {
        this.currentSite = currentSite;
    }

}
