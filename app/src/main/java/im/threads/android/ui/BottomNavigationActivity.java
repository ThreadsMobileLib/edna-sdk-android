package im.threads.android.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.TimeUnit;

import im.threads.ThreadsLib;
import im.threads.UserInfoBuilder;
import im.threads.android.R;
import im.threads.android.utils.ChatStyleBuilderHelper;
import im.threads.view.ChatFragment;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

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
    public static final String ARG_APP_MARKER = "appMarker";
    public static final String ARG_CLIENT_DATA = "clientData";
    public static final String ARG_CLIENT_ID_SIGNATURE = "clientIdSignature";
    public static final String ARG_AUTH_TOKEN = "authToken";
    public static final String ARG_AUTH_SCHEMA = "authSchema";
    public static final String ARG_NEEDS_SHOW_CHAT = "needsShowChat";
    private static final String ARG_CHAT_DESIGN = "chatDesign";

    private String clientId;
    private String clientData;
    private String clientIdSignature;
    private String authToken;
    private String authSchema;
    private String appMarker;
    private ChatStyleBuilderHelper.ChatDesign chatDesign;

    private BottomNavigationView bottomNavigationView;
    private TabItem selectedTab;

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
        if (item.getItemId() == R.id.navigation_home) {
            selectTab(TabItem.TAB_HOME);
            return true;
        } else if (item.getItemId() == R.id.navigation_chat) {
            selectTab(TabItem.TAB_CHAT);
            return true;
        }
        return false;
    };
    private CompositeDisposable compositeDisposable;

    /**
     * @return intent для открытия BottomNavigationActivity
     * с передачей clientId и userName.
     */
    public static Intent createIntent(Activity activity,
                                      String appMarker,
                                      String clientId,
                                      String clientData,
                                      String clientIdSignature,
                                      String authToken,
                                      String authSchema,
                                      ChatStyleBuilderHelper.ChatDesign chatDesign) {
        Intent intent = new Intent(activity, BottomNavigationActivity.class);
        intent.putExtra(ARG_APP_MARKER, appMarker);
        intent.putExtra(ARG_CLIENT_ID, clientId);
        intent.putExtra(ARG_CLIENT_DATA, clientData);
        intent.putExtra(ARG_CLIENT_ID_SIGNATURE, clientIdSignature);
        intent.putExtra(ARG_AUTH_TOKEN, authToken);
        intent.putExtra(ARG_AUTH_SCHEMA, authSchema);
        intent.putExtra(ARG_CHAT_DESIGN, chatDesign);
        return intent;
    }

    public static PendingIntent createPendingIntent(Context context,
                                                    String clientId,
                                                    String clientData,
                                                    String appMarker,
                                                    String clientIdSignature,
                                                    String authToken,
                                                    String authSchema,
                                                    ChatStyleBuilderHelper.ChatDesign chatDesign) {
        Intent intent = new Intent(context, BottomNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ARG_NEEDS_SHOW_CHAT, true);
        intent.putExtra(ARG_CLIENT_ID, clientId);
        intent.putExtra(ARG_CLIENT_DATA, clientData);
        intent.putExtra(ARG_APP_MARKER, appMarker);
        intent.putExtra(ARG_CLIENT_ID_SIGNATURE, clientIdSignature);
        intent.putExtra(ARG_AUTH_TOKEN, authToken);
        intent.putExtra(ARG_AUTH_SCHEMA, authSchema);
        intent.putExtra(ARG_CHAT_DESIGN, chatDesign);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        Intent intent = getIntent();
        clientId = intent.getStringExtra(ARG_CLIENT_ID);
        appMarker = intent.getStringExtra(ARG_APP_MARKER);
        clientData = intent.getStringExtra(ARG_CLIENT_DATA);
        clientIdSignature = intent.getStringExtra(ARG_CLIENT_ID_SIGNATURE);
        authToken = intent.getStringExtra(ARG_AUTH_TOKEN);
        authSchema = intent.getStringExtra(ARG_AUTH_SCHEMA);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.login) {
            login();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                ThreadsLib.getInstance().applyChatStyle(ChatStyleBuilderHelper.getChatStyle(chatDesign));
                fragment = ChatFragment.newInstance();
                break;
        }
        // добавляем фрагмент в контейнер
        fm.beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
        fm.executePendingTransactions();
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
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable("selectedTab", selectedTab);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unsubscribeAll();
    }

    private void login() {
        subscribe(
                Completable.timer(5, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> ThreadsLib.getInstance().initUser(
                                        new UserInfoBuilder(clientId)
                                                .setAuthData(authToken, authSchema)
                                                .setClientIdSignature(clientIdSignature)
                                                .setClientData(clientData)
                                                .setAppMarker(appMarker)
                                )
                        )
        );
    }

    private void subscribe(final Disposable event) {
        if (compositeDisposable == null || compositeDisposable.isDisposed()) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(event);
    }

    private void unsubscribeAll() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
            compositeDisposable = null;
        }
    }

    private enum TabItem {
        TAB_HOME(R.id.navigation_home),
        TAB_CHAT(R.id.navigation_chat);

        private final int menuId;

        TabItem(final int menuId) {
            this.menuId = menuId;
        }

        public int getMenuId() {
            return menuId;
        }
    }
}
