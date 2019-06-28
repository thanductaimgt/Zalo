package vng.zalo.tdtai.zalo.zalo.views.home.fragments.contact_fragment.contacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.models.RoomItem;
import vng.zalo.tdtai.zalo.zalo.utils.ModelViewHolder;
import vng.zalo.tdtai.zalo.zalo.utils.Utils;

public class AllContactAdapter extends ListAdapter<RoomItem, AllContactAdapter.ContactViewHolder> {
    private ContactSubFragment contactSubFragment;

    AllContactAdapter(ContactSubFragment contactSubFragment, @NonNull DiffUtil.ItemCallback<RoomItem> diffCallback) {
        super(diffCallback);
        this.contactSubFragment = contactSubFragment;
    }

    @NonNull
    @Override
    public AllContactAdapter.ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_sub_fragment, parent, false);
        return new AllContactAdapter.ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllContactAdapter.ContactViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder implements ModelViewHolder {
        View itemView;
        TextView phoneTextView;
        ImageView avatarImgView;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            phoneTextView = itemView.findViewById(R.id.nameTextViewContact);
            avatarImgView = itemView.findViewById(R.id.avatarImgButtonContact);
        }

        @Override
        public void bind(int position) {
            itemView.setOnClickListener(contactSubFragment);
            RoomItem roomItem = getItem(position);

            phoneTextView.setText(roomItem.name);
            Utils.formatTextOnNumberOfLines(phoneTextView, 1);

            Picasso.get()
                    .load(roomItem.avatar)
                    .fit()
                    .into(avatarImgView);
        }
    }
}