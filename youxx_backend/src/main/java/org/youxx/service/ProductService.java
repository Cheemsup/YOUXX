package org.youxx.service;

import org.springframework.web.multipart.MultipartFile;
import org.youxx.common.result.PageResult;
import org.youxx.entity.Product;
import org.youxx.entity.ProductCategory;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    List<ProductCategory> listCategories();

    PageResult<Product> listProducts(String keyword, String categoryId, String status, int page, int size);

    Product getProduct(String id);

    List<Product> listHotProducts();

    /**
     * 查询全部在售商品（status=ONSHELF），用于 Agent system prompt 注入商品知识库。
     * 走分页 selectPage 取较大 size 一次拉取，命中既有查询逻辑。
     */
    List<Product> listOnShelfProducts();

    Product addProduct(Product product);

    Product updateProduct(String id, Product product);

    void updateStatus(String id, String status);

    void updateBatchStatus(List<String> ids, String status);

    void updateDiscount(String id, BigDecimal discount);

    void deleteProduct(String id);

    void deductStock(String id, int quantity);

    /**
     * 上传商品图片：按商品分类落盘到 upload_resources/products/{categoryId}/ 下。
     *
     * @param file       上传的图片
     * @param categoryId 商品分类 ID，决定图片落盘的二级子目录；为空则拒绝上传
     * @return 对外可访问的资源路径，如 /upload_resources/products/drinks/xxx.jpg
     */
    String uploadImage(MultipartFile file, String categoryId);
}