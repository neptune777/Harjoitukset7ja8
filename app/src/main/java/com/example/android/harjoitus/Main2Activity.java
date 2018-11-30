package com.example.android.harjoitus;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.harjoitus.data.Aku;
import com.example.android.harjoitus.data.DatabaseContentAdapter;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main2Activity extends AppCompatActivity {

    private DatabaseContentAdapter mAdapter;
    private RecyclerView mNumbersList;
    private static final String TAG = "Child";
    private ProgressBar mLoadingIndicator;
    private DatabaseReference databaseReference;
    private List<Aku> allAku;
    private final String SORT_EXTRA = "sort";
    private boolean userSignedIn=false;
    private FirebaseAuth mFirebaseAuth;
    public static final int RC_SIGN_IN = 1;
    private FirebaseAuth.AuthStateListener mAuthStateListner;

    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mFirebaseAuth=FirebaseAuth.getInstance();
        mAuthStateListner=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();

                if (user!=null)
                {
                    Toast.makeText(Main2Activity.this, "User Signed In", Toast.LENGTH_SHORT).show();

                    if(userSignedIn) {
                        finish();
                        Intent i = new Intent(getApplicationContext(), Main2Activity.class);
                        i.setAction(Intent.ACTION_MAIN);
                        i.addCategory(Intent.CATEGORY_LAUNCHER);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(i);
                    }
                }
                else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN
                    );

                    userSignedIn=true;
                }
            }
        };

            databaseReference = FirebaseDatabase.getInstance().getReference();

            mNumbersList = (RecyclerView) findViewById(R.id.rv_numbers);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            mNumbersList.setLayoutManager(layoutManager);
            mNumbersList.setHasFixedSize(true);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mNumbersList.getContext(), layoutManager.getOrientation());
            mNumbersList.addItemDecoration(dividerItemDecoration);

            mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
            mLoadingIndicator.setVisibility(View.VISIBLE);
            allAku = new ArrayList();

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                // Called when a user swipes left or right on a ViewHolder
                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                    // Here is where you'll implement swipe to delete

                    //[Hint] Use getTag (from the adapter code) to get the id of the swiped item
                    // Retrieve the id of the task to delete
                    String id = (String) viewHolder.itemView.getTag();
                    deleteById(id);

                }
            }).attachToRecyclerView(mNumbersList);


            databaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    getAllAkus(dataSnapshot);

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    getAllAkus(dataSnapshot);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    akuDeletion(dataSnapshot);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    getAllAkus(dataSnapshot);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

    }

    private void sort(){

        ArrayList<Aku> tempList = (ArrayList<Aku>) allAku;
        ArrayList<Aku> newList = new ArrayList<>();

        while(tempList.size()>0) {
            int sm = getIndexOfSmallest(tempList);
             Aku aku = tempList.get(sm);
             newList.add(aku);
             tempList.remove(sm);
        }

        allAku=newList;
        Intent intent = new Intent(this, Main2Activity.class);
        intent.putExtra(SORT_EXTRA, newList);
        mAdapter.notifyDataSetChanged();
        mAdapter = new DatabaseContentAdapter(Main2Activity.this, newList);
        mNumbersList.setAdapter(mAdapter);

    }

    private int getIndexOfSmallest(List<Aku> list){

        int Smallest=1000000;
        int indexOfSmallest=1000000;

        for(int i=0;i<list.size();i++){
            int numero = Integer.parseInt(list.get(i).getNumero());
            if((Smallest)>numero){
                Smallest = numero;
                indexOfSmallest=i;
            }
        }
        return indexOfSmallest;
    }

    private void getAllAkus(DataSnapshot dataSnapshot){

        GenericTypeIndicator<HashMap<String, Object>> objectsGTypeInd = new GenericTypeIndicator<HashMap<String, Object>>() {};
        Map<String, Object> objectHashMap = dataSnapshot.getValue(objectsGTypeInd);

        for(int i=0;i<objectHashMap.size();i++){

           String id = dataSnapshot.getKey();

            if(!contains(id)) {
                String nimi = objectHashMap.get("nimi").toString();
                String hankintaPvm = objectHashMap.get("hankintaPvm").toString();
                String painos = objectHashMap.get("painos").toString();
                String numero = objectHashMap.get("numero").toString();

                Aku aku = new Aku(id, nimi, numero, hankintaPvm, painos);
                    allAku.add(aku);

            }
        }

        mAdapter = new DatabaseContentAdapter(this, allAku);
        mNumbersList.setAdapter(mAdapter);
        mLoadingIndicator.setVisibility(View.INVISIBLE);

    }

    private void addNewAku(String nimi, String numero, String painos, String hankintapvm) {

        Aku aku = new Aku(nimi,numero,hankintapvm,painos);
        databaseReference.push().setValue(aku);

    }

    private boolean contains(String id){

        for(Aku aku:allAku){

            if(aku.getId().equals(id)){
                return true;
            }

        }
        return false;
    }

    private void deleteFirst(){

      String id =  allAku.get(0).getId();
      deleteById(id);

    }

    private void akuDeletion(DataSnapshot dataSnapshot){
        for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
            String id = dataSnapshot.getKey();
            for(int i = 0; i < allAku.size(); i++){
                if(allAku.get(i).getId().equals(id)){
                    allAku.remove(i);
                }
            }

            mAdapter.notifyDataSetChanged();
            mAdapter = new DatabaseContentAdapter(Main2Activity.this, allAku);
            mNumbersList.setAdapter(mAdapter);
        }
    }

    private void deleteById(String id){

        databaseReference.child(id).removeValue();
    }

    public void onClick(View view){

        switch (view.getId()) {
            case R.id.add:

                EditText nimi=findViewById(R.id.editText2);
                EditText  nro=findViewById(R.id.editText3);
                EditText painos=findViewById(R.id.editText);
                EditText  hankintapvm=findViewById(R.id.editText4);

                if(!checkIfEmptyOrNull(nimi.getText().toString()) && !checkIfEmptyOrNull(nro.getText().toString())) {
                    addNewAku(nimi.getText().toString(), nro.getText().toString(), painos.getText().toString(), hankintapvm.getText().toString());

                }

                break;

            case R.id.delete:

                deleteFirst();

                break;

            case R.id.sort:

                sort();

                break;

            case R.id.sign_out:

                signout(view);

                break;

        }

    }

    public boolean checkIfEmptyOrNull(String fieldValue){

        if(!TextUtils.isEmpty(fieldValue)){

            if(fieldValue!=null){
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        mFirebaseAuth.addAuthStateListener(mAuthStateListner);

    }

    @Override
    protected void onPause() {
        super.onPause();

        mFirebaseAuth.removeAuthStateListener(mAuthStateListner);
    }

    public void signout(View view) {

        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(Main2Activity.this, "User Signed Out", Toast.LENGTH_SHORT).show();
                        finish();
                        stopApp();
                    }
                });

    }

    protected void stopApp(){

        ExitActivity.exit(getApplicationContext());
    }

}
