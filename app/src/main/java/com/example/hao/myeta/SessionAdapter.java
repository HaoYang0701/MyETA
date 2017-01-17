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

    public SessionViewHolder(View itemView) {
      super(itemView);
      nameTextview = (TextView) itemView.findViewById(R.id.session_row_name);
      locationTextview = (TextView) itemView.findViewById(R.id.session_row_location);
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
    tempviewHolder = holder;
    TextView sessionUser = ((SessionViewHolder)tempviewHolder).nameTextview;
    sessionUser.setText(session.getUser());
    TextView sessionLocation = ((SessionViewHolder)tempviewHolder).locationTextview;
    sessionLocation.setText(String.valueOf(session.getLocation().getLatitude()));
  }

  @Override
  public int getItemCount() {
    return sessionList.size();
  }
}
