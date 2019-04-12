package vng.zalo.tdtai.zalo.zalo.views.lobby.fragments.chat_fragment.chat_activity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.text.DateFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.models.MessageModel;
import vng.zalo.tdtai.zalo.zalo.models.DataModel;
import vng.zalo.tdtai.zalo.zalo.utils.ModelViewHolder;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.VIEW_TYPE_RECEIVER;
import static vng.zalo.tdtai.zalo.zalo.utils.Constants.VIEW_TYPE_SENDER;

public class ChatActivityAdapter extends ListAdapter<MessageModel,RecyclerView.ViewHolder> {
    private ChatActivity chatActivity;
    private static final String TAG = ChatActivityAdapter.class.getSimpleName();

    ChatActivityAdapter(ChatActivity chatActivity, @NonNull DiffUtil.ItemCallback<MessageModel> diffCallback) {
        super(diffCallback);
        this.chatActivity = chatActivity;
    }

    @Override
    public int getItemViewType(int position) {
        if(getItem(position).senderPhone.equals(ZaloApplication.sCurrentUserPhone)){
            return VIEW_TYPE_SENDER;
        } else {
            return VIEW_TYPE_RECEIVER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENDER){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_send_chat_activity,parent,false);
            return new SenderViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_receive_chat_activity,parent,false);
            return new ReceiverViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ModelViewHolder)holder).bind(getItem(position));
    }

    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }

    class SenderViewHolder extends RecyclerView.ViewHolder implements ModelViewHolder {
        TextView sendMsgTextView;
        TextView sendTimeTextView;

        SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            sendMsgTextView = itemView.findViewById(R.id.sendMsgTextView);
            sendTimeTextView = itemView.findViewById(R.id.sendTimeTextView);
        }

        @Override
        public void bind(DataModel dataModel) {
            MessageModel messageModel = (MessageModel) dataModel;

            sendMsgTextView.setText(messageModel.content);
            sendTimeTextView.setText(ZaloApplication.sDateFormat.format(messageModel.createdTime));
        }
    }

    class ReceiverViewHolder extends RecyclerView.ViewHolder implements ModelViewHolder {
        TextView recvMsgTextView;
        TextView recvTimeTextView;
        ImageView recvAvatarImgButton;

        ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            recvMsgTextView = itemView.findViewById(R.id.recvMsgTextView);
            recvTimeTextView = itemView.findViewById(R.id.recvTimeTextView);
            recvAvatarImgButton = itemView.findViewById(R.id.recvAvatarImgButton);
        }

        @Override
        public void bind(DataModel dataModel){
            MessageModel messageModel = (MessageModel) dataModel;
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(recvMsgTextView.getContext());

            recvMsgTextView.setText(messageModel.content);
            recvTimeTextView.setText(dateFormat.format( messageModel.createdTime));
            Picasso.with(recvMsgTextView.getContext())
                    .load(messageModel.avatar)
                    .fit()
                    .into(recvAvatarImgButton);
        }
    }
}