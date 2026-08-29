package com.example.internet.controller;

import com.example.internet.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "健康检查")
@RestController
public class PingController {

    @Operation(summary = "服务存活检查")
    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        return Result.ok(Map.of("service", "internet-backend", "time", LocalDateTime.now().toString()));
    }
}
