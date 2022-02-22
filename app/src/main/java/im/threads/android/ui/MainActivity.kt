package im.threads.android.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import im.threads.ThreadsLib
import im.threads.UserInfoBuilder
import im.threads.android.R
import im.threads.android.data.Card
import im.threads.android.databinding.ActivityMainBinding
import im.threads.android.ui.CardsAdapter.CardActionListener
import im.threads.android.ui.EditCardDialog.EditCardDialogActionsListener
import im.threads.android.ui.YesNoDialog.YesNoDialogActionListener
import im.threads.android.utils.CardsLinearLayoutManager
import im.threads.android.utils.CardsSnapHelper
import im.threads.android.utils.ChatStyleBuilderHelper
import im.threads.android.utils.PrefUtils.getCards
import im.threads.android.utils.PrefUtils.getTheme
import im.threads.android.utils.PrefUtils.storeCards
import im.threads.internal.utils.ThreadsLogger
import im.threads.view.ChatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Активность с примерами открытия чата:
 * - в виде новой Активности
 * - в виде активности, где чат выступает в качестве фрагмента
 */
class MainActivity : AppCompatActivity(), EditCardDialogActionsListener, YesNoDialogActionListener {
    lateinit var binding: ActivityMainBinding
    private lateinit var cardsAdapter: CardsAdapter
    private val cardsSnapHelper: CardsSnapHelper = CardsSnapHelper()
    private var cardForDelete: Card? = null

    private val compositeDisposable = CompositeDisposable()
    private lateinit var socketResponseDisposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = this
        binding.designSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>?, arg1: View?, arg2: Int, arg3: Long) {
                val theme = binding.designSpinner.selectedItem.toString()
                ChatStyleBuilderHelper.ChatDesign.setTheme(
                        this@MainActivity,
                        ChatStyleBuilderHelper.ChatDesign.enumOf(this@MainActivity, theme)
                )
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }
        val versionView = findViewById<TextView>(R.id.version_name)
        versionView.text = getString(
                R.string.lib_version,
                ThreadsLib.getLibVersion()
        )
        cardsSnapHelper.attachToRecyclerView(binding.cardsView)
        binding.cardsView.layoutManager =
                CardsLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.cardsView.setHasFixedSize(true)
        cardsAdapter = CardsAdapter()
        cardsAdapter.setCardActionListener(object : CardActionListener {
            override fun onDelete(card: Card) {
                cardForDelete = card
                YesNoDialog.open(
                        this@MainActivity, getString(R.string.card_delete_text),
                        getString(R.string.card_delete_yes),
                        getString(R.string.card_delete_no),
                        YES_NO_DIALOG_REQUEST_CODE
                )
            }

            override fun onEdit(card: Card) {
                showEditCardDialog(card)
            }
        })
        binding.cardsView.adapter = cardsAdapter
        showCards(getCards(this))
        intent?.data?.let {
            Toast.makeText(this, "intent contains data: $it", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCards(cards: List<Card?>?) {
        val hasCards = cards != null && cards.isNotEmpty()
        binding.addCard.visibility = if (!hasCards) View.VISIBLE else View.GONE
        binding.addCardHint.visibility =
                if (!hasCards) View.VISIBLE else View.GONE
        binding.cardsView.visibility =
                if (hasCards) View.VISIBLE else View.GONE
        binding.chatActivityButton.visibility =
                if (hasCards) View.VISIBLE else View.GONE
        binding.chatFragmentButton.visibility =
                if (hasCards) View.VISIBLE else View.GONE
        binding.sendMessageButton.visibility =
                if (hasCards) View.VISIBLE else View.GONE
        cardsAdapter.cards = if (hasCards) cards else ArrayList()
    }

    /**
     * Пример открытия чата в виде Активности
     */
    fun navigateToChatActivity() {
        subscribeOnSocketResponses()

        val currentCard = currentCard
        if (currentCard == null) {
            displayError(R.string.error_empty_user)
            return
        }
        currentCard.userId
        ThreadsLib.getInstance().initUser(
                UserInfoBuilder(currentCard.userId)
                        .setAuthData(currentCard.authToken, currentCard.authSchema)
                        .setClientData(currentCard.clientData)
                        .setClientIdSignature(currentCard.clientIdSignature)
                        .setAppMarker(currentCard.appMarker)
        )
        ThreadsLib.getInstance().applyChatStyle(ChatStyleBuilderHelper.getChatStyle(getTheme(this)))
        startActivity(Intent(this, ChatActivity::class.java))
    }

    /**
     * Пример открытия чата в виде фрагмента
     */
    fun navigateToBottomNavigationActivity() {
        subscribeOnSocketResponses()
        val currentCard = currentCard
        if (currentCard == null) {
            displayError(R.string.error_empty_user)
            return
        }
        currentCard.userId
        if (ThreadsLib.getInstance().isUserInitialized) {
            ThreadsLib.getInstance().initUser(
                    UserInfoBuilder(currentCard.userId)
                            .setAuthData(currentCard.authToken, currentCard.authSchema)
                            .setClientData(currentCard.clientData)
                            .setClientIdSignature(currentCard.clientIdSignature)
                            .setAppMarker(currentCard.appMarker)
            )
        }
        startActivity(
                BottomNavigationActivity.createIntent(
                        this,
                        currentCard.appMarker,
                        currentCard.userId,
                        currentCard.clientData,
                        currentCard.clientIdSignature,
                        currentCard.authToken,
                        currentCard.authSchema,
                        getTheme(this)
                )
        )
    }

    private fun subscribeOnSocketResponses() {
        if (!::socketResponseDisposable.isInitialized || socketResponseDisposable.isDisposed) {
            socketResponseDisposable = ThreadsLib.getInstance().socketResponseMapProcessor
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onBackpressureDrop()
                    .subscribe({ responseMap ->
                        ThreadsLogger.i(TAG_SOCKET_RESPONSE, responseMap.toString())
                    }, { error ->
                        ThreadsLogger.e(TAG_SOCKET_RESPONSE, error.message)
                    })
            compositeDisposable.add(socketResponseDisposable)
        }
    }

    fun showEditCardDialog() {
        EditCardDialog.open(this)
    }

    fun showEditCardDialog(card: Card) {
        EditCardDialog.open(this, card)
    }

    private fun showEditTransportConfigDialog() {
        EditTransportConfigDialog.open(this)
    }

    fun sendExampleMessage() {
        val view = findViewById<View>(android.R.id.content)
        view.isDrawingCacheEnabled = true
        view.buildDrawingCache()
        val icon = Bitmap.createBitmap(view.drawingCache)
        view.isDrawingCacheEnabled = false
        val imageFile = File(filesDir, "screenshot.jpg")
        try {
            FileOutputStream(imageFile).use { fos ->
                icon.compress(
                        Bitmap.CompressFormat.JPEG,
                        80,
                        fos
                )
            }
        } catch (ignored: FileNotFoundException) {
        } catch (ignored: IOException) {
        }
        val currentCard = currentCard
        if (currentCard == null) {
            displayError(R.string.error_empty_user)
            return
        }
        val userInfoBuilder = UserInfoBuilder(currentCard.userId)
                .setAuthData(currentCard.authToken, currentCard.authSchema)
                .setClientData(currentCard.clientData)
                .setClientIdSignature(currentCard.clientIdSignature)
                .setAppMarker(currentCard.appMarker)
        ThreadsLib.getInstance().initUser(userInfoBuilder)
        val messageSent =
                ThreadsLib.getInstance().sendMessage(getString(R.string.test_message), imageFile)
        if (messageSent) {
            Toast.makeText(this, R.string.send_text_message_success, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.send_text_message_error, Toast.LENGTH_SHORT).show()
        }
    }

    private val currentCard: Card?
        get() {
            val layoutManager = binding.cardsView.layoutManager
            if (layoutManager != null) {
                val centerView = cardsSnapHelper.findSnapView(layoutManager)
                if (centerView != null) {
                    return cardsAdapter.getCard(layoutManager.getPosition(centerView))
                }
            }
            return null
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.add_card) {
            showEditCardDialog()
            return true
        }
        if (id == R.id.edit_transport_config) {
            showEditTransportConfigDialog()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCardSaved(newCard: Card?) {
        val cards = cardsAdapter.cards
        val indexOf = cards.indexOf(newCard)
        if (indexOf != -1) {
            cards[indexOf] = newCard
            Toast.makeText(this, R.string.client_info_updated, Toast.LENGTH_LONG).show()
        } else {
            cards.add(newCard)
        }
        storeCards(this, cards)
        showCards(cards)
    }

    override fun onCancel() {}
    override fun onOkClicked(requestCode: Int) {
        val cards = cardsAdapter.cards
        cardForDelete?.let {
            if (cards.contains(it)) {
                cards.remove(it)
                showCards(cards)
                storeCards(this, cards)
                ThreadsLib.getInstance().logoutClient(it.userId)
            }
        }
        cardForDelete = null
    }

    override fun onCancelClicked(requestCode: Int) {
        cardForDelete = null
    }

    private fun displayError(@StringRes errorTextRes: Int) {
        displayError(getString(errorTextRes))
    }

    private fun displayError(errorText: String) {
        Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG_SOCKET_RESPONSE = "SocketResponse"
        private const val YES_NO_DIALOG_REQUEST_CODE = 323
    }
}