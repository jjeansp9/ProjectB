package kr.jeet.edu.bus.view;

import android.graphics.drawable.Drawable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.request.transition.TransitionFactory;

public class DrawableAlwaysCrossFadeFactory implements TransitionFactory<Drawable> {
    private DrawableCrossFadeTransition resourceTransition = new DrawableCrossFadeTransition(300, true); // customize to your own needs or apply a builder pattern

    @Override
    public Transition<Drawable> build(DataSource dataSource, boolean isFirstResource) {
        return resourceTransition;
    }
}
