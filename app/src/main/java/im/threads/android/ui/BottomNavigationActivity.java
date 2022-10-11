package im.threads.android.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import im.threads.ui.ChatStyle;
import im.threads.business.UserInfoBuilder;
import im.threads.android.R;
import im.threads.android.utils.ChatDesign;
import im.threads.android.utils.ChatStyleBuilderHelper;
import im.threads.android.utils.PermissionDescriptionDialogStyleBuilderHelper;
import im.threads.business.annotation.OpenWay;
import im.threads.business.logger.LoggerEdna;
import im.threads.ui.core.ThreadsLib;
import im.threads.ui.fragments.ChatFragment;
import im.threads.ui.styles.permissions.PermissionDescriptionType;
import im.threads.ui.utils.ColorsHelper;
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
    private ChatDesign chatDesign;

    private BottomNavigationView bottomNavigationView;
    private TabItem selectedTab;
    private Intent intent;

    private Fragment homeFragment, chatFragment;

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
                                      ChatDesign chatDesign) {
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
                                                    ChatDesign chatDesign) {
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
        int flags = PendingIntent.FLAG_CANCEL_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        int requestCode = (int)System.currentTimeMillis();
        return PendingIntent.getActivity(context, requestCode, intent, flags);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        this.intent = getIntent();
        clientId = intent.getStringExtra(ARG_CLIENT_ID);
        appMarker = intent.getStringExtra(ARG_APP_MARKER);
        clientData = intent.getStringExtra(ARG_CLIENT_DATA);
        clientIdSignature = intent.getStringExtra(ARG_CLIENT_ID_SIGNATURE);
        authToken = intent.getStringExtra(ARG_AUTH_TOKEN);
        authSchema = intent.getStringExtra(ARG_AUTH_SCHEMA);
        chatDesign = (ChatDesign) intent.getSerializableExtra(ARG_CHAT_DESIGN);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        ChatStyle style = ChatStyleBuilderHelper.getChatStyle(chatDesign);
        ColorsHelper.setStatusBarColor(new WeakReference<>(this), style.chatStatusBarColorResId, style.windowLightStatusBarResId);

        int checkedColor;
        if (style.chatBodyIconsTint != 0) {
            checkedColor = style.chatBodyIconsTint;
        } else if (style.chatToolbarColorResId != 0) {
            checkedColor = style.chatToolbarColorResId;
        } else {
            checkedColor = R.color.threads_black;
        }
        ColorStateList iconColorStates = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{
                        ContextCompat.getColor(this, style.chatDisabledTextColor),
                        ContextCompat.getColor(this, checkedColor)
                });

        bottomNavigationView.setItemIconTintList(iconColorStates);
        bottomNavigationView.setItemTextColor(iconColorStates);

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
        this.intent = intent;
        if (intent.getBooleanExtra(ARG_NEEDS_SHOW_CHAT, false)) {
            bottomNavigationView.setSelectedItemId(TabItem.TAB_CHAT.getMenuId());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
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
        boolean needToShowChat = intent.getBooleanExtra(ARG_NEEDS_SHOW_CHAT, false);
        if (currentFragment != null &&
                selectedTab == newTabItem &&
                !needToShowChat) {
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
                applyChatStyles(chatDesign);
                if (chatFragment == null) {
                    chatFragment = ChatFragment.newInstance(needToShowChat ? OpenWay.FROM_PUSH : OpenWay.DEFAULT);
                }
                fragment = chatFragment;
                break;
        }
        // добавляем фрагмент в контейнер
        fm.beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
        fm.executePendingTransactions();
    }

    private void applyChatStyles(@NonNull ChatDesign chatDesign) {
        ThreadsLib.getInstance().applyChatStyle(ChatStyleBuilderHelper.getChatStyle(chatDesign));
        ThreadsLib.getInstance().applyStoragePermissionDescriptionDialogStyle(
                PermissionDescriptionDialogStyleBuilderHelper.getDialogStyle(
                        chatDesign,
                        PermissionDescriptionType.STORAGE
                )
        );
        ThreadsLib.getInstance().applyRecordAudioPermissionDescriptionDialogStyle(
                PermissionDescriptionDialogStyleBuilderHelper.getDialogStyle(
                        chatDesign,
                        PermissionDescriptionType.RECORD_AUDIO
                )
        );
        ThreadsLib.getInstance().applyCameraPermissionDescriptionDialogStyle(
                PermissionDescriptionDialogStyleBuilderHelper.getDialogStyle(
                        chatDesign,
                        PermissionDescriptionType.CAMERA
                )
        );
    }

    /**
     * Меняет состояние ActionBar в зависимости от выбранной вкладки
     *
     * @param tabItem выбранная вкладка
     */
    private void showActionBar(final TabItem tabItem) {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            ChatStyle style = ChatStyleBuilderHelper.getChatStyle(chatDesign);
            actionBar.setBackgroundDrawable(ContextCompat.getDrawable(getBaseContext(), style.chatToolbarColorResId));
            Spannable text = new SpannableString(actionBar.getTitle());
            text.setSpan(new ForegroundColorSpan(getResources().getColor(style.chatToolbarTextColorResId)), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            actionBar.setTitle(text);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    && isTaskRoot()
                    && getSupportFragmentManager().getPrimaryNavigationFragment() != null
                    && getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager().getBackStackEntryCount() == 0
                    && getSupportFragmentManager().getBackStackEntryCount() == 0
            ) {
                finishAfterTransition();
            } else {
                super.onBackPressed();
            }
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
        homeFragment = null;
        chatFragment = null;
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
                                ),
                                throwable -> LoggerEdna.error("login: ", throwable)
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
