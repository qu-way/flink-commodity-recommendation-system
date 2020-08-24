package com.ly.task.OnlineRecommender;

import com.ly.map.OnlineRecommendMapFunction;
import com.ly.util.Property;
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
        dataStream.map(new OnlineRecommendMapFunction()).print();
        env.execute();
    }
    public static void main(String[] args) throws Exception {
        userActionRecommender();
    }

}
