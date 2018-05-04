package test.medicalapp;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment {

    ListView listView;
    List<String> itemsList;
    ArrayAdapter arrayAdapter;
    ProgressDialog progressDialog;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        itemsList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, itemsList);
        listView = view.findViewById(R.id.list);
        listView.setAdapter(arrayAdapter);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading, Please Wait");
        progressDialog.show();

        FirebaseDatabase.getInstance().getReference("stock").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                itemsList.clear();

                for (DataSnapshot snapshot :
                        dataSnapshot.getChildren()) {
                    itemsList.add(snapshot.child("name").getValue().toString() + " - " + snapshot.child("cost").getValue().toString());
                }

                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        EditText search = view.findViewById(R.id.search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                arrayAdapter.getFilter().filter(charSequence);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

                final EditText input = new EditText(getActivity());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                final String name = itemsList.get(i).split(" - ")[0];
                final String cost = itemsList.get(i).split(" - ")[1];

                alertDialog
                        .setTitle(name)
                        .setMessage("Enter Quantity of Packets")
                        .setPositiveButton("Add to Cart", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                String number = input.getText().toString();
                                if (validate(number)) {
                                    FirebaseDatabase.getInstance().getReference("users")
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .child("cart")
                                            .push()
                                            .setValue(new CartItem(name, Integer.parseInt(number), Integer.parseInt(cost)))
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                        Toast.makeText(getActivity(), "Item Added to Cart", Toast.LENGTH_SHORT).show();
                                                    else
                                                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                }
                            }
                        })
                        .show();

            }
        });

        return view;
    }

    boolean validate(String number) {
        if (number.trim().equals(""))
            return false;
        try {
            Integer.parseInt(number);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

}

class CartItem {
    public String name;
    public int quantity, cost;

    public CartItem() {
    }

    public CartItem(String name, int quantity, int cost) {
        this.name = name;
        this.quantity = quantity;
        this.cost = cost;
    }
}
