package com.dts.ladapter;

import android.content.Context;
import java.util.ArrayList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dts.base.DateUtils;
import com.dts.base.MiscUtils;
import com.dts.base.clsClasses;
import com.dts.mposmon.PBase;
import com.dts.mposmon.R;

public class LA_D_orden  extends BaseAdapter {

    private MiscUtils mu;
    private DateUtils du;

    private ArrayList<clsClasses.clsD_orden> items= new ArrayList<clsClasses.clsD_orden>();
    private int selectedIndex;
    private LayoutInflater l_Inflater;

    public LA_D_orden(Context context, PBase owner, ArrayList<clsClasses.clsD_orden> results) {
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
        int lim;

        if (convertView == null) {

            convertView = l_Inflater.inflate(R.layout.lv_d_orden, null);
            holder = new ViewHolder();
            holder.lbl6 = (TextView) convertView.findViewById(R.id.lblV6);
            holder.lbl10 = (TextView) convertView.findViewById(R.id.lblV10);
            holder.lbl11 = (TextView) convertView.findViewById(R.id.lblV11);
            holder.lbl12 = (TextView) convertView.findViewById(R.id.lblV12);
            holder.relBack = (RelativeLayout) convertView.findViewById(R.id.relbase);
            holder.imgw = (ImageView) convertView.findViewById(R.id.imageView4);
            holder.imgc = (ImageView) convertView.findViewById(R.id.imageView9);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        lim=items.get(position).limite;
        holder.lbl6.setText(""+items.get(position).num_orden.toUpperCase());
        holder.lbl10.setText("Tiempo : "+items.get(position).tiempo_total+" min");
        holder.lbl11.setText("Limite : "+items.get(position).limite+" min");
        holder.lbl12.setText(items.get(position).nota);

        if (items.get(position).tiempo_total<2 && lim==0) {
            holder.relBack.setBackgroundColor(items.get(position).color);
        } else {
            holder.relBack.setBackgroundColor(Color.parseColor("#DD9DF5"));
        }

        if (lim>0) {
            holder.imgc.setVisibility(View.VISIBLE);
            //#KM20211231 Agregué el color por defecto
            holder.relBack.setBackgroundColor(Color.rgb(255, 0, 255));
        } else {
            holder.imgc.setVisibility(View.INVISIBLE);
        }

        if (items.get(position).tiempo_total>items.get(position).tiempo_limite) {
            holder.imgw.setVisibility(View.VISIBLE);
            //#KM20211231 Agregué el color por defecto
            holder.relBack.setBackgroundColor(Color.rgb(255, 0, 0));
        } else {
            holder.imgw.setVisibility(View.INVISIBLE);
        }
        if (items.get(position).estado>2) holder.imgw.setVisibility(View.INVISIBLE);

        if(selectedIndex!= -1 && position == selectedIndex) {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView lbl6,lbl10,lbl11,lbl12;
        RelativeLayout relBack;
        ImageView imgw,imgc;
    }

}

