package test.medicalapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    // UI Components
    EditText name, address, phone, email, password, conPass;
    TextInputLayout nameView, addrView, phoneView, emailView, passwordView, conPassView;
    ProgressDialog progressDialog;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = findViewById(R.id.name);
        nameView = findViewById(R.id.nameView);
        address = findViewById(R.id.address);
        addrView = findViewById(R.id.addressView);
        phone = findViewById(R.id.phone);
        phoneView = findViewById(R.id.phoneView);
        email = findViewById(R.id.email);
        emailView = findViewById(R.id.emailView);
        password = findViewById(R.id.password);
        passwordView = findViewById(R.id.passwordView);
        conPass = findViewById(R.id.conPass);
        conPassView = findViewById(R.id.conPassView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading, Please Wait");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        auth = FirebaseAuth.getInstance();

        Button register = findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });
    }

    void attemptRegister() {

        nameView.setError(null);
        addrView.setError(null);
        phoneView.setError(null);
        emailView.setError(null);
        passwordView.setError(null);
        conPassView.setError(null);

        final String
                name = this.name.getText().toString(),
                address = this.address.getText().toString(),
                phone = this.phone.getText().toString(),
                email = this.email.getText().toString(),
                password = this.password.getText().toString(),
                conPass = this.conPass.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(conPass)) {
            this.conPassView.setError(getString(R.string.error_field_required));
            focusView = this.conPass;
            cancel = true;
        } else if (!(conPass.length() >= 8)) {
            this.conPassView.setError(getString(R.string.error_invalid_password));
            focusView = this.conPass;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            this.passwordView.setError(getString(R.string.error_field_required));
            focusView = this.password;
            cancel = true;
        } else if (!(password.length() >= 8)) {
            this.passwordView.setError(getString(R.string.error_invalid_password));
            focusView = this.password;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            this.emailView.setError(getString(R.string.error_field_required));
            focusView = this.email;
            cancel = true;
        } else if (!email.contains("@")) {
            this.emailView.setError(getString(R.string.error_invalid_email));
            focusView = this.email;
            cancel = true;
        }

        if (TextUtils.isEmpty(phone)) {
            this.phoneView.setError(getString(R.string.error_field_required));
            focusView = this.phone;
            cancel = true;
        }

        if (TextUtils.isEmpty(address)) {
            this.addrView.setError(getString(R.string.error_field_required));
            focusView = this.address;
            cancel = true;
        }

        if (TextUtils.isEmpty(name)) {
            this.nameView.setError(getString(R.string.error_field_required));
            focusView = this.name;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            progressDialog.show();
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(auth.getCurrentUser().getUid())
                                .setValue(new User(name, address, phone, "customer"));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}

class User {
    public String name;
    public String address;
    public String phone;

    public User(String name, String address, String phone, String role) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.role = role;
    }

    public String role;

    public User() {
    }


}