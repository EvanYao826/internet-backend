package com.example.internet.controller;

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
 * 角色管理接口测试：分页、全量、增删改、菜单授权、保护逻辑与权限拦截
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;

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
    @DisplayName("角色分页列表与名称筛选")
    void pageList() throws Exception {
        mockMvc.perform(get("/system/roles")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.list[0].roleCode").value("admin"));

        mockMvc.perform(get("/system/roles")
                        .param("roleName", "运营")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].roleCode").value("operator"));
    }

    @Test
    @Order(3)
    @DisplayName("全部角色下拉")
    void listAll() throws Exception {
        mockMvc.perform(get("/system/roles/all")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @Order(4)
    @DisplayName("新增角色成功；编码重复被拒绝")
    void create() throws Exception {
        mockMvc.perform(post("/system/roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"测试角色\",\"roleCode\":\"tester\",\"sort\":9,\"status\":1,\"remark\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleCode").value("tester"))
                .andExpect(jsonPath("$.data.roleName").value("测试角色"));

        mockMvc.perform(post("/system/roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"重复角色\",\"roleCode\":\"tester\",\"sort\":9,\"status\":1,\"remark\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("角色编码已存在"));
    }

    @Test
    @Order(5)
    @DisplayName("修改角色成功；内置 admin 角色改码/停用被拒绝")
    void update() throws Exception {
        mockMvc.perform(put("/system/roles/3")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"访客\",\"roleCode\":\"guest\",\"sort\":4,\"status\":0,\"remark\":\"只读权限\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/system/roles/1")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"超级管理员\",\"roleCode\":\"hacker\",\"sort\":1,\"status\":1,\"remark\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("内置管理员角色不允许修改编码或停用"));

        mockMvc.perform(put("/system/roles/1")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"超级管理员\",\"roleCode\":\"admin\",\"sort\":1,\"status\":0,\"remark\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("内置管理员角色不允许修改编码或停用"));
    }

    @Test
    @Order(6)
    @DisplayName("查询角色已分配菜单：operator 为 [1,10,11]，不存在角色 404")
    void getMenuIds() throws Exception {
        mockMvc.perform(get("/system/roles/2/menus")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));

        mockMvc.perform(get("/system/roles/9999/menus")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(7)
    @DisplayName("授权菜单：含不存在菜单被拒绝；授权后用户权限实时生效")
    void assignMenus() throws Exception {
        mockMvc.perform(put("/system/roles/2/menus")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuIds\":[1,10,9999]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("所选菜单不存在"));

        mockMvc.perform(put("/system/roles/2/menus")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuIds\":[1,10,11,12]}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/system/roles/2/menus")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(4));

        // 权限每次请求实时加载：operator 用户无需重新登录即获得 system:role:view
        String operatorToken = login("user001", "123456");
        mockMvc.perform(get("/auth/userInfo")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.permissions").value(
                        org.hamcrest.Matchers.hasItem("system:role:view")));
    }

    @Test
    @Order(8)
    @DisplayName("删除：内置角色不可删；已分配用户的角色不可删；空角色可删")
    void deleteRole() throws Exception {
        mockMvc.perform(delete("/system/roles/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("内置管理员角色不允许删除"));

        mockMvc.perform(delete("/system/roles/2")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("该角色已分配给")));

        // 测试角色未分配任何用户，可删除
        Long testerId = getRoleIdByCode("tester");
        mockMvc.perform(delete("/system/roles/" + testerId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/system/roles/all")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @Order(9)
    @DisplayName("权限拦截：operator 可查角色列表，增删与授权返回 403")
    void forbiddenWithoutPermission() throws Exception {
        String operatorToken = login("user001", "123456");

        mockMvc.perform(get("/system/roles")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/system/roles")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"越权角色\",\"roleCode\":\"hacker\",\"sort\":9,\"status\":1,\"remark\":\"\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/system/roles/2/menus")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuIds\":[1]}"))
                .andExpect(status().isForbidden());
    }

    private Long getRoleIdByCode(String roleCode) throws Exception {
        MvcResult result = mockMvc.perform(get("/system/roles/all")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        var roles = objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data");
        for (var role : roles) {
            if (roleCode.equals(role.path("roleCode").asText())) {
                return role.path("id").asLong();
            }
        }
        throw new AssertionError("角色不存在: " + roleCode);
    }
}
