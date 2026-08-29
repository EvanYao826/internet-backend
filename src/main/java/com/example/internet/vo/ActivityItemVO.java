package com.example.internet.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 最近动态，与前端 ActivityItem 对齐；数据来自操作日志表
 */
@Getter
@Builder
public class ActivityItemVO {

    private Long id;

    private String type;

    private String content;

    private String operator;

    private LocalDateTime createTime;
}
