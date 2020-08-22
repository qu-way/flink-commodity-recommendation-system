package com.ly.task.OfflineRecommender;

import com.ly.client.HbaseClient;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.IOException;
import java.util.List;

public class StatisticsTask {
    /*
    * 分析历史热门，最近热门，历史好评商品
    * */
    public static void main(String[] args) {

    }

    // 历史热门
    public static void HistoryHotItem() throws IOException {
        List<String> allItems = HbaseClient.getAllKey("rating");
//        Map<String, >
    }

}
