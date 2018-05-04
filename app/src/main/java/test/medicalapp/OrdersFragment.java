package test.medicalapp;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    TextView name, mobile, address, total;
    ListView listView;
    List<String> list;
    ArrayAdapter adapter;

    public OrdersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        name = view.findViewById(R.id.name);
        mobile = view.findViewById(R.id.mobile);
        address = view.findViewById(R.id.address);
        total = view.findViewById(R.id.total_cost);
        listView = view.findViewById(R.id.list);

        list = new ArrayList<>();

        list.add("TEST!");

        adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading, Please Wait");
        progressDialog.show();

        Toast.makeText(getActivity(), getArguments().getString("key"), Toast.LENGTH_SHORT).show();
        FirebaseDatabase.getInstance().getReference("orders")
                .child(getArguments().getString("key"))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(dataSnapshot.child("user").getValue().toString())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        progressDialog.dismiss();
                                        name.setText(dataSnapshot.child("name").getValue().toString());
                                        mobile.setText(dataSnapshot.child("phone").getValue().toString());
                                        address.setText(dataSnapshot.child("address").getValue().toString());
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                        list.clear();
                        int total = 0;
                        for (DataSnapshot snapshot :
                                dataSnapshot.child("order").getChildren()) {
                            CartItem item = snapshot.getValue(CartItem.class);
                            list.add(item.name + " - " + item.quantity);
                            total += item.quantity * item.cost;
                        }

                        adapter.notifyDataSetChanged();
                        OrdersFragment.this.total.setText(String.valueOf(total));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        return view;
    }

}
