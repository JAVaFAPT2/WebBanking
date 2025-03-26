package service.orchestrationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "orchestration-events" })
class OrchestrationServiceApplicationTests {

    @Test
    void contextLoads() {
        // Your test logic here, for example producing and consuming messages.
        assertThat(true).isTrue();
    }

}