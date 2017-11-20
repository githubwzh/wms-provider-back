package com.womai.wms.rf.manager.window.menu;

/**
 * ClassDescribe: RF菜单元素实体类
 * Author :Xiafei Qi
 * Date: 2016-08-15
 * Since
 * To change this template use File | Settings | File Templates.
 */
public class MenuElement {
    private String id;// 菜单id
    private String name;// 展示给用户的菜单名字
    private String pid;// 父级菜单id
    private String url;// 菜单目标springBeanId
    private String level;// 菜单等级

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
