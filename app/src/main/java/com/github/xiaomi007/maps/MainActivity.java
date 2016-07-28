package com.github.xiaomi007.maps;

import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {
    private static final String TAG = "MainActivity";
    private GoogleApiClient mClient;
    private List<ClientObserver> observers = new ArrayList<>();

    private ClientObservable clientObservable = new ClientObservable() {
        @Override
        public void addObserver(ClientObserver clientObserver) {
            if (!observers.contains(clientObserver)) {
                observers.add(clientObserver);
            }
        }

        @Override
        public void removeObserver(ClientObserver clientObserver) {
            if (observers.contains(clientObserver)) {
                observers.remove(clientObserver);
            }
        }

        @Override
        public void notifyObserver(GoogleApiClient client) {
            for (ClientObserver observer : observers) {
                observer.updateClient(client);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_main);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, MainFragment.newInstance(), "main").commit();
        mClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mClient.connect();
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, 123);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "onRequestPermissionsResult");

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getSupportFragmentManager().findFragmentByTag("main") != null) {
            clientObservable.addObserver((ClientObserver) getSupportFragmentManager().findFragmentByTag("main"));
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (getSupportFragmentManager().findFragmentByTag("main") != null){
            clientObservable.removeObserver((ClientObserver) getSupportFragmentManager().findFragmentByTag("main"));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mClient != null) {
            mClient.stopAutoManage(this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        clientObservable.notifyObserver(mClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
