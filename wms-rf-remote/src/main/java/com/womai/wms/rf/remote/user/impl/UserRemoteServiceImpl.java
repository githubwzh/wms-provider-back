package com.womai.wms.rf.remote.user.impl;

import com.womai.person.api.domain.User;
import com.womai.person.api.service.admin.AdminUserService;
import com.womai.person.api.service.admin.AdminWMPersonService;
import com.womai.wms.rf.remote.user.UserRemoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户Service接口实现类
 * User: zhangwei
 * Date: 2016-04-27
 * To change this template use File | Settings | File Templates.
 */
@Service("userRemoteService")
public class UserRemoteServiceImpl implements UserRemoteService {
    @Autowired
    private AdminWMPersonService adminWMPersonService;
    @Autowired
    private AdminUserService adminUserService;
    @Override
    public User findUser(String loginName) {
        return  adminWMPersonService.getUserByUserName(loginName);//根据用户名查询用户信息
    }

    /**
     * 修改密码
     *
     * @param userId
     * @param newPassword
     * @return
     */
    @Override
    public int resetPassword(Long userId, String newPassword) {
        return adminUserService.resetPassword(userId,newPassword);
    }
}
