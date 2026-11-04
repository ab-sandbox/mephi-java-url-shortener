package ru.mephi.promoit.urlshortener;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.mephi.promoit.urlshortener.config.TestcontainersConfig;

@SpringBootTest
@Import(TestcontainersConfig.class)
class UrlShortenerApplicationTests {

    @Test
    void contextLoads() {
    }
}
