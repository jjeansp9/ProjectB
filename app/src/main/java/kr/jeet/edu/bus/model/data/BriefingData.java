package kr.jeet.edu.bus.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class BriefingData implements Parcelable {
    @SerializedName("seq")
    public int seq;
    @SerializedName("title")
    public String title;
    @SerializedName("content")
    public String content;
    @SerializedName("acaCode")
    public String acaCode;
    @SerializedName("acaName")
    public String acaName;
    @SerializedName("date")
    public String date;
    @SerializedName("ptTime")
    public String ptTime;
    @SerializedName("place")
    public String place;
    @SerializedName("participantsCnt")
    public int participantsCnt;
    @SerializedName("reservationCnt")
    public int reservationCnt;
    @SerializedName("fileId")
    public String fileId;
    @SerializedName("fileList")
    public List<FileData> fileList = new ArrayList<>();

    public BriefingData(){}
    public BriefingData(int seq, String title, String content, String acaCode, String acaName, String date, String ptTime, String place, int participantsCnt, int reservationCnt, String fileId, List<FileData> fileList) {
        this.seq = seq;
        this.title = title;
        this.content = content;
        this.acaCode = acaCode;
        this.acaName = acaName;
        this.date = date;
        this.ptTime = ptTime;
        this.place = place;
        this.participantsCnt = participantsCnt;
        this.reservationCnt = reservationCnt;
        this.fileId = fileId;
        if(fileList != null)
            this.fileList = fileList;
    }

    protected BriefingData(Parcel in) {
        seq = in.readInt();
        title = in.readString();
        content = in.readString();
        acaCode = in.readString();
        acaName = in.readString();
        date = in.readString();
        ptTime = in.readString();
        place = in.readString();
        participantsCnt = in.readInt();
        reservationCnt = in.readInt();
        fileId = in.readString();
        fileList = in.createTypedArrayList(FileData.CREATOR);
    }

    public static final Creator<BriefingData> CREATOR = new Creator<BriefingData>() {
        @Override
        public BriefingData createFromParcel(Parcel in) {
            return new BriefingData(in);
        }

        @Override
        public BriefingData[] newArray(int size) {
            return new BriefingData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(seq);
        parcel.writeString(title);
        parcel.writeString(content);
        parcel.writeString(acaCode);
        parcel.writeString(acaName);
        parcel.writeString(date);
        parcel.writeString(ptTime);
        parcel.writeString(place);
        parcel.writeInt(participantsCnt);
        parcel.writeInt(reservationCnt);
        parcel.writeString(fileId);
        parcel.writeTypedList(fileList);
    }
}

