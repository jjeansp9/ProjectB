package kr.jeet.edu.bus.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import kr.jeet.edu.bus.model.data.FileData;
import kr.jeet.edu.bus.server.RetrofitApi;
import kr.jeet.edu.bus.utils.FileUtils;
import kr.jeet.edu.bus.utils.Utils;
import kr.jeet.edu.bus.view.photoview.PhotoView;

public class PhotoViewPagerAdapter extends PagerAdapter {
    private String TAG = PhotoViewPagerAdapter.class.getSimpleName();
    public enum Action{Delete, Download};
    public interface onItemClickListener{ public void onActionBtnClick(String file, Action action); }
//
    private ArrayList<FileData> mImageList = new ArrayList<>();
    private ArrayList<String> _webImageList = new ArrayList<>();
    private TextView mTvPage;
    boolean isFileData = false;
//    public PhotoViewPagerAdapter(ArrayList<FileData> mImageList, TextView mTvPage) {
//        this.mImageList = mImageList;
//        this.mTvPage = mTvPage;
//        isFileData = true;
//    }
    public PhotoViewPagerAdapter(TextView mTvPage, ArrayList<String> webImageList) {
        this._webImageList = webImageList;
        this.mTvPage = mTvPage;
        isFileData = false;
    }
    // pager의 전체 페이지 수 설정
    @Override
    public int getCount() {
        if(isFileData) {
            if (mImageList == null) return 0;
            return mImageList.size();
        }else{
            if (_webImageList == null) return 0;
            return _webImageList.size();
        }
    }

    public View instantiateItem(ViewGroup container, int position) {
        PhotoView photoView = new PhotoView(container.getContext());
        int paddingInPx = Utils.fromDpToPx(2);
        photoView.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);
        String imageUrl = "";
        if(isFileData) {
            if (mImageList != null && mImageList.size() > 0) { // 이미지가 있는 경우
                imageUrl = RetrofitApi.FILE_SUFFIX_URL + mImageList.get(0).path + "/" + mImageList.get(position).saveName;
                imageUrl = FileUtils.replaceMultipleSlashes(imageUrl);
//                Glide.with(container)
//                        .load(imageUrl)
//                        .apply(new RequestOptions()
//                                        .placeholder(R.drawable.ic_vector_image_placeholder) // 로딩 중 이미지
////                            .error(R.drawable.ic_vector_image_error) // 로딩 실패 시 이미지
//                        )
//                        .transition(DrawableTransitionOptions.with(new DrawableAlwaysCrossFadeFactory()))
//                        .into(photoView);
            }
        }else{
            if (_webImageList != null && _webImageList.size() > 0) { // 이미지가 있는 경우
                imageUrl = _webImageList.get(position);
                imageUrl = FileUtils.replaceMultipleSlashes(imageUrl);

            }
        }
        Glide.with(container)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//                    .transition(DrawableTransitionOptions.with(new DrawableAlwaysCrossFadeFactory()))
                .into(photoView);
        // Now just add PhotoView to ViewPager and return it
        container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); // 사이즈는 MATCH_PARENT 크기로 add

        return photoView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) { // ViewPager에서 페이지를 제거할 때 호출되는 메소드
        // 뷰페이저에서 삭제
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
