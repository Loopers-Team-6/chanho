package com.loopers.domain.payment;

import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Set;

@RequiredArgsConstructor
public enum PaymentStatus {
    CREATED,
    PENDING,
    SUCCESS,
    FAILED,
    CANCELED;

    private Set<PaymentStatus> allowedTransitions;

    static {
        CREATED.allowedTransitions = Set.of(PENDING, SUCCESS, FAILED);
        PENDING.allowedTransitions = Set.of(SUCCESS, FAILED, CANCELED);
        SUCCESS.allowedTransitions = Set.of();
        FAILED.allowedTransitions = Set.of();
        CANCELED.allowedTransitions = Set.of();
    }

    public boolean canTransitionTo(PaymentStatus newStatus) {
        return allowedTransitions.contains(newStatus);
    }

    public Set<PaymentStatus> getAllowedTransitions() {
        return Collections.unmodifiableSet(allowedTransitions);
    }

    public boolean isFinalState() {
        return allowedTransitions.isEmpty();
    }

}
