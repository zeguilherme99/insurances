package com.zagdev.insurances.infrastructure;

import com.zagdev.insurances.domain.event.PolicyEvent;
import com.zagdev.insurances.domain.exceptions.InvalidDataException;
import com.zagdev.insurances.domain.exceptions.UnexpectedErrorException;

public interface EventPublisher {
    void publish(PolicyEvent event) throws InvalidDataException, UnexpectedErrorException;
}
