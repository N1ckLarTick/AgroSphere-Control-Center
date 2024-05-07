package com.example.agrospherecontrolcenter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.agrospherecontrolcenter.databinding.ActivityRegistrationBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {
    private ActivityRegistrationBinding binding;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.signUpButton.setOnClickListener(view -> {
            String login = binding.username.getText().toString();
            String password = binding.password.getText().toString();
            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Введите данные", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(login, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignupActivity.this, "Успешно", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignupActivity.this, "Не удалось зарегистрироваться", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        });
        binding.loginRedirectText.setOnClickListener(view ->{
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

}