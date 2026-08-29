package com.example.internet.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Getter;

import java.util.List;

/**
 * 分页返回结构，与前端 PageResult<T> 对齐：{ list, total, page, pageSize }
 */
@Getter
public class PageResult<T> {

    private final List<T> list;
    private final long total;
    private final long page;
    private final long pageSize;

    private PageResult(List<T> list, long total, long page, long pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public static <T> PageResult<T> of(List<T> list, long total, long page, long pageSize) {
        return new PageResult<>(list, total, page, pageSize);
    }
}
