package com.loopers.domain.product;

import com.loopers.domain.order.OrderCommand;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductEntity findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("상품 ID는 null일 수 없습니다");
        }
        return productRepository.findById(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "해당 ID의 상품을 찾을 수 없습니다: " + id));
    }

    public Page<ProductEntity> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public List<ProductEntity> findAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("상품 ID 목록은 비어있을 수 없습니다");
        }
        return productRepository.findAllById(ids);
    }

    public Map<ProductEntity, Integer> getProductQuantities(List<OrderCommand.OrderItemDetail> items) {
        Map<Long, Integer> productIds = items.stream()
                .collect(Collectors.toMap(
                        OrderCommand.OrderItemDetail::productId,
                        OrderCommand.OrderItemDetail::quantity,
                        Integer::sum
                ));
        List<ProductEntity> productsToOrder = findAllById(productIds.keySet().stream().toList());

        return productsToOrder.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        product -> productIds.get(product.getId())
                ));
    }
}
