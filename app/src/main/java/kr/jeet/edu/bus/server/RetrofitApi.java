package kr.jeet.edu.bus.server;

import kr.jeet.edu.bus.model.request.BusDriveRequest;
import kr.jeet.edu.bus.model.response.AnnouncementListResponse;
import kr.jeet.edu.bus.model.response.BaseResponse;
import kr.jeet.edu.bus.model.response.BoardDetailResponse;
import kr.jeet.edu.bus.model.response.BriefingDetailResponse;
import kr.jeet.edu.bus.model.response.BriefingReservedListResponse;
import kr.jeet.edu.bus.model.response.BriefingResponse;
import kr.jeet.edu.bus.model.response.BusDriveResponse;
import kr.jeet.edu.bus.model.response.BusInfoResponse;
import kr.jeet.edu.bus.model.response.BusRouteResponse;
import kr.jeet.edu.bus.model.response.GetACAListResponse;
import kr.jeet.edu.bus.model.response.ScheduleDetailResponse;
import kr.jeet.edu.bus.model.response.ScheduleListResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitApi {

//    public final static String SERVER_BASE_URL = "http://192.168.2.51:7777/";   //kyt local
    public final static String SERVER_BASE_URL = "http://192.168.2.77:7777/";  //khj local
    //public final static String SERVER_BASE_URL = "http://192.168.2.55:7777/";   //pjh local
    //public final static String SERVER_BASE_URL = "http://211.252.86.237:7777/"; // 신규 cloud local

    public final static String PREFIX = "mobile/api/";
    public final static String FILE_SUFFIX_URL = SERVER_BASE_URL + "attachFile/";

    public final static int RESPONSE_CODE_SUCCESS = 200;
    public final static int RESPONSE_CODE_SUCCESS2 = 201;
    public final static int RESPONSE_CODE_BINDING_ERROR= 400;
    public final static int RESPONSE_CODE_NOT_FOUND= 404;
    public final static int RESPONSE_CODE_DUPLICATE_ERROR= 409;
    public final static int RESPONSE_CODE_INTERNAL_SERVER_ERROR= 500;

    // 캠퍼스 목록 조회
    @GET("aca")
    Call<GetACAListResponse> getACAList();

    // 공지사항 목록 조회
    @GET("notices")
    Call<AnnouncementListResponse> getAnnouncementList(@Query("noticeSeq") int noticeSeq, @Query("acaCode") String acaCode);

    // 공지사항 글 상세보기
    @GET("notice/{noticeSeq}")
    Call<BoardDetailResponse> getBoardDetail(@Path("noticeSeq") int noticeSeq);

    // 설명회 목록 조회
    @GET("pts")
    Call<BriefingResponse> getBriefingList(@Query("acaCode") String acaCode, @Query("year") int year, @Query("month") int month);

    // 설명회 글 상세보기
    @GET("pt/{ptSeq}")
    Call<BriefingDetailResponse> getBriefingDetail(@Path("ptSeq") int ptSeq);

    // 설명회 예약자 목록 조회
    @GET("pt/reservation/{ptSeq}")
    Call<BriefingReservedListResponse> getBrfReservedList(@Path("ptSeq") int ptSeq);

    // 캠퍼스일정 목록 조회
    @GET("schedules")
    Call<ScheduleListResponse> getScheduleList(@Query("acaCode") String acaCode, @Query("year") int year, @Query("month") int month);

    // 캠퍼스일정 상세 조회
    @GET("schedule/{scheduleSeq}")
    Call<ScheduleDetailResponse> getScheduleDetail(@Path("scheduleSeq") int scheduleSeq);

    // 버스 정보 조회
    @GET("bus")
    Call<BusInfoResponse> getBusInfo(@Query("phoneNumber") String phoneNumber);

    // 버스 운행 시작
    @POST("bus/drive")
    Call<BusDriveResponse> getBusDriveStart(@Body BusDriveRequest request);

    // 버스 운행 종료
    @PATCH("bus/drive")
    Call<BaseResponse> getBusDriveFinish(@Query("busDriveSeq") int busDriveSeq);

    // 버스 노선 조회
    @GET("bus/route")
    Call<BusRouteResponse> getBusRoute(@Query("busAcaName") String busAcaName, @Query("busCode") int busCode);

    // 버스 정류장 도착
    @PATCH("bus/busStop")
    Call<BaseResponse> getBusStop(@Query("busDriveSeq") int busDriveSeq, @Query("bpCode") String bpCode, @Query("isDrive") String isDrive);


}
