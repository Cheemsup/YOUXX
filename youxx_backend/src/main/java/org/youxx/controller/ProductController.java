package org.youxx.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.youxx.common.result.PageResult;
import org.youxx.common.result.Result;
import org.youxx.dto.UpdateBatchStatusRequest;
import org.youxx.dto.UpdateDiscountRequest;
import org.youxx.dto.UpdateStatusRequest;
import org.youxx.entity.Product;
import org.youxx.entity.ProductCategory;
import org.youxx.service.ProductService;
import org.youxx.vo.UploadVO;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/category/list")
    public Result<List<ProductCategory>> listCategories() {
        List<ProductCategory> categories = productService.listCategories();
        return Result.success(categories);
    }

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

    @PostMapping
    public Result<Product> add(@RequestBody Product product) {
        // 管理员新增商品：字段较多，保留 entity 入参
        Product created = productService.addProduct(product);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable String id, @RequestBody Product product) {
        // 管理员编辑商品：同上，保留 entity 入参
        Product updated = productService.updateProduct(id, product);
        return Result.success(updated);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable String id, @RequestBody UpdateStatusRequest request) {
        productService.updateStatus(id, request.getStatus());
        return Result.success();
    }

    @PutMapping("/batch/status")
    public Result<Void> updateBatchStatus(@RequestBody UpdateBatchStatusRequest request) {
        productService.updateBatchStatus(request.getIds(), request.getStatus());
        return Result.success();
    }

    @PutMapping("/{id}/discount")
    public Result<Void> updateDiscount(@PathVariable String id, @RequestBody UpdateDiscountRequest request) {
        productService.updateDiscount(id, request.getDiscount());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        productService.deleteProduct(id);
        return Result.success();
    }

    @PostMapping("/upload")
    public Result<UploadVO> upload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("categoryId") String categoryId) {
        String url = productService.uploadImage(file, categoryId);
        UploadVO vo = new UploadVO();
        vo.setUrl(url);
        return Result.success(vo);
    }
}
