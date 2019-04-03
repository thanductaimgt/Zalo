package vng.zalo.tdtai.zalo.screens.lobby.fragments.chat_fragment.chat_activity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.screens.utils.Model;
import vng.zalo.tdtai.zalo.screens.utils.OnDatasetChange;

import static vng.zalo.tdtai.zalo.screens.utils.Constants.CURRENT_USER_ID;
import static vng.zalo.tdtai.zalo.screens.utils.Constants.VIEW_TYPE_RECEIVER;
import static vng.zalo.tdtai.zalo.screens.utils.Constants.VIEW_TYPE_SENDER;

public class ChatActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnDatasetChange {
    public List<MessageModel> messageModelList;

    ChatActivityAdapter(List<MessageModel> messageModelList){
        this.messageModelList = messageModelList;
    }

    @Override
    public int getItemViewType(int position) {
        if(messageModelList.get(position).senderId == CURRENT_USER_ID){
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
            return new SendViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_receive_chat_activity,parent,false);
            return new RecvViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((Bind)holder).bind(messageModelList.get(position));
    }

    @Override
    public int getItemCount() {
        return messageModelList.size();
    }

    interface Bind{
        void bind(MessageModel model);
    }

    class SendViewHolder extends RecyclerView.ViewHolder implements Bind{
        TextView sendMsgTextView;
        TextView sendTimeTextView;

        SendViewHolder(@NonNull View itemView) {
            super(itemView);
            sendMsgTextView = itemView.findViewById(R.id.sendMsgTextView);
            sendTimeTextView = itemView.findViewById(R.id.sendTimeTextView);
        }

        @Override
        public void bind(MessageModel sendModel){
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(sendMsgTextView.getContext());

            sendMsgTextView.setText(sendModel.message);
            sendTimeTextView.setText(dateFormat.format(new Date(sendModel.timeStamp)));
        }
    }

    class RecvViewHolder extends RecyclerView.ViewHolder implements Bind{
        TextView recvMsgTextView;
        TextView recvTimeTextView;
        ImageView recvAvatarImgButton;

        RecvViewHolder(@NonNull View itemView) {
            super(itemView);
            recvMsgTextView = itemView.findViewById(R.id.recvMsgTextView);
            recvTimeTextView = itemView.findViewById(R.id.recvTimeTextView);
            recvAvatarImgButton = itemView.findViewById(R.id.recvAvatarImgButton);
        }

        @Override
        public void bind(MessageModel recvModel){
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(recvMsgTextView.getContext());

            recvMsgTextView.setText(recvModel.message);
            recvTimeTextView.setText(dateFormat.format(new Date(recvModel.timeStamp)));
            Picasso.with(recvMsgTextView.getContext())
                    .load(recvModel.avatarLink)
                    .fit()
                    .into(recvAvatarImgButton);
        }
    }

    @Override
    public void updateChanges(List<? extends Model> newDataItemListFinal) {
        final List<MessageModel> newDataItemList = (List<MessageModel>)newDataItemListFinal;
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return ChatActivityAdapter.this.messageModelList.size();
            }

            @Override
            public int getNewListSize() {
                return newDataItemList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return ChatActivityAdapter.this.messageModelList.get(newItemPosition).msgId == newDataItemList.get(oldItemPosition).msgId;
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return ChatActivityAdapter.this.messageModelList.get(newItemPosition).equals(newDataItemList.get(oldItemPosition));
            }
        });

//        this.dataItemList.clear();
//        this.dataItemList.addAll(newDataItemList);
        diffResult.dispatchUpdatesTo(this);
    }
}