package kr.jeet.edu.bus.common;

import kr.jeet.edu.bus.server.RetrofitApi;

public class Constants {

    // 로그인 400 BAD_REQUEST MSG
    public final static String ALREADY_LOGIN_IN = "ALREADY_LOGIN_IN"; // 이미 로그인된 사용자
    public final static String PARAMETER_BINDING_ERROR = "PARAMETER_BINDING_ERROR"; // 파라미터 바인딩 에러
    public final static String USER_GUBUN_MISMATCH = "USER_GUBUN_MISMATCH"; // JEET 회원이 아님(app 타입과 조회한 회원의 타입이 다를때)
    public final static String DUPLICATE_PHONE_NUMBER = "DUPLICATE_PHONE_NUMBER"; // 전화번호 중복

    public final static int PICKER_MIN_YEAR = 1999;
    public final static int PICKER_MAX_YEAR = 2099;

    //file provider column
    public static final String FILE_ID = "document_id";
    public static final String FILE_MIME_TYPE = "mime_type";
    public static final String FILE_DISPLAY_NAME = "_display_name";
    //    public static final String FILE_LAST_MODIFIED = "last_modified";
//    public static final String FILE_FLAGS = "flags";
    public static final String FILE_SIZE = "_size";

    //dateFormatter String
    public static final String DATE_FORMATTER_YYYY_MM_DD_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMATTER_YYYY_MM_DD_KOR = "yyyy년 M월 d일";
    public static final String TIME_FORMATTER_HH_MM = "HH:mm";
    public static final String TIME_FORMATTER_M_D_E = "M월 d일 E요일";
    public static final String DATE_FORMATTER_YYYY_MM_KOR = "yyyy년 M월";

    public static final int PHONE_NUM_LENGTH_1 = 11; // 휴대폰번호 length
    public static final int PHONE_NUM_LENGTH_2 = 10; // 휴대폰번호2 length

    public final static String POLICY_SERVICE = RetrofitApi.SERVER_BASE_URL+"web/api/policy/service"; // 이용약관
    public final static String POLICY_PRIVACY = RetrofitApi.SERVER_BASE_URL+"web/api/policy/privacy"; // 개인정보취급방침

    public static final int NOT_DRIVING = 0;

    public static final int MOVE_DEFAULT = -1;
    public static final int MOVE_LEFT = 0;
    public static final int MOVE_RIGHT = 1;
    public static final int MOVE_UP = 2;
    public static final int MOVE_DOWN = 3;
    public static final int MOVE_DETAIL_RIGHT = 4;

    // 설치문자 전송을 제외한 나머지 경우 무조건 아래 지정한 값으로 전달
    public static final String SMS_SENDER_CODE = "556"; // 발신자코드(sfCode)
    public static final String SMS_RECEIVER_CODE = "-1"; // 수신자코드(stCode)
}
