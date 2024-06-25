package com.example.demo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v3/lk/documents")
public class CrptApi {

    private final ConcurrentHashMap<String, List<Long>> requestCounts = new ConcurrentHashMap<>();

    @Value("${APP_RATE_LIMIT:#{5}}")
    private int rateLimit;

    @Value("${APP_RATE_DURATIONINMS:#{60000}}")
    private long rateDuration;

    @Data
    @Getter
    private static class Product {

        @JsonProperty("certificate_document")
        String certificateDocument;
        @JsonProperty("certificate_document_date")
        Date certificateDocumentDate;
        @JsonProperty("certificate_document_number")
        String certificateDocumentNumber;
        @JsonProperty("owner_inn")
        String ownerInn;
        @JsonProperty("producer_inn")
        String producerInn;
        @JsonProperty("production_date")
        Date productionDate;
        @JsonProperty("tnved_code")
        String tnvedCode;
        @JsonProperty("uit_code")
        String uitCode;
        @JsonProperty("uitu_code")
        String uituCode;

    }

    @Data
    @Getter
    private static class Doc {

        @JsonProperty("description")
        Object description;
        @JsonProperty("doc_id")
        String docId;
        @JsonProperty("doc_status")
        String docStatus;
        @JsonProperty("doc_type")
        String docType;
        @JsonProperty("importRequest")
        String importRequest;
        @JsonProperty("owner_inn")
        String ownerInn;
        @JsonProperty("participant_inn")
        String participantInn;
        @JsonProperty("producer_inn")
        String producerInn;
        @JsonProperty("production_date")
        Date productionDate;
        @JsonProperty("production_type")
        String productionType;
        @JsonProperty("reg_date")
        Date regDate;
        @JsonProperty("reg_number")
        String regNumber;
        @JsonProperty("products")
        List<Product> products;
    }

    @PostMapping("/create")
    String createDocument(@RequestBody Doc doc) {
        rateLimit();
        return rateLimit() ? null : getNewDocument(doc.toString(), doc.getProducts());
    }

    /**
     * Метод создания документа
     */
    String getNewDocument(String signature, List<Product> products) {
        return signature;
    }

    public boolean rateLimit() {
        final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        final String key = requestAttributes.getRequest().getRemoteAddr();
        final long currentTime = System.currentTimeMillis();
        requestCounts.putIfAbsent(key, new ArrayList<>());
        requestCounts.get(key).add(currentTime);
        cleanUpRequestCounts(currentTime);
        return requestCounts.get(key).size() > rateLimit;
    }

    private void cleanUpRequestCounts(final long currentTime) {
        requestCounts.values().forEach(l -> l.removeIf(t -> timeIsTooOld(currentTime, t)));
    }

    private boolean timeIsTooOld(final long currentTime, final long timeToCheck) {
        return currentTime - timeToCheck > rateDuration;
    }

}
