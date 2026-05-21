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

    Product addProduct(Product product);

    Product updateProduct(String id, Product product);

    void updateStatus(String id, String status);

    void updateBatchStatus(List<String> ids, String status);

    void updateDiscount(String id, BigDecimal discount);

    void deleteProduct(String id);

    void deductStock(String id, int quantity);

    String uploadImage(MultipartFile file);
}