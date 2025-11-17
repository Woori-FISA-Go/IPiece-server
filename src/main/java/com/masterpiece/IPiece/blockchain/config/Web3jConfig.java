package com.masterpiece.IPiece.blockchain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class Web3jConfig {

    @Value("${besu.rpc-url}")
    private String besuRpcUrl;

    @Value("${admin.private-key}")
    private String adminPrivateKey;

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(besuRpcUrl));
    }

    @Bean
    public Credentials adminCredentials() {
        return Credentials.create(adminPrivateKey);
    }
}
