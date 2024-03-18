package com.example.graphqlscrollsubrangebug;

import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.graphql.data.GraphQlRepository;

import java.util.Map;
import java.util.Optional;

@GraphQlRepository
public class CustomerRepository {
    public static final String DOCUMENT_KEY = "_id";
    protected final MongoTemplate mongoTemplate;
    private final String mongoCollectionName = "customers";

    CustomerRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Window<Map<String, Object>> findByStoreNumber(String storeNumber, ScrollPosition scrollPosition, int limit) {
        Criteria criteria = Criteria.where("storeNumber").is(storeNumber);

        var query = Query.query(criteria)
                .limit(limit)
                .with(scrollPosition)
                .with(Sort.by(Sort.Direction.DESC, DOCUMENT_KEY));
        return (Window)mongoTemplate.scroll(query, Map.class, mongoCollectionName);
    }

    public Optional<Map<String, Object>> upsert(String id, Map<String, Object> jsonBody) {
        jsonBody.put(DOCUMENT_KEY, id);
        mongoTemplate.insert(jsonBody, this.mongoCollectionName);
        return getEntityById(id, true);
    }

    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> getEntityById(String id, boolean filterId) {
        if (filterId) {
            var query = Query.query(Criteria.where(DOCUMENT_KEY).is(id));
            query.fields().exclude(DOCUMENT_KEY);
            return Optional.ofNullable(mongoTemplate.findOne(query, Map.class, this.mongoCollectionName));
        } else {
            return Optional.ofNullable(mongoTemplate.findById(id, Map.class, this.mongoCollectionName));
        }
    }

}