package com.masterpiece.IPiece.blockchain.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.concurrent.TimeUnit;

@Configuration
public class Web3jConfig {

    @Value("${besu.rpc-url}")
    private String besuRpcUrl;

    @Value("${ADMIN_PRIVATE_KEY}")
    private String adminPrivateKey;

    private Web3j web3jInstance;

    @PostConstruct
    public void validateConfig() {
        if (!StringUtils.hasText(besuRpcUrl)) {
            throw new IllegalStateException("besu.rpc-url must be configured");
        }
        if (!StringUtils.hasText(adminPrivateKey)) {
            throw new IllegalStateException("ADMIN_PRIVATE_KEY must be configured");
        }
        if (!WalletUtils.isValidPrivateKey(adminPrivateKey)) {
            throw new IllegalStateException("ADMIN_PRIVATE_KEY has invalid format");
        }
    }

    @Bean
    public Web3j web3j() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        HttpService httpService = new HttpService(besuRpcUrl, client);
        this.web3jInstance = Web3j.build(httpService);

        try {
            web3jInstance.web3ClientVersion().send();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to connect to Besu RPC: " + besuRpcUrl, e);
        }

        return this.web3jInstance;
    }

    @Bean
    public Credentials adminCredentials() {
        return Credentials.create(adminPrivateKey);
    }

    @PreDestroy
    public void cleanup() {
        if (web3jInstance != null) {
            web3jInstance.shutdown();
        }
    }
}
