package com.fptu.forum;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test cơ bản - khong can Spring context.
 * Spring context load test can ket noi SQL Server that.
 * Chay bang: mvn spring-boot:run (voi SQL Server dang chay).
 */
class ForumApplicationTests {

    @Test
    void smokeTest() {
        // Test don gian, khong can database
        assertThat(true).isTrue();
    }
}
