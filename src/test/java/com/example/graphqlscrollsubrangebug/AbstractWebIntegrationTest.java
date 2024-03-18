package com.example.graphqlscrollsubrangebug;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MongoDBContainer;

import java.util.HashMap;
import java.util.Map;

@WebAppConfiguration
@SpringBootTest
public abstract class AbstractWebIntegrationTest {
    static MongoDBContainer mongoDBContainer;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected MongoTemplate mongoTemplate;

    protected MockMvc mockMvc;

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
        cleanup();
    }

    protected abstract void cleanup();

    @SuppressWarnings("unchecked")
    protected Map<String, Object> convertToMap(final String jsonContent) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonContent, HashMap.class);
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeAll
    public static void startContainerAndPublicPortIsAvailable() {
        mongoDBContainer = new MongoDBContainer("mongo:5.0.3");
        mongoDBContainer.start();
    }

}
