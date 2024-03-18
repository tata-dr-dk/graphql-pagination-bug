package com.example.graphqlscrollsubrangebug;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Controller
class StoreController {

    private final AtomicLong nextId = new AtomicLong(0);
    protected final ObjectMapper mapper;

    private final CustomerRepository customerRepository;

    StoreController(ObjectMapper mapper, CustomerRepository customerRepository) {
        this.mapper = mapper;
        this.customerRepository = customerRepository;
    }

    @MutationMapping
    public Customer addCustomer(@Argument String name, @Argument String storeNumber) {
        Map<String, Object> converted = new HashMap<>();
        converted.put("name", name);
        converted.put("storeNumber", storeNumber);
        Optional<Map<String, Object>> customerMap = customerRepository.upsert(nextId.incrementAndGet()+"", converted);
        return customerMap.map(this::toCustomer).get();
    }

    @QueryMapping
    public Window<Customer> customers(@Argument String storeNumber, ScrollSubrange subrange) {
        ScrollPosition scrollPosition = subrange.position().orElse(ScrollPosition.offset());
        return customerRepository.findByStoreNumber(storeNumber, scrollPosition, 10)
                .map(this::toCustomer)  ;
    }

    private Customer toCustomer(Map<String, Object> customerMessage) {
        return mapper.convertValue(customerMessage, Customer.class);
    }
}