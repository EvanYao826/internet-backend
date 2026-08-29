package com.example.internet.security;

import com.example.internet.common.BizException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录失败次数限制：同一用户名连续失败超过阈值后锁定一段时间，
 * 防止暴力破解。内存态实现，重启后重置。
 */
@Component
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_MILLIS = 15 * 60 * 1000L;

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public void checkLocked(String username) {
        Attempt attempt = attempts.get(username);
        if (attempt != null && attempt.count() >= MAX_ATTEMPTS) {
            long remaining = attempt.lockedUntil() - System.currentTimeMillis();
            if (remaining > 0) {
                long minutes = Math.max(remaining / 60000, 1);
                throw new BizException("登录失败次数过多，账号已锁定，请约 " + minutes + " 分钟后再试");
            }
            attempts.remove(username);
        }
    }

    public void recordFailure(String username) {
        attempts.compute(username, (k, attempt) -> {
            long now = System.currentTimeMillis();
            if (attempt == null || attempt.lockedUntil() <= now) {
                return new Attempt(1, 0);
            }
            int count = attempt.count() + 1;
            long lockedUntil = count >= MAX_ATTEMPTS ? now + LOCK_MILLIS : 0;
            return new Attempt(count, lockedUntil);
        });
    }

    public void clear(String username) {
        attempts.remove(username);
    }

    private record Attempt(int count, long lockedUntil) {
    }
}
