package com.example.internet.controller;

import com.example.internet.entity.SysUser;
import com.example.internet.mapper.SysUserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户管理接口测试：分页、详情、增删改、状态、重置密码、权限拦截与保护逻辑
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static String adminToken;

    /** JUnit 默认每个测试方法新建实例，跨方法传递的值必须用 static */
    private static Long createdUserId;

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray())
                .path("data").path("token").asText();
    }

    @Test
    @Order(1)
    void adminLogin() throws Exception {
        adminToken = login("admin", "123456");
        assertThat(adminToken).isNotBlank();
    }

    @Test
    @Order(2)
    @DisplayName("分页列表：返回 UserItem 结构且不含密码字段")
    void pageList() throws Exception {
        mockMvc.perform(get("/system/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.list[0].roleIds").isArray())
                .andExpect(jsonPath("$.data.list[0].roleName").isNotEmpty())
                .andExpect(jsonPath("$.data.list[0].password").doesNotExist());
    }

    @Test
    @Order(3)
    @DisplayName("查询条件：用户名模糊 + 状态过滤")
    void pageListWithFilter() throws Exception {
        mockMvc.perform(get("/system/users")
                        .param("username", "user00")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2));

        mockMvc.perform(get("/system/users")
                        .param("status", "0")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].username").value("user002"));
    }

    @Test
    @Order(4)
    @DisplayName("详情存在与 404")
    void detail() throws Exception {
        mockMvc.perform(get("/system/users/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"));

        mockMvc.perform(get("/system/users/9999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @Order(5)
    @DisplayName("新增用户成功，用户名重复/角色不存在/缺密码被拒绝")
    void create() throws Exception {
        String body = "{\"username\":\"zhangsan\",\"nickname\":\"张三\",\"email\":\"zs@example.com\","
                + "\"phone\":\"13811112222\",\"password\":\"zhang123\",\"status\":1,\"roleIds\":[2],\"remark\":\"测试\"}";
        MvcResult result = mockMvc.perform(post("/system/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("zhangsan"))
                .andExpect(jsonPath("$.data.roleIds[0]").value(2))
                .andExpect(jsonPath("$.data.roleName").value("运营人员"))
                .andReturn();
        createdUserId = objectMapper.readTree(result.getResponse().getContentAsByteArray())
                .path("data").path("id").asLong();
        assertThat(createdUserId).isPositive();

        mockMvc.perform(post("/system/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("用户名已存在"));

        mockMvc.perform(post("/system/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("zhangsan", "lisi").replace("\"roleIds\":[2]", "\"roleIds\":[999]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("所选角色不存在"));

        mockMvc.perform(post("/system/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("zhangsan", "wangwu").replace("\"password\":\"zhang123\",", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("新增用户时密码不能为空"));
    }

    @Test
    @Order(6)
    @DisplayName("修改用户：不改密码时旧密码仍有效；改密码后新密码生效")
    void update() throws Exception {
        mockMvc.perform(put("/system/users/" + createdUserId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"zhangsan\",\"nickname\":\"张三丰\",\"email\":\"\","
                                + "\"phone\":\"\",\"status\":1,\"roleIds\":[2,3],\"remark\":\"\"}"))
                .andExpect(status().isOk());

        MvcResult detail = mockMvc.perform(get("/system/users/" + createdUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("张三丰"))
                .andExpect(jsonPath("$.data.roleIds.length()").value(2))
                .andReturn();
        assertThat(detail.getResponse().getContentAsByteArray()).asString().contains("张三丰");

        SysUser user = userMapper.selectById(createdUserId);
        assertThat(passwordEncoder.matches("zhang123", user.getPassword())).isTrue();

        mockMvc.perform(put("/system/users/" + createdUserId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"zhangsan\",\"nickname\":\"张三丰\",\"email\":\"\","
                                + "\"phone\":\"\",\"password\":\"newpass99\",\"status\":1,\"roleIds\":[2]}"))
                .andExpect(status().isOk());
        user = userMapper.selectById(createdUserId);
        assertThat(passwordEncoder.matches("newpass99", user.getPassword())).isTrue();
    }

    @Test
    @Order(7)
    @DisplayName("修改用户时用户名与其他用户冲突被拒绝")
    void updateDuplicateUsername() throws Exception {
        mockMvc.perform(put("/system/users/" + createdUserId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user001\",\"nickname\":\"X\",\"email\":\"\","
                                + "\"phone\":\"\",\"status\":1,\"roleIds\":[2]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    @Order(8)
    @DisplayName("状态管理：可禁用/启用普通用户，内置管理员不可禁用")
    void changeStatus() throws Exception {
        mockMvc.perform(put("/system/users/" + createdUserId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/system/users/" + createdUserId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/system/users/1/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("不能禁用内置管理员账号"));
    }

    @Test
    @Order(9)
    @DisplayName("重置密码后可使用 123456 登录")
    void resetPassword() throws Exception {
        mockMvc.perform(put("/system/users/" + createdUserId + "/resetPassword")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        SysUser user = userMapper.selectById(createdUserId);
        assertThat(passwordEncoder.matches("123456", user.getPassword())).isTrue();
    }

    @Test
    @Order(10)
    @DisplayName("删除：内置管理员不可删，普通用户删除后详情 404")
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/system/users/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("不能删除内置管理员账号"));

        mockMvc.perform(delete("/system/users/" + createdUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/system/users/" + createdUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(11)
    @DisplayName("权限拦截：只有 view 权限的运营角色可查列表，增删改返回 403")
    void forbiddenWithoutPermission() throws Exception {
        String operatorToken = login("user001", "123456");

        mockMvc.perform(get("/system/users")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/system/users")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"hacker1\",\"nickname\":\"H\",\"email\":\"\","
                                + "\"phone\":\"\",\"password\":\"abc123\",\"status\":1,\"roleIds\":[2]}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/system/users/3")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isForbidden());
    }
}
