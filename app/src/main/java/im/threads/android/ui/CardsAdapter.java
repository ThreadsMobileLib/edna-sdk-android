package im.threads.android.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import im.threads.android.data.Card;
import im.threads.android.databinding.ItemCardBinding;

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.ViewHolder> {

    private List<Card> cards = new ArrayList<>();
    private RemoveCardListener removeCardListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemCardBinding binding;

        ViewHolder(ItemCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @Override
    public CardsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemCardBinding binding = ItemCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Card card = cards.get(position);
        holder.binding.clientId.setText(card.getUserId());
        holder.binding.clientName.setText(card.getUserName());
        holder.binding.appMarker.setText(card.getAppMarker());
        holder.binding.clientIdSignature.setText(card.getClientIdSignature());
        holder.binding.removeCard.setOnClickListener(v -> removeCardListener.onRemoved(card));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cards.size();
    }

    public void setCards(final List<Card> cards) {
        this.cards.clear();
        this.cards.addAll(cards);
        notifyDataSetChanged();
    }

    public List<Card> getCards() {
        //return copy to prevent inconsistency if array modified outside of adapter
        return new ArrayList<>(cards);
    }

    public Card getCard(int position) {
        return cards != null && position < cards.size() ? cards.get(position) : null;
    }

    public void setRemoveCardListener(final RemoveCardListener removeCardListener) {
        this.removeCardListener = removeCardListener;
    }

    public interface RemoveCardListener {
        void onRemoved(Card card);
    }
}
