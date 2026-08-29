package com.example.internet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作类型，如 用户 / 角色 / 菜单 / 系统 */
    private String type;

    private String content;

    private String operator;

    private LocalDateTime createTime;
}
