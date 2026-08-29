package com.example.internet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 用户新增/修改表单，与前端 UserForm 对齐。
 * 新增时 password 必填；修改时留空表示不改密码。
 */
@Data
public class UserFormDTO {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,30}$", message = "用户名只能包含字母、数字、下划线，长度 3-30")
    private String username;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过 50")
    private String nickname;

    /** 与前端 isEmail 正则一致；空串表示不填 */
    @Pattern(regexp = "^$|^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$", message = "邮箱格式不正确")
    private String email;

    /** 与前端 isPhone 正则一致；空串表示不填 */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(min = 6, max = 32, message = "密码长度需在 6-32 位之间")
    private String password;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态取值不合法")
    @Max(value = 1, message = "状态取值不合法")
    private Integer status;

    @NotEmpty(message = "请选择角色")
    private List<Long> roleIds;

    private Long deptId;

    @Size(max = 255, message = "备注长度不能超过 255")
    private String remark;
}
