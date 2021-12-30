package im.threads.android.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import im.threads.android.R;
import im.threads.android.core.ThreadsDemoApplication;
import im.threads.internal.utils.ThreadsLogger;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Пустой фрагмент для примера использования чата в качестве фрагмента в нижней навигации
 */
public class BottomNavigationHomeFragment extends BaseFragment {

    private static final String TAG = BottomNavigationHomeFragment.class.getSimpleName();
    private TextView unreadMessagesCount;

    public static BottomNavigationHomeFragment newInstance() {
        return new BottomNavigationHomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_navigation_home, container, false);
        unreadMessagesCount = view.findViewById(R.id.unread_messages_count);
        subscribe(
                ThreadsDemoApplication.getUnreadMessagesSubject()
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(count -> unreadMessagesCount.setText(String.valueOf(count)),
                                error -> ThreadsLogger.e(TAG, "onCreateView: " + error.getMessage())
                        )

        );
        return view;
    }
}