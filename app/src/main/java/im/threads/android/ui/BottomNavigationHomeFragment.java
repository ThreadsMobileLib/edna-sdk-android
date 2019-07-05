package im.threads.android.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.threads.android.R;
import im.threads.controllers.ChatController;

/**
 * Пустой фрагмент для примера использования чата в качестве фрагмента в нижней навигации
 * Created by chybakut2004 on 12.04.17.
 */

public class BottomNavigationHomeFragment extends Fragment {

    private TextView unreadMessagesCount;

    public static BottomNavigationHomeFragment newInstance() {
        return new BottomNavigationHomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_navigation_home, container, false);
        unreadMessagesCount = view.findViewById(R.id.unread_messages_count);

        // Обработка изменения количества непрочитанных в чате сообщений
        ChatController.UnreadMessagesCountListener unreadMessagesCountListener =
                count -> new Handler(Looper.getMainLooper())
                        .post(() -> unreadMessagesCount.setText(String.valueOf(count)));

        ChatController.getUnreadMessagesCount(getActivity().getApplicationContext(), unreadMessagesCountListener);

        ChatController.setUnreadMessagesCountListener(unreadMessagesCountListener);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ChatController.removeUnreadMessagesCountListener();
    }
}
