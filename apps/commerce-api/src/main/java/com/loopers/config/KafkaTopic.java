package com.loopers.config;

// 토픽명 규칙
// {서비스명}.{도메인}.{이벤트타입}.v{버전}
public class KafkaTopic {

    public static class Like {
        public static final String LIKE_CHANGED = "commerce.like.changed.v1";
    }
}
