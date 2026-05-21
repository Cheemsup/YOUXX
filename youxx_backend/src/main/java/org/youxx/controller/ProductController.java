package org.youxx.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.youxx.common.result.PageResult;
import org.youxx.common.result.Result;
import org.youxx.entity.Product;
import org.youxx.entity.ProductCategory;
import org.youxx.service.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ==================== 分类 ====================

    @GetMapping("/category/list")
    public Result<List<ProductCategory>> listCategories() {
        List<ProductCategory> categories = productService.listCategories();
        return Result.success(categories);
    }

    // ==================== 商品查询 ====================

    @GetMapping("/list")
    public Result<PageResult<Product>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Product> result = productService.listProducts(keyword, categoryId, status, page, size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable String id) {
        Product product = productService.getProduct(id);
        return Result.success(product);
    }

    @GetMapping("/hot")
    public Result<List<Product>> hot() {
        List<Product> products = productService.listHotProducts();
        return Result.success(products);
    }

    // ==================== 商品管理（管理员） ====================

    @PostMapping
    public Result<Product> add(@RequestBody Product product) {
        Product created = productService.addProduct(product);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable String id, @RequestBody Product product) {
        Product updated = productService.updateProduct(id, product);
        return Result.success(updated);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        productService.updateStatus(id, status);
        return Result.success();
    }

    @PutMapping("/batch/status")
    public Result<Void> updateBatchStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) body.get("ids");
        String status = (String) body.get("status");
        productService.updateBatchStatus(ids, status);
        return Result.success();
    }

    @PutMapping("/{id}/discount")
    public Result<Void> updateDiscount(@PathVariable String id, @RequestBody Map<String, BigDecimal> body) {
        BigDecimal discount = body.get("discount");
        productService.updateDiscount(id, discount);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        productService.deleteProduct(id);
        return Result.success();
    }

    // ==================== 图片上传 ====================

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = productService.uploadImage(file);
        return Result.success(Map.of("url", url));
    }
}