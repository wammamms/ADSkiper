package com.seventyseven.adskiper.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.seventyseven.adskiper.R;
import com.seventyseven.adskiper.db.RecordDao;
import com.seventyseven.adskiper.model.Record;
import com.seventyseven.adskiper.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RankActivity extends AppCompatActivity {

    private List<AppRecord> list = new ArrayList<>();
    private TextView tvEmpty;
    private ListView lvTop;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);
        initView();
        initData();
    }

    private void initView() {
        tvEmpty = findViewById(R.id.tv_empty);
        lvTop = findViewById(R.id.lv_toplist);
    }

    private void initData(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                list.clear();
                List<Record> listRecord = new RecordDao(RankActivity.this).getRecordList();
                if(list != null && listRecord.size() > 0){
                    for(Record record : listRecord){
                        AppRecord appRecord = new AppRecord();
                        appRecord.record = record;
                        appRecord.drawable = AppUtils.getDrawable(RankActivity.this,record.getPackageName());
                        list.add(appRecord);
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setData();
                    }
                });
            }
        }.start();
    }

    private void setData(){
        if(list.size() > 0){
            tvEmpty.setVisibility(View.GONE);
            lvTop.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.VISIBLE);
            lvTop.setVisibility(View.GONE);
        }
        lvTop.setAdapter(new RankAdapter());
    }

    public class RankAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if(view == null){
                holder = new ViewHolder();
                view = LayoutInflater.from(RankActivity.this).inflate(R.layout.item,null);
                holder.ivLog = view.findViewById(R.id.iv_logo);
                holder.tvName = view.findViewById(R.id.tv_name);
                holder.tvTimes = view.findViewById(R.id.tv_times);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            AppRecord appRecord = list.get(i);
            holder.ivLog.setImageDrawable(appRecord.drawable);
            holder.tvName.setText(appRecord.record.getAppName());
            holder.tvTimes.setText("跳过"+appRecord.record.getTimes() + "次");
            return view;
        }

        class ViewHolder{
            ImageView ivLog;
            TextView tvName,tvTimes;
        }
    }

    public class AppRecord{
        Drawable drawable;
        Record record;
    }
}