package com.example.internet.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询参数，与前端 UserQuery 对齐
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserPageQuery extends PageQuery {

    /** 用户名模糊查询 */
    private String username;

    @Min(0)
    @Max(1)
    private Integer status;

    private Long deptId;
}
