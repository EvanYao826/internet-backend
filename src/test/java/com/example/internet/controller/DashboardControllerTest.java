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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 仪表盘接口测试：统计、趋势、分类、最近动态
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;

    @Test
    @Order(1)
    void adminLogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn();
        adminToken = objectMapper.readTree(result.getResponse().getContentAsByteArray())
                .path("data").path("token").asText();
        assertThat(adminToken).isNotBlank();
    }

    @Test
    @Order(2)
    @DisplayName("统计卡片：用户总数为真实数据，演示指标齐全")
    void stats() throws Exception {
        mockMvc.perform(get("/dashboard/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userTotal").value(3))
                .andExpect(jsonPath("$.data.userGrowth").isNumber())
                .andExpect(jsonPath("$.data.orderTotal").isNumber())
                .andExpect(jsonPath("$.data.revenue").isNumber())
                .andExpect(jsonPath("$.data.visitTotal").isNumber())
                .andExpect(jsonPath("$.data.visitGrowth").value(-3.2));
    }

    @Test
    @Order(3)
    @DisplayName("访问趋势：14 天日期与两组序列")
    void visitTrend() throws Exception {
        mockMvc.perform(get("/dashboard/visitTrend")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dates.length()").value(14))
                .andExpect(jsonPath("$.data.series.length()").value(2))
                .andExpect(jsonPath("$.data.series[0].name").value("访问量"))
                .andExpect(jsonPath("$.data.series[1].name").value("订单量"))
                .andExpect(jsonPath("$.data.series[0].data.length()").value(14));
    }

    @Test
    @Order(4)
    @DisplayName("分类统计：categories 与 values 一一对应")
    void categoryStats() throws Exception {
        mockMvc.perform(get("/dashboard/categoryStats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categories.length()").value(6))
                .andExpect(jsonPath("$.data.values.length()").value(6));
    }

    @Test
    @Order(5)
    @DisplayName("最近动态：来自操作日志，含登录记录")
    void activities() throws Exception {
        MvcResult result = mockMvc.perform(get("/dashboard/activities")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        var body = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        assertThat(body.path("data").size()).isGreaterThanOrEqualTo(1);
        assertThat(body.path("data").get(0).path("operator").asText()).isEqualTo("admin");
    }

    @Test
    @Order(6)
    @DisplayName("未登录访问仪表盘返回 401")
    void unauthorized() throws Exception {
        mockMvc.perform(get("/dashboard/stats"))
                .andExpect(status().isUnauthorized());
    }
}
