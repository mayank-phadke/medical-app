package test.medicalapp;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrderListFragment extends Fragment {

    ListView listView;
    List<String> list, keyList;
    ArrayAdapter adapter;

    public OrderListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);

        listView = view.findViewById(R.id.list);
        list = new ArrayList<>();
        keyList = new ArrayList<>();
        adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading, Please Wait");
        progressDialog.show();

        FirebaseDatabase.getInstance().getReference("orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        progressDialog.dismiss();
//                        list.clear();
                        keyList.clear();

                        for (final DataSnapshot snapshot :
                                dataSnapshot.getChildren()) {
//                            Toast.makeText(getActivity(), snapshot.getKey(), Toast.LENGTH_SHORT).show();
                            keyList.add(snapshot.getKey());
                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(snapshot.child("user").getValue().toString())
                                    .child("name")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot userDataSnapshot) {
//                                            Toast.makeText(getActivity(), "user name", Toast.LENGTH_SHORT).show();
                                            list.add(userDataSnapshot.getValue().toString() + " - " + snapshot.child("date").getValue().toString());
                                            adapter.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle = new Bundle();
                bundle.putString("key", keyList.get(i));

                OrdersFragment fragment = new OrdersFragment();
                fragment.setArguments(bundle);

                getFragmentManager().beginTransaction().replace(R.id.main_content, fragment).commitAllowingStateLoss();
            }
        });

        return view;
    }

}
