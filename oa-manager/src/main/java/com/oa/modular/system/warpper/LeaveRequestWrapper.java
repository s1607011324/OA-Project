package com.oa.modular.system.warpper;

import cn.stylefeng.roses.core.base.warpper.BaseControllerWrapper;
import cn.stylefeng.roses.kernel.model.page.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.core.common.constant.factory.ConstantFactory;

import java.util.List;
import java.util.Map;

public class LeaveRequestWrapper extends BaseControllerWrapper {

    public LeaveRequestWrapper(Map<String, Object> single) {
        super(single);
    }

    public LeaveRequestWrapper(List<Map<String, Object>> multi) {
        super(multi);
    }

    public LeaveRequestWrapper(Page<Map<String, Object>> page) {
        super(page);
    }

    public LeaveRequestWrapper(PageResult<Map<String, Object>> pageResult) {
        super(pageResult);
    }

    @Override
    protected void wrapTheMap(Map<String, Object> map) {
        map.put("proposer", ConstantFactory.me().getUserNameById((Long) map.get("uid")));
        if (map.get("status").equals("0")){
            map.put("statusName","未申报");
        }
        if (map.get("status").equals("1")){
            map.put("statusName","已启动");
        }
        if (map.get("status").equals("2")){
            map.put("statusName","已结束");
        }
    }
}
