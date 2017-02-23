package com.example.hao.myeta;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
  @BindView(R.id.placesLinearLayout) LinearLayout linear;
  @BindView(R.id.rv_linear_layout) LinearLayout rvlinear;

  private ArrayList<Marker> listOfMarkers = new ArrayList();
  private String sessionId;
  private Dialog startdialog;
  private Dialog joindialog;
  private Dialog enddialog;
  private Marker currentMarker;
  private LatLng currentLocation;
  private Location currentUserLocation;
  private Session endLocation = null;
  private ArrayList<Session> listOfSession = new ArrayList<>();
  private ValueEventListener queryListener;
  public boolean isFirstJoin = false;
  private PlaceAutocompleteFragment autocompleteFragment;

  private static SharedPreferences prefs;
  private static DialogManager dialogManager = new DialogManager();
  private static LocationAlarmManager locationAlarmManager = new LocationAlarmManager();
  private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATIONS = 1;
  private static GoogleMap googleMap;
  private static DatabaseReference mfirebaseDatabase;
  public static String databaseid = "databaseid";
  public static String joinedUserSecondaryId = "joinedusersecondaryid";
  private static String isSessionStarted = "isSessionStarted";
  public static String username = "username";
  public static String destinationLat = "destinationLat";
  public static String destinationLong = "destinationLong";
  private static SessionAdapter sessionadapter;
  boolean isPreviouslyStoredSession;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    setSupportActionBar(toolbar);

    prefs = this.getSharedPreferences("com.example.hao.myeta", Context.MODE_PRIVATE);

    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);

    initAutoComplete();

    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    toggle.syncState();

    navigationView.setNavigationItemSelectedListener(this);

    isPreviouslyStoredSession = prefs.getBoolean(isSessionStarted, false);
    if (isPreviouslyStoredSession){
      setUpQuerycallBacks();
      hideFloatingActionButton();
      linear.setVisibility(View.GONE);
      imageButton.setVisibility(View.VISIBLE);
    }
  }

  public void bindAdapterToRecycler(ArrayList<Session> list){
    RecyclerView rvSession = (RecyclerView) findViewById(R.id.rvSessions);
    rvlinear.setVisibility(View.VISIBLE);
    sessionadapter = new SessionAdapter(this, list);
    rvSession.setAdapter(sessionadapter);
    rvSession.setLayoutManager(new LinearLayoutManager(this));
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    this.googleMap = googleMap;
    if (PermissionUtil.isLocationPermissionsOn(this)) {
      setUpLocationCallback();
    }else if (!PermissionUtil.isLocationPermissionsOn(this)){
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
      drawer.openDrawer(GravityCompat.START);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt("isRecyclerVisible", rvlinear.getVisibility());
    outState.putParcelableArrayList("savedSessionList", listOfSession);
    outState.putParcelable("currentLocationMarker", currentLocation);
  }

  @SuppressWarnings("WrongConstant")
  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    if (savedInstanceState != null){
      rvlinear.setVisibility(savedInstanceState.getInt("isRecyclerVisible"));
      listOfSession = savedInstanceState.getParcelableArrayList("savedSessionList");
      bindAdapterToRecycler(listOfSession);
      currentLocation = savedInstanceState.getParcelable("currentLocationMarker");
      if (currentLocation != null){
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mfirebaseDatabase.removeEventListener(queryListener);
    queryListener = null;
  }

  @Override
  public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
    return super.onCreateView(parent, name, context, attrs);
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.nav_update) {
    } else if (id == R.id.nav_turn_off) {

    } else if (id == R.id.nav_color) {
    }else if (id == R.id.nav_share) {

    } else if (id == R.id.nav_playstore) {
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

  private synchronized void setCurrentLocationMarker(double latitude, double longitude) {
    currentLocation = new LatLng(latitude, longitude);
    Handler locationHandler = new Handler(Looper.getMainLooper());
    Runnable task = new Runnable() {
      @Override
      public void run() {
        if (currentMarker != null){
          currentMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions().position(currentLocation).title("Current Location");
        currentMarker = googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
      }
    };
    locationHandler.post(task);
  }

  private void initAutoComplete() {
    autocompleteFragment = (PlaceAutocompleteFragment)
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
        prefs.edit().putLong(destinationLat,
            Double.doubleToRawLongBits(endLocation.getLocation().getLatitude())).apply();
        prefs.edit().putLong(destinationLong,
            Double.doubleToRawLongBits(endLocation.getLocation().getLongitude())).apply();


        //add in destination Location
        String destinationSessionId = mfirebaseDatabase.push().getKey();
        mfirebaseDatabase.child(getString(R.string.session) + sessionId)
            .child(destinationSessionId)
            .setValue(endLocation);

        setUpQuerycallBacks();

        addStartDestinationSteps(currentUserLocation, endLocation.getLocation());
        locationAlarmManager.scheduleAlarm(getApplication(), sessionId, sessionId);

        bindAdapterToRecycler(listOfSession);
        imageButton.setVisibility(View.VISIBLE);
        if (autocompleteFragment != null){
          linear.setVisibility(View.GONE);
        }
        dialog.dismiss();
        hideFloatingActionButton();
      }
    });
  }

  private void addStartDestinationSteps(Location currentUserLocation, com.example.hao.myeta.Location location) {
    GoogleDirection.withServerKey("AIzaSyDaMLLRbHXqa1UB7U_dLXYnr6DuvTvaQYk")
        .from(new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude()))
        .to(new LatLng(location.getLatitude(), location.getLongitude()))
        .avoid(AvoidType.FERRIES)
        .execute(new DirectionCallback() {
          @Override
          public void onDirectionSuccess(Direction direction, String rawBody) {
            if(direction.isOK()) {
              Map<String, Object> childUpdates = new HashMap<>();

              //Firebase needs a custom object with empty constructor
              TripInfo tripInfo = new TripInfo();
              ArrayList<CustomLatLng> customDirectionList = new ArrayList<>();
              Leg directionLeg = direction.getRouteList().get(0).getLegList().get(0);
              String distance = directionLeg.getDistance().getText();
              String duration = directionLeg.getDuration().getText();
              String startAddress = directionLeg.getStartAddress().toString();
              ArrayList<LatLng> originalDirections = directionLeg.getDirectionPoint();
              int directionSize = directionLeg.getDirectionPoint().size();
              if ( directionSize > 0){
                for (LatLng L : originalDirections){
                  customDirectionList.add(new CustomLatLng(L.latitude, L.longitude));
                }
              }
              tripInfo.setCustomDirectionList(customDirectionList);
              tripInfo.setDistance(distance);
              tripInfo.setDuration(duration);
              tripInfo.setStartAddress(startAddress);
              childUpdates.put("/tripInfo/", tripInfo);

              //if User clicked join session initially
              if (isFirstJoin) {
                String tempSessionId = prefs.getString(joinedUserSecondaryId, getString(R.string.nullValue));
                String parentSessionId = prefs.getString(databaseid, null);
                mfirebaseDatabase.child(getString(R.string.session) + parentSessionId)
                    .child(tempSessionId).updateChildren(childUpdates);
                isFirstJoin = false;
              }else {
                mfirebaseDatabase.child(getString(R.string.session) + sessionId)
                    .child(sessionId).updateChildren(childUpdates);
              }
            } else {
            }
          }

          @Override
          public void onDirectionFailure(Throwable t) {
          }
        });
  }

  private void dialogEndSessionListener(final Dialog enddialog, Button confirmdialogButton) {
    confirmdialogButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (queryListener != null){
          mfirebaseDatabase.removeEventListener(queryListener);
        }
        String tempSessionId = prefs.getString(joinedUserSecondaryId, getString(R.string.nullValue));
        String parentSessionId = prefs.getString(databaseid, null);
        startSession.setVisibility(View.VISIBLE);
        joinSession.setVisibility(View.VISIBLE);
        endSession.setVisibility(View.GONE);
        locationAlarmManager.cancelAlarm(getApplication());

        setUpLocationCallback();
        if (!tempSessionId.equals(getString(R.string.nullValue)) && parentSessionId != null){
          mfirebaseDatabase.child(getString(R.string.session) + parentSessionId)
              .child(tempSessionId).removeValue();
        } else{
          if (parentSessionId != null){
            mfirebaseDatabase.child(getString(R.string.session) + parentSessionId)
                .child(parentSessionId).removeValue();
          }
        }
        prefs.edit().clear().apply();
        listOfSession.clear();
        if (sessionadapter != null){
          sessionadapter.notifyDataSetChanged();
        }
        rvlinear.setVisibility(View.GONE);
        listOfMarkers.clear();
        googleMap.clear();
        linear.setVisibility(View.VISIBLE);
        imageButton.setVisibility(View.GONE);
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
        String newSessionName = ((EditText)
            joindialog.findViewById(R.id.join_sessionUserName_editText)).getText().toString();

        prefs.edit().putString(databaseid, newSessionId).apply();
        prefs.edit().putString(username, newSessionName).apply();
        isFirstJoin = true;

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
            newSessionId).child(newUserKey).setValue(newSession);

        prefs.edit().putString(joinedUserSecondaryId, newUserKey).apply();
        setUpQuerycallBacks();

        prefs.edit().putBoolean(isSessionStarted, true).apply();
        locationAlarmManager.scheduleAlarm(getApplication(), newSessionId, newUserKey);

        bindAdapterToRecycler(listOfSession);
        imageButton.setVisibility(View.VISIBLE);
        if (autocompleteFragment != null){
          linear.setVisibility(View.GONE);
        }
        hideFloatingActionButton();
        dialog.dismiss();
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
        Boolean isPreviouslyStoredSession = prefs.getBoolean(isSessionStarted, false);
        if (!isPreviouslyStoredSession){
          return;
        }
        Iterable<DataSnapshot>  allUsers = dataSnapshot.getChildren();
        Iterator userIterator = allUsers.iterator();
        googleMap.clear();
        listOfMarkers.clear();
        listOfSession.clear();

        while(userIterator.hasNext()){
          DataSnapshot user = (DataSnapshot) userIterator.next();
          Session savedSession =  user.getValue(Session.class);
          if (!savedSession.getUser().equals("Destination")){
            listOfSession.add(savedSession);
          }
          MapsUtil.createMarkers(googleMap, listOfMarkers, savedSession);
          getDirections(savedSession);
          if (isFirstJoin && savedSession.getUser().equals("Destination")){
            prefs.edit().putLong(destinationLat,
                Double.doubleToRawLongBits(savedSession.getLocation().getLatitude())).apply();
            prefs.edit().putLong(destinationLong,
                Double.doubleToRawLongBits(savedSession.getLocation().getLongitude())).apply();

            //if the first call when joined : the user has no idea about destination.
            //we have to add destination from firebase into our app
            addStartDestinationSteps(currentUserLocation,
                new com.example.hao.myeta.Location(savedSession.getLocation().getLatitude(),
                    savedSession.getLocation().getLongitude()));
          }
        }
        if(listOfMarkers.size() > 1){
          MapsUtil.correctZoom(googleMap, listOfMarkers);
        }
        if (sessionadapter != null){
          sessionadapter.notifyDataSetChanged();
        }
        if (isPreviouslyStoredSession && listOfSession.size() > 0){
          bindAdapterToRecycler(listOfSession);
        }
      }
      @Override
      public void onCancelled(DatabaseError databaseError) {
      }
    };
    matchingSession.addValueEventListener(queryListener);
  }

  private void getDirections(Session savedSession) {
    TripInfo tripInfo = savedSession.getTripInfo();
    if (tripInfo == null){
      return;
    }
    ArrayList<CustomLatLng> steplist = tripInfo.getCustomDirectionList();
    if (steplist != null && !steplist.isEmpty()){
      PolylineOptions directionOptions = new PolylineOptions();
      directionOptions.color(Color.RED);
      directionOptions.width(10);
      for (CustomLatLng step : steplist){
        directionOptions.add(new LatLng(step.getLat(), step.getLong()));
      }
      googleMap.addPolyline(directionOptions);
    }
  }

  @OnClick(R.id.shareButton)
  void shareSessionId() {
    String shareableSessionId = prefs.getString(databaseid, getString(R.string.nullValue));
    Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.setType("text/plain");
    sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.share_session), shareableSessionId));
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