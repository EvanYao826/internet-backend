package com.example.internet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.internet.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/** SysUser 数据访问接口 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
