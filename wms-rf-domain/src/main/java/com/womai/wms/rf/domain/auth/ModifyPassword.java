package com.womai.wms.rf.domain.auth;

import com.womai.wms.rf.common.annotation.Receiver;
import com.womai.wms.rf.domain.BaseDomain;

/**
 * User:wangzhanhua
 * Date: 2016-05-25
 * To change this template use File | Settings | File Templates.
 */
public class ModifyPassword extends BaseDomain {

    @Receiver(colTip = "请输入旧密码：")
    private String oldPwd;
    @Receiver(colTip = "请输入新密码：")
    private String newPwdFir;
    @Receiver(colTip = "请确认新密码：")
    private String newPwdSec;

    public String getOldPwd() {
        return oldPwd;
    }

    public void setOldPwd(String oldPwd) {
        this.oldPwd = oldPwd;
    }

    public String getNewPwdFir() {
        return newPwdFir;
    }

    public void setNewPwdFir(String newPwdFir) {
        this.newPwdFir = newPwdFir;
    }

    public String getNewPwdSec() {
        return newPwdSec;
    }

    public void setNewPwdSec(String newPwdSec) {
        this.newPwdSec = newPwdSec;
    }

    @Override
    public String toString() {
        return "ModifyPassword{" +
                "oldPwd='" + oldPwd + '\'' +
                ", newPwdFir='" + newPwdFir + '\'' +
                ", newPwdSec='" + newPwdSec + '\'' +
                '}';
    }
}
