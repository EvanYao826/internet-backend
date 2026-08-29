package com.example.internet.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 用户分页查询参数，与前端 UserQuery 对齐
 */
@Data
public class UserPageQuery {

    private long page = 1;

    /** 上限 100，防止一次拉取过多数据 */
    private long pageSize = 10;

    /** 用户名模糊查询 */
    private String username;

    @Min(0)
    @Max(1)
    private Integer status;

    private Long deptId;

    public long getPage() {
        return Math.max(page, 1);
    }

    public long getPageSize() {
        return Math.min(Math.max(pageSize, 1), 100);
    }
}
