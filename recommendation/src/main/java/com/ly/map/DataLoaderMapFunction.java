package com.ly.map;

import com.ly.client.MysqlClient;
import com.ly.entity.ProductEntity;
import org.apache.flink.api.common.functions.MapFunction;

public class DataLoaderMapFunction implements MapFunction<ProductEntity, ProductEntity> {
    @Override
    public ProductEntity map(ProductEntity productEntity) throws Exception {
        System.out.println(productEntity);
        if(productEntity != null) {
            MysqlClient.putData(productEntity);
        }
        return productEntity;
    }
}

