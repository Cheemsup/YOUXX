package org.youxx.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * 图片处理工具类：基于 JDK ImageIO 实现等比缩放 + JPEG 质量压缩，无外部依赖。
 * <p>
 * 统一用于头像、商品图片等上传场景，落盘前压缩，控制磁盘占用与前端加载体积。
 */
@Slf4j
public class ImageUtils {

    private ImageUtils() {
    }

    /**
     * 判断上传文件是否为可解析的图片。
     * 通过 ImageIO 能否成功读取判定，可识别伪装扩展名的非图片文件。
     */
    public static boolean isImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        try (InputStream is = file.getInputStream()) {
            BufferedImage img = ImageIO.read(is);
            return img != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 读取原图并等比缩放至最长边不超过 maxEdge（小图不放大），输出 JPEG。
     *
     * @param file    原始上传文件
     * @param maxEdge 目标最长边（像素），头像 256 / 商品图 800
     * @param quality JPEG 压缩质量 0~1，推荐 0.8
     * @return 压缩后的 JPEG 字节数组
     */
    public static byte[] compress(MultipartFile file, int maxEdge, float quality) {
        BufferedImage src;
        try (InputStream is = file.getInputStream()) {
            src = ImageIO.read(is);
        } catch (IOException e) {
            throw new RuntimeException("读取上传图片失败: " + e.getMessage(), e);
        }
        if (src == null) {
            throw new IllegalArgumentException("无法解析的图片格式");
        }

        int w = src.getWidth();
        int h = src.getHeight();
        // 等比缩放：仅当最长边超过 maxEdge 时才缩小，小图保持原尺寸避免放大失真
        int maxSrc = Math.max(w, h);
        int targetW = w;
        int targetH = h;
        if (maxSrc > maxEdge) {
            double scale = (double) maxEdge / maxSrc;
            targetW = (int) Math.round(w * scale);
            targetH = (int) Math.round(h * scale);
        }

        // 统一重绘为 TYPE_INT_RGB（白底），避免 PNG/GIF 的 alpha 通道在 JPEG 编码时变红或报错
        BufferedImage dest = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dest.createGraphics();
        try {
            g.setComposite(AlphaComposite.SrcOver);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, targetW, targetH);
            // 双线性插值，缩放质量较好
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src, 0, 0, targetW, targetH, null);
        } finally {
            g.dispose();
        }

        return writeJpeg(dest, quality);
    }

    /**
     * 以指定质量将 BufferedImage 编码为 JPEG 字节数组。
     */
    private static byte[] writeJpeg(BufferedImage image, float quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("当前环境无 JPEG ImageWriter");
        }
        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }
            writer.write(null, new IIOImage(image, null, null), param);
        } catch (IOException e) {
            throw new RuntimeException("JPEG 编码失败: " + e.getMessage(), e);
        } finally {
            writer.dispose();
        }
        return baos.toByteArray();
    }
}
