package com.munger.tpfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class DashboardActivity extends AppCompatActivity {

    TextView nameTextView;
    ImageView codeQRImageView;
    FirebaseAuth firebaseAuth;
    FirebaseStorage storage;
    StorageReference storageRef;
    String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        nameTextView = findViewById(R.id.nameTextView);
        codeQRImageView = findViewById(R.id.codeQRImageView);

        firebaseAuth = FirebaseAuth.getInstance();

        getName();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://tpfinal-1c0f6.appspot.com/images").child(email + ".jpg");

        try {
            getQR();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void getName() {
        email = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail();
        nameTextView.setText("Ceci est le code QR de " + email);
    }

    private void getQR() throws IOException {
        File localFile = File.createTempFile("images", "jpg");
        storageRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            codeQRImageView.setImageBitmap(bitmap);

        }).addOnFailureListener(exception -> System.out.println("failure"));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        firebaseAuth.signOut();
    }
}