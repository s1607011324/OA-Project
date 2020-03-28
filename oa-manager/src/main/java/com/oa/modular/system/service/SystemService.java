package com.oa.modular.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oa.modular.system.entity.WXSystem;
import com.oa.modular.system.mapper.SystemMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SystemService {
    @Resource
    private SystemMapper systemMapper;

    public void update(WXSystem system){
        this.systemMapper.updateById(system);
    }
    public WXSystem selectSys(String pwd){
        return this.systemMapper.selectOne(new QueryWrapper<WXSystem>().eq("pwd",pwd));
    }
    public WXSystem selectSys(QueryWrapper<WXSystem> queryWrapper){
        return this.systemMapper.selectOne(queryWrapper);
    }
}
