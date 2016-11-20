package org.telegram.quickBlox.definitions;

public class Consts
{
    public static final long ANSWER_TIME_INTERVAL = 45L;
    public static final String APP_ID = "92";
    public static final String AUTH_KEY = "wJHdOcQSxXQGWx5";
    public static final String AUTH_SECRET = "BTFsj7Rtt27DAmT";
    public static final int AUTOSTART = 1006;
    public static final String CALL_ACTION_VALUE = "call_action_value";
    public static final int CALL_ACTIVITY_CLOSE = 1000;
    public static final int CALL_ACTIVITY_CLOSE_WIFI_DISABLED = 1001;
    public static final String CALL_DIRECTION_TYPE_EXTRAS = "call_direction_type";
    public static final int CALL_REJECT_BY_USER = 11112;
    public static final String CALL_RESULT = "call_result";
    public static final String CALL_TYPE_EXTRAS = "call_type";
    public static final int CONNECTED_TO_USER = 22221;
    public static final int CONNECTION_CLOSED_FOR_USER = 22222;
    public static final int CONNECTION_FAILED_WITH_USER = 22225;
    public static final int DISCONNECTED_FROM_USER = 22223;
    public static final int DISCONNECTED_TIMEOUT_FROM_USER = 22224;
    public static final int ERROR = 22226;
    public static final int LOGIN = 1008;
    public static final String LOGIN_RESULT = "result";
    public static final int LOGIN_RESULT_CODE = 1003;
    public static final int LOGIN_TASK_CODE = 1002;
    public static final int NOTIFICATION_CONNECTION_LOST = 1005;
    public static final int NOTIFICATION_FORAGROUND = 1004;
    public static final String OPPONENTS_LIST_EXTRAS = "opponents_list";
    public static final String PARAM_PINTENT = "pendingIntent";
    public static final String QB_EXCEPTION_EXTRAS = "exception";
    public static final int RECEIVE_HANG_UP_FROM_USER = 11113;
    public static final int RECEIVE_NEW_SESSION = 11110;
    public static final int RELOGIN = 1007;
    public static final int SESSION_CLOSED = 11114;
    public static final int SESSION_START_CLOSE = 11115;
    public static final String SHARED_PREFERENCES = "preferences";
    public static final int START_CONNECT_TO_USER = 22220;
    public static final String START_SERVICE_VARIANT = "start_service_variant";
    public static final String USER_ID = "user_id";
    public static final String USER_INFO_EXTRAS = "user_info";
    public static final String USER_LOGIN = "user_login";
    public static final int USER_NOT_ANSWER = 11111;
    public static final String USER_PASSWORD = "user_password";
    public static final String WIFI_DISABLED = "wifi_disabled";

    public static enum CALL_DIRECTION_TYPE
    {
        INCOMING,  OUTGOING;

        private CALL_DIRECTION_TYPE() {}
    }
}
