package com.ncg.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncg.model.OperationLog;
import com.ncg.service.OperationLogService;
import com.ncg.util.JwtUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 操作日志审计切面
 *
 * 拦截所有 Controller 方法，自动记录增删改操作。
 */
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<String> LOG_MODULES = Set.of(
            "/api/batch"
    );

    @Pointcut("execution(* com.ncg.web.controller..*.*(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return point.proceed();
        }

        HttpServletRequest request =  attrs.getRequest();
        String url = request.getRequestURI();
        String method = request.getMethod();

        // 只记录写操作的 API
        boolean shouldLog = false;
        for (String module : LOG_MODULES) {
            if (url.startsWith(module)) {
                shouldLog = true;
                break;
            }
        }

        OperationLog log = new OperationLog();
        log.setMethod(method);
        log.setRequestUrl(url);
        log.setOperateTime(LocalDateTime.now());

        // 解析 token 获取用户信息
        String operator = null;
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                log.setUsername(username != null ? username : "unknown");
                log.setRole(role != null ? role : "unknown");
                operator = username;
            }
        } catch (Exception ignored) {}
        if (operator == null) {
            operator = log.getUsername();
        }
        log.setOperator(operator);

        // 获取 IP
        log.setIpAddress(getIpAddress(request));

        // 确定操作类型和模块
        if (shouldLog) {
            if (method.equalsIgnoreCase("POST")) {
                log.setOperationType("CREATE");
                log.setModule(extractModule(url, "create"));
            } else if (method.equalsIgnoreCase("DELETE")) {
                log.setOperationType("DELETE");
                log.setModule(extractModule(url, "delete"));
            } else if (method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("PATCH")
                    || url.contains("/update")) {
                log.setOperationType("UPDATE");
                log.setModule(extractModule(url, "update"));
            } else {
                shouldLog = false;
            }
        }

        // 序列化请求参数（脱敏密码），同时捕获批次编号
        if (shouldLog) {
            String capturedBatchNo = null;
            try {
                Object[] args = point.getArgs();
                if (args != null && args.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (Object arg : args) {
                        if (arg == null) continue;
                        if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) continue;
                        try {
                            String json = objectMapper.writeValueAsString(arg);
                            json = json.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"******\"");
                            json = json.replaceAll("\"newPassword\"\\s*:\\s*\"[^\"]*\"", "\"newPassword\":\"******\"");
                            json = json.replaceAll("\"oldPassword\"\\s*:\\s*\"[^\"]*\"", "\"oldPassword\":\"******\"");
                            sb.append(json).append(" ");
                            // 从 JSON 中提取 batchNo 用于日志记录
                            if (capturedBatchNo == null && url.contains("/batch") && "CREATE".equals(log.getOperationType())) {
                                capturedBatchNo = extractFieldFromJson(json, "batchNo");
                            }
                        } catch (Exception ignored) {}
                    }
                    log.setRequestParams(sb.toString().trim());
                }
            } catch (Exception ignored) {}
            if (capturedBatchNo != null) {
                log.setBatchNo(capturedBatchNo);
            }
        }

        Object result = null;
        try {
            result = point.proceed();
            if (shouldLog) {
                log.setStatus("SUCCESS");
                log.setDescription(buildDescription(log, url));
                operationLogService.saveLog(log);
            }
            return result;
        } catch (Exception e) {
            if (shouldLog) {
                log.setStatus("FAIL");
                log.setErrorMsg(e.getMessage());
                log.setDescription(buildDescription(log, url) + " [失败]");
                operationLogService.saveLog(log);
            }
            throw e;
        }
    }

    private String extractModule(String url, String action) {
        if (url.contains("/batch")) return "批次管理";
        if (url.contains("/user")) return "用户管理";
        if (url.contains("/config")) return "参数配置";
        if (url.contains("/alert")) return "预警管理";
        return "其他";
    }

    private String buildDescription(OperationLog log, String url) {
        String op = switch (log.getOperationType()) {
            case "CREATE" -> "新增";
            case "UPDATE" -> "修改";
            case "DELETE" -> "删除";
            default -> log.getOperationType();
        };
        String module = log.getModule();
        if (url.contains("/batch")) {
            if (url.contains("create")) return op + "批次";
            if (url.contains("update")) return op + "批次信息";
            if (url.contains("delete")) return op + "批次";
        }
        if (url.contains("/user")) {
            if (url.contains("create")) return op + "用户";
            if (url.contains("update")) return op + "用户信息";
            if (url.contains("delete")) return op + "用户";
        }
        if (url.contains("/config")) return op + "系统配置";
        if (url.contains("/alert")) return op + "预警记录";
        return op + module;
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String extractFieldFromJson(String json, String fieldName) {
        if (json == null || fieldName == null) return null;
        String pattern = "\"" + fieldName + "\"\\s*:\\s*\"([^\"]*)\"";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
