package com.ly.task.OfflineRecommender;

import com.ly.dataSource.HbaseTableSource;
import com.ly.sink.HbaseSink;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.BatchTableEnvironment;

import javax.xml.crypto.Data;
import java.text.SimpleDateFormat;
import java.util.Date;


public class StatisticsTask {
    private static ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
    private static BatchTableEnvironment benv = BatchTableEnvironment.create(env);
    /*
    * 分析历史热门，最近热门，历史好评商品
    * */
    public static void main(String[] args) throws Exception {
        env = ExecutionEnvironment.getExecutionEnvironment();
        DataSet dataSet = env.createInput(new HbaseTableSource());
        Table table = benv.fromDataSet(dataSet, "f0, f1, f2, f3").renameColumns("f0 as userId, f1 as productId, f2 as score, f3 as timestamp");
        // 注册表为 “rating”
        benv.registerTable("rating", table);
        historyHotProducts();
//        recentHotProducts(dataSet);
        goodProducts();
        env.execute();
    }

    // 历史热门
    public static void historyHotProducts() throws Exception {
        // productId|hot|rank

        // ROW_NUMBER() 只能在 blink planner 中使用因此我们采用别的办法，直接根据 productId 存储数据，但是限制数据数量。
        String sql2 = " SELECT * , ROW_NUMBER() OVER (PARTITION BY productId ORDER BY hot DESC) as rowNumber " +
        "FROM (SELECT productId, COUNT(productId) as hot FROM rating " +
                "GROUP BY productId ORDER BY hot DESC)";
        // 只保存前 100 热门数据
        String sql = "SELECT productId, COUNT(productId) as hot FROM rating GROUP BY productId ORDER BY hot DESC LIMIT 100";

        Table table1 = benv.sqlQuery(sql);
        TupleTypeInfo tupleType = new TupleTypeInfo<Tuple2<String, Long>>(Types.STRING, Types.LONG);
        // table sink -> hbase 'historyHotProducts'
        DataSet result = benv.toDataSet(table1, tupleType);
        result.print();
        result.output(new HbaseSink("historyHotProducts"));
    }

    // 近期热门
    public static void recentHotProducts(DataSet<Tuple4<String, String, Double, String>> dataSet) throws Exception {
        // 转换时间格式
        DataSet source = dataSet.map(new MapFunction<Tuple4<String, String, Double, String>, Tuple3<String, Double, String>>() {
            @Override
            public Tuple3<String, Double, String> map(Tuple4<String, String, Double, String> s) throws Exception {
                SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
                String time = format.format(new Date(Long.parseLong(s.f3) * 1000L));
                return new Tuple3<String, Double, String>(s.f1, s.f2, time);
            }
        });
        // 转换成 Table
        Table table = benv.fromDataSet(source).renameColumns("f0 as productId, f1 as score, f2 as yearmonth");
        benv.registerTable("recentTable", table);
        Table table2 = benv.sqlQuery("select productId, count(productId) as hot, yearmonth " +
                "from recentTable group by yearmonth, productId order by yearmonth desc, hot desc");
        TupleTypeInfo tupleType = new TupleTypeInfo<Tuple2<String, Long>>(Types.STRING, Types.LONG, Types.STRING);
        // table sink -> hbase 'historyHotProducts'
        DataSet result = benv.toDataSet(table2, tupleType);
//        result.print();
        result.output(new HbaseSink("recentHotProducts"));
    }

    public static void goodProducts() throws Exception {
        Table table = benv.sqlQuery("select productId, avg(score) as avgScore from rating group by productId order by avgScore desc limit 100");
        TupleTypeInfo tupleType = new TupleTypeInfo<Tuple2<String, Double>>(Types.STRING, Types.DOUBLE);
        // table sink -> hbase 'goodProducts'
        DataSet result = benv.toDataSet(table, tupleType);
//        result.print();
        result.output(new HbaseSink("goodProducts"));
    }
}
