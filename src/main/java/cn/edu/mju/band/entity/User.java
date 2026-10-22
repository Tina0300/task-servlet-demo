package cn.edu.mju.band.entity;

public class User {
    private Integer id;
    private String loginName;
    private String loginPwd;
    private String nickname;
    private Byte status;

    public User() {
    }

    public User(Integer id, String loginName, String loginPwd, String nickname, Byte status) {
        this.id = id;
        this.loginName = loginName;
        this.loginPwd = loginPwd;
        this.nickname = nickname;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getLoginPwd() {
        return loginPwd;
    }

    public void setLoginPwd(String loginPwd) {
        this.loginPwd = loginPwd;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }
}
