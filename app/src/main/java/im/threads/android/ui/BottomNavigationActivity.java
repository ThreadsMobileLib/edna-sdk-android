package im.threads.android.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import im.threads.ThreadsLib;
import im.threads.UserInfoBuilder;
import im.threads.android.R;
import im.threads.android.utils.ChatStyleBuilderHelper;
import im.threads.view.ChatFragment;

/**
 * Пример активности с нижней навигацией,
 * где чат выступает в роли одного из пунктов меню.
 * <p>
 * Для использования чата в виде фрагмента
 * нужно создать его экземпляр, вызвав метод ChatFragment.newInstance(Bundle bundle),
 * передав в него Bundle с настройками.
 * <p>
 * Чтобы корректно обработать навигацию внутри чата,
 * переопределите у Активности метод onBackPressed()
 * и вызовите метод onBackPressed() у ChatFragment,
 * если в данный момент показан он.
 * Метод вернет true, если чат должен быть закрыт.
 * Для подробностей смотрите метод onBackPressed().
 */
public class BottomNavigationActivity extends AppCompatActivity {

    public static final String ARG_CLIENT_ID = "clientId";
    public static final String ARG_USER_NAME = "userName";
    public static final String ARG_APP_MARKER = "appMarker";
    public static final String ARG_CLIENT_ID_SIGNATURE = "clientIdSignature";
    public static final String ARG_NEEDS_SHOW_CHAT = "needsShowChat";
    private static final String ARG_CHAT_DESIGN = "chatDesign";

    private String clientId;
    private String clientIdSignature;
    private String userName;
    private String appMarker;
    private ChatStyleBuilderHelper.ChatDesign chatDesign;

    private BottomNavigationView bottomNavigationView;
    private TabItem selectedTab;

    private BottomNavigationHomeFragment homeFragment;
    private ChatFragment chatFragment;

    private enum TabItem {
        TAB_HOME(R.id.navigation_home),
        TAB_CHAT(R.id.navigation_chat);

        private int menuId;

        TabItem(final int menuId) {
            this.menuId = menuId;
        }

        public int getMenuId() {
            return menuId;
        }
    }

    /**
     * @return intent для открытия BottomNavigationActivity
     * с передачей clientId и userName.
     */
    public static Intent createIntent(Activity activity,
                                      String appMarker,
                                      String clientId,
                                      String clientIdSignature,
                                      String userName,
                                      ChatStyleBuilderHelper.ChatDesign chatDesign) {
        Intent intent = new Intent(activity, BottomNavigationActivity.class);
        intent.putExtra(ARG_APP_MARKER, appMarker);
        intent.putExtra(ARG_CLIENT_ID, clientId);
        intent.putExtra(ARG_CLIENT_ID_SIGNATURE, clientIdSignature);
        intent.putExtra(ARG_USER_NAME, userName);
        intent.putExtra(ARG_CHAT_DESIGN, chatDesign);
        return intent;
    }

    public static PendingIntent createPendingIntent(Context context,
                                                    String clientId,
                                                    String userName,
                                                    String appMarker,
                                                    String clientIdSignature,
                                                    ChatStyleBuilderHelper.ChatDesign chatDesign) {
        Intent i = new Intent(context, BottomNavigationActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(ARG_NEEDS_SHOW_CHAT, true);
        i.putExtra(ARG_CLIENT_ID, clientId);
        i.putExtra(ARG_USER_NAME, userName);
        i.putExtra(ARG_APP_MARKER, appMarker);
        i.putExtra(ARG_CLIENT_ID_SIGNATURE, clientIdSignature);
        i.putExtra(ARG_CHAT_DESIGN, chatDesign);
        return PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                selectTab(TabItem.TAB_HOME);
                return true;
            case R.id.navigation_chat:
                selectTab(TabItem.TAB_CHAT);
                return true;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        Intent intent = getIntent();
        clientId = intent.getStringExtra(ARG_CLIENT_ID);
        userName = intent.getStringExtra(ARG_USER_NAME);
        appMarker = intent.getStringExtra(ARG_APP_MARKER);
        clientIdSignature = intent.getStringExtra(ARG_CLIENT_ID_SIGNATURE);
        chatDesign = (ChatStyleBuilderHelper.ChatDesign) intent.getSerializableExtra(ARG_CHAT_DESIGN);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // При открытии экрана из пуш уведомления нужно сразу открыть чат,
        // а не главную страницу
        if (intent.getBooleanExtra(ARG_NEEDS_SHOW_CHAT, false)) {
            bottomNavigationView.setSelectedItemId(TabItem.TAB_CHAT.getMenuId());
        }
        // после переворота экрана открываем последнюю выбранную вкладку
        else if (savedInstanceState != null) {
            TabItem savedTab = (TabItem) savedInstanceState.getSerializable("selectedTab");
            if (savedTab == null) {
                savedTab = TabItem.TAB_HOME;
            }
            bottomNavigationView.setSelectedItemId(savedTab.getMenuId());
        }
        // при первом входе в приложение открываем главную страницу
        else {
            bottomNavigationView.setSelectedItemId(TabItem.TAB_HOME.getMenuId());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(ARG_NEEDS_SHOW_CHAT, false)) {
            bottomNavigationView.setSelectedItemId(TabItem.TAB_CHAT.getMenuId());
        }
    }

    private void selectTab(final TabItem newTabItem) {
        if (newTabItem == null) {
            return;
        }
        showActionBar(newTabItem);
        final FragmentManager fm = getSupportFragmentManager();
        final Fragment currentFragment = fm.findFragmentById(R.id.content);
        if (currentFragment != null && selectedTab == newTabItem) {
            // не показываем повторно текущую вкладку
            return;
        }
        selectedTab = newTabItem;
        Fragment fragment = null;
        switch (newTabItem) {
            case TAB_HOME:
                if (homeFragment == null) {
                    homeFragment = BottomNavigationHomeFragment.newInstance();
                }
                fragment = homeFragment;
                break;
            case TAB_CHAT:

                if (chatFragment == null) {
                    chatFragment = ChatFragment.newInstance();
                }
                ThreadsLib.getInstance().initUser(
                        new UserInfoBuilder(clientId)
                                .setClientIdSignature(clientIdSignature)
                                .setUserName(userName)
                                .setData("{\"phone\": \"+7-999-999-99-99\",\"email\": \"e@mail.com\"}")
                                .setAppMarker(appMarker)
                );
                ThreadsLib.getInstance().applyChatStyle(ChatStyleBuilderHelper.getChatStyle(chatDesign));
                fragment = chatFragment;
                break;
        }
        // добавляем фрагмент в контейнер
        if (fragment != null) {
            fm.beginTransaction()
                    .replace(R.id.content, fragment)
                    .commit();
            fm.executePendingTransactions();
        }
    }

    /**
     * Меняет состояние ActionBar в зависимости от выбранной вкладки
     *
     * @param tabItem выбранная вкладка
     */
    private void showActionBar(final TabItem tabItem) {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            switch (tabItem) {
                case TAB_HOME:
                    actionBar.show();
                    break;
                case TAB_CHAT:
                    actionBar.hide(); // Скрываем ActionBar, т.к. внутри чата есть свой
                    break;
            }
        }
    }

    /**
     * Обработка нажатия кнопки Back.
     * Если в данный момент показан фрагмент чата, то при нажатии на кнопку Back
     * переходим на главный фрагмент.
     */
    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content);
        if (fragment instanceof ChatFragment) {
            // Если чат нужно закрыть, возвращаем пользователя на предыдущий открытый экран
            boolean needsCloseChat = ((ChatFragment) fragment).onBackPressed();
            if (needsCloseChat) {
                bottomNavigationView.setSelectedItemId(TabItem.TAB_HOME.getMenuId());
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable("selectedTab", selectedTab);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
