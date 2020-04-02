package im.threads.android.utils;

import android.view.View;

import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class CardsSnapHelper extends LinearSnapHelper {

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        View centerView = findSnapView(layoutManager);
        if (centerView == null) {
            return RecyclerView.NO_POSITION;
        }
        int position = layoutManager.getPosition(centerView);
        int targetPosition = -1;
        if (layoutManager.canScrollHorizontally()) {
            if (velocityX < 0) {
                targetPosition = position - 1;
            } else {
                targetPosition = position + 1;
            }
        }
        return Math.min(layoutManager.getItemCount() - 1, Math.max(targetPosition, 0));
    }
}
