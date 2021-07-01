package com.yener.happypoint;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yener.happypoint.entity.Star;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by erhan on 14.1.2018.
 */

public class Tab3Fragment extends Fragment implements OnMapReadyCallback {
    private static final float METERS_100 = 100;
    //Google Map
    GoogleMap mGoogleMap;

    //User Location
    LocationManager locationManager;
    LocationListener locationListener;
    LatLng userLastLocation;

    //Firebase
    DatabaseReference dbRefStar;
    List<Star> starLocationList;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab3, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        return view;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        //Map style
        try {
            boolean success = mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.map_style));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        //FireBase read marker location and create marker
        fireBaseDateRead();

        //Kullanıcının Lokasyonu
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                Toast.makeText(getActivity(), "1", Toast.LENGTH_LONG).show();
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16));
                changeMyLocation(location);
            }


            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //Kullanıcı lokasyon için izin vermediyse
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            //2 saniyede bir ve 2 metrede bir update eder...
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2, 2, locationListener);
            //Kullanıcının son lokasyonu ilk acıldıgında oraya odaklar..
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                userLastLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                Toast.makeText(getActivity(), "2", Toast.LENGTH_LONG).show();
                changeMyLocation(lastLocation);
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLastLocation, 16));
            }
        }

        //Kişinin Local konumu
        mGoogleMap.setMyLocationEnabled(true);

    }

    //Kullanıcı izin verirse
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2, 2, locationListener);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Firebase data read
     */
    public void fireBaseDateRead() {
        starLocationList = new ArrayList<>();
        dbRefStar = FirebaseDatabase.getInstance().getReference("star");
        dbRefStar.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Star starLocation = postSnapShot.getValue(Star.class);
                    starLocationList.add(starLocation);
                }
                createMarker();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Veri okuma hatası", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Markers
     */
    public void createMarker() {
        if (starLocationList != null) {
            for (Star star : starLocationList) {
                LatLng location = new LatLng(star.getLatitude(), star.getLongitude());
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(star.getStarName())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        }
    }

    public void changeMyLocation(Location location) {
        Location target = new Location("star");
        for (Star star : starLocationList) {
            target.setLatitude(star.getLatitude());
            target.setLongitude(star.getLongitude());
            if (location.distanceTo(target) < METERS_100) {
                Toast.makeText(getActivity(), "Star Yakalandı:" + star.getStarName() + "Mesafe:" + location.distanceTo(target), Toast.LENGTH_LONG).show();
            }
        }
    }
}
