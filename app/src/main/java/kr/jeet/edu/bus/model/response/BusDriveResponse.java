package kr.jeet.edu.bus.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import kr.jeet.edu.bus.model.data.BusDriveSeqData;

public class BusDriveResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public BusDriveSeqData data;
}
