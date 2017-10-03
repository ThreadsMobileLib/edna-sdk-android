package im.threads.android;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.pushserver.android.PushController;
import com.sequenia.appwithchatdev.ChatBuilderHelper;

import im.threads.controllers.ChatController;
import im.threads.fragments.ChatFragment;
import im.threads.utils.PermissionChecker;

/**
 * Пример активности с нижней навигацией,
 * где чат выступает в роли одного из пунктов меню.
 *
 * Для использования чата в виде фрагмента
 * нужно создать его экземпляр, вызвав метод ChatFragment.newInstance(Bundle bundle),
 * передав в него Bundle с настройками.
 *
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
    public static final String ARG_NEEDS_SHOW_CHAT = "needsShowChat";

    private static final int PERM_REQUEST_CODE_CLICK = 1;

    private String clientId;
    private String userName;

    private BottomNavigationView bottomNavigationView;
    private TabItem selectedTab;

    private boolean showChatAfterGrantPermission;

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
    public static Intent createIntent(Activity activity, String clientId, String userName) {
        Intent intent = new Intent(activity, BottomNavigationActivity.class);
        intent.putExtra(ARG_CLIENT_ID, clientId);
        intent.putExtra(ARG_USER_NAME, userName);
        return intent;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    selectTab(TabItem.TAB_HOME);
                    return true;
                case R.id.navigation_chat:
                    // При попытке открыть чат нужно спросить разрешения.
                    if (!PermissionChecker.checkPermissions(BottomNavigationActivity.this)) {
                        PermissionChecker.requestPermissionsAndInit(PERM_REQUEST_CODE_CLICK, BottomNavigationActivity.this);
                        return false;
                    } else {
                        selectTab(TabItem.TAB_CHAT);
                        return true;
                    }
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);

        // Перед работой с чатом нужно инициализировать библиотеку пушей.
        PushController.getInstance(this).init();

        // При использовании чата в виде фрагмента нужно настроить логику открытия приложения
        // из пуш уведомления, так как по умолчанию открывается ChatActivity.
        ChatController.setPendingIntentCreator(chatWithFragmentPendingIntentCreator());

        Intent intent = getIntent();
        clientId = intent.getStringExtra(ARG_CLIENT_ID);
        userName = intent.getStringExtra(ARG_USER_NAME);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // При открытии экрана из пуш уведомления нужно сразу открыть чат,
        // а не главную страницу
        if (intent.getBooleanExtra(ARG_NEEDS_SHOW_CHAT, false)) {
            updateNavigationBarState(TabItem.TAB_CHAT.getMenuId());
        }
        // после переворота экрана открываем последнюю выбранную вкладку
        else if (savedInstanceState != null) {
            TabItem savedTab = (TabItem) savedInstanceState.getSerializable("selectedTab");
            if (savedTab == null) {
                savedTab = TabItem.TAB_HOME;
            }
            updateNavigationBarState(savedTab.getMenuId());
        }
        // при первом входе в приложение открываем главную страницу
        else {
            updateNavigationBarState(TabItem.TAB_HOME.getMenuId());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(ARG_NEEDS_SHOW_CHAT, false)) {
            updateNavigationBarState(TabItem.TAB_CHAT.getMenuId());
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
                fragment = BottomNavigationHomeFragment.newInstance();
                break;
            case TAB_CHAT:
                // генерируем настройки стилей чата
                ChatBuilderHelper.buildChatStyle(this, clientId, userName, "");
                // создаем фрагмент чата
                fragment = ChatFragment.newInstance();
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
     * Активирует программно выбранную вкладку
     * @param selectedMenuId id выбранной вкладки
     */
    private void updateNavigationBarState(int selectedMenuId){
        View view = bottomNavigationView.findViewById(selectedMenuId);
        view.performClick();
    }

    /**
     * Меняет состояние ActionBar в зависимости от выбранной вкладки
     * @param tabItem выбранная вкладка
     */
    private void showActionBar(final TabItem tabItem) {
        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
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
        if(fragment instanceof ChatFragment) {
            // Если чат нужно закрыть, возвращаем пользователя на предыдущий открытый экран
            boolean needsCloseChat = ((ChatFragment) fragment).onBackPressed();
            if (needsCloseChat) {
                updateNavigationBarState(TabItem.TAB_HOME.getMenuId());
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_REQUEST_CODE_CLICK) {
            if(PermissionChecker.checkGrantResult(grantResults)) {
                showChatAfterGrantPermission = true;
            } else {
                Toast.makeText(this, "Without that permissions, application may not work properly", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public ChatController.PendingIntentCreator chatWithFragmentPendingIntentCreator() {
        return new ChatController.PendingIntentCreator() {
            @Override
            public PendingIntent createPendingIntent(Context context) {
                Intent i = new Intent(context, BottomNavigationActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra(ARG_NEEDS_SHOW_CHAT, true);
                return PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (showChatAfterGrantPermission) {
            updateNavigationBarState(TabItem.TAB_CHAT.getMenuId());
            showChatAfterGrantPermission = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable("selectedTab", selectedTab);
    }
}
