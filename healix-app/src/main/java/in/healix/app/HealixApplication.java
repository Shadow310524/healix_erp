package in.healix.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "in.healix.core",
    "in.healix.security",
    "in.healix.persistence",
    "in.healix.rules",
    "in.healix.workflows",
    "in.healix.notifications",
    "in.healix.modules",
    "in.healix.app"
})
public class HealixApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealixApplication.class, args);
    }
}
