package com.ly.task.OfflineRecommender;

import com.ly.client.HbaseClient;
import com.ly.dataSource.HbaseSource;
import com.ly.dataSource.HbaseTableSource;
import javafx.scene.control.Tab;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.bridge.java.BatchTableEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemCFTask {
    // 未使用 flink 的方法，该方法内部使用了 Map，如果数据量巨大内存会溢出。
    public static void itemSimilarity() throws Exception {
        List<String> allUser = new ArrayList<>();
        try {
            allUser = HbaseClient.getAllKey("userProduct");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Integer> similarityMap= new HashMap<>();
        Map<String, Integer> productUserCountMap = new HashMap<>();

        for(String user : allUser) {
            List<Map.Entry> products = HbaseClient.getRow("userProduct", user);
            for(Map.Entry product : products) {
                productUserCountMap.put( (String) product.getKey(), productUserCountMap.getOrDefault(product.getKey(), 0) + 1);
                for(Map.Entry product2 : products) {
                    if(product.getKey() == product2.getKey()) continue;
                    String key = product.getKey() + "_" + product2.getKey();
                    similarityMap.put(key, similarityMap.getOrDefault(key, 0) + 1);
                }
            }
        }
        for(String key : similarityMap.keySet()) {
            String[] products = key.split("_");
            String product1 = products[0];
            String product2 = products[1];
            Double similarity = similarityMap.get(key)/Math.sqrt(productUserCountMap.get(product1) * productUserCountMap.get(product2));
            // 保留五位小数
            String res = String.format("%.5f", similarity);
            System.out.println(product1 + "\t" + product2 + "\t" + res);
            HbaseClient.putData("similarity", product1, "p", product2, res);
        }
    }

    public static void calSimilarityUsingFlink() throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
//        DataStreamSource<Tuple3<String, String, Double>> dataStream =  env.addSource(new HbaseSource());
        DataSet<Tuple3<String, String, Double>> dataSet =  env.createInput(new HbaseTableSource());
        dataSet.print();
//        EnvironmentSettings esettings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
//        BatchTableEnvironment tenv = BatchTableEnvironment.create(env);
//        Table  table1 = tenv.crea;
//        tenv.to
//        dataStream.print();
        env.execute();


    }


    public static void main(String[] args) throws Exception {
        calSimilarityUsingFlink();
    }


}
