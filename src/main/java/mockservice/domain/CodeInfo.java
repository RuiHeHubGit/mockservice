package mockservice.domain;

public class CodeInfo {
    private String code;
    private String clientId;
    private String redirectURI;
    private String scope;
    private String state;
    private String username;
    private Long createTime;

    public CodeInfo() {
    }

    public CodeInfo(String code, String clientId, String redirectURI, String scope, String state, String username, Long createTime) {
        this.code = code;
        this.clientId = clientId;
        this.redirectURI = redirectURI;
        this.scope = scope;
        this.state = state;
        this.username = username;
        this.createTime = createTime;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRedirectURI() {
        return redirectURI;
    }

    public void setRedirectURI(String redirectURI) {
        this.redirectURI = redirectURI;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}
