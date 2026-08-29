package com.example.internet.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 分类统计，与前端 CategoryStat 对齐；categories 与 values 下标一一对应
 */
@Getter
@AllArgsConstructor
public class CategoryStatVO {

    private final List<String> categories;

    private final List<Integer> values;
}
