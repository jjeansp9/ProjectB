package kr.jeet.edu.bus.model.data;

import android.os.Parcel;
import android.os.Parcelable;

public class BusInfoData implements Parcelable {
    public String bcName;         // 버스 캠퍼스 이름
    public String busName;        // 버스 버스 이름
    public int busCode;           // 버스 코드
    public String busPhoneNumber; // 기사(동승자) 휴대폰 번호
    public int busDriveSeq = 0;       // 버스 운행 seq

    public BusInfoData() {
    }

    protected BusInfoData(Parcel in) {
        bcName = in.readString();
        busName = in.readString();
        busCode = in.readInt();
        busPhoneNumber = in.readString();
        busDriveSeq = in.readInt();
    }

    public static final Creator<BusInfoData> CREATOR = new Creator<BusInfoData>() {
        @Override
        public BusInfoData createFromParcel(Parcel in) {
            return new BusInfoData(in);
        }

        @Override
        public BusInfoData[] newArray(int size) {
            return new BusInfoData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bcName);
        dest.writeString(busName);
        dest.writeInt(busCode);
        dest.writeString(busPhoneNumber);
        dest.writeInt(busDriveSeq);
    }
}
