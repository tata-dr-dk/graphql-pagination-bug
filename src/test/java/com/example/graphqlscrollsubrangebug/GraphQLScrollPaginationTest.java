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

        // first query all documents
        // language=GraphQL
        var queryAllDocument = """
                query MyQuery {
                   customers(storeNumber: "0001") {
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

        // ...then use the first document's cursor to make a filtered query
        var queryTemplate = """
                query MyQuery {
                   customers(storeNumber: "0001", after: "%s") {
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

        graphQlTester.document(queryAllDocument)
                .execute()
                .path("")
                .entity(Map.class)
                .satisfies(result -> {
                    var edges = (List)((Map)result.get("customers")).get("edges");
                    var firstCursor = ((Map)edges.get(0)).get("cursor");
                    graphQlTester.document(queryTemplate.formatted(firstCursor))
                            .execute()
                            .path("")
                            .entity(Map.class)
                            .satisfies(result2 -> {
                                var edges2 = (List)((Map)result2.get("customers")).get("edges");
                                assertThat((edges2).size()).isEqualTo(2);
                            });
                });

    }
}

