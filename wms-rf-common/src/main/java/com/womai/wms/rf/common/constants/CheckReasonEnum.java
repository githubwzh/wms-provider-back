package com.womai.wms.rf.common.constants;

import java.util.ArrayList;
import java.util.List;

/**
 * 常量
 * User:wangzhanhua
 * Date: 2016-05-18
 * To change this template use File | Settings | File Templates.
 */
public enum CheckReasonEnum {

    normal("正品",0),
    damaged("残品",1),
    frozen("冻结",2);



    public String name;//键的名称
    public Integer value;//默认名称

    CheckReasonEnum(String keyName, Integer defaultVal) {
        this.name = keyName;
        this.value = defaultVal;
    }

    public static Integer getValueByName(String name){
        for (CheckReasonEnum t : CheckReasonEnum.values()) {
            if(t.name.equals(name) ){
                return t.value;
            }
        }
        return null;
    }

    public static String getNameByValue(Integer myVal){
        for (CheckReasonEnum t : CheckReasonEnum.values()) {
            if(t.value.equals(myVal) ){
                return t.name;
            }
        }
        return null;
    }


    public static List<String> getNameList(){
        List<String> nameList = new ArrayList<String>();
        for (CheckReasonEnum t : CheckReasonEnum.values()) {
            nameList.add(t.name);
        }
        return nameList;
    }

}
