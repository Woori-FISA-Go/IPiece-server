package com.masterpiece.IPiece.blockchain.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Slf4j
@Configuration
@ConditionalOnProperty(
        name = "blockchain.enabled",
        havingValue = "true",
        matchIfMissing = true  // 기본값: true
)
public class Web3jConfig {

    @Value("${besu.rpc-url}")
    private String besuRpcUrl;

    @Value("${admin.private-key}")
    private String adminPrivateKey;

    @PostConstruct
    public void init() {
        log.info("=================================");
        log.info("🔧 Web3j 설정 초기화");
        log.info("=================================");
        log.info("Besu RPC URL: {}", besuRpcUrl);
        log.info("=================================");
    }

    @Bean
    public Web3j web3j() {
        try {
            Web3j web3j = Web3j.build(new HttpService(besuRpcUrl));
            String version = web3j.web3ClientVersion().send().getWeb3ClientVersion();
            log.info("✅ Besu 연결 성공: {}", version);
            return web3j;
        } catch (Exception e) {
            log.error("❌ Besu 연결 실패: {}", e.getMessage());
            throw new IllegalStateException("Besu RPC 연결 실패", e);
        }
    }

    @Bean
    public Credentials adminCredentials() {
        Credentials credentials = Credentials.create(adminPrivateKey);
        log.info("✅ Admin 계정 로드: {}", credentials.getAddress());
        return credentials;
    }
}