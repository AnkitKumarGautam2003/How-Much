package com.howmuch.service;

import com.howmuch.domain.IdempotencyRecord;
import com.howmuch.exception.ApiException;
import com.howmuch.repository.IdempotencyRecordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class IdempotencyService {

    private final IdempotencyRecordRepository repository;

    public IdempotencyService(IdempotencyRecordRepository repository) {
        this.repository = repository;
    }

    public Optional<IdempotencyRecord> findByKey(String key) {
        return repository.findByIdempotencyKey(key);
    }

    @Transactional
    public void save(String key, String requestHash, int status, String responseBody) {
        repository.findByIdempotencyKey(key).ifPresent(existing -> {
            throw new ApiException(HttpStatus.CONFLICT, "Idempotency key already used");
        });
        IdempotencyRecord record = new IdempotencyRecord();
        record.setIdempotencyKey(key);
        record.setRequestHash(requestHash);
        record.setResponseStatus(status);
        record.setResponseBody(responseBody);
        repository.save(record);
    }
}
