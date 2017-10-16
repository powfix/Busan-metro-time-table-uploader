package Database;

public class ConnectionInfo {
    private String host, username, password;
    private int port;

    public ConnectionInfo(String host, String username, String password, Integer port) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port == null ? 3306 : port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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

    public int getPort() {
        return port;
    }

    public void setPort(short port) {
        this.port = port;
    }

    public String getURL() {
        return "jdbc:mysql://" + getHost() + ":" + String.valueOf(getPort());
    }
}
