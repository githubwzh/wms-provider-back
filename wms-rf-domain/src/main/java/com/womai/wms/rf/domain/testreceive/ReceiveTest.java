package com.womai.wms.rf.domain.testreceive;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * User: zhangwei
 * Date: 2016-06-20
 * To change this template use File | Settings | File Templates.
 */
public class ReceiveTest {

    @Receiver(colTip = "姓名：",topTip = true)
    private String userName;
    @Receiver(colTip = "我的密码：",cursorDown = true,topTip = true)
    private String password;
    @Receiver(colTip = "性别：",cursorDown = true)
    private String sex;
    @Receiver(colTip = "手机：",topTip = true)
    private String mobile;
    @Receiver(colTip = "地址：")
    private String address;
    @Receiver(colTip = "邮编：",cursorDown = true)
    private String postCode;

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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    @Override
    public String toString() {
        return "ReceiveTest{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", sex='" + sex + '\'' +
                ", mobile='" + mobile + '\'' +
                ", address='" + address + '\'' +
                ", postCode='" + postCode + '\'' +
                '}';
    }
}
