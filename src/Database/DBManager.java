package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Created by KKM 2017-10-11
 * 데이터베이스 관련항목
 */
public class DBManager {
    private static ConnectionInfo mConnectionInfo;
    private Statement mStatement;
    private Connection mConnection;

    private DBManager() {
    }

    private static class Instance {
        private static final DBManager instance = new DBManager();
    }

    public static DBManager getInstance() {
        return Instance.instance;
    }

    /**
     * init
     */
    public void init() {
        if (mConnectionInfo != null) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                mConnection = DriverManager.getConnection(mConnectionInfo.getURL(), mConnectionInfo.getUsername(), mConnectionInfo.getPassword());
                mStatement = mConnection.createStatement();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Connection information object is NULL");
        }
    }

    public Statement getStatement() {
        return mStatement;
    }

    public Connection getConnection() {
        return mConnection;
    }

    public boolean isConnected() {
        if (mConnection == null || mStatement == null) {
            return false;
        } else {
            try {
                if (mConnection.isClosed()) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static void setConnectionInfo(ConnectionInfo connectionInfo) {
        mConnectionInfo = connectionInfo;
    }
}
