package com.esaip.arbresremarquables;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.squareup.picasso.Picasso;

public class LaunchActivity extends AppCompatActivity {

    private boolean isTimerDone;
    private boolean isPermissionGiven;

    public LaunchActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        //Setup de l'image
        ImageView imageView = findViewById(R.id.splash);
        imageView.setDrawingCacheEnabled(true);
        Picasso.get().load(R.drawable.splash_min).fit().noFade().into(imageView);

        // Permission
        if (ActivityCompat.checkSelfPermission(LaunchActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LaunchActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        } else {
            setPermissionGiven(true);
        }

        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(3000); //Display for 3 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    setTimerDone(true);
                }
            }
        };
        timer.start();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setPermissionGiven(true);
            } else {
                Toast.makeText(this, "L'application ne peut pas fonctionner sans la location.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    public void setPermissionGiven(boolean permissionGiven) {
        isPermissionGiven = permissionGiven;
        startMapsActity();
    }

    public void setTimerDone(boolean timerDone) {
        isTimerDone = timerDone;
        startMapsActity();
    }

    public void startMapsActity() {
        if (isPermissionGiven && isTimerDone) {
            Intent i = new Intent(LaunchActivity.this, MapsActivity.class);
            startActivity(i);
            finish();
        }
    }
}