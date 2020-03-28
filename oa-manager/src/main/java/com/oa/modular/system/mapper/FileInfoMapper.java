package com.oa.modular.system.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.modular.system.entity.FileInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 文件信息表
 Mapper 接口
 * </p>
 *
 * @author stylefeng
 * @since 2018-12-07
 */
public interface FileInfoMapper extends BaseMapper<FileInfo> {
    /**
     * 查询附件
     */
    Page<Map<String, Object>> selectFiles(@Param("page")Page page, @Param("id")String id);
}
