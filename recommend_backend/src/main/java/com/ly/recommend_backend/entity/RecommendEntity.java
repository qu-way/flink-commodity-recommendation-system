package com.ly.recommend_backend.entity;

public class RecommendEntity {
    private int productId;
    private double score;

    public RecommendEntity() {
    }

    public RecommendEntity(int productId, Double score) {
        this.productId = productId;
        this.score = score;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "RecommendEntity{" +
                "productId=" + productId +
                ", score=" + score +
                '}';
    }
}
