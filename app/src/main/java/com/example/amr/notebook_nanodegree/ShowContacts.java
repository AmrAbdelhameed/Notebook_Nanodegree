package com.example.amr.notebook_nanodegree;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShowContacts extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    ListView lv;
    TextView textempty;
    ArrayAdapter<String> adapter;
    DatabaseReference databaseReference;
    ArrayList<String> specimens_name, specimens_email, specimens_phone, specimens_street, specimens_desc, specimens_id;
    int size_arraylist = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_contacts);

        specimens_name = new ArrayList<>();
        specimens_email = new ArrayList<>();
        specimens_phone = new ArrayList<>();
        specimens_street = new ArrayList<>();
        specimens_desc = new ArrayList<>();
        specimens_id = new ArrayList<>();

        //Toast.makeText(MainActivity.this, FirebaseAuth.getInstance().getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        //Toast.makeText(ShowContacts.this, user.getUid(), Toast.LENGTH_SHORT).show();

        lv = (ListView) findViewById(R.id.listView1);
        textempty = (TextView) findViewById(R.id.textempty);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        databaseReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                for (DataSnapshot child : children) {
                    String uid = child.getKey();
                    String name = child.getValue(Contact.class).getName();
                    String email = child.getValue(Contact.class).getEmail();
                    String phone = child.getValue(Contact.class).getPhone();
                    String street = child.getValue(Contact.class).getAddress();
                    String desc = child.getValue(Contact.class).getDescription();
                    specimens_name.add(name);
                    specimens_email.add(email);
                    specimens_phone.add(phone);
                    specimens_street.add(street);
                    specimens_desc.add(desc);
                    specimens_id.add(uid);
                }
                adapter = new ArrayAdapter<String>(ShowContacts.this, android.R.layout.simple_list_item_1, specimens_name);
                lv.setAdapter(adapter);
                size_arraylist = specimens_name.size();
                if (size_arraylist == 0) {
                    textempty.setVisibility(View.VISIBLE);
                    textempty.setText("(Empty)");
                } else {
                    textempty.setVisibility(View.INVISIBLE);
                    textempty.setText("");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                // TODO Auto-generated method stub
                int id_To_Search = arg2 + 1;

                Bundle dataBundle = new Bundle();

                dataBundle.putInt("id", id_To_Search);
                dataBundle.putString("iduser", specimens_id.get(arg2));
                dataBundle.putString("nameuser", specimens_name.get(arg2));
                dataBundle.putString("emailuser", specimens_email.get(arg2));
                dataBundle.putString("phoneuser", specimens_phone.get(arg2));
                dataBundle.putString("addressuser", specimens_street.get(arg2));
                dataBundle.putString("descuser", specimens_desc.get(arg2));
                //Toast.makeText(ShowContacts.this, specimens_id.get(position) + specimens_name.get(position) + specimens_email.get(position) + specimens_phone.get(position) + specimens_street.get(position) + specimens_desc.get(position), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), AddFriend.class);
                intent.putExtras(dataBundle);
                startActivity(intent);
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                           int index, long arg3) {

                if (!specimens_phone.get(index).isEmpty()) {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", specimens_phone.get(index), null)));
                } else {
                    Toast.makeText(ShowContacts.this, "There is not phone of this contact", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int a = item.getItemId();
        if (a == R.id.item1) {
            Bundle dataBundle = new Bundle();
            dataBundle.putInt("id", 0);
            Intent intent = new Intent(getApplicationContext(), AddFriend.class);
            intent.putExtras(dataBundle);
            startActivity(intent);
            return true;
        }
        if (a == R.id.item2) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you want to Logout ?!")
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            FirebaseAuth.getInstance().signOut();
                            finish();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Nothing
                }
            });
            AlertDialog d = builder.create();
            d.setTitle("Are you sure");
            d.show();
            return true;
        }
        if (a == R.id.item3) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you want to delete all your data ?!")
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            if (size_arraylist > 0) {

                                RemoveAllContact(user.getUid());
                                Toast.makeText(getApplicationContext(), "Deleted All Data Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(getIntent());

                            } else {
                                Toast.makeText(getApplicationContext(), "There is Nothing", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Nothing
                }
            });
            AlertDialog d = builder.create();
            d.setTitle("Are you sure");
            d.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void RemoveAllContact(String id) {

        Query applesQuery = databaseReference.child(id);

        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keycode, event);
    }

}
