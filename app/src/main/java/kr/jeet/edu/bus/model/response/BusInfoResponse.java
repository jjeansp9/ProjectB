package kr.jeet.edu.bus.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import kr.jeet.edu.bus.model.data.BusInfoData;

public class BusInfoResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public List<BusInfoData> data;
}
