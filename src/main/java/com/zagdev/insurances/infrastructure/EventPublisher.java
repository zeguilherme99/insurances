package com.zagdev.insurances.infrastructure;

import com.zagdev.insurances.domain.event.PolicyEvent;

public interface EventPublisher {
    void publish(PolicyEvent event);
}
