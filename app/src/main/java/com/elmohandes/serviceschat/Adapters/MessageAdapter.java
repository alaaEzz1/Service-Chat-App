package com.elmohandes.serviceschat.Adapters;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.elmohandes.serviceschat.Models.Messages;
import com.elmohandes.serviceschat.R;
import com.elmohandes.serviceschat.Screens.ImageviewerActivity;
import com.elmohandes.serviceschat.Screens.MainActivity;
import com.elmohandes.serviceschat.databinding.CustomMessageItemBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MHolder> {

    private List<Messages> messagesList;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private ProgressDialog dialog;

    public MessageAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public MHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.custom_message_item,parent,false);
        auth = FirebaseAuth.getInstance();
        return new MHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MHolder holder,
                                 @SuppressLint("RecyclerView") int position) {

        String senderId = auth.getCurrentUser().getUid();
        dialog = new ProgressDialog(holder.itemView.getContext());
        dialog.setTitle("deleting message");
        dialog.setMessage("loading...");
        dialog.setCancelable(false);

        Messages messages = messagesList.get(position);
        String messagesFrom = messages.getFrom();
        String messagesType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(messagesFrom);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               if (snapshot.exists()){

                   if (snapshot.hasChild("image")){

                       String receiverImage =snapshot.child("image").getValue().toString();

                       Picasso.get().load(receiverImage).placeholder(R.drawable.person_or_avatar)
                               .into(holder.binding.customMessageImg);

                   }

               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        defaultVisibilities(holder);

        if (messagesType.equals("text")){

            if (messagesFrom.equals(senderId)){
                holder.binding.messageSenderText.setVisibility(View.VISIBLE);
                holder.binding.messageSenderText.setBackgroundResource(R.drawable.sender_message);
                holder.binding.messageSenderText.setText(messages.getMessage() +
                        "\n \n " + messages.getDate() +"\t" + messages.getTime());
            }else {

                holder.binding.customMessageImg.setVisibility(View.VISIBLE);
                holder.binding.messageReceiverTxt.setVisibility(View.VISIBLE);

                holder.binding.messageReceiverTxt.setBackgroundResource(R.drawable.receiver_message);
                holder.binding.messageReceiverTxt.setText(messages.getMessage() +
                        "\n \n " + messages.getDate() +"    " + messages.getTime());

            }

        }
        else if (messagesType.equals("image")){
            if (messagesFrom.equals(senderId)){

                holder.binding.messageSenderImg.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.binding.messageSenderImg);
                
            }else{

                holder.binding.messageRecieverImg.setVisibility(View.VISIBLE);
                holder.binding.customMessageImg.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(holder.binding.messageRecieverImg);

            }

        }else {

            if (messagesFrom.equals(senderId)){

                holder.binding.messageSenderImg.setVisibility(View.VISIBLE);

                if (messagesType.equals("pdf")){
                    holder.binding.messageSenderImg.setImageResource(R.drawable.pdf);
                }
                if (messagesType.equals("docx")){
                    holder.binding.messageSenderImg.setImageResource(R.drawable.ms_word);
                }

            }else{

                holder.binding.messageRecieverImg.setVisibility(View.VISIBLE);
                holder.binding.customMessageImg.setVisibility(View.VISIBLE);

                if (messagesType.equals("pdf")){
                    holder.binding.messageRecieverImg.setImageResource(R.drawable.pdf);
                }
                if (messagesType.equals("docx")){
                    holder.binding.messageRecieverImg.setImageResource(R.drawable.ms_word);
                }

            }

        }

        if (messagesFrom.equals(senderId)){

            holder.itemView.setOnClickListener(view -> {

                if (messagesList.get(position).getType().equals("pdf")
                        || messagesList.get(position).getType().equals("docx") ){

                    CharSequence options[] = new CharSequence[]{
                            "Delete for me" ,
                            "Download and view this Document" ,
                            "Cancel" ,
                            "Delete for everyone"
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete or Download message");
                    builder.setItems(options, (dialogInterface, i) -> {

                        if (i == 0){
                            dialog.show();
                            deleteSenderMessage(position,holder);
                        }
                        if (i == 1){

                            Intent intent = new Intent(Intent.ACTION_VIEW ,
                                    Uri.parse(messagesList.get(position).getMessage()));
                            holder.itemView.getContext().startActivity(intent);

                        }

                        if (i == 3){
                            dialog.show();
                            deleteMessageForEveryOne(position,holder);
                        }

                    });
                    builder.show();

                }else if (messagesList.get(position).getType().equals("text")){

                    CharSequence options[] = new CharSequence[]{
                            "Delete for me" ,
                            "Cancel" ,
                            "Delete for everyone"
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete or Download message");
                    builder.setItems(options, (dialogInterface, i) -> {

                        if (i == 0){
                            dialog.show();
                            deleteSenderMessage(position,holder);
                        }

                        if (i == 2){
                            dialog.show();
                            deleteMessageForEveryOne(position , holder);
                        }

                    });
                    builder.show();

                } else if (messagesList.get(position).getType().equals("image")){

                    CharSequence options[] = new CharSequence[]{
                            "Delete for me" ,
                            "view this image" ,
                            "Cancel" ,
                            "Delete for everyone"
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete or Download message");
                    builder.setItems(options, (dialogInterface, i) -> {

                        if (i == 0){
                            dialog.show();
                            deleteSenderMessage(position,holder);
                        }
                        if (i == 1){

                            Intent intent = new Intent(holder.itemView.getContext()
                                    , ImageviewerActivity.class);
                            intent.putExtra("imgUrl" , messagesList.
                                    get(position).getMessage());
                            holder.itemView.getContext().startActivity(intent);

                        }

                        if (i == 3){
                            dialog.show();
                            deleteMessageForEveryOne(position , holder);
                        }

                    });
                    builder.show();

                }

            });

        }else {

            holder.itemView.setOnClickListener(view -> {

                if (messagesList.get(position).getType().equals("pdf")
                        || messagesList.get(position).getType().equals("docx") ){

                    CharSequence options[] = new CharSequence[]{
                            "Delete for me" ,
                            "Download and view this Document" ,
                            "Cancel"
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete or Download message");
                    builder.setItems(options, (dialogInterface, i) -> {

                        if (i == 0){
                            dialog.show();
                            deleteReceiverMessage(position , holder);
                        }
                        if (i == 1){

                            Intent intent = new Intent(Intent.ACTION_VIEW ,
                                    Uri.parse(messagesList.get(position).getMessage()));
                            holder.itemView.getContext().startActivity(intent);

                        }

                    });
                    builder.show();

                }else if (messagesList.get(position).getType().equals("text")){

                    CharSequence options[] = new CharSequence[]{
                            "Delete for me" ,
                            "Cancel" ,
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete or Download message");
                    builder.setItems(options, (dialogInterface, i) -> {

                        if (i == 0){
                            dialog.show();
                            deleteReceiverMessage(position , holder);
                        }

                    });
                    builder.show();

                } else if (messagesList.get(position).getType().equals("image")){

                    CharSequence options[] = new CharSequence[]{
                            "Delete for me" ,
                            "view this image" ,
                            "Cancel" ,
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete or Download message");
                    builder.setItems(options, (dialogInterface, i) -> {

                        if (i == 0){
                            dialog.show();
                            deleteReceiverMessage(position , holder);
                        }
                        if (i == 1){

                            Intent intent = new Intent(holder.itemView.getContext()
                                    , ImageviewerActivity.class);
                            intent.putExtra("imgUrl" , messagesList.
                                    get(position).getMessage());
                            holder.itemView.getContext().startActivity(intent);

                        }


                    });
                    builder.show();

                }

            });

        }

    }

    private void defaultVisibilities(MHolder holder) {
        holder.binding.customMessageImg.setVisibility(View.GONE);
        holder.binding.messageReceiverTxt.setVisibility(View.GONE);
        holder.binding.messageSenderText.setVisibility(View.GONE);
        holder.binding.messageRecieverImg.setVisibility(View.GONE);
        holder.binding.messageSenderImg.setVisibility(View.GONE);
    }

    private void deleteSenderMessage(int position , MHolder holder){

        String senderId = messagesList.get(position).getFrom();
        String receiverId = messagesList.get(position).getTo();
        String messageId = messagesList.get(position).getMessageId();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("messages").child(senderId).child(receiverId).child(messageId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(holder.itemView.getContext(),
                                "message deleted successfully",
                                Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(holder.itemView.getContext(),
                                "error occurred", Toast.LENGTH_SHORT).show();
                    }
                });
        dialog.dismiss();
        Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
        holder.itemView.getContext().startActivity(intent);

    }

    private void deleteReceiverMessage(int position , MHolder holder){

        String senderId = messagesList.get(position).getFrom();
        String receiverId = messagesList.get(position).getTo();
        String messageId = messagesList.get(position).getMessageId();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("messages").child(receiverId).child(senderId).child(messageId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(holder.itemView.getContext(),
                                "message deleted successfully",
                                Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(holder.itemView.getContext(),
                                "error occurred", Toast.LENGTH_SHORT).show();
                    }
                });

        dialog.dismiss();
        Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
        holder.itemView.getContext().startActivity(intent);

    }

    private void deleteMessageForEveryOne(int position , MHolder holder){

        String senderId = messagesList.get(position).getFrom();
        String receiverId = messagesList.get(position).getTo();
        String messageId = messagesList.get(position).getMessageId();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("messages").child(senderId).child(receiverId).child(messageId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){

                        reference.child("messages").child(receiverId).child(senderId).child(messageId)
                                .removeValue().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()){
                                        messagesList.remove(position).getMessage();
                                        Toast.makeText(holder.itemView.getContext(),
                                                "message deleted successfully",
                                                Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(holder.itemView.getContext(),
                                                "error occurred",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });


                    }else {
                        Toast.makeText(holder.itemView.getContext(),
                                "error occurred",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        dialog.dismiss();
        Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
        holder.itemView.getContext().startActivity(intent);

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class MHolder extends RecyclerView.ViewHolder{

        CustomMessageItemBinding binding;

        public MHolder(@NonNull View itemView) {
            super(itemView);

            binding = CustomMessageItemBinding.bind(itemView);

        }
    }

}
