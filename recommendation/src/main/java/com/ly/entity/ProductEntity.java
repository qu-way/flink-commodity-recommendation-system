package com.ly.entity;

public class ProductEntity {

    private Integer productId;
    private String name;
    private String imageUrl;
    private String categories;
    private String tags;

    /**
     * @param productId     3982,
     * @param name          "Fuhlen 富勒 M8眩光舞者时尚节能无线鼠标(草绿)(眩光.悦动.时尚炫舞鼠标 12个月免换电池 高精度光学寻迹引擎 超细微接收器10米传输距离)
     * @param imageUrl      "https://images-cn-4.ssl-images-amazon.com/images/I/31QPvUDNavL._SY300_QL70_.jpg"
     * @param categories    "外设产品|鼠标|电脑/办公"
     * @param tags          "富勒|鼠标|电子产品|好用|外观漂亮"
    * */
    public ProductEntity(Integer productId, String name, String imageUrl, String categories, String tags) {
        this.productId = productId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.categories = categories;
        this.tags = tags;
    }

    public ProductEntity() {
    }

    public Integer getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategories() {
        return categories;
    }

    public String getTags() {
        return tags;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "ProductEntity{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", categories='" + categories + '\'' +
                ", tags='" + tags + '\'' +
                '}';
    }
}
