package com.example.internet.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 访问趋势，与前端 VisitTrend 对齐；演示数据按日期种子生成，同一天内稳定
 */
@Getter
@AllArgsConstructor
public class VisitTrendVO {

    private final List<String> dates;

    private final List<Series> series;

    @Getter
    @AllArgsConstructor
    public static class Series {

        private final String name;

        private final List<Integer> data;
    }
}
