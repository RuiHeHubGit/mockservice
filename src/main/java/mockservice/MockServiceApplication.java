package mockservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableAspectJAutoProxy
public class MockServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MockServiceApplication.class, args);
    }
}
