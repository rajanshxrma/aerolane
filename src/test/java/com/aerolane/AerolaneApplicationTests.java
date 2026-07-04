package com.aerolane;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AerolaneApplicationTests {

    @Test
    void contextLoads() {
        // Boots the full application context against H2 + Flyway.
        // Fails if wiring, migrations or entity mapping validation break.
    }
}
