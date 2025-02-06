package com.tangyujun.datashadow.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据影子许可证管理类
 * 负责生成机器码、验证许可证等功能
 * 
 * 主要功能:
 * 1. 生成基于硬件信息的机器码
 * 2. 验证许可证有效性
 * 3. 格式化机器码显示
 * 
 * 机器码生成原理:
 * 1. 获取CPU、主板、硬盘、BIOS等硬件序列号
 * 2. 将所有序列号组合并进行MD5加密
 * 3. 生成32位的唯一标识符
 * 
 * 使用场景:
 * - 软件授权验证
 * - 防止非法复制
 * - 限制软件使用范围
 */
public class DataShadowLicense {

    /**
     * 日志记录器
     * 用于记录许可证验证和硬件信息获取过程中的异常信息
     */
    private static final Logger log = LoggerFactory.getLogger(DataShadowLicense.class);

    /**
     * 验证许可证是否有效
     * 通过比对输入的许可证与当前机器生成的机器码是否一致来验证
     * 
     * 验证步骤:
     * 1. 获取当前机器的机器码
     * 2. 与输入的许可证进行比对
     * 3. 完全匹配则视为有效许可
     * 
     * @param license 待验证的许可证字符串
     * @return 如果许可证与机器码匹配则返回true,否则返回false
     */
    public static boolean isLicenseValid(String license) {
        String machineCode = getMachineCode();
        return Objects.equals(license, machineCode);
    }

    /**
     * 生成机器码
     * 通过组合多个硬件信息(CPU、主板、硬盘、BIOS)并进行MD5加密生成唯一标识
     * 
     * 生成步骤:
     * 1. 获取各种硬件序列号
     * 2. 使用冒号拼接所有序列号
     * 3. 对组合信息进行MD5加密
     * 4. 生成32位的唯一标识符
     *
     * @return 32位的机器码字符串,如果获取硬件信息失败会包含Unknown前缀
     */
    public static String getMachineCode() {
        List<String> hardwareInfo = new ArrayList<>();

        // 获取CPU序列号
        hardwareInfo.add(getCPUSerial());
        // 获取主板序列号
        hardwareInfo.add(getMotherboardSerial());
        // 获取硬盘序列号
        hardwareInfo.add(getHardDiskSerial());
        // 获取BIOS序列号
        hardwareInfo.add(getBIOSSerial());

        // 组合所有硬件信息,使用冒号分隔
        String combinedInfo = String.join(":", hardwareInfo);

        // 使用MD5加密生成最终的机器码
        return getMD5(combinedInfo);
    }

    /**
     * 获取CPU序列号
     * 通过执行wmic命令获取处理器ID
     * 
     * 获取步骤:
     * 1. 执行wmic cpu get ProcessorId命令
     * 2. 读取命令输出
     * 3. 提取处理器ID
     * 
     * @return CPU序列号,如果获取失败返回"Unknown-CPU"
     */
    private static String getCPUSerial() {
        try {
            Process process = Runtime.getRuntime().exec(
                    new String[] { "wmic", "cpu", "get", "ProcessorId" });
            process.getOutputStream().close();

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.equalsIgnoreCase("ProcessorId")) {
                        return line;
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to get CPU serial", e);
        }
        return "Unknown-CPU";
    }

    /**
     * 获取主板序列号
     * 通过执行wmic命令获取主板序列号
     * 
     * 获取步骤:
     * 1. 执行wmic baseboard get SerialNumber命令
     * 2. 读取命令输出
     * 3. 提取序列号
     * 
     * @return 主板序列号,如果获取失败返回"Unknown-MB"
     */
    private static String getMotherboardSerial() {
        try {
            Process process = Runtime.getRuntime().exec(
                    new String[] { "wmic", "baseboard", "get", "SerialNumber" });
            process.getOutputStream().close();

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.equalsIgnoreCase("SerialNumber")) {
                        return line;
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to get motherboard serial", e);
        }
        return "Unknown-MB";
    }

    /**
     * 获取硬盘序列号
     * 通过执行wmic命令获取硬盘序列号
     * 
     * 获取步骤:
     * 1. 执行wmic diskdrive get SerialNumber命令
     * 2. 读取命令输出
     * 3. 提取序列号
     * 
     * @return 硬盘序列号,如果获取失败返回"Unknown-HDD"
     */
    private static String getHardDiskSerial() {
        try {
            Process process = Runtime.getRuntime().exec(
                    new String[] { "wmic", "diskdrive", "get", "SerialNumber" });
            process.getOutputStream().close();

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.equalsIgnoreCase("SerialNumber")) {
                        return line;
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to get hard disk serial", e);
        }
        return "Unknown-HDD";
    }

    /**
     * 获取BIOS序列号
     * 通过执行wmic命令获取BIOS序列号
     * 
     * 获取步骤:
     * 1. 执行wmic bios get SerialNumber命令
     * 2. 读取命令输出
     * 3. 提取序列号
     * 
     * @return BIOS序列号,如果获取失败返回"Unknown-BIOS"
     */
    private static String getBIOSSerial() {
        try {
            Process process = Runtime.getRuntime().exec(
                    new String[] { "wmic", "bios", "get", "SerialNumber" });
            process.getOutputStream().close();

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.equalsIgnoreCase("SerialNumber")) {
                        return line;
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to get BIOS serial", e);
        }
        return "Unknown-BIOS";
    }

    /**
     * 对字符串进行MD5加密
     * 将输入字符串转换为32位的MD5哈希值
     * 
     * 加密步骤:
     * 1. 获取MD5算法实例
     * 2. 将输入字符串转换为字节数组
     * 3. 计算MD5哈希值
     * 4. 将字节数组转换为16进制字符串
     * 
     * @param input 需要加密的字符串
     * @return 32位MD5哈希值,如果加密失败返回"ERROR-MD5"
     */
    private static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());

            // 将字节数组转换为16进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 algorithm not found", e);
            return "ERROR-MD5";
        }
    }

    /**
     * 格式化机器码显示
     * 将32位字符串格式化为8-4-4-4-12的形式,便于阅读和使用
     * 
     * 格式化步骤:
     * 1. 验证输入长度是否为32位
     * 2. 使用正则表达式分组匹配
     * 3. 按照8-4-4-4-12格式重组字符串
     * 
     * 格式示例:
     * 输入: 12345678123412341234123456789012
     * 输出: 12345678-1234-1234-1234-123456789012
     *
     * @param machineCode 原始32位机器码
     * @return 格式化后的机器码,如果输入无效则返回原始机器码
     */
    public static String formatMachineCode(String machineCode) {
        if (machineCode == null || machineCode.length() != 32) {
            return machineCode;
        }
        Pattern pattern = Pattern.compile("(.{8})(.{4})(.{4})(.{4})(.{12})");
        Matcher matcher = pattern.matcher(machineCode);
        if (matcher.matches()) {
            return String.format("%s-%s-%s-%s-%s",
                    matcher.group(1),
                    matcher.group(2),
                    matcher.group(3),
                    matcher.group(4),
                    matcher.group(5));
        }
        return machineCode;
    }
}
