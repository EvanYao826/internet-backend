package com.example.internet.dto;

import lombok.Data;

/**
 * 分页查询基类：页码从 1 起，pageSize 上限 100
 */
@Data
public class PageQuery {

    private long page = 1;

    private long pageSize = 10;

    public long getPage() {
        return Math.max(page, 1);
    }

    public long getPageSize() {
        return Math.min(Math.max(pageSize, 1), 100);
    }
}
