package ru.futoke.arproject;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import ru.futoke.arproject.renderer.R;

/**
 * Created by ichiro on 25.03.2017.
 */

public class ListAdapter extends BaseAdapter {

    private ArrayList<HashMap<String, String>> modelsList = new ArrayList<>();
    private LayoutInflater inflater;

    public ListAdapter(
            Context context,
            ArrayList<HashMap<String, String>> modelsList
    ) {
        this.modelsList = modelsList;
        inflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return modelsList.size();
    }

    @Override
    public String getItem(int position) {
        return modelsList.get(position).get("name");
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        String emptyImg =
            "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity_listview, null);

            holder = new ViewHolder();
            holder.itemTitle = (TextView) convertView.findViewById(
                R.id.listview_item_title
            );
            holder.itemImage = (ImageView) convertView.findViewById(
                R.id.listview_image
            );
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        // Set title.
        holder.itemTitle.setText(modelsList.get(position).get("name"));
        // Set image.
        String img = modelsList.get(position).get("img");
        byte[] imageAsBytes;

        if (img != "") {
            String[] buffer = img.split(",");
            imageAsBytes = Base64.decode(buffer[1].getBytes(), Base64.DEFAULT);
        } else {
            imageAsBytes = Base64.decode(emptyImg.getBytes(), Base64.DEFAULT);
        }

        holder.itemImage.setImageBitmap(
            BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length)
        );

        return convertView;
    }

    private static class ViewHolder {
        TextView itemTitle;
        ImageView itemImage;
    }
}


