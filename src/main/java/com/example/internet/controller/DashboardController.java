package com.example.internet.controller;

import com.example.internet.common.Result;
import com.example.internet.service.DashboardService;
import com.example.internet.vo.ActivityItemVO;
import com.example.internet.vo.CategoryStatVO;
import com.example.internet.vo.DashboardStatsVO;
import com.example.internet.vo.VisitTrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仪表盘接口，登录即可访问
 */
@Tag(name = "仪表盘")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "顶部统计")
    @GetMapping("/stats")
    public Result<DashboardStatsVO> stats() {
        return Result.ok(dashboardService.stats());
    }

    @Operation(summary = "访问趋势")
    @GetMapping("/visitTrend")
    public Result<VisitTrendVO> visitTrend() {
        return Result.ok(dashboardService.visitTrend());
    }

    @Operation(summary = "分类统计")
    @GetMapping("/categoryStats")
    public Result<CategoryStatVO> categoryStats() {
        return Result.ok(dashboardService.categoryStats());
    }

    @Operation(summary = "最近动态")
    @GetMapping("/activities")
    public Result<List<ActivityItemVO>> activities() {
        return Result.ok(dashboardService.activities());
    }
}
