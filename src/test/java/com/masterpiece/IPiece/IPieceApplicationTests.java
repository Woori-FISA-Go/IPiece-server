package com.masterpiece.IPiece;

import com.masterpiece.IPiece.config.TestConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Disabled // 이 테스트 클래스 전체를 일시적으로 건너뜁니다.
@SpringBootTest
@Import(TestConfig.class)
class IPieceApplicationTests {

	@Test
	void contextLoads() {
	}

}
