package com.myblogbackend.blog.webclient;

import com.myblogbackend.blog.mapper.PostMapper;
import com.myblogbackend.blog.response.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PostWebClient {
    private final PostMapper postMapper;
    private final WebClient webClient;

    public Mono<PostResponse> getMonoPost(final UUID postId) {
        Mono<Object> postDataMono = webClient.get()
                .uri("/posts/{postId}", postId)
                .retrieve()
                .bodyToMono(Object.class)
                .defaultIfEmpty(Mono.empty());
        // convert object to PostResponse
        PostResponse postResponse = postMapper.toPostResponseFromObject(postDataMono);
        return Mono.just(postResponse);
    }

    public Mono<List<PostResponse>> getListOfPostsReactive() {
        Mono<List<Object>> data = webClient.get()
                .uri("https://jsonplaceholder.typicode.com/posts")
                .retrieve()
                .bodyToFlux(Object.class)
                .collectList()
                .defaultIfEmpty(Collections.emptyList());
        //convert array of objects to list of postResponse
        List<PostResponse> postResponseList = postMapper.toListPostResponseFromObject((List<Object>) data);
        return Mono.just(postResponseList);
    }
}

