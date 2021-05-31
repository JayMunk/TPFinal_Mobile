package com.munger.tpfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.WriterException;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class MainActivity extends AppCompatActivity {

    EditText emailEditText;
    EditText passwordEditText;
    FirebaseAuth firebaseAuth;
    ImageView codeQRImageView;
    Bitmap bitmap;
    String email;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        codeQRImageView = findViewById(R.id.codeQRImageView);

        firebaseAuth = FirebaseAuth.getInstance();


        if (firebaseAuth.getCurrentUser() != null) {
            login();
        }
    }

    public void login() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
    }


    public void loginClicked(View view) {
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();
        if (!email.equals("") && !password.equals("")) {
            firebaseAuth.signInWithEmailAndPassword(emailEditText.getText().toString(),
                    passwordEditText.getText().toString()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    login();
                    emailEditText.setText("");
                    passwordEditText.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "Connexion échouée ! Veuillez réessayer!",
                            Toast.LENGTH_SHORT).show();
                }

            });
        } else {
            Toast.makeText(MainActivity.this, "Remplir les champs",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void signInClicked(View view) {
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();
        if (!email.equals("") && !password.equals("")) {
            firebaseAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(),
                    passwordEditText.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseDatabase.getInstance().getReference().child("Users")
                                    .child(Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getUser()).getUid())
                                    .child("email").setValue(email)
                            ;
                            generateQR();
                            emailEditText.setText("");
                            passwordEditText.setText("");
                        } else {
                            Toast.makeText(MainActivity.this, "Création du compte échouée ! Veuillez réessayer!", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(MainActivity.this, "Remplir les champs",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void generateQR() {
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = Math.min(width, height);
        smallerDimension = smallerDimension * 3 / 4;


        QRGEncoder qrgEncoder = new QRGEncoder(email, null, QRGContents.Type.TEXT, smallerDimension);
        try {
            bitmap = qrgEncoder.encodeAsBitmap();
        } catch (WriterException e) {
            Log.i("generate", e.toString());
        }
        save();
    }

    private void save() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child("images").child(email + ".jpg").putBytes(data);
        uploadTask.addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Upload Failed!!", Toast.LENGTH_SHORT).show());
        uploadTask.addOnCompleteListener(task -> login());
    }
}