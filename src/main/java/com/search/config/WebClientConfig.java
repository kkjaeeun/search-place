package com.search.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


@Configuration
public class WebClientConfig {

    @Value("${external.kakao.api-url}")
    private String kakaoApiUrl;

    @Value("${external.naver.api-url}")
    private String naverApiUrl;

    @Value("${external.kakao.key}")
    private String kakaoApiKey;

    @Value("${external.naver.client-id}")
    private String naverClientId;

    @Value("${external.naver.client-secret}")
    private String naverClientSecret;

    @Bean
    public WebClient kakaoWebClient() {
        // kakao 인증 헤더 추가
        return createWebClient(kakaoApiUrl).mutate()
                .defaultHeader("Authorization", "KakaoAK " + kakaoApiKey)
                .build();
    }

    @Bean
    public WebClient naverWebClient() {
        // Naver 인증 헤더 추가
        return createWebClient(naverApiUrl).mutate()
                .defaultHeader("X-Naver-Client-Id", naverClientId)
                .defaultHeader("X-Naver-Client-Secret", naverClientSecret)
                .build();
    }

    private WebClient createWebClient(String apiUrl) {
        // 외부 API 추가시 사용 가능
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)  // 연결 타임아웃
                .responseTimeout(Duration.ofMillis(5000))  // 응답 타임아웃
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))  // 읽기 타임아웃
                                .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));  // 쓰기 타임아웃


        return WebClient.builder()
                .baseUrl(apiUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))  // WebClient에 HttpClient 설정
                .build();
    }
}
