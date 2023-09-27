package kr.jeet.edu.bus.activity;

import android.os.Bundle;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.view.CustomAppbarLayout;

public class SelectBusActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_bus);
        initView();
        initAppbar();
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setLogoVisible(true);
        setSupportActionBar(customAppbar.getToolbar());
    }

    private void initData(){

    }

    @Override
    void initView() {
        initData();
    }


}