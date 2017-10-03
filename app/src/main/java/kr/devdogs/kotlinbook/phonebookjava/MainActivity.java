package kr.devdogs.kotlinbook.phonebookjava;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.Vector;

import io.realm.Realm;
import io.realm.RealmResults;
import kr.devdogs.kotlinbook.phonebookjava.activity.FormActivity;
import kr.devdogs.kotlinbook.phonebookjava.adapter.PhoneBookListAdapter;
import kr.devdogs.kotlinbook.phonebookjava.model.PhoneBook;

public class MainActivity extends AppCompatActivity {
    private ListView phoneBookListView;
    private Button insertBtn;
    private Vector<PhoneBook> items;
    private PhoneBookListAdapter adapter;
    private EditText searchText;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        Realm.init(getApplicationContext());
        realm = Realm.getDefaultInstance();
        items = new Vector<PhoneBook>();
        adapter = new PhoneBookListAdapter(this, R.layout.phonebook_listitem, items) ;
        phoneBookListView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        findByName(searchText.getText().toString());
    }

    private void initView() {
        phoneBookListView = (ListView) findViewById(R.id.main_tel_list);
        insertBtn = (Button) findViewById(R.id.main_btn_insert);
        searchText = (EditText) findViewById(R.id.main_search_text);

        insertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent insertViewIntent = new Intent(MainActivity.this, FormActivity.class);
                startActivity(insertViewIntent);
            }
        });

        searchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                findByName(searchText.getText().toString());
                return false;
            }
        });
    }

    private void findByName(String name) {
        items.clear();
        RealmResults<PhoneBook> allUser = realm.where(PhoneBook.class)
                .beginsWith("name", name)
                .findAll()
                .sort("name");
        for(PhoneBook p:allUser) {
            items.add(p);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
