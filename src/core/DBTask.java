package core;

import Database.DBManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Statement;

public class DBTask extends Thread {
    private OnDBTask mOnDBTask;
    private final int mStationCode;
    private String mAPIData;
    private TaskStatus mTaskStatus = new TaskStatus();

    /**
     * Created by KKM 2017-10-13
     * DBTask 클래스의 작업결과 객체
     */
    public class TaskStatus {
        private boolean mSucceed = false;   // 성공여부
        private String mResultCode;         // API header Result Code
        private String mResultMessage;      // API header Result Message
        private int mItemSize = -1;         // API item size (Loaded)
        private int mItemTotalSize = -1;    // API time table total size (Not loaded)
        private int mQueryResultCount;      // Database update(inserted) item count

        public boolean isSucceed() {
            return mSucceed;
        }
        public void setSucceed(boolean mSucceed) {
            this.mSucceed = mSucceed;
        }
        public String getResultCode() {
            return mResultCode;
        }
        public String getResultMessage() {
            return mResultMessage;
        }
        public int getItemSize() {
            return mItemSize;
        }
        public int getItemTotalSize() {
            return mItemTotalSize;
        }
        public int getQueryResultCount() {
            return mQueryResultCount;
        }
        public int setQueryResultCount(int mQueryResultCount) {
            return this.mQueryResultCount = mQueryResultCount;
        }
    }

    public DBTask(OnDBTask onDBTask, int stationCode) {
        this.mOnDBTask = onDBTask;
        this.mStationCode = stationCode;
    }

    public class TaskResult {
        private long mSucceed, mFailed, mTotal;

        public TaskResult(long succeed, long failed, long total) {
            this.mSucceed = succeed;
            this.mFailed = failed;
            this.mTotal = total;
        }

        public long getSucceed() {
            return mSucceed;
        }

        public long getFailed() {
            return mFailed;
        }

        public long getTotal() {
            return mTotal;
        }
    }

    @Override
    public void run() {
        super.run();
        try {
            JSONObject response = new JSONObject(mAPIData = BusanMetroAPI.getResult(mStationCode)).getJSONObject("response");
            JSONObject header = response.getJSONObject("header");
            final String resultCode = header.getString("resultCode");
            final String resultMsg = header.getString("resultMsg");
            if (!resultCode.equalsIgnoreCase("00") || !resultMsg.equalsIgnoreCase("NORMAL SERVICE.")) {
                mTaskStatus.setSucceed(false);
            } else {
                JSONObject body = response.getJSONObject("body");
//                int itemCount = mTaskStatus.setItemSize(body.getInt("numOfRows"));
//                int totalCount = mTaskStatus.setItemTotalSize(body.getInt("totalCount"));
                JSONArray array = body.getJSONArray("item");
                String sql;
                {
                    StringBuilder builder = new StringBuilder("INSERT INTO metro_time_table (LINE_NO,S_CODE,S_NAME_KR,S_NAME_EN,TRAIN_NO,HOUR,MINUTE,DAY,UPDOWN,END_S_CODE) VALUES ");
                    JSONObject object;
                    String LINE_NO, STATION_CODE, STATION_NAME_KR, STATION_NAME_EN, TRAIN_NUMBER, HOUR, MINUTE, DAY, UPDOWN, END_STATION_CODE;
                    for (int i = 0; i < array.length(); ++i) {
                        object = array.getJSONObject(i);
                        LINE_NO = object.getString("line");
                        STATION_CODE = object.getString("scode");
                        STATION_NAME_KR = object.getString("sname").replace("'", "''");
                        STATION_NAME_EN = object.getString("engname").replace("'", "''");
                        TRAIN_NUMBER = object.getString("trainno");
                        HOUR = object.getString("hour");
                        MINUTE = object.getString("time");
                        DAY = object.getString("day");
                        UPDOWN = object.getString("updown");
                        END_STATION_CODE = object.getString("endcode");

                        builder.append("('")
                                .append(LINE_NO).append("','")
                                .append(STATION_CODE).append("','")
                                .append(STATION_NAME_KR).append("','")
                                .append(STATION_NAME_EN).append("','")
                                .append(TRAIN_NUMBER).append("','")
                                .append(HOUR).append("','")
                                .append(MINUTE).append("','")
                                .append(DAY).append("','")
                                .append(UPDOWN).append("','")
                                .append(END_STATION_CODE).append("'),");
                    }
                    // When that length < 0 Has exception
                    if (builder.length() > 0) {
                        // Remove last ','
                        builder.deleteCharAt(builder.length() - 1);
    // builder.deleteCharAt(builder.lastIndexOf(","));
                    }
                    builder.append(';');
                    sql = builder.toString();
                    // System.out.println("SQL Query : " + (sql = builder.toString()));
                }
                Statement stmt = DBManager.getInstance().getStatement();
                mTaskStatus.setQueryResultCount(stmt.executeUpdate(sql));
                // System.out.println("Query result : " + mTaskStatus.setQueryResultCount(stmt.executeUpdate(sql)));

                mTaskStatus.setSucceed(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mTaskStatus.setSucceed(false);
        }

        onThreadEnd();
    }

    /**
     * Call when thread end
     */
    private void onThreadEnd() {
        pushResult();
    }

    private void pushResult() {
        if (mOnDBTask != null) {
            mOnDBTask.OnTaskEnd(this);
            mOnDBTask = null;
        }
    }

    public TaskStatus getTaskStatus() {
        return mTaskStatus;
    }

    public int getStationCode() {
        return mStationCode;
    }

    public String getAPIData() {
        return mAPIData;
    }
}