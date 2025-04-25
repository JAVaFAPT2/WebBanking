package service.messagebroker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication()
@EntityScan("service.messagebroker.models")
@EnableJpaRepositories("service.messagebroker.repository")
public class MessageBrokerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessageBrokerApplication.class, args);
	}

}
