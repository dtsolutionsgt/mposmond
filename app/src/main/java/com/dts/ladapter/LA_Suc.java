package com.dts.ladapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dts.base.DateUtils;
import com.dts.base.MiscUtils;
import com.dts.base.clsClasses;
import com.dts.mposmon.PBase;
import com.dts.mposmon.R;

import java.util.ArrayList;

public class LA_Suc extends BaseAdapter {

    private MiscUtils mu;
    private DateUtils du;

    private ArrayList<clsClasses.clsSuc> items= new ArrayList<clsClasses.clsSuc>();
    private int selectedIndex;
    private LayoutInflater l_Inflater;

    public LA_Suc(Context context, PBase owner, ArrayList<clsClasses.clsSuc> results) {
        items = results;
        l_Inflater = LayoutInflater.from(context);
        selectedIndex = -1;

        mu=owner.mu;
        du=owner.du;

    }

    public void setSelectedIndex(int ind) {
        selectedIndex = ind;
        notifyDataSetChanged();
    }

    public void refreshItems() {
        notifyDataSetChanged();
    }

    public int getCount() {
        return items.size();
    }

    public Object getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {

            convertView = l_Inflater.inflate(R.layout.lv_suc, null);
            holder = new ViewHolder();

            holder.lbl6 = (TextView) convertView.findViewById(R.id.lblV6);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.lbl6.setText(""+items.get(position).nombre);

        if(selectedIndex!= -1 && position == selectedIndex) {
            convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView lbl6;
    }

}

