package com.masterpiece.IPiece;

import com.masterpiece.IPiece.integration.besu.BesuClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@RequiredArgsConstructor
@SpringBootApplication
public class IPieceApplication implements CommandLineRunner {

	private final BesuClient besuClient; // BesuClient 주입

	public static void main(String[] args) {
        SpringApplication.run(IPieceApplication.class, args);
    }

	@Override
	public void run(String... args) throws Exception {
		// 애플리케이션 시작 시 Admin 지갑 주소 로그 출력
		log.info("Admin Wallet Address: {}", besuClient.getAdminAddress());
	}
}
