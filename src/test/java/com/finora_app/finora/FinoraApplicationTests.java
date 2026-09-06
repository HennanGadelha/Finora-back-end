package com.finora_app.finora;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "JWT_SECRET=test-only-jwt-secret-with-at-least-32-bytes")
class FinoraApplicationTests {

	@Test
	void contextLoads() {
	}

}
