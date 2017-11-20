package com.womai.wms.rf.remote.user;


import com.womai.person.api.domain.User;

/**
 * 用户Service接口
 * User: zhangwei
 * Date: 2016-04-27
 * To change this template use File | Settings | File Templates.
 */
public interface UserRemoteService {

    /**
     * 用户登录查询
     * @param loginName 用户名
     * @return 返回查询到的用户
     */
    User findUser(String loginName);

    /**
     * 修改密码
     * @param userId
     * @param newPassword
     * @return
     */
    int resetPassword(Long userId,String newPassword);
}
