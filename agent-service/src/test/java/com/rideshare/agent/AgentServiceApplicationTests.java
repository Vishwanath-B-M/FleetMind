// package com.rideshare.agent;

// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;

// @SpringBootTest
// class AgentServiceApplicationTests {

// 	@Test
// 	void contextLoads() {
// 	}

// }

package com.rideshare.agent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.ai.openai.api-key=test-api-key"
})
class AgentServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
