package com.oa.core.common.constant.dictmap;

import com.oa.core.common.constant.dictmap.base.AbstractDictMap;

public class LeaveRequestMap extends AbstractDictMap {
    @Override
    public void init() {
        put("","");
    }

    @Override
    protected void initBeWrapped() {
        putFieldWrapperMethodName("", "");
    }
}
