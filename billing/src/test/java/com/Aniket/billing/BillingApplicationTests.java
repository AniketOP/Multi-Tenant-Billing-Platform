package com.Aniket.billing;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
@Disabled("Integration test: needs JWT_SECRET/COUCHBASE_* env vars and Docker running")
@SpringBootTest
class BillingApplicationTests {

	@Test
	void contextLoads() {
	}

}
