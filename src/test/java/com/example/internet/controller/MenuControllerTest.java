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
 * 菜单管理接口测试：树结构、增删改、父级校验、删除保护与权限拦截
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;

    /** JUnit 默认每个测试方法新建实例，跨方法传递的值必须用 static */
    private static Long createdMenuId;

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
    @DisplayName("菜单树：根节点与嵌套 children 结构正确")
    void tree() throws Exception {
        mockMvc.perform(get("/system/menus")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].menuName").value("仪表盘"))
                .andExpect(jsonPath("$.data[1].menuName").value("系统管理"))
                .andExpect(jsonPath("$.data[1].children.length()").value(3))
                .andExpect(jsonPath("$.data[1].children[0].menuName").value("用户管理"))
                // 用户管理下有 4 个按钮权限
                .andExpect(jsonPath("$.data[1].children[0].children.length()").value(4))
                .andExpect(jsonPath("$.data[1].children[0].children[0].menuType").value(3));
    }

    @Test
    @Order(3)
    @DisplayName("新增菜单成功并入树；缺路由地址/上级不存在/按钮挂目录被拒绝")
    void create() throws Exception {
        String body = "{\"parentId\":10,\"menuName\":\"字典管理\",\"menuType\":2,\"path\":\"/system/dict\","
                + "\"component\":\"system/dict/index\",\"icon\":\"menu\",\"permission\":\"system:dict:view\","
                + "\"sort\":4,\"status\":1,\"visible\":1}";
        MvcResult result = mockMvc.perform(post("/system/menus")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menuName").value("字典管理"))
                .andExpect(jsonPath("$.data.parentId").value(10))
                .andReturn();
        createdMenuId = objectMapper.readTree(result.getResponse().getContentAsByteArray())
                .path("data").path("id").asLong();
        assertThat(createdMenuId).isPositive();

        mockMvc.perform(get("/system/menus")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[1].children.length()").value(4));

        String bodyNoPath = body.replace("\"path\":\"/system/dict\",", "\"path\":\"\",")
                .replace("system/dict/index", "");
        mockMvc.perform(post("/system/menus")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyNoPath))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("请输入路由地址"));

        mockMvc.perform(post("/system/menus")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("\"parentId\":10", "\"parentId\":9999")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("上级菜单不存在"));

        mockMvc.perform(post("/system/menus")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("\"menuType\":2", "\"menuType\":3")
                                .replace("\"path\":\"/system/dict\",", "\"path\":\"\",")
                                .replace("system/dict/index", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("按钮的上级必须是菜单"));
    }

    @Test
    @Order(4)
    @DisplayName("修改菜单成功；移动到自身子孙节点被拒绝")
    void update() throws Exception {
        mockMvc.perform(put("/system/menus/" + createdMenuId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parentId\":10,\"menuName\":\"数据字典\",\"menuType\":2,\"path\":\"/system/dict\","
                                + "\"component\":\"system/dict/index\",\"icon\":\"menu\",\"permission\":\"system:dict:view\","
                                + "\"sort\":5,\"status\":1,\"visible\":1}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/system/menus")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[1].children[3].menuName").value("数据字典"))
                .andExpect(jsonPath("$.data[1].children[3].sort").value(5));

        // 系统管理(10) 不能移动到其子菜单 用户管理(11) 之下
        mockMvc.perform(put("/system/menus/10")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parentId\":11,\"menuName\":\"系统管理\",\"menuType\":1,\"path\":\"/system\","
                                + "\"icon\":\"system\",\"sort\":2,\"status\":1,\"visible\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("不能将菜单移动到自身或其子菜单下"));
    }

    @Test
    @Order(5)
    @DisplayName("删除保护：有子菜单/已授权角色被拒；无引用叶子节点可删")
    void deleteMenu() throws Exception {
        // 系统管理(10) 有子菜单
        mockMvc.perform(delete("/system/menus/10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("该菜单存在子菜单，请先删除子菜单"));

        // 仪表盘(1) 无子菜单但已授权给内置 admin 角色
        mockMvc.perform(delete("/system/menus/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("已授权给")));

        // 新建的字典管理无子项且未被任何角色引用，可删
        mockMvc.perform(delete("/system/menus/" + createdMenuId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/system/menus")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[1].children.length()").value(3));
    }

    @Test
    @Order(6)
    @DisplayName("权限拦截：operator 授权后可读菜单树，增删改返回 403")
    void forbiddenWithoutPermission() throws Exception {
        // 显式给 operator 角色授予菜单查看权限（含菜单13），避免测试类执行顺序影响
        mockMvc.perform(put("/system/roles/2/menus")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuIds\":[1,10,11,12,13]}"))
                .andExpect(status().isOk());

        String operatorToken = login("user001", "123456");

        // operator 拥有 system:menu:view，可读菜单树
        mockMvc.perform(get("/system/menus")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/system/menus")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parentId\":10,\"menuName\":\"越权菜单\",\"menuType\":2,\"path\":\"/hack\","
                                + "\"sort\":1,\"status\":1,\"visible\":1}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/system/menus/13")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isForbidden());
    }
}
