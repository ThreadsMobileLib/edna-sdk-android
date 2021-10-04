package im.threads.android.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import im.threads.android.data.Card;
import im.threads.android.databinding.ItemCardBinding;

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.ViewHolder> {

    private List<Card> cards = new ArrayList<>();
    private CardActionListener cardActionListener;

    @NonNull
    @Override
    public CardsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCardBinding binding = ItemCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Card card = cards.get(position);
        holder.binding.clientId.setText(card.getUserId());
        holder.binding.clientData.setText(card.getClientData());
        holder.binding.appMarker.setText(card.getAppMarker());
        holder.binding.clientIdSignature.setText(card.getClientIdSignature());
        holder.binding.ivEdit.setOnClickListener(v -> cardActionListener.onEdit(card));
        holder.binding.removeCard.setOnClickListener(v -> cardActionListener.onDelete(card));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cards.size();
    }

    public List<Card> getCards() {
        //return copy to prevent inconsistency if array modified outside of adapter
        return new ArrayList<>(cards);
    }

    public void setCards(final List<Card> cards) {
        this.cards.clear();
        this.cards.addAll(cards);
        notifyDataSetChanged();
    }

    public Card getCard(int position) {
        return cards != null && position < cards.size() ? cards.get(position) : null;
    }

    public void setCardActionListener(CardActionListener cardActionListener) {
        this.cardActionListener = cardActionListener;
    }

    public interface CardActionListener {
        void onDelete(Card card);

        void onEdit(Card card);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemCardBinding binding;

        ViewHolder(ItemCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
