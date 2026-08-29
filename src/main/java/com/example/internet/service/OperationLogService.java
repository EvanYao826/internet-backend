package com.example.internet.service;

import com.example.internet.entity.SysOperationLog;
import com.example.internet.mapper.SysOperationLogMapper;
import com.example.internet.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志：登录登出、各管理模块的增删改和授权操作统一记录，
 * 供仪表盘「最近动态」与审计使用。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final SysOperationLogMapper logMapper;

    public void log(String type, String content, String operator) {
        SysOperationLog entry = new SysOperationLog();
        entry.setType(type);
        entry.setContent(content);
        entry.setOperator(operator);
        entry.setCreateTime(LocalDateTime.now());
        try {
            logMapper.insert(entry);
        } catch (Exception e) {
            // 日志失败不影响主流程
            log.error("操作日志写入失败: {}", content, e);
        }
    }

    /**
     * 以当前登录用户作为操作人；无登录上下文（如登录失败场景）返回入参或"系统"
     */
    public void log(String type, String content) {
        log(type, content, currentUsername());
    }

    public static String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUsername();
        }
        return "系统";
    }
}
