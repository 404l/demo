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
        String certificate_document;
        @JsonProperty("certificate_document_date")
        Date certificate_document_date;
        @JsonProperty("certificate_document_number")
        String certificate_document_number;
        @JsonProperty("owner_inn")
        String owner_inn;
        @JsonProperty("producer_inn")
        String producer_inn;
        @JsonProperty("production_date")
        Date production_date;
        @JsonProperty("tnved_code")
        String tnved_code;
        @JsonProperty("uit_code")
        String uit_code;
        @JsonProperty("uitu_code")
        String uitu_code;

    }

    @Data
    @Getter
    private static class Doc {

        @JsonProperty("description")
        Object description;
        @JsonProperty("doc_id")
        String doc_id;
        @JsonProperty("doc_status")
        String doc_status;
        @JsonProperty("doc_type")
        String doc_type;
        @JsonProperty("importRequest")
        String importRequest;
        @JsonProperty("owner_inn")
        String owner_inn;
        @JsonProperty("participant_inn")
        String participant_inn;
        @JsonProperty("producer_inn")
        String producer_inn;
        @JsonProperty("production_date")
        Date production_date;
        @JsonProperty("production_type")
        String production_type;
        @JsonProperty("reg_date")
        Date reg_date;
        @JsonProperty("reg_number")
        String reg_number;
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
