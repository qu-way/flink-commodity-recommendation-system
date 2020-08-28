package com.ly.map;

import com.ly.client.HbaseClient;
import com.ly.entity.RatingEntity;
import org.apache.flink.api.common.functions.MapFunction;

public class DataToHbaseMapFunction implements MapFunction<RatingEntity, RatingEntity> {
    public RatingEntity map(RatingEntity ratingEntity) throws Exception {
        if(ratingEntity != null) {
            String rowkey =ratingEntity.getProductId() + "_" + ratingEntity.getUserId() + "_" + ratingEntity.getTimestamp();
            System.out.println(rowkey);
            // record user rate info
            HbaseClient.putData("rating", rowkey, "log", "productId", String.valueOf(ratingEntity.getProductId()));
            HbaseClient.putData("rating", rowkey, "log", "userId", String.valueOf(ratingEntity.getUserId()));
            HbaseClient.putData("rating", rowkey, "log", "score", String.valueOf(ratingEntity.getScore()));
            HbaseClient.putData("rating", rowkey, "log", "timestamp", String.valueOf(ratingEntity.getTimestamp()));
            // record user-product info
            HbaseClient.increamColumn("userProduct", String.valueOf(ratingEntity.getUserId()), "product", String.valueOf(ratingEntity.getProductId()));
            // record product-user info
            HbaseClient.increamColumn("productUser", String.valueOf(ratingEntity.getProductId()), "user", String.valueOf(ratingEntity.getUserId()));
        }
        return ratingEntity;
    }
}
