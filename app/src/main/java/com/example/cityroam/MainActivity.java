package com.example.cityroam;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int ROAM_LOCATION_PERMISSION_CODE = 42;

    // Map view
    private MapView roamMap;

    // Single reusable marker — no duplicates
    private Marker currentPositionPin;

    // (UPGRADE 1) Breadcrumb trail drawn behind the explorer
    private Polyline trail;

    // (UPGRADE 2) Accuracy circle that reflects real GPS precision
    private Polygon accuracyCircle;

    // (UPGRADE 3) Distance tracking
    private GeoPoint lastPosition = null;
    private float totalDistance   = 0f;

    // Location service
    private LocationManager locationManager;

    // -------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        roamMap = findViewById(R.id.explorerMapView);
        roamMap.setTileSource(TileSourceFactory.MAPNIK);
        roamMap.setMultiTouchControls(true);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Initial spot — Marrakesh
        GeoPoint initialSpot = new GeoPoint(31.6258, -7.9892);
        roamMap.getController().setZoom(15.0);
        roamMap.getController().setCenter(initialSpot);

        // ── UPGRADE 1 : Breadcrumb trail ─────────────────────────────────────
        trail = new Polyline();
        trail.setColor(Color.parseColor("#FF6B35")); // explorer orange
        trail.setWidth(8f);
        roamMap.getOverlays().add(trail);

        // ── UPGRADE 2 : Accuracy circle ──────────────────────────────────────
        accuracyCircle = new Polygon();
        accuracyCircle.setPoints(Polygon.pointsAsCircle(initialSpot, 50)); // placeholder 50m radius
        accuracyCircle.getFillPaint().setColor(Color.argb(40, 100, 149, 237));   // soft blue fill
        accuracyCircle.getOutlinePaint().setColor(Color.argb(180, 100, 149, 237));
        accuracyCircle.getOutlinePaint().setStrokeWidth(4f);
        roamMap.getOverlays().add(accuracyCircle);

        // Position marker (added last so it renders on top)
        currentPositionPin = new Marker(roamMap);
        currentPositionPin.setPosition(initialSpot);
        currentPositionPin.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        currentPositionPin.setTitle("📍 Starting Point — Marrakesh");
        roamMap.getOverlays().add(currentPositionPin);

        Toast.makeText(this, "Map Ready — Exploring!", Toast.LENGTH_SHORT).show();

        // Permission check
        boolean permissionGranted =
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        if (permissionGranted) {
            startTrackingLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    ROAM_LOCATION_PERMISSION_CODE);
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    // -------------------------------------------------------------------------
    private void startTrackingLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,  // 1 second
                1,     // 1 metre — detects even small movement
                new LocationListener() {

                    @Override
                    public void onLocationChanged(Location location) {
                        double latitude  = location.getLatitude();
                        double longitude = location.getLongitude();

                        GeoPoint freshPosition = new GeoPoint(latitude, longitude);

                        // ── UPGRADE 2 : Update accuracy circle ───────────────
                        accuracyCircle.setPoints(
                                Polygon.pointsAsCircle(freshPosition, location.getAccuracy())
                        );

                        // ── UPGRADE 3 : Distance counter ─────────────────────
                        if (lastPosition != null) {
                            float[] result = new float[1];
                            Location.distanceBetween(
                                    lastPosition.getLatitude(), lastPosition.getLongitude(),
                                    latitude, longitude,
                                    result
                            );
                            totalDistance += result[0];
                        }
                        lastPosition = freshPosition;

                        // ── UPGRADE 1 : Extend breadcrumb trail ──────────────
                        trail.addPoint(freshPosition);

                        // Move marker (no clear — trail & circle stay)
                        currentPositionPin.setPosition(freshPosition);
                        currentPositionPin.setTitle(
                                String.format("🧭 %.0fm explored", totalDistance)
                        );

                        // ── UPGRADE 4 : Rotate marker to face direction ───────
                        if (location.hasBearing()) {
                            currentPositionPin.setRotation(-location.getBearing());
                        }

                        // Smooth camera follow
                        roamMap.getController().setZoom(15.0);
                        roamMap.getController().animateTo(freshPosition);

                        roamMap.invalidate();
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}

                    @Override
                    public void onProviderEnabled(String provider) {
                        Toast.makeText(getApplicationContext(),
                                "✅ Provider enabled: " + provider,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        buildAlertMessageNoGps();
                    }
                }
        );
    }

    // -------------------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ROAM_LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTrackingLocation();
            } else {
                Toast.makeText(this,
                        "Location permission denied — GPS tracking disabled.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // -------------------------------------------------------------------------
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder gpsWarning = new AlertDialog.Builder(this);

        gpsWarning
                .setTitle("🧭 Navigation Unavailable")
                .setMessage("Your GPS is currently off. Enable it to explore your surroundings!")
                .setCancelable(false)
                .setPositiveButton("Take me there", (dialog, id) -> {
                    startActivity(new Intent(
                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    ));
                })
                .setNegativeButton("Not now", (dialog, id) -> dialog.cancel());

        gpsWarning.create().show();
    }

    // -------------------------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();
        roamMap.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        roamMap.onPause();
    }
}