package com.ly.recommend_backend.dao;

import com.ly.recommend_backend.entity.Product;
import com.ly.recommend_backend.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductInterface extends JpaRepository<ProductEntity, Integer>, JpaSpecificationExecutor<ProductEntity> {
    ProductEntity getProductByProductId(Integer productid);
//    List<ProductEntity> getProductEntitiesByName(String name);

    @Query("select product from ProductEntity product where product.name like CONCAT('%', :name, '%')")
    List<ProductEntity> findByNameLike(@Param("name") String name);
}
