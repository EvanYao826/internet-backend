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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 个人中心接口测试：资料查询/修改、邮箱手机唯一校验、密码修改全流程。
 * 使用独立测试账号，避免影响其他测试类的 admin 登录。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;
    private static String userToken;

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
    void adminLoginAndCreateFixtureUser() throws Exception {
        adminToken = login("admin", "123456");
        mockMvc.perform(post("/system/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"pwdtest\",\"nickname\":\"密码测试\",\"email\":\"pwdtest@example.com\","
                                + "\"phone\":\"13899990000\",\"password\":\"123456\",\"status\":1,\"roleIds\":[2],\"remark\":\"\"}"))
                .andExpect(status().isOk());
        userToken = login("pwdtest", "123456");
        assertThat(userToken).isNotBlank();
    }

    @Test
    @Order(2)
    @DisplayName("获取个人信息：当前登录用户")
    void info() throws Exception {
        mockMvc.perform(get("/profile/info")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("pwdtest"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @Order(3)
    @DisplayName("修改资料成功；非法邮箱/占用邮箱/占用手机号被拒绝")
    void updateProfile() throws Exception {
        mockMvc.perform(put("/profile/info")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"新昵称\",\"email\":\"newpwd@example.com\",\"phone\":\"13899991111\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/profile/info")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("新昵称"));

        mockMvc.perform(put("/profile/info")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"X\",\"email\":\"bad-email\",\"phone\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("邮箱格式不正确"));

        // admin@example.com 已被 admin 占用
        mockMvc.perform(put("/profile/info")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"X\",\"email\":\"admin@example.com\",\"phone\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("邮箱已被其他账号使用"));

        // 13800000000 已被 admin 占用
        mockMvc.perform(put("/profile/info")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"X\",\"email\":\"\",\"phone\":\"13800000000\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("手机号已被其他账号使用"));

        // 自己保持原值不受唯一性校验影响
        mockMvc.perform(put("/profile/info")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"新昵称\",\"email\":\"newpwd@example.com\",\"phone\":\"13899991111\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    @DisplayName("修改密码：旧密码错/新旧相同被拒；成功后新密码生效")
    void changePassword() throws Exception {
        mockMvc.perform(put("/profile/password")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"wrong\",\"newPassword\":\"brandnew1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("原密码不正确"));

        mockMvc.perform(put("/profile/password")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"123456\",\"newPassword\":\"123456\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("新密码不能与原密码相同"));

        mockMvc.perform(put("/profile/password")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"123456\",\"newPassword\":\"brandnew1\"}"))
                .andExpect(status().isOk());

        // 旧密码失效、新密码可登录
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"pwdtest\",\"password\":\"123456\"}"))
                .andExpect(status().isBadRequest());

        String newToken = login("pwdtest", "brandnew1");
        assertThat(newToken).isNotBlank();
        userToken = newToken;
    }

    @Test
    @Order(5)
    @DisplayName("清理：删除测试账号")
    void cleanup() throws Exception {
        Long userId = getUserIdByUsername("pwdtest");
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/system/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    private Long getUserIdByUsername(String username) throws Exception {
        MvcResult result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/system/users")
                        .param("username", username)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        var list = objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data").path("list");
        assertThat(list.size()).isEqualTo(1);
        return list.get(0).path("id").asLong();
    }
}
