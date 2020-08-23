package com.ly.map;

import com.ly.client.HbaseClient;
import com.ly.entity.RecommendEntity;
import com.ly.entity.RecommendReduceEntity;
import org.apache.flink.api.common.functions.MapFunction;

import java.util.ArrayList;

public class RecommendEntityMapFunction implements MapFunction<RecommendReduceEntity, RecommendReduceEntity> {
    @Override
    public RecommendReduceEntity map(RecommendReduceEntity r) throws Exception {
        String rowKey = r.getProductId();
        for(RecommendEntity recommendEntity : r.getList()) {
            String column = recommendEntity.getProductId();
            Double sim = recommendEntity.getSim();
            String value = String.valueOf(sim);
            // 写入 hbase
            HbaseClient.putData("itemCFRecommend", rowKey, "p", column, value);
        }
        return r;
    }
}
