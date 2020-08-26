package com.ly.task.OfflineRecommender;

import com.ly.client.HbaseClient;
import com.ly.dataSource.HbaseTableSource;
import com.ly.entity.RecommendEntity;
import com.ly.entity.RecommendReduceEntity;
import com.ly.map.CalculateSimilarityMapFunction;
import com.ly.map.RecommendEntityMapFunction;
import com.ly.util.ReduceRecommend;
import org.apache.flink.api.common.functions.*;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.aggregation.AggregationFunction;
import org.apache.flink.api.java.aggregation.Aggregations;
import org.apache.flink.api.java.operators.AggregateOperator;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.BatchTableEnvironment;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.util.Collector;

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
        BatchTableEnvironment bbTableEnv = BatchTableEnvironment.create(env);
        // userId|productId|score
        DataSet<Tuple3<String, String, Double>> dataSet =  env.createInput(new HbaseTableSource()).map(new MapFunction<Tuple4<String, String, Double, String>, Tuple3<String, String, Double>>() {
            @Override
            public Tuple3<String, String, Double> map(Tuple4<String, String, Double, String> s) throws Exception {
                return new Tuple3<String, String, Double>(s.f0, s.f1, s.f2);
            }
        });
        // userId|productId|score|count
        DataSet<Tuple4<String, String, Double, Integer>> productCount = dataSet.flatMap(new FlatMapFunction<Tuple3<String, String, Double>, Tuple4<String, String, Double, Integer>>() {
            @Override
            public void flatMap(Tuple3<String, String, Double> s, Collector<Tuple4<String, String, Double, Integer>> collector) throws Exception {
                collector.collect(new Tuple4<>(s.f0, s.f1, s.f2, 1));
            }
        }).groupBy(1).sum(3);
        // join
        // userId|productId|score|count
        DataSet<Tuple5<String, String, Integer, String, Integer>> joinedByUserId = productCount.join(productCount).where(0).equalTo(0).flatMap(new FlatMapFunction<Tuple2<Tuple4<String, String, Double, Integer>, Tuple4<String, String, Double, Integer>>, Tuple5<String, String, Integer, String, Integer>>() {
            @Override
            public void flatMap(Tuple2<Tuple4<String, String, Double, Integer>, Tuple4<String, String, Double, Integer>> t, Collector<Tuple5<String, String, Integer, String, Integer>> collector) throws Exception {
                collector.collect(new Tuple5<String, String, Integer, String, Integer>(t.f0.f0, t.f0.f1, t.f0.f3, t.f1.f1, t.f1.f3));
            }
        });
//        joinedByUserId.print();
        // 创建表
//        EnvironmentSettings bbSettings = EnvironmentSettings.newInstance().useBlinkPlanner().inBatchMode().build();
//        TableEnvironment bbTableEnv = TableEnvironment.create(bbSettings);
        Table table = bbTableEnv.fromDataSet(joinedByUserId).renameColumns("f0 as userId, f1 as product1, f2 as count1, f3 as product2, f4 as count2");
        bbTableEnv.registerTable("joined", table);
//
//        Table table3 = table.groupBy("product1, product2").select("product1, product2, count(userId) as cocount, AVG(count1) as count1, AVG(count2) as count2");
        Table table3 = bbTableEnv.sqlQuery(
                "SELECT product1, product2 ,COUNT(userId) as cocount, AVG(count1) as count1, AVG(count2) as count2 FROM  joined group by product1, product2"
        );
        ScalarFunction calSim = new CalculateSimilarityMapFunction();
        bbTableEnv.registerFunction("calSim", calSim);
        Table table4 = table3.map("calSim(product1, product2, cocount, count1, count2)").as("product1, product2, sim").filter("product1 != product2");
        TupleTypeInfo tupleType = new TupleTypeInfo<Tuple3<String, String, Double>>(Types.STRING, Types.STRING, Types.DOUBLE);
        DataSet<Tuple3<String, String, Double>> dsRow = bbTableEnv.toDataSet(table4, tupleType);
        DataSet<RecommendReduceEntity> newRow = dsRow.map(new MapFunction<Tuple3<String, String, Double>, RecommendReduceEntity>() {
            @Override
            public RecommendReduceEntity map(Tuple3<String, String, Double> s) throws Exception {
                ArrayList<RecommendEntity> tmp = new ArrayList<>();
                tmp.add(new RecommendEntity(s.f1, s.f2));
                return new RecommendReduceEntity(s.f0, tmp);
            }
        }).groupBy("productId").reduce(new ReduceRecommend()).map(new RecommendEntityMapFunction());
        newRow.print();
        env.execute();


    }
    public static void main(String[] args) throws Exception {
        calSimilarityUsingFlink();
    }

}
