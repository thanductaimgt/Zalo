package vng.zalo.tdtai.zalo.screens.lobby.fragments.chat_fragment;

import android.net.Uri;
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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.screens.utils.Model;
import vng.zalo.tdtai.zalo.screens.utils.OnDatasetChange;

public class ChatFragmentAdapter extends RecyclerView.Adapter<ChatFragmentAdapter.ChatViewHolder> implements OnDatasetChange {
    List<ChatItemModel> dataItemList;
    private ChatFragment chatFragment;

    ChatFragmentAdapter(List<ChatItemModel> dataItemList, ChatFragment chatFragment) {
        this.dataItemList = dataItemList;
        this.chatFragment = chatFragment;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_fragment,parent,false);
        view.setOnClickListener(chatFragment);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(holder.avatarImgView.getContext());

        ChatItemModel item = dataItemList.get(position);
        holder.nameTextView.setText(item.name);
        holder.timeTextView.setText(dateFormat.format(item.date));
        holder.descTextView.setText(item.description);
        new Picasso.Builder(holder.avatarImgView.getContext()).listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                Log.e(ChatFragmentAdapter.this.getClass().getSimpleName(),exception.getMessage());
                exception.printStackTrace();
            }
        }).build().load(item.avatar)
                .fit()
                .into(holder.avatarImgView);
        if (item.showIcon)
            holder.iconImgView.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return dataItemList.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView descTextView;
        TextView timeTextView;
        ImageView iconImgView;
        ImageView avatarImgView;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            descTextView = itemView.findViewById(R.id.descTextView);
            timeTextView = itemView.findViewById(R.id.recvTimeTextView);
            iconImgView = itemView.findViewById(R.id.iconImgView);
            avatarImgView = itemView.findViewById(R.id.avatarImgView);
        }
    }

    @Override
    public void updateChanges(List<? extends Model> newDataItemListFinal) {
        final List<ChatItemModel> newDataItemList = (List<ChatItemModel>)newDataItemListFinal;
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return ChatFragmentAdapter.this.dataItemList.size();
            }

            @Override
            public int getNewListSize() {
                return newDataItemList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return ChatFragmentAdapter.this.dataItemList.get(newItemPosition).id == newDataItemList.get(oldItemPosition).id;
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return ChatFragmentAdapter.this.dataItemList.get(newItemPosition).equals(newDataItemList.get(oldItemPosition));
            }
        });

//        this.dataItemList.clear();
//        this.dataItemList.addAll(newDataItemList);
        diffResult.dispatchUpdatesTo(this);
    }
}