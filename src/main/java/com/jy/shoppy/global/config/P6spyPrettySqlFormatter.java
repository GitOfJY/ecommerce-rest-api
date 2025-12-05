package com.jy.shoppy.global.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.FormatStyle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class P6spyPrettySqlFormatter implements MessageFormattingStrategy {
    private static final String NEW_LINE = System.lineSeparator();
    private static final String SEPARATOR = "=".repeat(120);
    private static final String SUB_SEPARATOR = "-".repeat(120);
    private static final String TAB = "    ";

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        sql = formatSql(category, sql);
        Date currentDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yy.MM.dd HH:mm:ss");

        // 표 형식 포맷팅
        StringBuilder sb = new StringBuilder();
        sb.append(NEW_LINE);
        sb.append(SEPARATOR).append(NEW_LINE);
        sb.append("SQL EXECUTION REPORT").append(NEW_LINE);
        sb.append(SEPARATOR).append(NEW_LINE);
        sb.append("Timestamp     : ").append(format.format(currentDate)).append(NEW_LINE);
        sb.append("Execution Time: ").append(formatElapsedTime(elapsed)).append(NEW_LINE);
        sb.append("Connection ID : ").append(connectionId).append(NEW_LINE);
        sb.append("Category      : ").append(category).append(NEW_LINE);
        sb.append(SUB_SEPARATOR).append(NEW_LINE);
        sb.append(sql).append(NEW_LINE);
        sb.append(SEPARATOR).append(NEW_LINE);

        return sb.toString();
    }

    private String formatElapsedTime(long elapsed) {
        if (elapsed > 1000) {
            return String.format("%d ms [SLOW QUERY]", elapsed);
        } else if (elapsed > 500) {
            return String.format("%d ms [WARNING]", elapsed);
        } else if (elapsed > 100) {
            return String.format("%d ms [CAUTION]", elapsed);
        } else {
            return String.format("%d ms [OK]", elapsed);
        }
    }

    private String formatSql(String category, String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        // DDL은 format 적용 안함
        if (category.contains("statement") && sql.trim().toLowerCase(Locale.ROOT).startsWith("create")) {
            return "DDL Statement:" + NEW_LINE + TAB + sql;
        }

        // Hibernate SQL 포맷 적용
        if (category.equals("statement")) {
            String trimmedSQL = sql.trim().toLowerCase(Locale.ROOT);
            if (trimmedSQL.startsWith("select") ||
                    trimmedSQL.startsWith("insert") ||
                    trimmedSQL.startsWith("update") ||
                    trimmedSQL.startsWith("delete")) {
                sql = FormatStyle.BASIC.getFormatter().format(sql);
                return "HeFormatSql(P6Spy sql, Hibernate format):" + NEW_LINE + addIndentation(sql);
            }
        }
        return "P6Spy sql:" + NEW_LINE + TAB + sql;
    }

    private String addIndentation(String sql) {
        return sql.lines()
                .map(line -> TAB + line)
                .reduce((a, b) -> a + NEW_LINE + b)
                .orElse(sql);
    }
}