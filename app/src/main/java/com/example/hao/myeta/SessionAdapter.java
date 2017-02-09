package com.example.hao.myeta;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class SessionAdapter extends RecyclerView.Adapter {
  Context context;
  List<Session> sessionList;
  RecyclerView.ViewHolder tempviewHolder;

  public static class SessionViewHolder extends RecyclerView.ViewHolder {
    public TextView nameTextview;
    public TextView locationTextview;
    public TextView durationTextview;
    public TextView distanceTextview;

    public SessionViewHolder(View itemView) {
      super(itemView);
      nameTextview = (TextView) itemView.findViewById(R.id.session_row_name);
      locationTextview = (TextView) itemView.findViewById(R.id.session_row_starLocation);
      durationTextview = (TextView) itemView.findViewById(R.id.session_row_Duration);
      distanceTextview = (TextView) itemView.findViewById(R.id.session_row_Distance);
    }
  }

  public SessionAdapter (Context context, List<Session> sessionList){
    this.context = context;
    this.sessionList = sessionList;
  }


  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    View sessionView = inflater.inflate(R.layout.session_row, parent, false);
    SessionViewHolder viewHolder = new SessionViewHolder(sessionView);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    Session session = sessionList.get(position);
    TripInfo tripInfo = session.getTripInfo();
    tempviewHolder = holder;

    TextView sessionUser = ((SessionViewHolder)tempviewHolder).nameTextview;
    sessionUser.setText(session.getUser());
    if (tripInfo != null){
      TextView sessionLocation = ((SessionViewHolder)tempviewHolder).locationTextview;
      sessionLocation.setText(context.getString(R.string.trip_start_location) + tripInfo.getStartAddress());
      TextView sessionDuration = ((SessionViewHolder)tempviewHolder).durationTextview;
      sessionDuration.setText(context.getString(R.string.trip_duration) + tripInfo.getDuration());
      TextView sessionDistance = ((SessionViewHolder)tempviewHolder).distanceTextview;
      sessionDistance.setText(context.getString(R.string.trip_distance) + tripInfo.getDistance());
    }
  }

  @Override
  public int getItemCount() {
    return sessionList.size();
  }
}
