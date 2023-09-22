package kr.jeet.edu.bus.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import kr.jeet.edu.bus.model.data.ScheduleData;

public class ScheduleDetailResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public ScheduleData data;

}
