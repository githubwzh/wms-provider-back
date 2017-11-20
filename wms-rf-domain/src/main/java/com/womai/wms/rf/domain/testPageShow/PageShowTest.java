package com.womai.wms.rf.domain.testPageShow;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * Created by wangzhanhua on 2016/7/21.
 */
public class PageShowTest {
    @Receiver(colTip = "")
    private String pageNum;

    public String getPageNum() {
        return pageNum;
    }

    public void setPageNum(String pageNum) {
        this.pageNum = pageNum;
    }

    @Override
    public String toString() {
        return "PageShowTest{" +
                "pageNum='" + pageNum + '\'' +
                '}';
    }
}
