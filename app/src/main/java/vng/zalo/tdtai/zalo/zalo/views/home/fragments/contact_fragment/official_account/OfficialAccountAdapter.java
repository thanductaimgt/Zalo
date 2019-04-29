package vng.zalo.tdtai.zalo.zalo.views.home.fragments.contact_fragment.official_account;

import android.net.Uri;
import android.util.Log;
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
import vng.zalo.tdtai.zalo.zalo.models.DataModel;
import vng.zalo.tdtai.zalo.zalo.models.RoomItem;
import vng.zalo.tdtai.zalo.zalo.utils.ModelViewHolder;

public class OfficialAccountAdapter extends ListAdapter<RoomItem, OfficialAccountAdapter.OfficialAccountViewHolder> {
    private OfficialAccountSubFragment officialAccountSubFragment;

    OfficialAccountAdapter(OfficialAccountSubFragment officialAccountSubFragment, @NonNull DiffUtil.ItemCallback<RoomItem> diffCallback) {
        super(diffCallback);
        this.officialAccountSubFragment = officialAccountSubFragment;
    }

    @NonNull
    @Override
    public OfficialAccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_official_account, parent, false);
//        view.setOnClickListener(officialAccountSubFragment);
        return new OfficialAccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfficialAccountViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }

    class OfficialAccountViewHolder extends RecyclerView.ViewHolder implements ModelViewHolder {
        View itemView;
        TextView nameTextView;
        ImageView avatarImgView;
        ImageView iconImgView;

        OfficialAccountViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            nameTextView = itemView.findViewById(R.id.nameTextView);
            avatarImgView = itemView.findViewById(R.id.avatarImgView);
            iconImgView = itemView.findViewById(R.id.iconTextView);
        }

        @Override
        public void bind(int position) {
            itemView.setOnClickListener(officialAccountSubFragment);
            RoomItem room = getItem(position);

            nameTextView.setText(room.name);

            new Picasso.Builder(avatarImgView.getContext()).listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    Log.e(OfficialAccountAdapter.this.getClass().getSimpleName(), exception.getMessage());
                    exception.printStackTrace();
                }
            }).build().load(room.avatar)
                    .fit()
                    .into(avatarImgView);
        }
    }
}