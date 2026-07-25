package com.neusoft.amos.common;

/**
 * 多租户 / 范围上下文（Fleet + Scope）。
 * installation = 船（逻辑分区键）；department = 部门（范围维度）。
 * 由 {@link TenantFilter} 从请求头 X-Installation / X-Department 写入，
 * 后续在 Repository / Service 层统一注入查询过滤条件。
 */
public final class TenantContext {

    private static final ThreadLocal<String> INSTALLATION = new ThreadLocal<>();
    private static final ThreadLocal<String> DEPARTMENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(String installation, String department) {
        INSTALLATION.set(installation);
        DEPARTMENT.set(department);
    }

    public static String installation() {
        return INSTALLATION.get();
    }

    public static String department() {
        return DEPARTMENT.get();
    }

    public static boolean hasScope() {
        return INSTALLATION.get() != null || DEPARTMENT.get() != null;
    }

    public static void clear() {
        INSTALLATION.remove();
        DEPARTMENT.remove();
    }
}
