package com.example.graphqlscrollsubrangebug;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.util.List;

@SpringBootApplication
public class GraphqlScrollSubrangeBugApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraphqlScrollSubrangeBugApplication.class, args);
	}

}



