package vng.zalo.tdtai.zalo.zalo.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.models.RoomItem;
import vng.zalo.tdtai.zalo.zalo.utils.Constants;
import vng.zalo.tdtai.zalo.zalo.utils.ModelViewHolder;
import vng.zalo.tdtai.zalo.zalo.utils.Utils;

public class RoomItemAdapter extends ListAdapter<RoomItem, RoomItemAdapter.ChatViewHolder> {
    private static final String TAG = RoomItemAdapter.class.getSimpleName();

    private Fragment fragment;

    public RoomItemAdapter(Fragment fragment, @NonNull DiffUtil.ItemCallback<RoomItem> diffCallback) {
        super(diffCallback);
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
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
        CircleImageView avatarImgView;

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
            itemView.setOnClickListener((View.OnClickListener) fragment);
            RoomItem roomItem = getItem(position);

            nameTextView.setText(roomItem.name);
            Utils.formatTextOnNumberOfLines(nameTextView, 1);

            if (roomItem.lastMsgTime != null) {
                String formatTime = Utils.getTimeDiffOrFormatTime(roomItem.lastMsgTime.toDate());
                timeTextView.setText(formatTime);
            } else {
                timeTextView.setText("");
            }

            descTextView.setText(roomItem.lastMsg);
            Utils.formatTextOnNumberOfLines(descTextView, 1);

            Picasso.get()
                    .load(roomItem.avatar)
                    .fit()
                    .into(avatarImgView);

            if (roomItem.unseenMsgNum > 0) {
                String unseenMsgNum = roomItem.unseenMsgNum < Constants.MAX_UNSEEN_MSG_NUM ?
                        String.valueOf(roomItem.unseenMsgNum) : (Constants.MAX_UNSEEN_MSG_NUM + "+");
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