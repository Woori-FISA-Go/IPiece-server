package com.masterpiece.IPiece;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

@SpringBootTest
@ActiveProfiles("test")
class IPieceApplicationTests {

	@MockitoBean
	Web3j web3j;

	@MockitoBean
	Credentials credentials;

	@Test
	void contextLoads() {
	}

}
