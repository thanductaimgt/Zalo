package vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment;

import android.graphics.Typeface;
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
import vng.zalo.tdtai.zalo.zalo.models.RoomItem;
import vng.zalo.tdtai.zalo.zalo.models.DataModel;
import vng.zalo.tdtai.zalo.zalo.utils.Constants;
import vng.zalo.tdtai.zalo.zalo.utils.ModelViewHolder;
import vng.zalo.tdtai.zalo.zalo.utils.Utils;

public class ChatFragmentAdapter extends ListAdapter<RoomItem, ChatFragmentAdapter.ChatViewHolder> {
    private static final String TAG = ChatFragmentAdapter.class.getSimpleName();

    private ChatFragment chatFragment;

    ChatFragmentAdapter(ChatFragment chatFragment, @NonNull DiffUtil.ItemCallback<RoomItem> diffCallback) {
        super(diffCallback);
        this.chatFragment = chatFragment;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_fragment, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.bind(position);
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
        TextView iconTextView;
        ImageView avatarImgView;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            nameTextView = itemView.findViewById(R.id.nameTextView);
            descTextView = itemView.findViewById(R.id.descTextView);
            timeTextView = itemView.findViewById(R.id.receiveTimeTextView);
            iconTextView = itemView.findViewById(R.id.iconTextView);
            avatarImgView = itemView.findViewById(R.id.avatarImgView);
        }

        @Override
        public void bind(int position) {
            itemView.setOnClickListener(chatFragment);
            RoomItem roomItem = getItem(position);

            nameTextView.setText(roomItem.name);

            if(roomItem.lastMsgTime != null) {
                String formatTime = Utils.getTimeDiffOrFormatTime(roomItem.lastMsgTime.toDate());
                timeTextView.setText(formatTime);
            } else {
                timeTextView.setText("");
            }

            descTextView.setText(roomItem.lastMsg);
            Utils.formatTextOnOneLine(descTextView);

            new Picasso.Builder(avatarImgView.getContext()).listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    Log.e(ChatFragmentAdapter.this.getClass().getSimpleName(), exception.getMessage());
                    exception.printStackTrace();
                }
            }).build().load(roomItem.avatar)
                    .fit()
                    .into(avatarImgView);

            if (roomItem.unseenMsgNum > 0) {
                String unseenMsgNum = roomItem.unseenMsgNum < Constants.MAX_UNSEEN_MSG_NUM ?
                        roomItem.unseenMsgNum.toString():(Constants.MAX_UNSEEN_MSG_NUM+"+");
                iconTextView.setText(unseenMsgNum);
                iconTextView.setVisibility(View.VISIBLE);
                nameTextView.setTypeface(null, Typeface.BOLD);
                descTextView.setTypeface(null, Typeface.BOLD);
                timeTextView.setTypeface(null, Typeface.BOLD);
            } else {
                iconTextView.setVisibility(View.GONE);
                nameTextView.setTypeface(null, Typeface.NORMAL);
                descTextView.setTypeface(null, Typeface.NORMAL);
                timeTextView.setTypeface(null, Typeface.NORMAL);
            }
        }
    }
}