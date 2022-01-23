package com.example.hackvengers.feed;

import android.content.Context;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hackvengers.FeedActivity;
import com.example.hackvengers.R;

import java.util.ArrayList;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    ArrayList<FeedObject> feedList;
    Context context;

    public FeedAdapter(ArrayList<FeedObject> feedList, FeedActivity feedActivity){
        this.feedList = feedList;
        context = feedActivity;
    }

    @NonNull
    @Override
    public FeedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed_post,null,false);

        RecyclerView.LayoutParams layoutParams=new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(layoutParams);

        return new FeedAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedAdapter.ViewHolder holder, int position) {

        FeedObject feedObject = feedList.get(position);

        holder.orgName.setText(feedObject.getOrgName());
        holder.eventName.setText(feedObject.getEventName());
        holder.eventDetails.setText(feedObject.getEventDetails());
        holder.eventLink.setText(feedObject.getEventLink());
        holder.eventLink.setMovementMethod(LinkMovementMethod.getInstance());

        if(!feedObject.getOrgImage().equals("")){
            holder.orgImage.setClipToOutline(true);
            Glide.with(context).load(Uri.parse(feedObject.getOrgImage())).into(holder.orgImage);
        }
        else{
            holder.orgImage.setImageResource(R.drawable.ic_baseline_person_24);
        }

        holder.eventPoster.setClipToOutline(true);
        Glide.with(context).load(Uri.parse(feedObject.getEventPoster())).into(holder.eventPoster);

    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView orgName,eventName,eventDetails,eventLink;
        ImageView orgImage,eventPoster;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            orgName = itemView.findViewById(R.id.feed_org_name);
            orgImage = itemView.findViewById(R.id.feed_organiser_image);
            eventName = itemView.findViewById(R.id.feed_event_name);
            eventDetails = itemView.findViewById(R.id.feed_details);
            eventPoster = itemView.findViewById(R.id.feed_poster);
            eventLink = itemView.findViewById(R.id.feed_link);
        }
    }
}
