package mockservice.domain;

import java.util.HashMap;
import java.util.Map;

public class User {
    private Long id;
    private String username;
    private String password;
    private String headImg;
    private Map<String, String> info;

    public User(Long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public Map<String, String> getInfo() {
        if(info == null) {
            info = new HashMap<>();
        }
        return info;
    }

    public void setInfo(Map<String, String> info) {
        this.info = info;
    }
}
