package org.youxx.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.youxx.entity.Product;
import org.youxx.entity.ProductCategory;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ProductMapper {

    List<ProductCategory> selectAllCategories();

    List<Product> selectPage(
            @Param("keyword") String keyword,
            @Param("categoryId") String categoryId,
            @Param("status") String status,
            @Param("offset") int offset,
            @Param("size") int size);

    long count(
            @Param("keyword") String keyword,
            @Param("categoryId") String categoryId,
            @Param("status") String status);

    Product selectById(@Param("id") String id);

    List<Product> selectHot();

    int insert(Product product);

    int update(Product product);

    int updateStatus(@Param("id") String id, @Param("status") String status);

    int updateDiscount(@Param("id") String id, @Param("discount") BigDecimal discount);

    int deductStock(@Param("id") String id, @Param("quantity") int quantity);

    int deleteById(@Param("id") String id);
}