package com.gym.management.util; // 请根据你的实际包名修改这里

import org.springframework.security.crypto.bcrypt.BCrypt;
import java.security.SecureRandom;

/**
 * 密码工具类
 * 负责：生成随机初始密码、密码加密(BCrypt)、密码校验
 */
public class PasswordUtil {

    // 定义随机字符集 (包含大小写字母和数字)
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    // 默认初始密码长度
    private static final int DEFAULT_PASSWORD_LENGTH = 8;

    /**
     * 1. 生成随机初始密码
     * 用于管理员后台创建用户时，或者重置密码时
     *
     * @return 随机生成的密码字符串
     */
    public static String generateRandomPassword() {
        return generateRandomPassword(DEFAULT_PASSWORD_LENGTH);
    }

    /**
     * 生成指定长度的随机密码
     *
     * @param length 密码长度
     * @return 随机生成的密码字符串
     */
    public static String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }

        return sb.toString();
    }

    /**
     * 2. 密码加密 (BCrypt)
     * 将明文密码转换为哈希值存入数据库
     *
     * @param rawPassword 明文密码
     * @return 加密后的哈希字符串
     */
    public static String encode(String rawPassword) {
        // BCrypt.hashpw(明文, 盐)
        // gensalt() 会自动生成随机盐，强度默认为 10 (范围 4-31，越大越慢但越安全)
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * 3. 密码校验
     * 用户登录时，比对输入的明文和数据库里的哈希值是否匹配
     *
     * @param rawPassword 用户输入的明文密码
     * @param encodedPassword 数据库中存储的哈希密码
     * @return true: 匹配成功; false: 匹配失败
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (IllegalArgumentException e) {
            // 防止数据库里的哈希格式错误导致程序崩溃
            return false;
        }
    }
}