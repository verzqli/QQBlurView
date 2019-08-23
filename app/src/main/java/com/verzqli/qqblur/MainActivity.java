package com.verzqli.qqblur;

import android.content.Context;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.verzqli.blurview.blur.QQBlurView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public List<String> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QQBlurView qqBlurView = findViewById(R.id.blur);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            list.add("" + i);
        }
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(new MainAdapter(this, list));
        qqBlurView.setEnableBlur(true);
        qqBlurView.setBlurView(qqBlurView);
        qqBlurView.setTargetView(listView);
        qqBlurView.setBlurRadius(1);
        qqBlurView.setEraseColor(-1);
        qqBlurView.onCreate();
        qqBlurView.onAttached();
    }

    public class MainAdapter extends BaseAdapter {

        private List<String> mList;
        private Context mContext;

        public MainAdapter(Context context, List<String> list) {
            this.mContext = context;
            this.mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public String getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item, null);
                holder.imageView = convertView.findViewById(R.id.avatar);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.imageView.setImageResource((position & 1) != 0 ? R.drawable.chenyao1 : R.drawable.chenyao2);
            return convertView;
        }

        class ViewHolder {
            ImageView imageView;
        }
    }

}
