package kr.jeet.edu.bus.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class FileData implements Parcelable {
    @SerializedName("seq")
    public int seq;

    @SerializedName("fileId")
    public String fileId;

    @SerializedName("orgName")
    public String orgName;

    @SerializedName("saveName")
    public String saveName;

    @SerializedName("path")
    public String path;

    @SerializedName("extension")
    public String extension;
    public FileData(){}
    protected FileData(Parcel in) {
        seq = in.readInt();
        fileId = in.readString();
        orgName = in.readString();
        saveName = in.readString();
        path = in.readString();
        extension = in.readString();
    }

    public static final Creator<FileData> CREATOR = new Creator<FileData>() {
        @Override
        public FileData createFromParcel(Parcel in) {
            return new FileData(in);
        }

        @Override
        public FileData[] newArray(int size) {
            return new FileData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(seq);
        parcel.writeString(fileId);
        parcel.writeString(orgName);
        parcel.writeString(saveName);
        parcel.writeString(path);
        parcel.writeString(extension);
    }
}
