package com.example.internet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.internet.entity.SysOperationLog;
import com.example.internet.entity.SysUser;
import com.example.internet.mapper.SysOperationLogMapper;
import com.example.internet.mapper.SysUserMapper;
import com.example.internet.vo.ActivityItemVO;
import com.example.internet.vo.CategoryStatVO;
import com.example.internet.vo.DashboardStatsVO;
import com.example.internet.vo.VisitTrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 仪表盘数据。
 * 用户总数为数据库真实统计；订单/营收/访问量等演示指标无对应业务表，
 * 按固定值或日期种子生成，接入真实业务表后替换即可。
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final DateTimeFormatter MONTH_DAY = DateTimeFormatter.ofPattern("M/d");
    private static final int TREND_DAYS = 14;

    private final SysUserMapper userMapper;
    private final SysOperationLogMapper operationLogMapper;

    public DashboardStatsVO stats() {
        Long userTotal = userMapper.selectCount(null);
        return new DashboardStatsVO(
                userTotal == null ? 0 : userTotal,
                12.5,
                8652, 8.3,
                326800, 15.2,
                45689, -3.2);
    }

    public VisitTrendVO visitTrend() {
        LocalDate today = LocalDate.now();
        List<String> dates = new ArrayList<>();
        List<Integer> visits = new ArrayList<>();
        List<Integer> orders = new ArrayList<>();
        for (int i = TREND_DAYS - 1; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            dates.add(day.format(MONTH_DAY));
            // 以日期为种子生成稳定的演示数值
            long seed = day.toEpochDay();
            visits.add((int) (800 + Math.floorMod(seed * 131, 1200)));
            orders.add((int) (200 + Math.floorMod(seed * 97, 400)));
        }
        return new VisitTrendVO(dates, List.of(
                new VisitTrendVO.Series("访问量", visits),
                new VisitTrendVO.Series("订单量", orders)));
    }

    public CategoryStatVO categoryStats() {
        return new CategoryStatVO(
                List.of("电子产品", "服装鞋帽", "食品生鲜", "家居日用", "美妆个护", "图书文创"),
                List.of(3200, 2800, 1900, 2400, 1700, 1200));
    }

    public List<ActivityItemVO> activities() {
        List<SysOperationLog> logs = operationLogMapper.selectList(
                new LambdaQueryWrapper<SysOperationLog>()
                        .orderByDesc(SysOperationLog::getId)
                        .last("LIMIT 10"));
        return logs.stream()
                .map(log -> ActivityItemVO.builder()
                        .id(log.getId())
                        .type(log.getType())
                        .content(log.getContent())
                        .operator(log.getOperator())
                        .createTime(log.getCreateTime())
                        .build())
                .toList();
    }
}
