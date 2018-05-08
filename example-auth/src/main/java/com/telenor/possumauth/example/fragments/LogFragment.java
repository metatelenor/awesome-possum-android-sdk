package com.telenor.possumauth.example.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.telenor.possumauth.example.R;
import com.telenor.possumcore.PossumCore;
import com.telenor.possumcore.constants.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogFragment extends TrustFragment {
    private LogAdapter logAdapter;
    private BroadcastReceiver receiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle bundle) {
        return inflater.inflate(R.layout.fragment_sub_log, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        Button clearButton = view.findViewById(R.id.clearButton);
        clearButton.setOnClickListener(v -> PossumCore.clearLog(getContext()));
        logAdapter = new LogAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(logAdapter);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long time = intent.getLongExtra("time", 0);
                String text = intent.getStringExtra("log");
                if (time == 0 || text == null) {
                    // Clear log
                    logAdapter.clearLog();
                } else {
                    logAdapter.addEntry(new LogEntry(time, text));
                }
            }
        };
        getContext().getApplicationContext().registerReceiver(receiver, new IntentFilter(Constants.PossumLog));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().getApplicationContext().unregisterReceiver(receiver);
    }

    @Override
    public void newTrustScore(String graphName, int graphPos, float newScore) {

    }

    @Override
    public void detectorValues(String graphName, int graphPos, float score, float training) {

    }

    @Override
    public void updateVisibility(String graphName, boolean visible) {

    }

    class LogEntry {
        long timestamp;
        public String text;
        LogEntry(long time, String text) {
            this.timestamp = time;
            this.text = text;
        }
    }

    private class LogAdapter extends RecyclerView.Adapter<LogViewHolder> {
        private List<LogEntry> logData = new ArrayList<>();
        private SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss", Locale.US);

        @Override
        public LogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new LogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.log_row, parent, false));
        }

        @Override
        public void onBindViewHolder(LogViewHolder holder, int position) {
            holder.timeField.setText(String.format(Locale.US, "%s", dateFormat.format(new Date(logData.get(position).timestamp))));
            holder.textField.setText(logData.get(position).text);
        }

        @Override
        public int getItemCount() {
            return logData.size();
        }

        void addEntry(LogEntry entry) {
            logData.add(entry);
            notifyItemInserted(logData.size()-1);
        }

        void clearLog() {
            logData.clear();
            notifyDataSetChanged();
        }
    }

    class LogViewHolder extends RecyclerView.ViewHolder {
        TextView timeField;
        TextView textField;
        LogViewHolder(View itemView) {
            super(itemView);
            timeField = itemView.findViewById(R.id.time);
            textField = itemView.findViewById(R.id.text);
        }
    }
}