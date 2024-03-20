package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder>{

    Context mContext;
    List<Comment> commentList;
    public CommentAdapter(Context mContext, List<Comment> commentList)
    {
        this.mContext = mContext;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment, null, false);
        return new CommentViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position)
    {
        Comment currentComment = commentList.get(position);
        holder.commentDate.setText(currentComment.getDate());
        holder.commentAuthor.setText(currentComment.getUsername());
        holder.commentBody.setText(currentComment.getBody());

    }

    @Override
    public int getItemCount() {
       return commentList.size();
    }

    static public class CommentViewHolder extends RecyclerView.ViewHolder {
        CardView commentView;
        TextView commentBody;
        TextView commentAuthor;
        TextView commentDate;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            commentView = itemView.findViewById(R.id.comment_card_view);
            commentBody = itemView.findViewById(R.id.comment_body);
            commentAuthor = itemView.findViewById(R.id.comment_author);
            commentDate = itemView.findViewById(R.id.comment_date);
            if( commentView == null || commentBody == null || commentAuthor == null || commentDate == null)
                throw new RuntimeException("Could not initialize XML Components for Comment\n");
        }
    }


}
