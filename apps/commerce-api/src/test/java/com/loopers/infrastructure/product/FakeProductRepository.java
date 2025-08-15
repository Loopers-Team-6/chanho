package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.infrastructure.InMemoryCrudRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FakeProductRepository extends InMemoryCrudRepository<ProductEntity> implements ProductRepository {

    @Override
    public Page<ProductEntity> findAll(Pageable pageable) {
        List<ProductEntity> allProducts = new ArrayList<>(map.values());

        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            Comparator<ProductEntity> comparator = Comparator.comparing(ProductEntity::getId);

            Sort.Order sortOrder = sort.get().findFirst().orElse(null);
            if (sortOrder != null && sortOrder.isDescending()) {
                comparator = comparator.reversed();
            }
            allProducts.sort(comparator);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allProducts.size());

        List<ProductEntity> pageContent = start >= allProducts.size()
                ? List.of()
                : allProducts.subList(start, end);

        return new PageImpl<>(pageContent, pageable, allProducts.size());
    }

    @Override
    public Optional<ProductEntity> findByIdWithPessimisticLock(Long id) {
        return map.values().stream()
                .filter(product -> product.getId().equals(id))
                .findFirst();
    }
}
