package com.ly.dataSource;

import com.ly.util.Property;
import org.apache.flink.addons.hbase.TableInputFormat;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;

//bbsmax.com/A/obzbMwK3dE/
public class HbaseTableSource extends TableInputFormat<Tuple3<String, String, Double>> {
//    private static HTable table;
    private static Connection conn;
//    private static Scan scan;
    private static Admin admin;
    private String familyName = "log";
    private static Logger logger = LoggerFactory.getLogger(HbaseSource.class);

    public HbaseTableSource() {
        super();
    }

    @Override
    protected Scan getScanner() {
        return this.scan;
    }

    @Override
    protected String getTableName() {
        return "rating";
    }

    @Override
    protected Tuple3<String, String, Double> mapResultToTuple(Result result) {
        byte[] userId = null;
        byte[] productId = null;
        byte[] score = null;
        Tuple3<String, String, Double> tuple3 = null;
       try {
           userId = result.getValue(familyName.getBytes(), "userId".getBytes());
           productId = result.getValue(familyName.getBytes(), "productId".getBytes());
           score = result.getValue(familyName.getBytes(), "score".getBytes());
       } catch (Exception e) {
           e.printStackTrace();
       }
       Double s = 0.0;
       try {
           if(score != null)
                s = Double.parseDouble(new String(score));
       } catch (Exception e) {
           System.out.println("===========================");
           System.out.println(userId);
           System.out.println(productId);
           System.out.println(score);
           System.out.println("===========================");
           e.printStackTrace();
       }
        if(userId != null && productId != null && score != null) {
            try {
                tuple3 = new Tuple3<>(new String(userId), new String(productId), s);
            } catch (Exception e){
                System.out.println("===========================");
                System.out.println(userId);
                System.out.println(productId);
                System.out.println(score);
                System.out.println("===========================");
                e.printStackTrace();
            }
        }
        return tuple3;
    }

    @Override
    public void configure(Configuration parameters) {

        try {
            this.table = createTable();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.scan = new Scan();
    }
    private HTable createTable() throws IOException {
        LOG.info("Initializing HBaseConfiguration");
        org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.rootdir", Property.getStrValue("hbase.rootdir"));
        conf.set("hbase.zookeeper.quorum", Property.getStrValue("hbase.zookeeper.quorum"));
        conf.set("hbase.client.scanner.timeout.period", Property.getStrValue("hbase.client.scanner.timeout.period"));
        conf.set("hbase.rpc.timeout", Property.getStrValue("hbase.rpc.timeout"));
        System.out.println(Property.getStrValue("hbase.rootdir"));
        conn = ConnectionFactory.createConnection(conf);
        return new HTable(conf, this.getTableName());
    }

    @Override
    protected Tuple3<String, String, Double> mapResultToOutType(Result r) {
        return this.mapResultToTuple(r);
    }

//    @Override
//    public void close() throws IOException {
//        try {
//            if(this.table != null) {
//                table.close();
//            }
//            if(this.conn != null) {
//                conn.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            logger.error("Close hbase exception: ", e.toString());
//        }
//
//    }
}
