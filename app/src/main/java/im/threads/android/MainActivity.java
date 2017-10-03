package im.threads.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pushserver.android.PushBroadcastReceiver;
import com.pushserver.android.PushController;
import com.pushserver.android.PushServerIntentService;
import com.pushserver.android.model.PushMessage;
import com.sequenia.appwithchatdev.ChatBuilderHelper;

import im.threads.activities.ChatActivity;
import im.threads.controllers.ChatController;
import im.threads.utils.PermissionChecker;

/**
 * Активность с примерами открытия чата:
 * - в виде новой Активности
 * - в виде активности, где чат выступает в качестве фрагмента
 */
public class MainActivity extends AppCompatActivity {

    private static final int MIN_CLIENT_ID_LENGHT = 5;
    private static final int CHAT_PERMISSIONS_REQUEST_CODE = 1;

    private TextInputLayout clientIdLayout;
    private TextInputLayout userNameLayout;

    private Button chatActivityButton;
    private Button chatFragmentButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Перед работой с чатом должна быть настроена библиотека пуш уведомлений
        PushController.getInstance(this).init();

        setContentView(R.layout.activity_main);

        TextView versionView = (TextView) findViewById(R.id.version_name);
        versionView.setText(getString(R.string.lib_version, im.threads.BuildConfig.VERSION_NAME));

        clientIdLayout = (TextInputLayout) findViewById(R.id.client_id);
        userNameLayout = (TextInputLayout) findViewById(R.id.user_name);

        chatActivityButton = (Button) findViewById(R.id.chat_activity_button);
        chatFragmentButton = (Button) findViewById(R.id.chat_fragment_button);

        // Отслеживание Push-уведомлений, нераспознанных чатом.
        ChatController.setFullPushListener(new CustomFullPushListener());
        ChatController.setShortPushListener(new CustomShortPushListener());

        chatActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showChatAsActivity();
            }
        });
        chatFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showChatAsFragment();
            }
        });
    }

    /**
     * Пример открытия чата в виде Активности
     */
    private void showChatAsActivity() {
        if (!checkFieldValid()) {
            return;
        }

        String clientId = clientIdLayout.getEditText().getText().toString();
        String userName = userNameLayout.getEditText().getText().toString();

        // При открытии чата нужно проверить, выданы ли необходимые разрешения.
        if (!PermissionChecker.checkPermissions(this)) {
            PermissionChecker.requestPermissionsAndInit(CHAT_PERMISSIONS_REQUEST_CODE, this);
        } else {
            // генерируем настройки стилей чата
            ChatBuilderHelper.buildChatStyle(this, clientId, userName, "");
            startActivity(new Intent(this, ChatActivity.class));
        }
    }

    /**
     * Пример открытя чата в виде фрагмента
     */
    private void showChatAsFragment() {
        if (!checkFieldValid()) {
            return;
        }
        String clientId = clientIdLayout.getEditText().getText().toString();
        String userName = userNameLayout.getEditText().getText().toString();

        Intent i = BottomNavigationActivity.createIntent(this, clientId, userName);
        startActivity(i);
    }

    private boolean checkFieldValid() {
        if (clientIdLayout.getEditText() == null) {
            return false;
        }
        boolean isClientIdValid = clientIdLayout.getEditText().getText().length() >= MIN_CLIENT_ID_LENGHT;
        clientIdLayout.setError(isClientIdValid ? null : getString(R.string.client_id_error));
        return isClientIdValid;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CHAT_PERMISSIONS_REQUEST_CODE) {
            if(PermissionChecker.checkGrantResult(grantResults)) {
                showChatAsActivity();
            } else {
                Toast.makeText(this, "Without that permissions, application may not work properly", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class CustomShortPushListener implements ChatController.ShortPushListener {

        public static final String TAG = "CustomShortPushListener";

        @Override
        public void onNewShortPushNotification(PushBroadcastReceiver pushBroadcastReceiver, Context context, String s, Bundle bundle) {
            Log.i(TAG, "Short Push Accepted");
            Log.i(TAG, bundle.toString());
        }
    }

    public static class CustomFullPushListener implements ChatController.FullPushListener {

        public static final String TAG = "CustomFullPushListener";

        @Override
        public void onNewFullPushNotification(PushServerIntentService pushServerIntentService, PushMessage pushMessage) {
            Log.i(TAG, "Full Push Accepted");
            Log.i(TAG, pushMessage.fullMessage);
        }
    }
}
