package in.arjsna.audiorecorder.activities;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import in.arjsna.audiorecorder.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

  /**
   * Map is initialized when it's fully loaded and ready to be used.
   *
   * @see #onMapReady(com.google.android.gms.maps.GoogleMap)
   */
  private GoogleMap mMap;

  public void onCreate(Bundle savedState) {
    super.onCreate(savedState);
    setContentView(R.layout.activity_maps);
    // Obtain the MapFragment and set the async listener to be notified when the map is ready.
    MapFragment mapFragment =
            (MapFragment) getFragmentManager().findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
  }


  @Override
  public void onMapReady(GoogleMap googleMap) {
    // Map is ready to be used.
    CameraUpdate center=
            CameraUpdateFactory.newLatLng(new LatLng(25.700712, -100.325079));
    CameraUpdate zoom= CameraUpdateFactory.zoomTo(10);

    googleMap.getUiSettings().setZoomControlsEnabled(true);
    googleMap.moveCamera(center);
    googleMap.animateCamera(zoom);
    googleMap.addMarker(new MarkerOptions()
            .position(new LatLng(25.700712, -100.325079))
            .title("Marker"));
  }
}
