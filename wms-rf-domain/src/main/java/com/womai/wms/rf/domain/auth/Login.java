package com.womai.wms.rf.domain.auth;

import com.womai.wms.rf.common.annotation.Receiver;
import com.womai.wms.rf.domain.BaseDomain;

/**
 * User:zhangwei
 * Date: 2016-05-26
 * To change this template use File | Settings | File Templates.
 */
public class Login extends BaseDomain{
    @Receiver(colTip = "用户名：")
    private String userName;
    @Receiver(colTip = "密码：",encrypt = ".")
    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Login{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
