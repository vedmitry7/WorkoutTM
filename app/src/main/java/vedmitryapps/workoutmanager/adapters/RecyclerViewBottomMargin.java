package vedmitryapps.workoutmanager.adapters;

import android.graphics.Rect;
import android.support.annotation.IntRange;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import vedmitryapps.workoutmanager.App;

public class RecyclerViewBottomMargin extends RecyclerView.ItemDecoration {

    int bottomMargin;
    public RecyclerViewBottomMargin(@IntRange(from = 0) int margin) {
        bottomMargin = margin;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {

        int position = parent.getChildLayoutPosition(view);

        if(position == parent.getAdapter().getItemCount()-1){
            outRect.bottom = App.dpToPx(parent.getContext(), bottomMargin);
        }
    }
}