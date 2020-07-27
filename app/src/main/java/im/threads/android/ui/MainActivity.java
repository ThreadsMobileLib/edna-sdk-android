package im.threads.android.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.threads.ThreadsLib;
import im.threads.UserInfoBuilder;
import im.threads.android.R;
import im.threads.android.data.Card;
import im.threads.android.databinding.ActivityMainBinding;
import im.threads.android.utils.CardsLinearLayoutManager;
import im.threads.android.utils.CardsSnapHelper;
import im.threads.android.utils.ChatStyleBuilderHelper;
import im.threads.android.utils.PrefUtils;
import im.threads.view.ChatActivity;

/**
 * Активность с примерами открытия чата:
 * - в виде новой Активности
 * - в виде активности, где чат выступает в качестве фрагмента
 */
public class MainActivity extends AppCompatActivity implements AddCardDialog.AddCardDialogActionsListener, YesNoDialog.YesNoDialogActionListener {

    private static final int YES_NO_DIALOG_REQUEST_CODE = 323;

    private CardsAdapter cardsAdapter;
    private CardsSnapHelper cardsSnapHelper;
    private Card cardForDelete;

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setViewModel(this);
        TextView versionView = findViewById(R.id.version_name);
        versionView.setText(getString(R.string.lib_version, ThreadsLib.getLibVersion(), getString(R.string.transport_type)));
        cardsSnapHelper = new CardsSnapHelper();
        cardsSnapHelper.attachToRecyclerView(binding.cardsView);
        binding.cardsView.setLayoutManager(new CardsLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.cardsView.setHasFixedSize(true);
        cardsAdapter = new CardsAdapter();
        cardsAdapter.setRemoveCardListener(card -> {
            cardForDelete = card;
            YesNoDialog.open(MainActivity.this, getString(R.string.card_delete_text),
                    getString(R.string.card_delete_yes),
                    getString(R.string.card_delete_no),
                    YES_NO_DIALOG_REQUEST_CODE);

        });
        binding.cardsView.setAdapter(cardsAdapter);
        showCards(PrefUtils.getCards(this));
    }

    private void showCards(List<Card> cards) {

        boolean hasCards = cards != null && !cards.isEmpty();

        binding.addCard.setVisibility(!hasCards ? View.VISIBLE : View.GONE);
        binding.addCardHint.setVisibility(!hasCards ? View.VISIBLE : View.GONE);

        binding.cardsView.setVisibility(hasCards ? View.VISIBLE : View.GONE);
        binding.chatActivityButton.setVisibility(hasCards ? View.VISIBLE : View.GONE);
        binding.chatFragmentButton.setVisibility(hasCards ? View.VISIBLE : View.GONE);
        binding.sendMessageButton.setVisibility(hasCards ? View.VISIBLE : View.GONE);
        cardsAdapter.setCards(hasCards ? cards : new ArrayList<>());
    }

    /**
     * Пример открытия чата в виде Активности
     */
    public void navigateToChatActivity() {
        Card currentCard = getCurrentCard();
        if (currentCard == null) {
            displayError(R.string.error_empty_user);
            return;
        }
        if (currentCard.getUserId() == null) {
            displayError(R.string.error_empty_userid);
            return;
        }
        ThreadsLib.getInstance().initUser(
                new UserInfoBuilder(currentCard.getUserId())
                        .setClientIdSignature(currentCard.getClientIdSignature())
                        .setUserName(currentCard.getUserName())
                        .setData("{\"phone\": \"+7-999-999-99-99\",\"email\": \"e@mail.com\"}")
                        .setAppMarker(currentCard.getAppMarker())
        );
        ThreadsLib.getInstance().applyChatStyle(ChatStyleBuilderHelper.getChatStyle(getCurrentDesign()));
        startActivity(new Intent(this, ChatActivity.class));
    }

    /**
     * Пример открытя чата в виде фрагмента
     */
    public void navigateToBottomNavigationActivity() {
        Card currentCard = getCurrentCard();
        if (currentCard == null) {
            displayError(R.string.error_empty_user);
            return;
        }
        if (currentCard.getUserId() == null) {
            displayError(R.string.error_empty_userid);
            return;
        }
        ThreadsLib.getInstance().initUser(
                new UserInfoBuilder(currentCard.getUserId())
                        .setClientIdSignature(currentCard.getClientIdSignature())
                        .setUserName(currentCard.getUserName())
                        .setData("{\"phone\": \"+7-999-999-99-99\",\"email\": \"e@mail.com\"}")
                        .setAppMarker(currentCard.getAppMarker())
        );
        startActivity(BottomNavigationActivity.createIntent(this, currentCard.getAppMarker(),
                currentCard.getUserId(), currentCard.getClientIdSignature(),
                currentCard.getUserName(), getCurrentDesign()));
    }

    public void showAddCardDialog() {
        AddCardDialog.open(this);
    }

    public void sendExampleMessage() {
        View view = findViewById(android.R.id.content);
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap icon = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        File imageFile = new File(getFilesDir(), "screenshot.jpg");
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            icon.compress(Bitmap.CompressFormat.JPEG, 80, fos);
        } catch (FileNotFoundException ignored) {
        } catch (IOException ignored) {
        }
        Card currentCard = getCurrentCard();
        if (currentCard == null) {
            displayError(R.string.error_empty_user);
            return;
        }
        if (currentCard.getUserId() == null) {
            displayError(R.string.error_empty_userid);
            return;
        }
        UserInfoBuilder userInfoBuilder = new UserInfoBuilder(currentCard.getUserId())
                .setClientIdSignature(currentCard.getClientIdSignature())
                .setUserName(currentCard.getUserName())
                .setData("{\"phone\": \"+7-999-999-99-99\",\"email\": \"e@mail.com\"}")
                .setAppMarker(currentCard.getAppMarker());
        ThreadsLib.getInstance().initUser(userInfoBuilder);
        boolean messageSent = ThreadsLib.getInstance().sendMessage(getString(R.string.test_message), imageFile);
        if (messageSent) {
            Toast.makeText(this, R.string.send_text_message_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.send_text_message_error, Toast.LENGTH_SHORT).show();
        }
    }

    private ChatStyleBuilderHelper.ChatDesign getCurrentDesign() {
        return ChatStyleBuilderHelper.ChatDesign.enumOf(this, (String) binding.designSpinner.getSelectedItem());
    }

    @Nullable
    private Card getCurrentCard() {
        RecyclerView.LayoutManager layoutManager = binding.cardsView.getLayoutManager();
        if (layoutManager != null) {
            View centerView = cardsSnapHelper.findSnapView(layoutManager);
            if (centerView != null) {
                return cardsAdapter.getCard(layoutManager.getPosition(centerView));
            }
        }
        return null;
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
            showAddCardDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCardAdded(Card newCard) {

        List<Card> cards = cardsAdapter.getCards();

        if (cards.contains(newCard)) {
            Toast.makeText(this, R.string.client_id_already_exist, Toast.LENGTH_LONG).show();
        } else {
            cards.add(newCard);
            showCards(cards);
            PrefUtils.storeCards(this, cards);
        }
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onOkClicked(final int requestCode) {
        List<Card> cards = cardsAdapter.getCards();
        if (cards.contains(cardForDelete)) {
            cards.remove(cardForDelete);
            showCards(cards);
            PrefUtils.storeCards(this, cards);
            ThreadsLib.getInstance().logoutClient(cardForDelete.getUserId());
        }
        cardForDelete = null;
    }

    @Override
    public void onCancelClicked(final int requestCode) {
        cardForDelete = null;
    }


    private void displayError(final @StringRes int errorTextRes) {
        displayError(getString(errorTextRes));
    }

    private void displayError(final @NonNull String errorText) {
        Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show();
    }
}
