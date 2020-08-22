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
    private static HTable table;
    private static Connection conn;
    private static Scan scan;
    private static Admin admin;
    private String familyName = "log";
    private static Logger logger = LoggerFactory.getLogger(HbaseSource.class);

    public HbaseTableSource() {
        super();
    }

    @Override
    protected Scan getScanner() {
        return scan;
    }

    @Override
    protected String getTableName() {
        return "rating";
    }

    @Override
    protected Tuple3<String, String, Double> mapResultToTuple(Result result) {
        ResultScanner rs = null;
        try {
            rs = table.getScanner(scan);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] userId = result.getValue(familyName.getBytes(), "userId".getBytes());
        byte[] productId = result.getValue(familyName.getBytes(), "productId".getBytes());
        byte[] score = result.getValue(familyName.getBytes(), "score".getBytes());
        Tuple3<String, String, Double> tuple3 = new Tuple3<>();
        if(userId != null && productId != null && score != null) {
            tuple3.setFields(new String(userId), new String(productId), Double.parseDouble(new String(score)));
            return tuple3;
        }
        return tuple3;
    }

    @Override
    public void configure(Configuration parameters) {

        try {
            table = createTable();
        } catch (IOException e) {
            e.printStackTrace();
        }
        scan = new Scan();
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
        admin = conn.getAdmin();
        return new HTable(conf, this.getTableName());
    }

    @Override
    protected Tuple3<String, String, Double> mapResultToOutType(Result r) {
        return super.mapResultToOutType(r);
    }

    @Override
    public void close() throws IOException {
        try {
            if(table != null) {
                table.close();
            }
            if(conn != null) {
                conn.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Close hbase exception: ", e.toString());
        }

    }
}
