package vn.com.fpt.sep490_g28_summer2024_be.common;

public class AppConfig {
    public static int BCRYPT_PASSWORD_ENCODER = 10;
    public static int VALID_OTP_TIME = 3*60;
    public static String STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    public static String PROJECT_PREFIX = "DA";
    public static String CHALLENGE_PREFIX = "TT";
    public static String REFER_PREFIX = "DS";
    public static String ACCOUNT_PREFIX = "ACC";
    public static String CASSO_URL = "https://oauth.casso.vn/v2/transactions?fromDate=%s&page=%d&pageSize=%d&sort=ASC";
}
