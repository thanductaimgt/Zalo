package vng.zalo.tdtai.zalo.zalo.views.home.fragments.contact_fragment.contacts;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.models.ContactItemModel;
import vng.zalo.tdtai.zalo.zalo.models.DataModel;
import vng.zalo.tdtai.zalo.zalo.utils.ModelViewHolder;

public class AllContactAdapter extends ListAdapter<ContactItemModel, AllContactAdapter.ContactViewHolder> {
    private ContactSubFragment contactSubFragment;

    AllContactAdapter(ContactSubFragment contactSubFragment, @NonNull DiffUtil.ItemCallback<ContactItemModel> diffCallback) {
        super(diffCallback);
        this.contactSubFragment = contactSubFragment;
    }

    @NonNull
    @Override
    public AllContactAdapter.ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_sub_fragment, parent, false);
//        view.setOnClickListener(contactSubFragment);
        return new AllContactAdapter.ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllContactAdapter.ContactViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder implements ModelViewHolder {
        View itemView;
        TextView phoneTextView;
        ImageView avatarImgButton;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            phoneTextView = itemView.findViewById(R.id.nameTextViewContact);
            avatarImgButton = itemView.findViewById(R.id.avatarImgButtonContact);
        }

        @Override
        public void bind(DataModel dataModel) {
            itemView.setOnClickListener(contactSubFragment);
            ContactItemModel contactItemModel = (ContactItemModel) dataModel;

            phoneTextView.setText(contactItemModel.phone);

            new Picasso.Builder(avatarImgButton.getContext()).listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    Log.e(AllContactAdapter.this.getClass().getSimpleName(), exception.getMessage());
                    exception.printStackTrace();
                }
            }).build().load(contactItemModel.avatar)
                    .fit()
                    .into(avatarImgButton);
        }
    }
}