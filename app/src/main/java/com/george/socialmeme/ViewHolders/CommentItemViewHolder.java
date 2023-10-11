package com.george.socialmeme.ViewHolders;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.george.socialmeme.Activities.UserProfileActivity;
import com.george.socialmeme.R;

import de.hdodenhof.circleimageview.CircleImageView;
import maes.tech.intentanim.CustomIntent;

public class CommentItemViewHolder extends RecyclerView.ViewHolder {

    public Context context;
    public String userID;
    public CardView cardView;
    public CircleImageView profilePicture;
    public TextView usernameTV, commentContent;
    public ImageButton deleteCommentBtn;
    public View openProfile;

    public CommentItemViewHolder(@NonNull View itemView) {
        super(itemView);

        cardView = itemView.findViewById(R.id.comment_card);
        openProfile = itemView.findViewById(R.id.comment_open_profile);
        profilePicture = itemView.findViewById(R.id.circleImageView3);
        usernameTV = itemView.findViewById(R.id.textView65);
        commentContent = itemView.findViewById(R.id.textView69);
        deleteCommentBtn = itemView.findViewById(R.id.delete_comment_btn);

        openProfile.setOnClickListener(v -> {

            if (userID != null) {
                Intent intent = new Intent(itemView.getContext(), UserProfileActivity.class);
                intent.putExtra("user_id", userID);
                intent.putExtra("username", usernameTV.getText().toString());
                itemView.getContext().startActivity(intent);
                CustomIntent.customType(itemView.getContext(), "left-to-right");
            }

        });

        commentContent.setOnClickListener(v -> copyCommentToClipboard());
        commentContent.setOnLongClickListener(v -> {
            copyCommentToClipboard();
            return true;
        });

    }

    void copyCommentToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("username", commentContent.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Comment copied to clipboard", Toast.LENGTH_SHORT).show();
    }

}
