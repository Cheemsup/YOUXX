package org.youxx.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.youxx.common.result.PageResult;
import org.youxx.entity.Product;
import org.youxx.entity.ProductCategory;
import org.youxx.mapper.ProductMapper;
import org.youxx.service.ProductService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    @Cacheable(value = "product:categories")
    public List<ProductCategory> listCategories() {
        return productMapper.selectAllCategories();
    }

    @Override
    public PageResult<Product> listProducts(String keyword, String categoryId, String status, int page, int size) {
        long total = productMapper.count(keyword, categoryId, status);
        int offset = (page - 1) * size;
        List<Product> records = productMapper.selectPage(keyword, categoryId, status, offset, size);
        return PageResult.of(records, total, page, size);
    }

    @Override
    @Cacheable(value = "product:detail", key = "#id")
    public Product getProduct(String id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在: " + id);
        }
        return product;
    }

    @Override
    @Cacheable(value = "product:hot")
    public List<Product> listHotProducts() {
        return productMapper.selectHot();
    }

    @Override
    public List<Product> listOnShelfProducts() {
        // Agent 知识库注入用：一次性取在售商品。复用 selectPage 走 status=ONSHELF 查询，
        // size 取较大值容纳全量；商品规模不大，无需分批。
        PageResult<Product> page = listProducts(null, null, "ONSHELF", 1, 1000);
        return page.getRecords();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "product:categories", allEntries = true),
            @CacheEvict(value = "product:hot", allEntries = true),
            @CacheEvict(value = "product:detail", allEntries = true)
    })
    public Product addProduct(Product product) {
        if (product.getCreateTime() == null) {
            product.setCreateTime(LocalDateTime.now());
        }
        if (product.getUpdateTime() == null) {
            product.setUpdateTime(LocalDateTime.now());
        }
        if (product.getStatus() == null) {
            product.setStatus("ONSHELF");
        }
        if (product.getStock() == null) {
            product.setStock(999);
        }
        if (product.getDiscount() == null) {
            product.setDiscount(BigDecimal.ONE);
        }
        if (product.getIsHot() == null) {
            product.setIsHot(false);
        }
        productMapper.insert(product);
        log.info("商品已添加: id={}, name={}", product.getId(), product.getName());
        return product;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "product:categories", allEntries = true),
            @CacheEvict(value = "product:hot", allEntries = true),
            @CacheEvict(value = "product:detail", key = "#id")
    })
    public Product updateProduct(String id, Product product) {
        getProduct(id);
        product.setId(id);
        productMapper.update(product);
        log.info("商品已更新: id={}", id);
        return getProduct(id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "product:categories", allEntries = true),
            @CacheEvict(value = "product:hot", allEntries = true),
            @CacheEvict(value = "product:detail", key = "#id")
    })
    public void updateStatus(String id, String status) {
        getProduct(id);
        productMapper.updateStatus(id, status);
        log.info("商品状态已更新: id={}, status={}", id, status);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product:categories", allEntries = true),
            @CacheEvict(value = "product:hot", allEntries = true),
            @CacheEvict(value = "product:detail", allEntries = true)
    })
    public void updateBatchStatus(List<String> ids, String status) {
        // 多商品状态更新属于多写操作：加事务保证全成全败，避免部分成功导致批量状态不一致
        if (ids == null || ids.isEmpty()) {
            return;
        }
        productMapper.updateBatchStatus(ids, status);
        log.info("商品批量状态更新: ids={}, status={}", ids, status);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "product:categories", allEntries = true),
            @CacheEvict(value = "product:hot", allEntries = true),
            @CacheEvict(value = "product:detail", key = "#id")
    })
    public void updateDiscount(String id, BigDecimal discount) {
        getProduct(id);
        productMapper.updateDiscount(id, discount);
        log.info("商品折扣已更新: id={}, discount={}", id, discount);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "product:categories", allEntries = true),
            @CacheEvict(value = "product:hot", allEntries = true),
            @CacheEvict(value = "product:detail", key = "#id")
    })
    public void deleteProduct(String id) {
        getProduct(id);
        productMapper.deleteById(id);
        log.info("商品已删除: id={}", id);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product:detail", key = "#id"),
            @CacheEvict(value = "product:hot", allEntries = true)
    })
    public void deductStock(String id, int quantity) {
        int affected = productMapper.deductStock(id, quantity);
        if (affected == 0) {
            throw new IllegalArgumentException("库存不足或商品不存在: " + id);
        }
    }

    @Override
    public String uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = UUID.randomUUID().toString() + extension;

        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "products";

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());

            log.info("商品图片已上传: {}", filePath);
            return "/uploads/products/" + filename;
        } catch (IOException e) {
            log.error("图片上传失败", e);
            throw new RuntimeException("图片上传失败: " + e.getMessage());
        }
    }
}
