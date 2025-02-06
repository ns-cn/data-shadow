package com.tangyujun.datashadow.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 日期时间工具类测试
 * 用于测试DateTimeUtils的各项功能
 * 
 * 主要测试以下功能:
 * 1. 日期解析(parseDate)
 * 2. 时间解析(parseTime)
 * 3. 日期时间解析(parseDateTime)
 */
public class DateTimeUtilsTest {

        /**
         * 测试日期解析功能
         */
        @Test
        public void testParseDate() {
                // 测试null值
                assertNull(DateTimeUtils.parseDate(null), "空值应该返回null");

                // 测试LocalDate类型
                LocalDate date = LocalDate.of(2024, 3, 15);
                assertEquals(date, DateTimeUtils.parseDate(date), "LocalDate类型应该直接返回");

                // 测试LocalDateTime类型
                LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 10, 30);
                assertEquals(LocalDate.of(2024, 3, 15), DateTimeUtils.parseDate(dateTime),
                                "LocalDateTime类型应该转换为LocalDate");

                // 测试Date类型
                Date javaDate = new Date();
                LocalDate expected = javaDate.toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate();
                assertEquals(expected, DateTimeUtils.parseDate(javaDate),
                                "Date类型应该正确转换为LocalDate");

                // 测试字符串格式
                assertEquals(LocalDate.of(2024, 3, 15), DateTimeUtils.parseDate("2024-03-15"),
                                "标准日期格式 yyyy-MM-dd 解析失败");
                assertEquals(LocalDate.of(2024, 3, 15), DateTimeUtils.parseDate("20240315"),
                                "紧凑日期格式 yyyyMMdd 解析失败");
                assertEquals(LocalDate.of(2024, 3, 15), DateTimeUtils.parseDate("2024-03-15 10:30"),
                                "日期时间格式 yyyy-MM-dd HH:mm 解析失败");
                assertEquals(LocalDate.of(2024, 3, 15), DateTimeUtils.parseDate("2024年03月15日"),
                                "中文日期格式解析失败");
                assertEquals(LocalDate.of(2024, 3, 15), DateTimeUtils.parseDate("2024/03/15"),
                                "斜杠分隔的日期格式解析失败");
                assertEquals(LocalDate.of(2024, 3, 15), DateTimeUtils.parseDate("15.03.2024"),
                                "点分隔的日期格式解析失败");

                // 测试无效输入
                assertNull(DateTimeUtils.parseDate("invalid-date"), "无效日期字符串应该返回null");
                assertNull(DateTimeUtils.parseDate("2024-13-45"), "无效日期值应该返回null");
                assertNull(DateTimeUtils.parseDate(""), "空字符串应该返回null");

                // 测试时间戳
                long timestamp = System.currentTimeMillis();
                LocalDate expectedFromTimestamp = new Date(timestamp).toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate();
                assertEquals(expectedFromTimestamp, DateTimeUtils.parseDate(timestamp),
                                "时间戳应该正确转换为LocalDate");
                assertEquals(expectedFromTimestamp, DateTimeUtils.parseDate(BigDecimal.valueOf(timestamp)),
                                "BigDecimal时间戳应该正确转换为LocalDate");
        }

        /**
         * 测试时间解析功能
         */
        @Test
        public void testParseTime() {
                // 测试null值
                assertNull(DateTimeUtils.parseTime(null), "空值应该返回null");

                // 测试LocalTime类型
                LocalTime time = LocalTime.of(10, 30);
                assertEquals(time, DateTimeUtils.parseTime(time), "LocalTime类型应该直接返回");

                // 测试LocalDateTime类型
                LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 10, 30);
                assertEquals(LocalTime.of(10, 30), DateTimeUtils.parseTime(dateTime),
                                "LocalDateTime类型应该转换为LocalTime");

                // 测试Date类型
                Date javaDate = new Date();
                LocalTime expected = javaDate.toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalTime();
                assertEquals(expected, DateTimeUtils.parseTime(javaDate),
                                "Date类型应该正确转换为LocalTime");

                // 测试字符串格式
                assertEquals(LocalTime.of(10, 30), DateTimeUtils.parseTime("10:30"),
                                "时间格式 HH:mm 解析失败");
                assertEquals(LocalTime.of(10, 30, 45), DateTimeUtils.parseTime("10:30:45"),
                                "时间格式 HH:mm:ss 解析失败");
                assertEquals(LocalTime.of(10, 30, 45, 123000000), DateTimeUtils.parseTime("10:30:45.123"),
                                "时间格式 HH:mm:ss.SSS 解析失败");

                // 测试无效输入
                assertNull(DateTimeUtils.parseTime("invalid-time"), "无效时间字符串应该返回null");
                assertNull(DateTimeUtils.parseTime("25:70"), "无效时间值应该返回null");
                assertNull(DateTimeUtils.parseTime(""), "空字符串应该返回null");

                // 测试时间戳
                long timestamp = System.currentTimeMillis();
                LocalTime expectedFromTimestamp = new Date(timestamp).toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalTime();
                assertEquals(expectedFromTimestamp, DateTimeUtils.parseTime(timestamp),
                                "时间戳应该正确转换为LocalTime");
                assertEquals(expectedFromTimestamp, DateTimeUtils.parseTime(BigDecimal.valueOf(timestamp)),
                                "BigDecimal时间戳应该正确转换为LocalTime");
        }

        /**
         * 测试日期时间解析功能
         */
        @Test
        public void testParseDateTime() {
                // 测试null值
                assertNull(DateTimeUtils.parseDateTime(null), "空值应该返回null");

                // 测试LocalDate类型
                LocalDate date = LocalDate.of(2024, 3, 15);
                assertEquals(LocalDateTime.of(2024, 3, 15, 0, 0), DateTimeUtils.parseDateTime(date),
                                "LocalDate类型应该转换为LocalDateTime");

                // 测试LocalDateTime类型
                LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 10, 30);
                assertEquals(dateTime, DateTimeUtils.parseDateTime(dateTime),
                                "LocalDateTime类型应该直接返回");

                // 测试Date类型
                Date javaDate = new Date();
                LocalDateTime expected = javaDate.toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime();
                assertEquals(expected, DateTimeUtils.parseDateTime(javaDate),
                                "Date类型应该正确转换为LocalDateTime");

                // 测试字符串格式
                assertEquals(LocalDateTime.of(2024, 3, 15, 0, 0), DateTimeUtils.parseDateTime("2024-03-15"),
                                "标准日期格式 yyyy-MM-dd 解析失败");
                assertEquals(LocalDateTime.of(2024, 3, 15, 10, 30), DateTimeUtils.parseDateTime("2024-03-15 10:30"),
                                "日期时间格式 yyyy-MM-dd HH:mm 解析失败");
                assertEquals(LocalDateTime.of(2024, 3, 15, 10, 30, 45),
                                DateTimeUtils.parseDateTime("2024-03-15 10:30:45"),
                                "日期时间格式 yyyy-MM-dd HH:mm:ss 解析失败");

                // 测试无效输入
                assertNull(DateTimeUtils.parseDateTime("invalid-datetime"), "无效日期时间字符串应该返回null");
                assertNull(DateTimeUtils.parseDateTime("2024-13-45 25:70"), "无效日期时间值应该返回null");
                assertNull(DateTimeUtils.parseDateTime(""), "空字符串应该返回null");

                // 测试时间戳
                long timestamp = System.currentTimeMillis();
                LocalDateTime expectedFromTimestamp = new Date(timestamp).toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime();
                assertEquals(expectedFromTimestamp, DateTimeUtils.parseDateTime(timestamp),
                                "时间戳应该正确转换为LocalDateTime");
                assertEquals(expectedFromTimestamp, DateTimeUtils.parseDateTime(BigDecimal.valueOf(timestamp)),
                                "BigDecimal时间戳应该正确转换为LocalDateTime");
        }
}
