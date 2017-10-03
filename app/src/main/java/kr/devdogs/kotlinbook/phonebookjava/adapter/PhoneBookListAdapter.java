package kr.devdogs.kotlinbook.phonebookjava.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Vector;

import kr.devdogs.kotlinbook.phonebookjava.MainActivity;
import kr.devdogs.kotlinbook.phonebookjava.R;
import kr.devdogs.kotlinbook.phonebookjava.activity.FormActivity;
import kr.devdogs.kotlinbook.phonebookjava.model.PhoneBook;

/**
 * Created by Daniel on 2017. 9. 24..
 */

public class PhoneBookListAdapter extends ArrayAdapter<PhoneBook> {
    public Vector<PhoneBook> items ;
    private int viewId;

    public PhoneBookListAdapter(Context context, int viewId, Vector<PhoneBook> items) {
        super(context, viewId, items);
        this.items = items;
        this.viewId = viewId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(this.viewId, null);
        }

        if(items!=null && items.size() > 0) {
            final PhoneBook item = items.get(position);

            if(item != null) {
                ImageView photoView = (ImageView) view.findViewById(R.id.book_item_photo);
                LinearLayout itemLayout = (LinearLayout) view.findViewById(R.id.book_item);
                TextView nameView = (TextView) view.findViewById(R.id.book_item_name);
                Button callView = (Button) view.findViewById(R.id.book_item_call);

                if(item.getPhotoSrc() == null) {
                    photoView.setImageDrawable(getContext().getDrawable(R.drawable.icon_man));
                } else {
                    photoView.setImageBitmap(BitmapFactory.decodeFile(Uri.parse(item.getPhotoSrc()).getPath()));
                }

                nameView.setText(item.getName());
                callView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + item.getPhone()));
                        getContext().startActivity(intent);
                    }
                });

                itemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent modifyViewIntent = new Intent(getContext(), FormActivity.class);
                        modifyViewIntent.putExtra("mode", FormActivity.MODE_UPDATE);
                        modifyViewIntent.putExtra("bookId", item.getId());
                        getContext().startActivity(modifyViewIntent);
                    }
                });
            }
        }

        return view;
    }

}
