package im.threads.android.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.azoft.carousellayoutmanager.CenterScrollListener;
import com.mfms.android.push_lite.PushBroadcastReceiver;
import com.mfms.android.push_lite.PushController;
import com.mfms.android.push_lite.PushServerIntentService;
import com.mfms.android.push_lite.repo.push.remote.model.PushMessage;

import java.util.List;

import im.threads.activities.ChatActivity;
import im.threads.android.R;
import im.threads.android.data.Card;
import im.threads.android.databinding.ActivityMainBinding;
import im.threads.android.utils.ChatBuilderHelper;
import im.threads.android.utils.PrefUtils;
import im.threads.controllers.ChatController;
import im.threads.utils.AppInfoHelper;
import im.threads.utils.PermissionChecker;

/**
 * Активность с примерами открытия чата:
 * - в виде новой Активности
 * - в виде активности, где чат выступает в качестве фрагмента
 */
public class MainActivity extends AppCompatActivity implements AddCardDialog.AddCardDialogActionsListener, YesNoDialog.YesNoDialogActionListener {

    private static final int CHAT_PERMISSIONS_REQUEST_CODE = 123;
    private static final int YES_NO_DIALOG_REQUEST_CODE = 323;

    private CardsAdapter cardsAdapter;
    private Card cardForDelete;

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Перед работой с чатом должна быть настроена библиотека пуш уведомлений
        PushController.getInstance(this).init();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        TextView versionView = (TextView) findViewById(R.id.version_name);
        versionView.setText(getString(R.string.lib_version, AppInfoHelper.getLibVersion()));

        final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL);
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
        binding.cardsView.setLayoutManager(layoutManager);
        binding.cardsView.setHasFixedSize(true);
        binding.cardsView.addOnScrollListener(new CenterScrollListener());
        cardsAdapter = new CardsAdapter();
        cardsAdapter.setRemoveCardListener(new CardsAdapter.RemoveCardListener() {
            @Override
            public void onRemoved(final Card card) {
                cardForDelete = card;
                YesNoDialog.open(MainActivity.this, getString(R.string.card_delete_text),
                                                            getString(R.string.card_delete_yes),
                                                            getString(R.string.card_delete_no),
                                                            YES_NO_DIALOG_REQUEST_CODE);

            }
        });
        binding.cardsView.setAdapter(cardsAdapter);

        updateViews();

        // Отслеживание Push-уведомлений, нераспознанных чатом.
        ChatController.setFullPushListener(new CustomFullPushListener());
        ChatController.setShortPushListener(new CustomShortPushListener());

        binding.chatActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showChatAsActivity();
            }
        });
        binding.chatFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showChatAsFragment();
            }
        });

        binding.addCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AddCardDialog.open(MainActivity.this);
            }
        });
    }

    private void updateViews() {
        updateViews(PrefUtils.getCards(this));
    }

    private void updateViews(List<Card> cards) {
        boolean hasCards = cards != null && !cards.isEmpty();
        binding.cardsView.setVisibility(hasCards ? View.VISIBLE : View.GONE);
        binding.addCard.setVisibility(!hasCards ? View.VISIBLE : View.GONE);
        binding.addCardHint.setVisibility(!hasCards ? View.VISIBLE : View.GONE);
        binding.chatActivityButton.setVisibility(hasCards ? View.VISIBLE : View.GONE);
        binding.chatFragmentButton.setVisibility(hasCards ? View.VISIBLE : View.GONE);

        if (hasCards) {
            cardsAdapter.setCards(cards);
        }
    }
    /**
     * Пример открытия чата в виде Активности
     */
    private void showChatAsActivity() {
        Card currentCard = getCurrentCard();

        // При открытии чата нужно проверить, выданы ли необходимые разрешения.
        if (!PermissionChecker.checkPermissions(this)) {
            PermissionChecker.requestPermissionsAndInit(CHAT_PERMISSIONS_REQUEST_CODE, this);
        } else {
            ChatBuilderHelper.buildChatStyle(this, currentCard.getAppMarker(), currentCard.getUserId(), currentCard.getUserName(),
                    "", getCurrentDesign());
            startActivity(new Intent(this, ChatActivity.class));
        }
    }

    private ChatBuilderHelper.ChatDesign getCurrentDesign() {
        return ChatBuilderHelper.ChatDesign.enumOf(this, (String) binding.designSpinner.getSelectedItem());
    }

    /**
     * Пример открытя чата в виде фрагмента
     */
    private void showChatAsFragment() {
        Card currentCard = getCurrentCard();
        Intent i = BottomNavigationActivity.createIntent(this, currentCard.getAppMarker(), currentCard.getUserId(),
                currentCard.getUserName(), getCurrentDesign());
        startActivity(i);
    }

    private Card getCurrentCard() {
        final CarouselLayoutManager layoutManager = (CarouselLayoutManager) binding.cardsView.getLayoutManager();
        return cardsAdapter.getCard(layoutManager.getCenterItemPosition());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_card, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_card) {
            AddCardDialog.open(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCardAdded(Card newCard) {
        List<Card> cards = PrefUtils.getCards(this);
        if (cards.contains(newCard)) {
            Toast.makeText(this, R.string.client_id_already_exist, Toast.LENGTH_LONG).show();
        }
        else {
            cards.add(newCard);
            updateViews(cards);
            PrefUtils.storeCards(this, cards);
        }
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onOkClicked(final int requestCode) {
        List<Card> cards = PrefUtils.getCards(this);
        if (cards.contains(cardForDelete)) {
            cards.remove(cardForDelete);
            updateViews(cards);
            PrefUtils.storeCards(this, cards);

            ChatController.logoutClient(this, cardForDelete.getUserId());
        }
        cardForDelete = null;
    }

    @Override
    public void onCancelClicked(final int requestCode) {
        cardForDelete = null;
    }

    public static class CustomShortPushListener implements ChatController.ShortPushListener {

        public static final String TAG = "CustomShortPushListener";

        @Override
        public void onNewShortPushNotification(PushBroadcastReceiver pushBroadcastReceiver, Context context, String s, Bundle bundle) {
            Log.i(TAG, "Short push not accepted by chat: " + bundle.toString());
            Toast.makeText(context, "Short push not accepted by chat: " + bundle.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public static class CustomFullPushListener implements ChatController.FullPushListener {

        public static final String TAG = "CustomFullPushListener";

        @Override
        public void onNewFullPushNotification(PushServerIntentService pushServerIntentService, PushMessage pushMessage) {
            Toast.makeText(pushServerIntentService.getApplicationContext(), "Full push not accepted by chat: " + String.valueOf(pushMessage), Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Full push not accepted by chat: " + String.valueOf(pushMessage));
        }
    }
}
