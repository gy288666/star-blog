package com.blog.util;

import com.blog.common.exception.BizException;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/** 图形验证码：Java2D 自绘 4 位数字字母 + 干扰线，PNG dataURL 输出。 */
public final class CaptchaUtil {

    private CaptchaUtil() {
    }

    private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();

    /** key + image(dataURL) + code，调用方负责把 code 存入 KVCache（TTL 5 分钟）。 */
    public record Captcha(String key, String image, String code) {
    }

    /** 生成 4 位数字字母验证码。 */
    public static String randomCode() {
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /** 完整生成一个验证码。 */
    public static Captcha generate() {
        String key = UUID.randomUUID().toString().replace("-", "");
        String code = randomCode();
        return new Captcha(key, renderPngDataUrl(code), code);
    }

    /** 渲染 PNG 并转 dataURL。 */
    public static String renderPngDataUrl(String code) {
        int w = 130, h = 44;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(245, 245, 245));
        g.fillRect(0, 0, w, h);
        // 干扰线
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(RANDOM.nextInt(200), RANDOM.nextInt(200), RANDOM.nextInt(200)));
            g.setStroke(new BasicStroke(1.2f));
            g.drawLine(RANDOM.nextInt(w), RANDOM.nextInt(h), RANDOM.nextInt(w), RANDOM.nextInt(h));
        }
        // 逐字符绘制并轻微旋转
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(RANDOM.nextInt(120), RANDOM.nextInt(120), RANDOM.nextInt(150) + 50));
            g.setFont(new Font("Arial", Font.BOLD, 26 + RANDOM.nextInt(8)));
            double theta = (RANDOM.nextDouble() - 0.5) * 0.5;
            int x = 12 + i * 28, y = 32;
            g.rotate(theta, x, y);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            g.rotate(-theta, x, y);
        }
        // 噪点
        for (int i = 0; i < 30; i++) {
            img.setRGB(RANDOM.nextInt(w), RANDOM.nextInt(h),
                    new Color(RANDOM.nextInt(255), RANDOM.nextInt(255), RANDOM.nextInt(255)).getRGB());
        }
        g.dispose();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(img, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new BizException(500, "验证码生成失败");
        }
    }
}
