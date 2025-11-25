package com.masterpiece.IPiece;

import com.masterpiece.IPiece.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfig.class)
class IPieceApplicationTests {

	@Test
	void contextLoads() {
	}

}
