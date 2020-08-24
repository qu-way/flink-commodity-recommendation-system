package com.ly.task.OfflineRecommender;

import com.ly.dataSource.HbaseTableSource;
import com.ly.util.Property;
import com.sun.org.apache.bcel.internal.generic.BasicType;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.jdbc.JDBCInputFormat;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.typeutils.RowTypeInfo;



public class ContentRecommender {
    public static void contentRecommend() throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        // mysql datasource
        TypeInformation[] fieldTypes = new TypeInformation[] {
                BasicTypeInfo.INT_TYPE_INFO, BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.STRING_TYPE_INFO
        };
        RowTypeInfo rowTypeInfo = new RowTypeInfo(fieldTypes);
        JDBCInputFormat jdbcInputFormat = JDBCInputFormat.buildJDBCInputFormat()
                .setDrivername(Property.getStrValue("mysql.driver"))
                .setDBUrl(Property.getStrValue("mysql.url"))
                .setUsername(Property.getStrValue("mysql.username"))
                .setPassword(Property.getStrValue("mysql.password"))
                .setQuery("select productId, name, tags from product")
                .setRowTypeInfo(rowTypeInfo)
                .finish();
        DataSet<Tuple3<Integer, String, String>> dataSet = env.createInput(jdbcInputFormat);
        System.out.println("hello");
        dataSet.print();
//        dataSet.map(new MapFunction<Row, Tuple3<Integer, String, String>>() {
//            @Override
//            public Tuple3<Integer, String, String> map(Row row) throws Exception {
//                row.
//            }
//        })
        env.execute();
    }
    public static void main(String[] args) {

    }
}
