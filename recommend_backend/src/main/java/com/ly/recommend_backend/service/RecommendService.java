package com.ly.recommend_backend.service;


import com.ly.recommend_backend.dao.ProductInterface;
import com.ly.recommend_backend.util.HbaseClient;
import com.ly.recommend_backend.entity.ProductEntity;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class RecommendService {
    private Logger logger = LoggerFactory.getLogger(RecommendService.class);


    public List<ProductEntity> getHistoryHotOrGoodProducts(int num, String tableName) throws IOException {
        List<ProductEntity> recommendEntityList = new ArrayList<>(num);
        // 查出所有热门商品 list
        List<String> allProductsId = HbaseClient.getAllKey(tableName);
        List<Pair<String, Double>> list = new ArrayList<>();
        for(String productId : allProductsId) {
            List<Map.Entry> row = HbaseClient.getRow(tableName, productId);
            if(row != null) {
                double count = (double) row.get(0).getValue();
                list.add(new Pair<String, Double>(productId, count));
            }
        }
        // 排序
        Collections.sort(list, new Comparator<Pair<String, Double>>() {
            @Override
            public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        for(int i = 1; i <= num; i++) {
            ProductEntity product = getProductEntity(Integer.parseInt(list.get(i).getKey()));
            product.setScore(3.5);
            System.out.println(product);
            recommendEntityList.add(product);
        }
        return recommendEntityList;
    }

    @Autowired
    private ProductInterface productInterface;

    public ProductEntity getProductEntity(int productId) {
        ProductEntity productEntity = productInterface.getProductByProductId(productId);
        System.out.println(productEntity);
        return productEntity;
    }

    public List<ProductEntity> getItemCFProducts(int productId, String tableName) throws IOException {
        // 查询 hbase 获取 itemCFRecommend 表对应内容
        List<ProductEntity> result = new ArrayList<>();
        List<Map.Entry> allProducts = HbaseClient.getRow(tableName, String.valueOf(productId));
        for(Map.Entry entry : allProducts) {
            String id = (String) entry.getKey();
            double sim = (double) entry.getValue();
            ProductEntity product = getProductEntity(Integer.parseInt(id));
            product.setScore(sim);
            result.add(product);
        }
        return result;
    }

    public List<ProductEntity> getProductBySql(String sql) {
        return productInterface.findByNameLike(sql);
    }

    public List<ProductEntity> getOnlineRecs(String userId, String tableName) throws IOException {
        List<Map.Entry> allProducts = HbaseClient.getRow(tableName, userId);
        List<ProductEntity> res = new ArrayList<>(allProducts.size());
        for (Map.Entry entry : allProducts) {
            String productId = (String) entry.getKey();
            ProductEntity productEntity = productInterface.getProductByProductId(Integer.parseInt(productId));
            productEntity.setScore(3.5);
            System.out.println("onlineRecs: " + productEntity);
            res.add(productEntity);
        }
        return res;
    }

    public List<ProductEntity> getOnlineHot(String tableName, int nums){
        List<String> allKeys = null;
        try {
             allKeys = HbaseClient.getAllKey(tableName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(allKeys);
        List<ProductEntity> res = new ArrayList<>(nums);
        try {
            for(int i = 0; i < nums && i < allKeys.size(); i++) {
                List<Map.Entry> row = HbaseClient.getRow(tableName, String.valueOf(i));
                Double productId = null;
                Double count = null;
                for(Map.Entry entry : row) {
                    if(entry.getKey().equals("productId")) {
                        productId = (Double) entry.getValue();
                    }
                    if(entry.getKey().equals("count")) {
                        count = (Double) entry.getValue();
                    }
                }
                ProductEntity productEntity = productInterface.getProductByProductId((int)(double)productId);
                productEntity.setScore(i+1);
                res.add(productEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }



}
