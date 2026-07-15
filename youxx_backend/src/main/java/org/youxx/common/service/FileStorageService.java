package org.youxx.common.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务：统一收口图片上传的"校验 + 压缩 + 落盘 + 返回可访问路径"流程，
 * 消除 UserServiceImpl / ProductServiceImpl 中重复的落盘代码。
 */
public interface FileStorageService {

    /**
     * 存储(并压缩)上传图片到 upload_resources/{subDir}/ 下。
     *
     * @param file    上传的图片文件
     * @param subDir  落盘子目录(如 user_icon / products)
     * @param maxEdge 压缩目标最长边(像素)，头像 256 / 商品图 800
     * @return 对外可访问的资源路径，如 /upload_resources/user_icon/xxx.jpg
     */
    String storeImage(MultipartFile file, String subDir, int maxEdge);
}
