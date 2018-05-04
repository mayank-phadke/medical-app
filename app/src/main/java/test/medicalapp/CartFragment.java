package test.medicalapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CartFragment extends Fragment {

    List<CartItem> list;
    MyCartRecyclerViewAdapter adapter;
    TextView totalCost;

    public CartFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart_list, container, false);

        list = new ArrayList<CartItem>();
        adapter = new MyCartRecyclerViewAdapter(list);

        totalCost = view.findViewById(R.id.total);

        // Set the adapter
        RecyclerView recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading, Please Wait");
        progressDialog.show();

        FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("cart")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        progressDialog.dismiss();
                        list.clear();
                        int total = 0;
                        for (DataSnapshot snapshot :
                                dataSnapshot.getChildren()) {
                            CartItem item = snapshot.getValue(CartItem.class);
                            list.add(item);
                            total += item.cost * item.quantity;
                        }
                        adapter.notifyDataSetChanged();
                        totalCost.setText(total + "");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        Button place_order = view.findViewById(R.id.buy);
        place_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(list.isEmpty())
                    return;

                FirebaseDatabase.getInstance().getReference("users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("cart")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("orders")
                                        .push();
                                ref.child("user").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                ref.child("order").setValue(dataSnapshot.getValue());

                                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                Date date = new Date();
                                String dateStr = dateFormat.format(date);

                                ref.child("date").setValue(dateStr);

                                FirebaseDatabase.getInstance().getReference("users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("cart")
                                        .removeValue();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }
        });

        return view;
    }
}
