
package com.womai.wms.rf.manager.util;

import com.womai.person.api.domain.User;
import com.womai.wms.rf.common.util.JedisUtils;
import com.womai.wms.rf.common.util.RFUtil;

import java.util.List;

/**
 * 用户的redis缓存
 * User: zhangwei
 * Date: 2016-05-10
 * To change this template use File | Settings | File Templates.
 */
public class UserCache {
    public static final Integer INVALID_TIME = 3600;//缓存失效时间，单位为秒，暂定一个小时
    public static final String USER_KEY = "user:";//用户对象缓存key

    /**
     * 初始化用户对象缓存及用户下的所有站点缓存
     *
     * @param user
     */
    public static void initUserCache(User user) {
        JedisUtils.setObject(USER_KEY + user.getId(), user, INVALID_TIME);
    }

    /**
     * 重新设置用户缓存失效时间
     *
     * @param userId 用户ID
     */
    public static void extendUserCacheTime(Long userId) {
        JedisUtils.expire(USER_KEY + userId, INVALID_TIME);
    }

    /**
     * 按照ID获取当前缓存用户
     *
     * @param userId 用户ID
     * @return
     */
    public static User getUser(Long userId) {
        return (User) JedisUtils.getObject(USER_KEY + userId);
    }


}
