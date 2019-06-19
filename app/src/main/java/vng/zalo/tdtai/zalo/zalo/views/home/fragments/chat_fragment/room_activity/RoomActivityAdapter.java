package vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.room_activity;

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
import java.text.DateFormat;
import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.models.Message;
import vng.zalo.tdtai.zalo.zalo.utils.Constants;
import vng.zalo.tdtai.zalo.zalo.utils.ModelViewHolder;
import vng.zalo.tdtai.zalo.zalo.utils.Utils;

public class RoomActivityAdapter extends ListAdapter<Message, RecyclerView.ViewHolder> {
    private RoomActivity roomActivity;
    private static final String TAG = RoomActivityAdapter.class.getSimpleName();
    private DateFormat dateFormat = java.text.SimpleDateFormat.getDateInstance();
    private DateFormat timeFormat = java.text.SimpleDateFormat.getTimeInstance();

    RoomActivityAdapter(RoomActivity roomActivity, @NonNull DiffUtil.ItemCallback<Message> diffCallback) {
        super(diffCallback);
        this.roomActivity = roomActivity;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).senderPhone.equals(ZaloApplication.currentUser.phone)) {
            return Constants.VIEW_TYPE_SENDER;
        } else {
            return Constants.VIEW_TYPE_RECEIVER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Constants.VIEW_TYPE_SENDER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_send_room_activity, parent, false);
            return new SenderViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_receive_room_activity, parent, false);
            return new ReceiverViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ModelViewHolder) holder).bind(position);
    }

    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }

    class SenderViewHolder extends RecyclerView.ViewHolder implements ModelViewHolder {
        TextView sendMsgTextView;
        TextView sendTimeTextView;
        TextView dateTextView;

        SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            sendMsgTextView = itemView.findViewById(R.id.sendMsgTextView);
            sendTimeTextView = itemView.findViewById(R.id.sendTimeTextView);
            dateTextView = itemView.findViewById(R.id.dateTextViewItemSend);
        }

        @Override
        public void bind(int position) {
            Message curMessage = getItem(position);
            Message prevMessage = position > 0 ? getItem(position - 1) : null;
            Message nextMessage = position < getItemCount() - 1 ? getItem(position + 1) : null;

            sendMsgTextView.setText(curMessage.content);

            /* if one of these is true:
            - current message is first message
            - previous message date != current message date
            => display date
            */
            if (prevMessage == null ||
                    Utils.areInDifferentDay(curMessage.createdTime.toDate(), prevMessage.createdTime.toDate())) {
                dateTextView.setText(dateFormat.format(curMessage.createdTime.toDate()));
                dateTextView.setVisibility(View.VISIBLE);
            } else {
                dateTextView.setVisibility(View.GONE);
            }

            /* if one of these is true:
            - current message is last message
            - next message time - current message time < 1 min
            - next message date != current message date
            => display message time
            */
            if (nextMessage == null ||
                    Utils.timeGapInMillisecond(curMessage.createdTime.toDate(), nextMessage.createdTime.toDate()) > Constants.ONE_MIN_IN_MILLISECOND ||
                    Utils.areInDifferentDay(curMessage.createdTime.toDate(), nextMessage.createdTime.toDate())) {
                sendTimeTextView.setVisibility(View.VISIBLE);
                sendTimeTextView.setText(timeFormat.format(curMessage.createdTime.toDate()));
            } else {
                sendTimeTextView.setVisibility(View.GONE);
            }
        }
    }

    class ReceiverViewHolder extends RecyclerView.ViewHolder implements ModelViewHolder {
        TextView recvMsgTextView;
        TextView recvTimeTextView;
        ImageView recvAvatarImgView;
        TextView dateTextView;

        ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            recvMsgTextView = itemView.findViewById(R.id.receiveMsgTextView);
            recvTimeTextView = itemView.findViewById(R.id.receiveTimeTextView);
            recvAvatarImgView = itemView.findViewById(R.id.receiveAvatarImgView);
            dateTextView = itemView.findViewById(R.id.dateTextViewItemRecv);
        }

        @Override
        public void bind(int position) {
            Message curMessage = getItem(position);
            Message prevMessage = position > 0 ? getItem(position - 1) : null;
            Message nextMessage = position < getItemCount() - 1 ? getItem(position + 1) : null;

            recvMsgTextView.setText(curMessage.content);

            /* if one of these is true:
            - current message is first message
            - previous message date != current message date
            => display date
            */
            if (prevMessage == null ||
                    Utils.areInDifferentDay(curMessage.createdTime.toDate(), prevMessage.createdTime.toDate())) {
                dateTextView.setText(dateFormat.format(curMessage.createdTime.toDate()));
                dateTextView.setVisibility(View.VISIBLE);
            } else {
                dateTextView.setVisibility(View.GONE);
            }

            /* if one of these is true:
            - current message is last message
            - next message time - current message time < 1 min
            - next message date != current message date
            => display message time
            */
            boolean displayMessageTime = false;
            if (nextMessage == null ||
                    Utils.timeGapInMillisecond(curMessage.createdTime.toDate(), nextMessage.createdTime.toDate()) > Constants.ONE_MIN_IN_MILLISECOND ||
                    Utils.areInDifferentDay(curMessage.createdTime.toDate(), nextMessage.createdTime.toDate())) {
                recvTimeTextView.setVisibility(View.VISIBLE);
                recvTimeTextView.setText(timeFormat.format(curMessage.createdTime.toDate()));
                displayMessageTime = true;
            } else {
                recvTimeTextView.setVisibility(View.GONE);
            }

            /* if one of these is true:
            - message time displayed
            - next message sender != current message sender
            => display avatar
            */
            if (displayMessageTime || !nextMessage.senderPhone.equals(curMessage.senderPhone)) {
                Picasso.with(recvMsgTextView.getContext())
                        .load(curMessage.senderAvatar)
                        .fit()
                        .into(recvAvatarImgView);
                recvAvatarImgView.setVisibility(View.VISIBLE);
            } else {
                recvAvatarImgView.setVisibility(View.GONE);
            }
        }
    }
}