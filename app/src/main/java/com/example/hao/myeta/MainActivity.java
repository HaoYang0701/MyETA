package com.example.hao.myeta;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements
    NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {
  @BindView(R.id.fab_start_session) FloatingActionButton startSession;
  @BindView(R.id.fab_join_session) FloatingActionButton joinSession;
  @BindView(R.id.fab_end_session) FloatingActionButton endSession;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.shareButton) ImageButton imageButton;
  @BindView(R.id.drawer_layout) DrawerLayout drawer;
  @BindView(R.id.nav_view) NavigationView navigationView;
  @BindView(R.id.menu_green) FloatingActionMenu floatingActionMenu;

  private ArrayList<Marker> listOfMarkers = new ArrayList();
  private DialogManager dialogManager = new DialogManager();
  private String sessionId;
  private Dialog startdialog;
  private Dialog joindialog;
  private Dialog enddialog;
  private Location currentUserLocation;
  private Session endLocation = null;
  private ArrayList<Session> listOfSession = new ArrayList<>();
  private ValueEventListener queryListener;
  private Handler locationHandler;

  private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATIONS = 1;
  private static GoogleMap googleMap;
  private static DatabaseReference mfirebaseDatabase;
  private static String databaseid = "databaseid";
  private static String databasepassword = "databasepassword";
  private static String isSessionStarted = "isSessionStarted";
  public static String username = "username";
  private static SessionAdapter sessionadapter;
  private static SharedPreferences prefs;
  private static LocationAlarmManager locationAlarmManager = new LocationAlarmManager();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    setSupportActionBar(toolbar);

    endSession.setVisibility(View.GONE);
    prefs = this.getSharedPreferences("com.example.hao.myeta", Context.MODE_PRIVATE);

    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);

    initAutoComplete();

    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    toggle.syncState();

    navigationView.setNavigationItemSelectedListener(this);

    Boolean isPreviouslyStoredSession = prefs.getBoolean(isSessionStarted, false);
    if (isPreviouslyStoredSession){
      setUpQuerycallBacks();
    }
  }

  public void bindAdapterToRecycler(){
    RecyclerView rvSession = (RecyclerView) findViewById(R.id.rvSessions);
    sessionadapter = new SessionAdapter(this, listOfSession);
    rvSession.setAdapter(sessionadapter);
    rvSession.setLayoutManager(new LinearLayoutManager(this));
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    this.googleMap = googleMap;
    if (PermissionUtil.isLocationPermissionsOn(this)) {
      setUpLocationCallback();
    }else{
      PermissionUtil.checkLocationPermissions(this);
    }
  }

  @Override
  public void onBackPressed() {
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.nav_camera) {
    } else if (id == R.id.nav_gallery) {

    } else if (id == R.id.nav_slideshow) {

    } else if (id == R.id.nav_manage) {

    } else if (id == R.id.nav_share) {

    } else if (id == R.id.nav_send) {

    }
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         String permissions[], int[] grantResults) {
    switch (requestCode) {
      case MY_PERMISSIONS_REQUEST_FINE_LOCATIONS: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          setUpLocationCallback();
        } else {
        }
        return;
      }
      default: {
        return;
      }
    }
  }

  private void setCurrentLocationMarker(double latitude, double longitude) {
    final LatLng currentLocation = new LatLng(latitude, longitude);
    locationHandler = new Handler(Looper.getMainLooper());
    Runnable task = new Runnable() {
      @Override
      public void run() {
        googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
      }
    };
    locationHandler.post(task);
  }

  private void initAutoComplete() {
    PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
        this.getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
    autocompleteFragment.setHint(getString(R.string.destination_location));
    autocompleteFragmentListener(autocompleteFragment);
  }

  private void setUpLocationCallback() {
    LocationUtils.LocationResult locationResult = new LocationUtils.LocationResult() {
      @Override
      public void gotLocation(Location location) {
        setCurrentLocationMarker(location.getLatitude(), location.getLongitude());
        currentUserLocation = location;
      }
    };
    LocationUtils myLocation = new LocationUtils();
    myLocation.getLocation(this, locationResult);
  }

  private void autocompleteFragmentListener(PlaceAutocompleteFragment autocompleteFragment) {
    autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
      @Override
      public void onPlaceSelected(Place place) {
        if (googleMap != null){
          if(mfirebaseDatabase == null){
            mfirebaseDatabase = FirebaseDatabase.getInstance().getReference();
          }
          String user = "Destination";
          endLocation = new Session();
          endLocation.setUser(user);

          com.example.hao.myeta.Location location = new com.example.hao.myeta.Location();

          location.setLatitude(place.getLatLng().latitude);
          location.setLongitude(place.getLatLng().longitude);
          endLocation.setLocation(location);
        }
      }

      @Override
      public void onError(Status status) {
      }
    });
  }

  private void dialogStartSessionListener(final Dialog dialog, Button dialogButton) {
    dialogButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(mfirebaseDatabase == null){
          mfirebaseDatabase = FirebaseDatabase.getInstance().getReference();
        }
        EditText editText = (EditText) startdialog.findViewById(R.id.session_edit_Text);
        String user = editText.getText().toString();
        sessionId = mfirebaseDatabase.push().getKey();

        //add user location
        Session session = new Session();
        session.setUser(user);
        com.example.hao.myeta.Location location = new com.example.hao.myeta.Location();

        if (currentUserLocation != null){
          location.setLatitude(currentUserLocation.getLatitude());
          location.setLongitude(currentUserLocation.getLongitude());
        }
        session.setLocation(location);

        mfirebaseDatabase.child(getString(R.string.session) + sessionId)
            .child(sessionId)
            .setValue(session);

        prefs.edit().putString(databaseid, sessionId).apply();
        prefs.edit().putBoolean(isSessionStarted, true).apply();
        prefs.edit().putString(username, user).apply();

        //add in destination Location
        String destinationSessionId = mfirebaseDatabase.push().getKey();
        mfirebaseDatabase.child(getString(R.string.session) + sessionId)
            .child(destinationSessionId)
            .setValue(endLocation);

        setUpQuerycallBacks();
        locationAlarmManager.scheduleAlarm(getApplication(), sessionId, sessionId);

        bindAdapterToRecycler();
        dialog.dismiss();
        //hideFloatingActionButton();
      }
    });
  }

  private void dialogEndSessionListener(final Dialog enddialog, Button confirmdialogButton) {
    confirmdialogButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        prefs.edit().remove(databaseid).apply();
        prefs.edit().remove(databasepassword).apply();
        prefs.edit().putBoolean(isSessionStarted, false).apply();
        if (queryListener != null){
          mfirebaseDatabase.removeEventListener(queryListener);
        }
        startSession.setVisibility(View.VISIBLE);
        joinSession.setVisibility(View.VISIBLE);
        endSession.setVisibility(View.GONE);
        locationAlarmManager.cancelAlarm(getApplication());
        enddialog.dismiss();
      }
    });
  }

  private void dialogJoinSessionListener(final Dialog dialog, Button dialogButton) {
    dialogButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mfirebaseDatabase = FirebaseDatabase.getInstance().getReference();
        String newSessionId = ((EditText)
            joindialog.findViewById(R.id.join_sessionID_editText)).getText().toString();
        String newSessionPassword = ((EditText)
            joindialog.findViewById(R.id.join_sessionPassword_editText)).getText().toString();
        String newSessionName = ((EditText)
            joindialog.findViewById(R.id.join_sessionUserName_editText)).getText().toString();

        prefs.edit().putString(databaseid, sessionId).apply();
        prefs.edit().putString(databasepassword, newSessionPassword).apply();
        prefs.edit().putString(username, newSessionName).apply();
        Session newSession = new Session();
        newSession.setUser(newSessionName);
        com.example.hao.myeta.Location location = new com.example.hao.myeta.Location();

        if (currentUserLocation != null){
          location.setLatitude(currentUserLocation.getLatitude());
          location.setLongitude(currentUserLocation.getLongitude());
        }
        newSession.setLocation(location);

        String newUserKey = mfirebaseDatabase.push().getKey();
        mfirebaseDatabase.child(getString(R.string.session) +
            sessionId).child(newUserKey).setValue(newSession);

        setUpQuerycallBacks();

        prefs.edit().putBoolean(isSessionStarted, true).apply();
        locationAlarmManager.scheduleAlarm(getApplication(), sessionId, newUserKey);
        dialog.dismiss();
        //hideFloatingActionButton();
      }
    });
  }

  private void hideFloatingActionButton() {
    startSession.setVisibility(View.GONE);
    joinSession.setVisibility(View.GONE);
    endSession.setVisibility(View.VISIBLE);
  }

  private void setUpQuerycallBacks() {
    String storedSessionId = prefs.getString(databaseid, getString(R.string.nullValue));
    if (storedSessionId.equals(getString(R.string.nullValue))){
      return;
    }
    if(mfirebaseDatabase == null){
      mfirebaseDatabase = FirebaseDatabase.getInstance().getReference();
    }
    Query matchingSession = mfirebaseDatabase.child(getString(R.string.session) +
        storedSessionId);

    queryListener = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        Iterable<DataSnapshot>  allUsers = dataSnapshot.getChildren();
        Iterator userIterator = allUsers.iterator();
        googleMap.clear();
        listOfMarkers.clear();
        listOfSession.clear();

        while(userIterator.hasNext()){
          DataSnapshot user = (DataSnapshot) userIterator.next();
          Session savedSession =  user.getValue(Session.class);
          listOfSession.add(savedSession);
          MapsUtil.createMarkers(googleMap, listOfMarkers, savedSession);
        }
        if(listOfMarkers.size() > 1){
          MapsUtil.correctZoom(googleMap, listOfMarkers);
        }
        if (sessionadapter != null){
          sessionadapter.notifyDataSetChanged();
        }
      }
      @Override
      public void onCancelled(DatabaseError databaseError) {
      }
    };
    matchingSession.addValueEventListener(queryListener);
  }

  @OnClick(R.id.shareButton)
  void shareSessionId() {
    Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.setType("text/plain");
    sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_session_playstore));
    startActivity(sendIntent);
  }

  @OnClick(R.id.fab_start_session)
  void clickStartSession() {
    floatingActionMenu.close(true);
    startdialog = new Dialog(this);
    if (endLocation == null){
      startdialog.setContentView(R.layout.non_selected_location_dialog);
      Button dialogButton = (Button) startdialog.findViewById(R.id.non_selected_location_button);
      dialogButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          startdialog.dismiss();
        }
      });
      startdialog.show();
      return;
    }
    startdialog.setContentView(R.layout.start_dialog);
    dialogManager.expandWindow(startdialog);
    Button dialogButton = (Button) startdialog.findViewById(R.id.start_dialog_confirm_button);
    dialogStartSessionListener(startdialog, dialogButton);
    startdialog.show();
  }

  @OnClick(R.id.fab_join_session)
  void clickJoinSession() {
    floatingActionMenu.close(true);
    joindialog = new Dialog(this);
    joindialog.setContentView(R.layout.join_dialog);
    dialogManager.expandWindow(joindialog);
    Button dialogButton = (Button) joindialog.findViewById(R.id.join_dialog_confirm_button);
    dialogJoinSessionListener(joindialog, dialogButton);
    joindialog.show();
  }

  @OnClick(R.id.fab_end_session)
  void clickEndSession() {
    floatingActionMenu.close(true);
    enddialog = new Dialog(this);
    enddialog.setContentView(R.layout.end_dialog);
    dialogManager.expandWindow(enddialog);
    Button confirmdialogButton = (Button) enddialog.findViewById(R.id.end_dialog_confirm_button);
    dialogEndSessionListener(enddialog, confirmdialogButton);
    Button canceldialogButton = (Button) enddialog.findViewById(R.id.end_dialog_cancel_button);
    canceldialogButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        enddialog.dismiss();
      }
    });
    enddialog.show();
  }
}