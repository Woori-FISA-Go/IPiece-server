package com.masterpiece.IPiece;

import com.masterpiece.IPiece.integration.besu.BesuClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j

@SpringBootApplication

public class IPieceApplication implements CommandLineRunner {



	private final BesuClient besuClient;



	public IPieceApplication(@org.springframework.beans.factory.annotation.Autowired(required = false) BesuClient besuClient) {

		this.besuClient = besuClient;

	}



	public static void main(String[] args) {

        SpringApplication.run(IPieceApplication.class, args);

    }



	 @Override

	public void run(String... args) throws Exception {

		if (besuClient != null) {

			log.info("Admin Wallet Address: {}", besuClient.getAdminAddress());

		} else {

			log.info("Blockchain integration is disabled");

		}

	}

}
