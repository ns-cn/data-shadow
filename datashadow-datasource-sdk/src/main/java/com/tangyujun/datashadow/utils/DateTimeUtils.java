package com.tangyujun.datashadow.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 日期时间工具类
 * 提供日期时间解析和验证功能
 */
public class DateTimeUtils {

    /**
     * 将对象转换为LocalDate日期类型
     * 支持以下输入类型的转换:
     * 1. LocalDate - 直接返回
     * 
     * 2. LocalDateTime - 提取日期部分
     * 3. java.util.Date - 转换为LocalDate
     * 4. String - 支持多种日期字符串格式:
     * - yyyy-MM-dd
     * - yyyyMMdd
     * - yyyy-MM-dd HH:mm
     * - yyyy-MM-dd HH:mm:ss
     * - yyyy-MM-dd HH:mm:ss.SSS
     * - yyyy年MM月dd日
     * - yyyy/MM/dd
     * - dd.MM.yyyy
     * 5. Number - 解析为时间戳
     * 
     * @param o 要转换的对象,支持多种类型
     * @return 转换后的LocalDate对象,如果转换失败返回null
     */
    public static LocalDate parseDate(Object o) {
        if (o == null) {
            return null;
        }
        try {
            // 尝试解析LocalDate类型
            if (o instanceof LocalDate localDate) {
                return localDate;
            }
            // 尝试解析LocalDateTime类型
            if (o instanceof LocalDateTime localDateTime) {
                return localDateTime.toLocalDate();
            }
            // 尝试解析Date类型
            if (o instanceof Date date) {
                return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            // 尝试解析String类型
            if (o instanceof String dateStr) {
                // 尝试日期格式 yyyy-MM-dd
                if (dateStr.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
                    String[] parts = dateStr.split("-");
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);
                    if (isValidDate(year, month, day)) {
                        return LocalDate.of(year, month, day);
                    }
                    return null;
                }
                // 尝试日期格式 MM-dd
                if (dateStr.matches("\\d{1,2}-\\d{1,2}")) {
                    String[] parts = dateStr.split("-");
                    int month = Integer.parseInt(parts[0]);
                    int day = Integer.parseInt(parts[1]);
                    return LocalDate.of(LocalDate.now().getYear(), month, day);
                }
                // 尝试日期格式 yyyyMMdd
                if (dateStr.matches("\\d{4}\\d{2}\\d{2}")) {
                    int year = Integer.parseInt(dateStr.substring(0, 4));
                    int month = Integer.parseInt(dateStr.substring(4, 6));
                    int day = Integer.parseInt(dateStr.substring(6, 8));
                    if (isValidDate(year, month, day)) {
                        return LocalDate.of(year, month, day);
                    }
                    return null;
                }
                // 尝试日期格式 yyyy-MM-dd HH:mm
                if (dateStr.matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}")) {
                    String[] parts = dateStr.split(" ");
                    String[] dateParts = parts[0].split("-");
                    return LocalDate.of(
                            Integer.parseInt(dateParts[0]),
                            Integer.parseInt(dateParts[1]),
                            Integer.parseInt(dateParts[2]));
                }
                // 尝试日期格式 yyyy-MM-dd HH:mm:ss
                if (dateStr.matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}")) {
                    String[] parts = dateStr.split(" ");
                    String[] dateParts = parts[0].split("-");
                    return LocalDate.of(
                            Integer.parseInt(dateParts[0]),
                            Integer.parseInt(dateParts[1]),
                            Integer.parseInt(dateParts[2]));
                }
                // 尝试日期格式 yyyy-MM-dd HH:mm:ss.SSS
                if (dateStr.matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}")) {
                    String[] parts = dateStr.split(" ");

                    String[] dateParts = parts[0].split("-");
                    return LocalDate.of(
                            Integer.parseInt(dateParts[0]),
                            Integer.parseInt(dateParts[1]),
                            Integer.parseInt(dateParts[2]));
                }
                // 尝试中文日期格式 yyyy年MM月dd日
                if (dateStr.matches("\\d{4}年\\d{1,2}月\\d{1,2}日")) {
                    String[] parts = dateStr.split("[年月日]");
                    return LocalDate.of(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]));
                }
                // 尝试斜杠分隔格式 yyyy/MM/dd
                if (dateStr.matches("\\d{4}/\\d{1,2}/\\d{1,2}")) {
                    String[] parts = dateStr.split("/");
                    return LocalDate.of(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]));
                }
                // 尝试点分隔格式 dd.MM.yyyy
                if (dateStr.matches("\\d{1,2}\\.\\d{1,2}\\.\\d{4}")) {
                    String[] parts = dateStr.split("\\.");
                    return LocalDate.of(
                            Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[0]));
                }
            }
            // 尝试解析数值类型的timestamp
            if (o instanceof Number number) {
                Instant ofEpochMilli = Instant.ofEpochMilli(number.longValue());
                return LocalDate.ofInstant(ofEpochMilli, ZoneId.systemDefault());
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 将对象转换为LocalTime时间类型
     * 支持以下输入类型的转换:
     * 1. LocalTime - 直接返回
     * 2. LocalDateTime - 提取时间部分
     * 3. java.util.Date - 转换为LocalTime
     * 4. String - 支持多种时间字符串格式:
     * - HH:mm
     * - HH:mm:ss
     * - HH:mm:ss.SSS
     * 5. Number - 解析为时间戳
     * 
     * @param o 要转换的对象,支持多种类型
     * @return 转换后的LocalTime对象,如果转换失败返回null
     */
    public static LocalTime parseTime(Object o) {
        if (o == null) {
            return null;
        }
        try {
            // 尝试解析LocalTime类型
            if (o instanceof LocalTime localTime) {
                return localTime;
            }
            // 尝试解析LocalDateTime类型
            if (o instanceof LocalDateTime localDateTime) {
                return localDateTime.toLocalTime();
            }
            // 尝试解析Date类型
            if (o instanceof Date date) {
                return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            }
            // 尝试解析String类型
            if (o instanceof String dateStr) {
                // 尝试时间格式 HH:mm
                if (dateStr.matches("\\d{1,2}:\\d{1,2}")) {
                    String[] parts = dateStr.split(":");
                    int hour = Integer.parseInt(parts[0]);
                    int minute = Integer.parseInt(parts[1]);
                    if (!isValidTime(hour, minute, 0)) {
                        return null;
                    }
                    return LocalTime.of(hour, minute);
                }
                // 尝试时间格式 HH:mm:ss
                if (dateStr.matches("\\d{1,2}:\\d{1,2}:\\d{1,2}")) {
                    String[] parts = dateStr.split(":");
                    int hour = Integer.parseInt(parts[0]);
                    int minute = Integer.parseInt(parts[1]);
                    int second = Integer.parseInt(parts[2]);
                    if (!isValidTime(hour, minute, second)) {
                        return null;
                    }
                    return LocalTime.of(hour, minute, second);
                }
                // 尝试时间格式 HH:mm:ss.SSS
                if (dateStr.matches("\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}")) {
                    String[] parts = dateStr.split(":");
                    String[] msParts = parts[2].split("\\.");
                    int hour = Integer.parseInt(parts[0]);
                    int minute = Integer.parseInt(parts[1]);
                    int second = Integer.parseInt(msParts[0]);
                    if (!isValidTime(hour, minute, second)) {
                        return null;
                    }
                    int nanos = Integer.parseInt(msParts[1]) * 1_000_000;
                    return LocalTime.of(hour, minute, second, nanos);
                }
            }
            // 尝试解析数值类型的timestamp
            if (o instanceof Number number) {
                Instant ofEpochMilli = Instant.ofEpochMilli(number.longValue());
                return LocalTime.ofInstant(ofEpochMilli, ZoneId.systemDefault());
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 将对象转换为LocalDateTime日期类型
     * 支持以下输入类型的转换:
     * 1. LocalDate - 转换为LocalDateTime,时间部分为00:00
     * 2. LocalDateTime - 直接返回
     * 3. java.util.Date - 转换为LocalDateTime
     * 4. String - 支持多种日期字符串格式:
     * - yyyy-MM-dd (时间部分为00:00)
     * - yyyyMMdd (时间部分为00:00)
     * - yyyy-MM-dd HH:mm
     * - yyyy-MM-dd HH:mm:ss
     * - yyyy-MM-dd HH:mm:ss.SSS
     * - yyyy年MM月dd日 (时间部分为00:00)
     * - yyyy/MM/dd (时间部分为00:00)
     * - dd.MM.yyyy (时间部分为00:00)
     * 5. Number - 解析为时间戳对应的日期时间
     * 
     * @param o 要转换的对象,支持多种类型
     * @return 转换后的LocalDateTime对象,如果转换失败返回null
     */
    public static LocalDateTime parseDateTime(Object o) {
        if (o == null) {
            return null;
        }
        try {
            // 尝试解析LocalDate类型
            if (o instanceof LocalDate localDate) {
                return localDate.atStartOfDay();
            }
            // 尝试解析LocalDateTime类型
            if (o instanceof LocalDateTime localDateTime) {
                return localDateTime;
            }

            // 尝试解析Date类型
            if (o instanceof Date date) {
                return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            // 尝试解析String类型
            if (o instanceof String dateStr) {
                // 尝试日期格式 yyyy-MM-dd
                if (dateStr.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
                    String[] parts = dateStr.split("-");
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);
                    if (!isValidDate(year, month, day)) {
                        return null;
                    }
                    return LocalDateTime.of(year, month, day, 0, 0);
                }
                // 尝试日期格式 yyyyMMdd
                if (dateStr.matches("\\d{4}\\d{2}\\d{2}")) {
                    int year = Integer.parseInt(dateStr.substring(0, 4));
                    int month = Integer.parseInt(dateStr.substring(4, 6));
                    int day = Integer.parseInt(dateStr.substring(6, 8));
                    if (!isValidDate(year, month, day)) {
                        return null;
                    }
                    return LocalDateTime.of(year, month, day, 0, 0);
                }
                // 尝试日期格式 yyyy-MM-dd HH:mm
                if (dateStr.matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}")) {
                    String[] parts = dateStr.split(" ");
                    String[] dateParts = parts[0].split("-");
                    String[] timeParts = parts[1].split(":");
                    int year = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]);
                    int day = Integer.parseInt(dateParts[2]);
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);
                    if (!isValidDate(year, month, day) || !isValidTime(hour, minute, 0)) {
                        return null;
                    }
                    return LocalDateTime.of(year, month, day, hour, minute);
                }
                // 尝试日期格式 yyyy-MM-dd HH:mm:ss
                if (dateStr.matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}")) {
                    String[] parts = dateStr.split(" ");
                    String[] dateParts = parts[0].split("-");
                    String[] timeParts = parts[1].split(":");
                    int year = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]);
                    int day = Integer.parseInt(dateParts[2]);
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);
                    int second = Integer.parseInt(timeParts[2]);
                    if (!isValidDate(year, month, day) || !isValidTime(hour, minute, second)) {
                        return null;
                    }
                    return LocalDateTime.of(year, month, day, hour, minute, second);
                }
                // 尝试日期格式 yyyy-MM-dd HH:mm:ss.SSS
                if (dateStr.matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}")) {
                    String[] parts = dateStr.split(" ");
                    String[] dateParts = parts[0].split("-");
                    String[] timeParts = parts[1].split(":");
                    String[] msParts = timeParts[2].split("\\.");
                    int year = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]);
                    int day = Integer.parseInt(dateParts[2]);
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);
                    int second = Integer.parseInt(msParts[0]);
                    if (!isValidDate(year, month, day) || !isValidTime(hour, minute, second)) {
                        return null;
                    }
                    int nanos = Integer.parseInt(msParts[1]) * 1_000_000;
                    return LocalDateTime.of(year, month, day, hour, minute, second, nanos);
                }
                // 尝试中文日期格式 yyyy年MM月dd日
                if (dateStr.matches("\\d{4}年\\d{1,2}月\\d{1,2}日")) {
                    String[] parts = dateStr.split("[年月日]");
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);
                    if (!isValidDate(year, month, day)) {
                        return null;
                    }
                    return LocalDateTime.of(year, month, day, 0, 0);
                }
                // 尝试斜杠分隔格式 yyyy/MM/dd
                if (dateStr.matches("\\d{4}/\\d{1,2}/\\d{1,2}")) {
                    String[] parts = dateStr.split("/");
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);
                    if (!isValidDate(year, month, day)) {
                        return null;
                    }
                    return LocalDateTime.of(year, month, day, 0, 0);
                }
                // 尝试点分隔格式 dd.MM.yyyy
                if (dateStr.matches("\\d{1,2}\\.\\d{1,2}\\.\\d{4}")) {
                    String[] parts = dateStr.split("\\.");
                    int year = Integer.parseInt(parts[2]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[0]);
                    if (!isValidDate(year, month, day)) {
                        return null;
                    }
                    return LocalDateTime.of(year, month, day, 0, 0);
                }
            }
            // 尝试解析数值类型的timestamp
            if (o instanceof Number number) {
                Instant ofEpochMilli = Instant.ofEpochMilli(number.longValue());
                return LocalDateTime.ofInstant(ofEpochMilli, ZoneId.systemDefault());
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 验证日期是否有效
     * 检查年月日的取值是否在合法范围内:
     * 1. 月份范围: 1-12
     * 2. 日期范围: 1-31(根据月份调整)
     * 3. 特殊处理:
     * - 4,6,9,11月最多30天
     * - 2月闰年29天,平年28天
     * 
     * @param year  年份,用于判断闰年
     * @param month 月份(1-12)
     * @param day   日期(1-31)
     * @return 日期是否有效:
     *         - true: 日期合法
     *         - false: 日期非法
     */
    private static boolean isValidDate(int year, int month, int day) {
        if (month < 1 || month > 12) {
            return false;
        }
        if (day < 1 || day > 31) {
            return false;
        }

        return switch (month) {
            case 4, 6, 9, 11 -> day <= 30;
            case 2 -> {
                boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
                yield day <= (isLeapYear ? 29 : 28);
            }
            default -> day <= 31;
        };
    }

    /**
     * 验证时间是否有效
     * 检查小时、分钟和秒的取值是否在合法范围内:
     * 1. 小时范围: 0-23
     * 2. 分钟范围: 0-59
     * 3. 秒范围: 0-59
     * 
     * @param hour   小时(0-23)
     * @param minute 分钟(0-59)
     * @param second 秒(0-59)
     * @return 时间是否有效:
     *         - true: 时间合法
     *         - false: 时间非法
     */
    private static boolean isValidTime(int hour, int minute, int second) {
        return hour >= 0 && hour <= 23
                && minute >= 0 && minute <= 59
                && second >= 0 && second <= 59;
    }

}
