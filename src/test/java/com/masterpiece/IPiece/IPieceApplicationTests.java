package com.masterpiece.IPiece;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

@SpringBootTest
@ActiveProfiles("test")
class IPieceApplicationTests {

	@MockBean
	Web3j web3j;

	@MockBean
	Credentials credentials;

	@Test
	void contextLoads() {
	}

}
