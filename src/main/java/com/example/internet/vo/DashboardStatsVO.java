package com.example.internet.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 仪表盘统计卡片，与前端 DashboardStats 对齐。
 * userTotal 为数据库真实用户数，其余为演示指标（系统暂无订单/访问日志表）。
 */
@Getter
@AllArgsConstructor
public class DashboardStatsVO {

    private final long userTotal;
    private final double userGrowth;
    private final long orderTotal;
    private final double orderGrowth;
    private final long revenue;
    private final double revenueGrowth;
    private final long visitTotal;
    private final double visitGrowth;
}
