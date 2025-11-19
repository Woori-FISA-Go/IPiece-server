package com.masterpiece.IPiece.config;

import com.masterpiece.IPiece.integration.besu.BesuClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public BesuClient besuClient() {
        return mock(BesuClient.class);
    }
}