# 시퀀스 다이어그램

1. [상품조회](#1-상품조회)
2. [주문생성](#2-주문생성)
3. [좋아요 등록](#3-좋아요-등록)
4. [좋아요 취소](#4-좋아요-취소)
5. [좋아요 목록 조회](#5-좋아요-목록-조회)

## 1. 상품조회

```mermaid
sequenceDiagram
    participant User as 사용자
    participant ProductController as 상품 컨트롤러
    participant ProductFacade as 상품 파사드
    participant ProductService as 상품 서비스
    participant ProductRepository as 상품 레포지토리
    participant LikeService as 좋아요 서비스
    User ->> ProductController: GET /api/v1/products
    ProductController ->> ProductFacade: findProducts(userId, pageable, filter)
    Note over ProductFacade: 상품 목록 조회
    ProductFacade ->> ProductService: findProducts(pageable, filter)
    ProductService ->> ProductRepository: findProducts(pageable, filter)
    ProductRepository -->> ProductService: Page<Product> (상품 목록 반환)
    ProductService -->> ProductFacade: Page<Product> (상품 목록)

    alt userId가 존재할 경우 (X-USER-ID)
        Note over ProductFacade: 조회된 상품들의 ID를 이용해 좋아요 정보 일괄 조회
        ProductFacade ->> LikeService: findLikedProductIds(userId, productIds)
        LikeService -->> ProductFacade: Set<Long> (좋아요한 상품 ID 목록)
        Note over ProductFacade: 조회된 상품 정보와 좋아요 정보를 조합
    end

    ProductFacade -->> ProductController: Page<ProductDto> (최종 데이터 반환)
    ProductController -->> User: 200 OK
```

## 2. 주문생성

```mermaid
sequenceDiagram
    participant User as 사용자
    participant OrderController as 주문 컨트롤러
    participant OrderFacade as 주문 파사드
    participant OrderService as 주문 서비스
    participant OrderRepository as 주문 레포지토리
    participant PaymentService as 결제 서비스
    User ->> OrderController: POST /api/v1/orders
    OrderController ->> OrderFacade: placeOrder(orderRequest)
    OrderFacade ->> OrderService: saveOrder(orderRequest)
    Note over OrderService: Order 생성 및 저장 <br> (상태: PENDING)
    OrderService ->> OrderRepository: save(order)
    OrderRepository -->> OrderService: Order (저장된 주문 정보)
    Note over OrderService: 결제 요청
    OrderService ->> PaymentService: createPayment(order)
    PaymentService -->> OrderService: 결제 결과 반환
    alt 결제 성공
        Note over OrderService: Order 상태 변경 <br> (COMPLETED)
    else 결제 실패
        Note over OrderService: Order 상태 변경 <br> (FAILED)
    end
    Note over OrderService: 변경된 Order 정보 저장
    OrderService ->> OrderRepository: save(order)
    OrderRepository -->> OrderService: Order (저장된 주문 정보)
    OrderService -->> OrderFacade: OrderResult
    OrderFacade -->> OrderController: OrderDto (주문 결과)
    OrderController -->> User: 201 Created
```

## 3. 좋아요 등록

```mermaid
sequenceDiagram
    participant User as 사용자
    participant LikeController as 좋아요 컨트롤러
    participant LikeService as 좋아요 서비스
    participant LikeRepository as 좋아요 레포지토리
    User ->> LikeController: POST /api/v1/products/{productId}/likes
    LikeController ->> LikeService: addLike(userId, productId)
    Note over LikeService: 기존 좋아요 기록 조회 (삭제된 것도 포함)
    LikeService ->> LikeRepository: findByUserIdAndProductId(userId, productId)
    LikeRepository -->> LikeService: Optional<Like>

    alt 좋아요 기록이 있고, 삭제된 상태일 경우 (Undelete)
        Note over LikeService: 삭제된 좋아요 기록을 복원
        LikeService ->> LikeService: like.restore() (deleted_at = null)
        LikeService ->> LikeRepository: save(like)
    else 좋아요 기록이 아예 없을 경우 (Create New)
        Note over LikeService: 새로운 좋아요 기록 생성
        LikeService ->> LikeRepository: save(new Like(userId, productId))
    else 이미 활성화 상태일 경우
        Note over LikeService: 아무 작업도 수행하지 않음 (멱등성)
    end

    LikeService -->> LikeController: SuccessResponse
    LikeController -->> User: 201 Created or 200 OK
```

## 4. 좋아요 취소

```mermaid
sequenceDiagram
    participant User as 사용자
    participant LikeController as 좋아요 컨트롤러
    participant LikeService as 좋아요 서비스
    participant LikeRepository as 좋아요 레포지토리
    User ->> LikeController: DELETE /api/v1/products/{productId}/likes
    LikeController ->> LikeService: removeLike(userId, productId)
    Note over LikeService: 활성화된 좋아요를 찾아 soft delete 처리
    LikeService ->> LikeRepository: findByUserIdAndProductId(userId, productId)
    LikeRepository -->> LikeService: Optional<Like>
    alt 좋아요 기록이 존재할 경우
        Note over LikeService: 좋아요 기록을 soft delete 처리
        LikeService ->> LikeService: like.delete() (deleted_at = 현재 시간)
        LikeService ->> LikeRepository: save(like)
    else 좋아요 기록이 없는 경우
        Note over LikeService: 아무 작업도 수행하지 않음 (멱등성)
    end

    LikeService -->> LikeController: SuccessResponse
    LikeController -->> User: 204 No Content
```

## 5. 좋아요 누른 상품 목록 조회

```mermaid
sequenceDiagram
    participant User as 사용자
    participant UserController as 사용자 컨트롤러
    participant UserFacade as 사용자 파사드
    participant LikeService as 좋아요 서비스
    participant ProductService as 상품 서비스
    User ->> UserController: GET /api/v1/users/me/likes
    Note over UserController: X-USER-ID 헤더로 사용자 식별
    UserController ->> UserFacade: findLikedProducts(userId, pageable)
    Note over UserFacade: 사용자가 좋아요한 상품 ID 목록 조회
    UserFacade ->> LikeService: findLikedProductIds(userId, pageable)
    LikeService -->> UserFacade: Page<Long> (좋아요한 상품 ID 목록)
    Note over UserFacade: 상품 ID 목록을 이용해 상품 정보 조회
    UserFacade -->> ProductService: findProductsByIds(productIds, pageable)
    ProductService -->> UserFacade: Page<Product>
    UserFacade -->> UserController: Page<ProductDto>
    UserController -->> User: 200 OK
```
