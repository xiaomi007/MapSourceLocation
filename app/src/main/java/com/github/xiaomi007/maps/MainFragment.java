package com.github.xiaomi007.maps;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.xiaomi007.maps.databinding.FragmentMainBinding;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

/**
 * Created by xiaomi on 28/07/2016.
 */

public final class MainFragment extends Fragment implements ClientObserver, OnMapReadyCallback {
    private static final String TAG = MainFragment.class.getSimpleName();
    private FragmentMainBinding mBinding;
    private AlarmManager mAlarmManager;
    private GoogleApiClient mClient;
    private GoogleMap mGoogleMap;
    private LocationSource.OnLocationChangedListener mOnLocationChangedListener;
    private LocationSource locationSource = new LocationSource() {
        @Override
        public void activate(OnLocationChangedListener onLocationChangedListener) {
            mOnLocationChangedListener = onLocationChangedListener;
        }

        @Override
        public void deactivate() {
            mOnLocationChangedListener = null;
        }
    };

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                final String action = intent.getAction();
                Log.d(TAG, "onReceive: " + action);
                Location location = new Location("fake");
                Random random = new Random();
                location.setLatitude(random.nextInt() > 0.5 ? -random.nextDouble() * 90 : random.nextDouble() * 90);
                location.setLongitude(random.nextInt() > 0.5 ? -random.nextDouble() * 180 : random.nextDouble() * 90);
                location.setAccuracy(random.nextFloat() * 2000);
                location.setBearing(random.nextFloat() * 360);
                if (mOnLocationChangedListener != null) {
                    mOnLocationChangedListener.onLocationChanged(location);
                }
                if (mGoogleMap != null) {
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15.0f));
                }
            }
        }
    };

    public static MainFragment newInstance() {

        Bundle args = new Bundle();

        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction().replace(R.id.map_fragment, supportMapFragment, "MAP").commit();
        supportMapFragment.getMapAsync(this);

        mAlarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(receiver, new IntentFilter("ACTION_LOC"));
        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 5000, 5000, getPendingIntent());
    }

    private PendingIntent getPendingIntent() {
        return PendingIntent.getBroadcast(getActivity(), 123, new Intent("ACTION_LOC"), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(receiver);
        mAlarmManager.cancel(getPendingIntent());

    }

    @Override
    public void updateClient(GoogleApiClient client) {
        Log.d(TAG, "updateClient: " + client.isConnected());
        mClient = client;
        mapAndClientReady();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        mapAndClientReady();
    }

    private void mapAndClientReady() {
        if (mGoogleMap != null && mClient != null) {
            mGoogleMap.setLocationSource(locationSource);
        }
    }




}
