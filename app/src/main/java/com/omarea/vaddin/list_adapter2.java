package com.omarea.vaddin;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;

public class list_adapter2 extends BaseAdapter {

    private Context context;
    private ArrayList<HashMap<String, Object>> list;
    public HashMap<Integer, Boolean> states = new HashMap<>();

    public list_adapter2(Context context, ArrayList<HashMap<String, Object>> list) {
        this.context = context;
        this.list = list;
        for (int i = 0; i < list.size(); i++) {
            states.put(i, !(list.get(i).get("select_state") == null || !((Boolean) (list.get(i).get("select_state")))));
        }
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public HashMap<String, Object> getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = View.inflate(context, R.layout.app_item2, null);
            viewHolder.itemTitle = (TextView) convertView.findViewById(R.id.ItemTitle);
            viewHolder.enabledStateText = (TextView) convertView.findViewById(R.id.ItemEnabledStateText);
            viewHolder.itemText = (TextView) convertView.findViewById(R.id.ItemText);
            viewHolder.imgView = (ImageView) convertView.findViewById(R.id.ItemIcon);
            viewHolder.itemChecke = (CheckBox) convertView.findViewById(R.id.select_state);
            viewHolder.wranStateText = (TextView) convertView.findViewById(R.id.ItemWranText);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.itemTitle.setText(getItem(position).get("name").toString());
        viewHolder.itemText.setText(getItem(position).get("packageName").toString());
        viewHolder.imgView.setImageDrawable((Drawable) getItem(position).get("icon"));
        if (getItem(position).get("enabled_state") != null)
            viewHolder.enabledStateText.setText(getItem(position).get("enabled_state").toString());
        else
            viewHolder.enabledStateText.setText("");

        //为checkbox添加复选监听,把当前位置的checkbox的状态存进一个HashMap里面
        viewHolder.itemChecke.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            states.put(position, isChecked);
            }
        });
        //从hashmap里面取出我们的状态值,然后赋值给listview对应位置的checkbox
        viewHolder.itemChecke.setChecked(states.get(position));

        if (getItem(position).get("wran_state") != null)
            viewHolder.wranStateText.setText(getItem(position).get("wran_state").toString());
        else
            viewHolder.wranStateText.setText("");

        return convertView;
    }

    public class ViewHolder {
        TextView itemTitle;
        CheckBox itemChecke;
        ImageView imgView;
        TextView itemText;
        TextView enabledStateText;
        TextView wranStateText;
    }
}