package com.example.kolokvijum2;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    TextView tvLokacija;
    ImageButton imageButton;
    ImageView imageView;
    Switch switchMain;
    Button btnObrisi;

    SensorManager sensorManager;
    Sensor ziroskop;
    Sensor akcelerometar;

    LocationManager locationManager;
    LocationListener locationListener;

    AppDatabase db;
    PostDao postDao;

    boolean switchPrviPut = true;

    static final String CHANNEL_ID = "kanal1";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_LOCATION = 2;
    static final int REQUEST_CONTACTS = 3;
    static final int REQUEST_CAMERA = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLokacija = findViewById(R.id.tvLokacija);
        imageButton = findViewById(R.id.imageButton);
        imageView = findViewById(R.id.imageView);
        switchMain = findViewById(R.id.switchMain);
        btnObrisi = findViewById(R.id.btnObrisi);

        db = AppDatabase.getInstance(this);
        postDao = db.postDao();

        kreirajKanalNotifikacija();
        setupLokacija();
        setupSenzori();
        setupImageButton();
        setupSwitch();
        setupDugme();
    }

    // ===================== LOKACIJA =====================

    void setupLokacija() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                tvLokacija.setText("Lat: " + location.getLatitude()
                        + " | Lon: " + location.getLongitude());
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    // ===================== KAMERA =====================

    void setupImageButton() {
        imageButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            } else {
                otvoriKameru();
            }
        });
    }

    void otvoriKameru() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
            sensorManager.registerListener(this, ziroskop, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    // ===================== SENZORI =====================

    void setupSenzori() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ziroskop = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        akcelerometar = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, akcelerometar, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            Toast.makeText(this, "Žiroskop X:" + x + " Y:" + y + " Z:" + z,
                    Toast.LENGTH_SHORT).show();
            sensorManager.unregisterListener(this, ziroskop);
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            btnObrisi.setText("X:" + x + " Y:" + y + " Z:" + z);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // ===================== SWITCH =====================

    void setupSwitch() {
        switchMain.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (switchPrviPut) {
                    switchPrviPut = false;
                    fetchIUpisiPostove();
                } else {
                    new Thread(() -> {
                        List<Post> posts = postDao.getAll();
                        if (!posts.isEmpty()) {
                            runOnUiThread(() ->
                                    Toast.makeText(this, posts.get(0).title,
                                            Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                }
            } else {
                SharedPreferences prefs = getSharedPreferences("moje_prefs", MODE_PRIVATE);
                prefs.edit().putString("tekst", tvLokacija.getText().toString()).apply();
                dohvatiPrviKontakt();
            }
        });
    }

    void fetchIUpisiPostove() {
        RetrofitClient.getApiService().getPosts().enqueue(new Callback<List<ApiPost>>() {
            @Override
            public void onResponse(Call<List<ApiPost>> call, Response<List<ApiPost>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ApiPost> apiPosts = response.body();
                    new Thread(() -> {
                        int limit = Math.min(10, apiPosts.size());
                        for (int i = 0; i < limit; i++) {
                            ApiPost ap = apiPosts.get(i);
                            Post p = new Post();
                            p.id = ap.getId();
                            p.title = ap.getTitle();
                            p.body = ap.getBody();
                            p.userId = ap.getUserId();
                            postDao.insert(p);
                        }
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<List<ApiPost>> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===================== DUGME OBRIŠI =====================

    void setupDugme() {
        btnObrisi.setOnClickListener(v -> {
            new Thread(() -> {
                int count = postDao.getCount();
                if (count > 0) {
                    postDao.deleteFirst();
                    int newCount = postDao.getCount();
                    if (newCount == 0) {
                        runOnUiThread(() -> posaljiNotifikaciju("Nema više postova!"));
                    }
                }
            }).start();
        });
    }

    // ===================== NOTIFIKACIJA =====================

    void kreirajKanalNotifikacija() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Kanal 1", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    void posaljiNotifikaciju(String tekst) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Info")
                .setContentText(tekst)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }

    // ===================== KONTAKTI =====================

    void dohvatiPrviKontakt() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACTS);
            return;
        }

        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int col = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            if (col >= 0) {
                String ime = cursor.getString(col);
                tvLokacija.setText(ime);
            }
            cursor.close();
        }
    }

    // ===================== PERMISSIONS =====================

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_LOCATION) setupLokacija();
            if (requestCode == REQUEST_CONTACTS) dohvatiPrviKontakt();
            if (requestCode == REQUEST_CAMERA) otvoriKameru();
        }
    }

    // ===================== LIFECYCLE =====================

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, akcelerometar, SensorManager.SENSOR_DELAY_NORMAL);
        setupLokacija();
    }
}
