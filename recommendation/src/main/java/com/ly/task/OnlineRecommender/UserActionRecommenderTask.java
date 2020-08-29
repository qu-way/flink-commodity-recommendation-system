package com.ly.task.OnlineRecommender;

import com.ly.client.HbaseClient;
import com.ly.map.OnlineRecommendMapFunction;
import com.ly.util.Property;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;

public class UserActionRecommenderTask {
    /**
     * 基于用户评分行为，对用户进行实时推荐
     */

    public static void userActionRecommender() throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties properties = Property.getKafkaProperties("rating");
        DataStream<String> dataStream = env.addSource(new FlinkKafkaConsumer<String>("rating", new SimpleStringSchema(), properties));
        dataStream.map(new MapFunction<String, String>() {
            @Override
            public String map(String s) throws Exception {
                String[] tmp = s.split(",");
                String rowkey = tmp[0] + "_" + tmp[1] + "_" + tmp[3];
                System.out.println(rowkey);
                // record user rate info
                //  String msg = userId + "," + productId + "," + score + "," + System.currentTimeMillis() / 1000;
                HbaseClient.putData("rating", rowkey, "log", "productId", tmp[1]);
                HbaseClient.putData("rating", rowkey, "log", "userId", tmp[0]);
                HbaseClient.putData("rating", rowkey, "log", "score", tmp[2]);
                HbaseClient.putData("rating", rowkey, "log", "timestamp", tmp[3]);
                // record user-product info
                HbaseClient.increamColumn("userProduct", tmp[0], "product", tmp[1]);
                // record product-user info 这个表暂时用不着，不入库
//                HbaseClient.increamColumn("productUser", tmp[1], "user", tmp[0]);
                return s;
            }
        }).map(new OnlineRecommendMapFunction()).print();
        env.execute();
    }
    public static void main(String[] args) throws Exception {
        userActionRecommender();
    }

}
