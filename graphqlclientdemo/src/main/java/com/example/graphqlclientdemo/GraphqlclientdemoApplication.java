package com.example.graphqlclientdemo;

import java.util.List;
import java.util.stream.Collectors;

import com.example.graphqlclientdemo.generated.client.ArtifactRepositoriesGraphQLQuery;
import com.example.graphqlclientdemo.generated.client.ArtifactRepositoriesProjectionRoot;
import com.example.graphqlclientdemo.generated.client.GreetingGraphQLQuery;
import com.example.graphqlclientdemo.generated.types.ArtifactRepository;
import com.jayway.jsonpath.TypeRef;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@SpringBootApplication
public class GraphqlclientdemoApplication implements CommandLineRunner {

	private final static Logger log = LoggerFactory.getLogger(GraphqlclientdemoApplication.class);

	@Override
	public void run(String... args) throws Exception {
		WebClient webClient = WebClient.create("http://localhost:8080/graphql");
		WebClientGraphQLClient client = MonoGraphQLClient.createWithWebClient(webClient);

		greeting(client);
		artifactRepositories(client);
	}

	private void greeting(WebClientGraphQLClient client) {
		GreetingGraphQLQuery query = GreetingGraphQLQuery.newRequest().build();
		GraphQLQueryRequest request = new GraphQLQueryRequest(query);
		Mono<GraphQLResponse> response = client.reactiveExecuteQuery(request.serialize());
		log.info("Request {}", request.serialize());
		GraphQLResponse block = response.block();
		log.info("Result {}", block);
	}

	private void artifactRepositories(WebClientGraphQLClient client) {
		ArtifactRepositoriesGraphQLQuery query = ArtifactRepositoriesGraphQLQuery.newRequest().build();
		ArtifactRepositoriesProjectionRoot projection = new ArtifactRepositoriesProjectionRoot().name();
		GraphQLQueryRequest request = new GraphQLQueryRequest(query, projection);
		Mono<GraphQLResponse> response = client.reactiveExecuteQuery(request.serialize());
		log.info("Request {}", request.serialize());
		GraphQLResponse block = response.block();
		log.info("Result1 {}", block);
		TypeRef<List<ArtifactRepository>> typeRef = new TypeRef<List<ArtifactRepository>>(){};
		List<ArtifactRepository> repos = block.extractValueAsObject("artifactRepositories", typeRef);
		log.info("Result2 {}", repos);
		List<String> names = repos.stream().map(r -> r.getName()).collect(Collectors.toList());
		log.info("Result3 {}", names);
	}

	public static void main(String[] args) {
		SpringApplication.run(GraphqlclientdemoApplication.class, args);
	}
}
