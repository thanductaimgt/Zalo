package vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.models.RoomItem;
import vng.zalo.tdtai.zalo.zalo.models.DataModel;
import vng.zalo.tdtai.zalo.zalo.utils.ModelViewHolder;

public class ChatFragmentAdapter extends ListAdapter<RoomItem, ChatFragmentAdapter.ChatViewHolder> {
    private ChatFragment chatFragment;

    ChatFragmentAdapter(ChatFragment chatFragment, @NonNull DiffUtil.ItemCallback<RoomItem> diffCallback) {
        super(diffCallback);
        this.chatFragment = chatFragment;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
//        view.setOnClickListener(chatFragment);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder implements ModelViewHolder {
        View itemView;
        TextView nameTextView;
        TextView descTextView;
        TextView timeTextView;
        ImageView iconImgView;
        ImageView avatarImgView;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            nameTextView = itemView.findViewById(R.id.nameTextView);
            descTextView = itemView.findViewById(R.id.descTextView);
            timeTextView = itemView.findViewById(R.id.receiveTimeTextView);
            iconImgView = itemView.findViewById(R.id.iconImgView);
            avatarImgView = itemView.findViewById(R.id.avatarImgView);
        }

        @Override
        public void bind(DataModel dataModel) {
            itemView.setOnClickListener(chatFragment);
            RoomItem roomItem = (RoomItem) dataModel;

            nameTextView.setText(roomItem.name);

            if(roomItem.lastMsgTime != null)
                timeTextView.setText(ZaloApplication.dateFormat.format(roomItem.lastMsgTime.toDate()));
            else
                timeTextView.setText("");

            descTextView.setText(roomItem.lastMsg);
            new Picasso.Builder(avatarImgView.getContext()).listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    Log.e(ChatFragmentAdapter.this.getClass().getSimpleName(), exception.getMessage());
                    exception.printStackTrace();
                }
            }).build().load(roomItem.avatar)
                    .fit()
                    .into(avatarImgView);

            if (roomItem.unseenMsgNum > 0)
                iconImgView.setVisibility(View.VISIBLE);
        }
    }
}