package com.oa.modular.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.modular.system.entity.WXInfo;
import com.oa.modular.system.mapper.WXInfoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class WxInfoService extends ServiceImpl<WXInfoMapper, WXInfo> {
    @Resource
    private WXInfoMapper wxInfoMapper;

    public void add(WXInfo wxInfo){
        wxInfoMapper.insert(wxInfo);
    }

    public WXInfo selectList(String event_key) {
        QueryWrapper<WXInfo> queryWrapper = new QueryWrapper<WXInfo>();
        queryWrapper.eq("eventkey",event_key);
        return wxInfoMapper.selectOne(queryWrapper);
    }

    public List<WXInfo> select(QueryWrapper<WXInfo> queryWrapper) {
        return wxInfoMapper.selectList(queryWrapper);
    }

    public void update(WXInfo wxInfo) {
        wxInfoMapper.update(wxInfo, new QueryWrapper<WXInfo>().eq("openid",wxInfo.getOpenid()));
    }

    public void delete(String openid){
        wxInfoMapper.delete(new QueryWrapper<WXInfo>().eq("openid",openid));
    }
}
