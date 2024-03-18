# graphql-pagination-bug

This project tries to reproduce a bug with GraphQL pagination we are experiencing in our project using Spring Boot GraphQL and Mongodb.
Basically, using 'after' with a cursor of position N, yields a result-set starting from position N+2 upwards, skipping the 
document right after the cursor that should have been included.

A Query:
```graphql
query MyQuery {
  customers(storeNumber: "123", after: "T18x") {
    edges {
      cursor
      node {
        name
        storeNumber
      }
    }
  }
}
```

results in:

```graphql
{
  "data": {
    "customers": {
      "edges": [
        {
          "cursor": "T18z",
          "node": {
            "name": "Third",
            "storeNumber": "123"
          }
        },
        {
          "cursor": "T180",
          "node": {
            "name": "Fourth",
            "storeNumber": "123"
          }
        }
      ]
    }
  }
}
```

skipping the Second document with cursor 'T18y'.

# Findings

The source of the bug seems to be a mismatch between ScrollSubrange expectation of the offset behavior and the actual
behavior of the mongodb.

ScrollSubrange expects it to be inclusive, thus it advances by 1:


https://github.com/spring-projects/spring-graphql/blob/47b873af12c07f690e68d9072ebf50c882fa6359/spring-graphql/src/main/java/org/springframework/graphql/data/query/ScrollSubrange.java#L110
```java
// Offset is inclusive, adapt to exclusive:
//  - for forward, add 1 to return items after position
//  - for backward, subtract count to get items before position

		if (forward) {
			position = position.advanceBy(1);
		}
```

However, the spring data mongodb implementation skips the number of documents given by scroll position, thus it ends up skipping one extra document
See following lines:


https://github.com/spring-projects/spring-data-mongodb/blob/af6da0f67d8b91e656fa210a8d386b5cab59b1ed/spring-data-mongodb/src/main/java/org/springframework/data/mongodb/core/MongoTemplate.java#L912
```java
		return ScrollUtils.createWindow(result, query.getLimit(), OffsetScrollPosition.positionFunction(query.getSkip()));
```

which ends up being set to skip:

https://github.com/spring-projects/spring-data-mongodb/blob/af6da0f67d8b91e656fa210a8d386b5cab59b1ed/spring-data-mongodb/src/main/java/org/springframework/data/mongodb/core/MongoTemplate.java#L3380
```java 
        if (skip > 0) {
            cursorToUse = cursorToUse.skip((int) skip);
        }
```


# Test

A naïve test is provided in GraphQLScrollPaginationTest. It creates 3 documents, skips the first, and expects a result og 2
but gets only 1 document and fails.
