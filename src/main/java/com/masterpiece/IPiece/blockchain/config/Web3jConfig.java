/*
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

import java.io.IOException;

@Slf4j
@Configuration
@ConditionalOnProperty(
        name = "blockchain.enabled",
        havingValue = "true",
        matchIfMissing = true  // 기본값: true
)
public class Web3jConfig {

    @Value("${besu.rpc-url:}")   // rpc-url 없으면 빈 문자열
    private String besuRpcUrl;

    @Value("${admin.private-key:}")  // private-key 없으면 기본값
    private String adminPrivateKey;

    @PostConstruct
    public void init() {
        log.info("=================================");
        log.info("🔧 Web3j 설정 초기화");
        log.info("Besu RPC URL: {}", besuRpcUrl);
        log.info("=================================");
    }

    /**
     * Besu 연결 실패해도 서버는 정상 기동되도록 처리
     */
    @Bean
    public Web3j web3j() {
        try {
            if (besuRpcUrl == null || besuRpcUrl.isBlank()) {
                log.warn("⚠️ BESU RPC URL이 비어있음. Dummy Web3j 사용");
                return buildDummyWeb3j();
            }

            Web3j web3j = Web3j.build(new HttpService(besuRpcUrl));
            String version = web3j.web3ClientVersion().send().getWeb3ClientVersion();
            log.info("✅ Besu 연결 성공: {}", version);
            return web3j;

        } catch (Exception e) {
            log.error("❌ Besu 연결 실패: {} — Dummy Web3j 사용", e.getMessage());
            return buildDummyWeb3j();  // 절대 null 반환 금지!
        }
    }

    /**
     * 실제 RPC가 오면 무조건 실패하도록 하는 더미 Web3j
     */
    private Web3j buildDummyWeb3j() {
        return Web3j.build(new HttpService() {
            @Override
            public <T extends org.web3j.protocol.core.Response> T send(
                    org.web3j.protocol.core.Request request,
                    Class<T> responseType
            ) throws IOException {
                throw new IOException("Blockchain disabled (Dummy Web3j)");
            }
        });
    }

    /**
     * Admin PrivateKey도 없으면 Dummy Credentials 생성
     */
    @Bean
    public Credentials adminCredentials() {
        try {
            if (adminPrivateKey == null || adminPrivateKey.isBlank()) {
                log.warn("⚠️ Admin PrivateKey 없음 → Dummy Credentials 사용");
                return Credentials.create("0x0000000000000000000000000000000000000000000000000000000000000001");
            }

            Credentials credentials = Credentials.create(adminPrivateKey);
            log.info("✅ Admin 계정 로드: {}", credentials.getAddress());
            return credentials;

        } catch (Exception e) {
            log.error("❌ Admin 계정 로드 실패: {} — Dummy Credentials 사용", e.getMessage());
            return Credentials.create("0x0000000000000000000000000000000000000000000000000000000000000001");
        }
    }
}