package Main;

import Database.ConnectionInfo;
import Database.DBManager;
import core.DBTask;
import core.OnDBTask;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main extends Thread implements OnDBTask {
    private Scanner mScanner = new Scanner(System.in);

    public static void main(String[] args) {
        if (args.length > 0) {
            // TODO: Some functions...
        } else {
            // Start program
            getInstance().start();
        }
    }

    private static class Instance {
        private static Main instance = new Main();
    }
    public static Main getInstance() {
        return Instance.instance;
    }

    @Override
    public void run() {
        super.run();
        s_start();
    }

    /**
     * Sequence start
     */
    private void s_start() {
        s_menu();
    }

    /**
     * Sequence print menu
     */
    private void s_menu() {
        System.out.println();
        System.out.println("[Menu]");
        System.out.println("1. 역코드 직접 입력");
        System.out.println("2. 지하철 시간표 일괄 업데이트");
        System.out.println("3. 시간표 테이블 비우기");
        System.out.println("0. Exit");
        System.out.println();
        System.out.print("Please mScanner menu number : ");
        try {
            int input_menu = Integer.parseInt(mScanner.nextLine().trim());
            switch (input_menu) {
                case 1:
                    s_input_station_code();
                    break;
                case 2:
                    s_input_all_station_code();
                    break;
                case 3:
                    s_start();
                    break;
                case 0:
                    System.exit(0);
                    break;
                default:
                    System.out.println("올바른 메뉴를 선택하세요.");
                    Thread.sleep(500);
                    s_menu();
            }
        } catch (Exception ignore) {
            System.out.println("올바른 메뉴를 선택하세요.");
            try {Thread.sleep(500);} catch (Exception ignore1) {}
            s_menu();
        }
    }

    /**
     * Sequence input station code
     */
    private void s_input_station_code() {
        System.out.print("요청할 역코드 : ");
        String[] input_station_codes = mScanner.nextLine().trim().split(", |,| ");
        System.out.println("Parsing data...");
        List<Integer> station_code = new ArrayList<>();
        List<String> station_code_error = new ArrayList<>();
        for (String code : input_station_codes) {
            if (code != null && code.length() > 0) {
                try {
                    station_code.add(Integer.parseInt(code));
                } catch (Exception ignore) {
                    station_code_error.add(code);
                }
            }
        }

        System.out.println("※ Include/Exclude/Total : " + String.valueOf(station_code.size()) + '/' + String.valueOf(station_code_error.size()) + '/' + String.valueOf(input_station_codes.length));
        System.out.print("계속 진행하시겠습니까? Y/N : ");
        if (mScanner.nextLine().equalsIgnoreCase("Y")) {
            s_db_setting();
            resetCounter();
            mTaskTotal_cnt = station_code.size();
            mTaskStart = System.currentTimeMillis();
            for (int code : station_code) {
                new DBTask(this, code).start();
            }

        } else {
            System.exit(0);
        }
    }

    /**
     * Sequence input all station code
     */
    private void s_input_all_station_code() {
        System.out.print("모든 지하철역의 시간표를 갱신하는 작업을 진행합니다. 기존 데이터는 삭제됩니다. 계속 하시겠습니까? Y/N : ");
        if (mScanner.nextLine().equalsIgnoreCase("Y")) {
            if (!DBManager.getInstance().isConnected()) {
                s_db_setting();
            }
            System.out.print("지하철역 코드가 포함된 테이블명 : ");
            ArrayList<Integer> station_code = new ArrayList<>();
            try {
                ResultSet rs = DBManager.getInstance().getStatement().executeQuery("SELECT CODE FROM " + mScanner.nextLine() + ";");
                while (rs.next()) {
                    station_code.add(rs.getInt(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {DBManager.getInstance().getStatement().execute("TRUNCATE metro_time_table;");} catch (SQLException e) {e.printStackTrace();}   // 테이블 비움
            resetCounter();
            mTaskTotal_cnt = station_code.size();
            mTaskStart = System.currentTimeMillis();
            for (int sCode : station_code) {
                new DBTask(this, sCode).start();
            }
        } else {
            s_start();
        }
    }

    /**
     * Sequence database connection setting
     */
    private void s_db_setting() {
        System.out.print("DB Host[String] : ");
        String db_host = mScanner.nextLine();
        System.out.print("DB Username[String] : ");
        String db_username = mScanner.nextLine();
        System.out.print("DB Password[String] : ");
        String db_password = mScanner.nextLine();
        System.out.print("Database[String] : ");
        DBManager.setConnectionInfo(new ConnectionInfo(db_host, db_username, db_password, null));
        DBManager.getInstance().init();
        try {
            DBManager.getInstance().getStatement().execute("use " + mScanner.nextLine() + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int mTaskSucceed_cnt = 0, mTaskFailed_cnt = 0, mTaskTotal_cnt = 0;
    private long mTaskQueryTotal_cnt = 0;
    private long mTaskStart = 0, mTaskEnd = 0;

    /**
     * Call when Task end
     * @param dbTask DBTask extends Thread
     */
    @Override
    public synchronized void OnTaskEnd(DBTask dbTask) {
        if (dbTask != null) {
            if (dbTask.getTaskStatus().isSucceed()) {
                ++mTaskSucceed_cnt;
            } else {
                ++mTaskFailed_cnt;
            }
            mTaskQueryTotal_cnt += dbTask.getTaskStatus().getQueryResultCount();
        }
        System.out.println("### Succeed/Failed/Total : " + String.valueOf(mTaskSucceed_cnt) + '/' + String.valueOf(mTaskFailed_cnt) + '/' + String.valueOf(mTaskTotal_cnt) + "(" + String.format("%.2f", (double) (mTaskSucceed_cnt + mTaskFailed_cnt) / mTaskTotal_cnt * 100.0) + "%" + ")");

        if (mTaskTotal_cnt == mTaskSucceed_cnt + mTaskFailed_cnt) {
            if (mTaskSucceed_cnt > 0) {
                System.out.println("※ Task succeed : " + String.valueOf(mTaskSucceed_cnt));
            }
            if (mTaskFailed_cnt > 0) {
                System.err.println("※ Task failed : " + String.valueOf(mTaskFailed_cnt));
            }
            if (mTaskQueryTotal_cnt > 0) {
                System.out.println("※ Query updated : " + String.valueOf(mTaskQueryTotal_cnt));
            }

            mTaskEnd = System.currentTimeMillis();
            System.out.println("※ Task work time : " + String.format("%.2f", (double)(mTaskEnd - mTaskStart) / 1000) + "s (" + (mTaskEnd - mTaskStart) + "ms)");

            // Reset
            resetCounter();
        }
    }

    /**
     * Reset counter
     */
    private void resetCounter() {
        mTaskTotal_cnt = mTaskSucceed_cnt = mTaskFailed_cnt = 0;
        mTaskQueryTotal_cnt = 0;
        mTaskStart = mTaskEnd = 0;
    }
}