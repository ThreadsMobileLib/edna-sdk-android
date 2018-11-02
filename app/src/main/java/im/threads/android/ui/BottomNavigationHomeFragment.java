package im.threads.android.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import im.threads.android.R;
import im.threads.controllers.ChatController;

/**
 * Пустой фрагмент для примера использования чата в качестве фрагмента в нижней навигации
 * Created by chybakut2004 on 12.04.17.
 */

public class BottomNavigationHomeFragment extends Fragment {

    private TextView unreadMessagesCount;
    private ChatController.UnreadMessagesCountListener unreadMessagesCountListener;

    public static BottomNavigationHomeFragment newInstance() {
        return new BottomNavigationHomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_navigation_home, container, false);
        unreadMessagesCount = (TextView) view.findViewById(R.id.unread_messages_count);

        // Обработка изменения количества непрочитанных в чате сообщений
        unreadMessagesCountListener = new ChatController.UnreadMessagesCountListener() {
            @Override
            public void onUnreadMessagesCountChanged(final int count) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        showUnreadMessagesCount(count);
                    }
                });
            }
        };

        ChatController.getUnreadMessagesCount(getActivity().getApplicationContext(), unreadMessagesCountListener);

        ChatController.setUnreadMessagesCountListener(unreadMessagesCountListener);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ChatController.removeUnreadMessagesCountListener();
        unreadMessagesCountListener = null;
    }

    public void showUnreadMessagesCount(int count) {
        unreadMessagesCount.setText(String.valueOf(count));
    }
}
