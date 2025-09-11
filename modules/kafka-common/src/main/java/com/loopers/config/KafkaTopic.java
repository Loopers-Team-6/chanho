package com.loopers.config;

// 토픽명 규칙
// {서비스명}.{도메인}.{이벤트타입}
public class KafkaTopic {

    public static class Catalog {

        public static final String PRODUCT_VIEWED = "commerce.product.viewed";
        public static final String LIKE_CHANGED = "commerce.like.changed";
        public static final String ORDER_COMPLETED = "commerce.order.completed";
    }
}
