package com.example.graphqlscrollsubrangebug;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.AutoConfigureGraphQl;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@Import(CustomerRepository.class)
@AutoConfigureGraphQl
@AutoConfigureGraphQlTester
class GraphQLScrollPaginationTest extends AbstractWebIntegrationTest {

    protected void cleanup() {

    }

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void ShouldFindProduction() {
        var storeNumber = "0001";

        var mutationDocument = """                
                mutation MyMutations {
                  addCustomer(name: "%s", storeNumber: "0001") {
                    name
                    storeNumber
                  }
                }
                """;
        graphQlTester.document(mutationDocument.formatted("First"))
                .execute()
                .path("")
                .entity(Customer.class);
        graphQlTester.document(mutationDocument.formatted("Second"))
                .execute()
                .path("")
                .entity(Customer.class);
        graphQlTester.document(mutationDocument.formatted("Third"))
                .execute()
                .path("")
                .entity(Customer.class);

        // language=GraphQL
        var document = """
                query MyQuery {
                   customers(storeNumber: "0001", after: "T18x") {
                     edges {
                           cursor
                           node {
                             storeNumber
                             name
                           }
                         }
                     }
                 }
            """;
        graphQlTester.document(document)
                .execute()
                .path("")
                .entity(Map.class)
                .satisfies(result -> {
                    var edges = (List)((Map)result.get("customers")).get("edges");
                    assertThat((edges).size()).isEqualTo(2);
                });
    }
}

