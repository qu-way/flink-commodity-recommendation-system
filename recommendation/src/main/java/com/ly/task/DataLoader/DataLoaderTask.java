package com.ly.task.DataLoader;


import com.ly.entity.ProductEntity;
import com.ly.entity.RatingEntity;
import com.ly.map.DataLoaderMapFunction;
import com.ly.map.DataLoaderMapFunction2;
import com.ly.map.DataToHbaseMapFunction;
import com.ly.util.Property;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;

public class DataLoaderTask {
    public static void main(String[] args) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        String productFilePath = Property.getStrValue("product.data.path");
        String ratingFilePath = Property.getStrValue("rating.data.path");
        /*
        * fieldDelimiter(",")表示分隔符
        * ignoreFirstLine()表示是否忽略第一行
        * includeFields(true,false,true)表示不需要第二列
        * pojoType(ProductEntity.class, "productId", "name", "imageUrl", "categories", "tags")ProductEntity，使用后面这5个属性
        * */
//        DataSet<ProductEntity> source= env.readCsvFile(productFilePath)
//                .fieldDelimiter("^")
//                .includeFields("1100111")
//                .pojoType(ProductEntity.class, "productId", "name", "imageUrl", "categories", "tags")
//                .map(new DataLoaderMapFunction());
        DataSet<RatingEntity> source2 = env.readCsvFile(ratingFilePath)
                .fieldDelimiter(",")
                .pojoType(RatingEntity.class, "userId", "productId", "score", "timestamp")
                .map(new DataToHbaseMapFunction());
//        source.print();
        source2.print();
        env.execute("Load Data");
    }


}
