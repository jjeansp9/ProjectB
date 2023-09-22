package kr.jeet.edu.bus.model.data;

public class BusRouteData {
    public String bpCode; // 노선 코드
    public String bpName; // 노선 이름
    public String isArrive; // 도착여부
    public boolean isSuccess = false; // 서버 통신 성공여부
    public boolean setClickable = false; // 클릭 활성화 여부
}
