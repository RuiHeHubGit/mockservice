package mockservice.web.controller;

import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("get1")
    public String get1(User user) {
        System.out.println(user);
        return user.toString();
    }

    @Data
    static class User {
        private String name;
        private String password;
    }
}
