package kr.jeet.edu.bus.common;

import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.bus.model.data.ACAData;
import kr.jeet.edu.bus.model.data.BoardAttributeData;
import kr.jeet.edu.bus.model.data.BusInfoData;

public class DataManager {
    private static final String TAG = "dataMgr";

    public static DataManager getInstance() { return DataManager.LazyHolder.INSTANCE; }
    private static class LazyHolder { private static final DataManager INSTANCE = new DataManager(); }

    public final String BOARD_NOTICE = "notice";
    public final String BOARD_PT = "pt";

    // 캠퍼스 리스트
    private ArrayMap<String, ACAData> ACAListMap = new ArrayMap<>();
    // 게시판 정보 리스트
    private ArrayMap<String, BoardAttributeData> BoardListMap = new ArrayMap<>();
    // 버스 정보 리스트
    private List<BusInfoData> busInfoList = new ArrayList<>();

    public ArrayMap<String, ACAData> getACAListMap() {
        return ACAListMap;
    }
    public void setACAListMap(ArrayMap<String, ACAData> map) {
        this.ACAListMap =  map;
    }
    public boolean initACAListMap(List<ACAData> list)
    {
        if(list == null) return false;
        for(ACAData item : list) {
            String key = item.acaCode;
            if (!ACAListMap.containsKey(key)) {
                ACAListMap.put(key, item);
            }
        }
        return true;
    }
    public ACAData getACAData(String acaCode) {
        if(ACAListMap.containsKey(acaCode)) {
            return ACAListMap.get(acaCode);
        }
        return null;
    }
    public boolean setACAData(ACAData data) {
        if(data == null) return false;
        String key = data.acaCode;
        if(!ACAListMap.containsKey(key)) {
            ACAListMap.put(key, data);
            return true;
        }
        return false;
    }
    public ArrayMap<String, BoardAttributeData> getBoardInfoArrayMap() {
        return BoardListMap;
    }
    public void setBoardInfoMap(ArrayMap<String, BoardAttributeData> map) {
        this.BoardListMap =  map;
    }
    public BoardAttributeData getBoardInfo(String boardType) {
        if(BoardListMap.containsKey(boardType)) {
            return BoardListMap.get(boardType);
        }
        return null;
    }
    public boolean setBoardInfo(BoardAttributeData data) {
        if(data == null) return false;
        String key = data.boardType;
        if(!BoardListMap.containsKey(key)) {
            BoardListMap.put(key, data);
            return true;
        }
        return false;
    }

    /**
     * JEET 버스정보 리스트 정보 조회
     * @return List<BusInfoData>
     */
    public List<BusInfoData> getBusInfoList() {
        return busInfoList;
    }

    /**
     * JEET 버스정보 리스트 정보 저장
     * @param busInfoList
     */
    public void setbusInfoList(List<BusInfoData> busInfoList) {
        this.busInfoList = busInfoList;
    }
}
