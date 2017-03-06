package com.example.hao.myeta;

import android.app.Dialog;
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.example.hao.myeta.LocationAlarmManager;
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
import com.stephentuso.welcome.WelcomeHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import java.util.Map;
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import static com.myeta.ShareUtils.createPlaystoreIntent;
import static com.myeta.ShareUtils.createShareSessionIntent;

public class MainActivity extends AppCompatActivity implements
    NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {
  @BindView(R.id.fab_start_session) FloatingActionButton startSessionButton;
  @BindView(R.id.fab_join_session) FloatingActionButton joinSessionButton;
  @BindView(R.id.fab_end_session) FloatingActionButton endSessionButton;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.shareButton) ImageButton shareImageButton;
  @BindView(R.id.drawer_layout) DrawerLayout drawer;
  @BindView(R.id.nav_view) NavigationView navigationView;
  @BindView(R.id.menu_green) FloatingActionMenu floatingActionMenu;
  @BindView(R.id.placesLinearLayout) LinearLayout placesLinearLayout;
  @BindView(R.id.rv_linear_layout) LinearLayout recyclerViewLinearLayout;

  private ArrayList<Marker> listOfMarkers;
  private ArrayList<Session> listOfSession;
  private Dialog startDialog;
  private Dialog joinDialog;
  private Dialog endDialog;
  private Dialog mapUpdateDialog;
  private Dialog randomColorDialog;
  private Marker currentMarker;
  private LatLng currentLocation;
  private Location currentUserLocation;
  private Session endLocation = null;
  private ValueEventListener queryListener;
  private boolean isFirstJoin = false;
  private boolean isPreviouslyStoredSession;
  private WelcomeHelper welcomeScreen;
  private PlaceAutocompleteFragment autocompleteFragment;
  private static SessionAdapter sessionadapter;
  private String sessionId;
  private static GoogleMap googleMap;
  private static SharedPreferences prefs;
  private static DialogManager dialogManager;
  private static LocationAlarmManager locationAlarmManager;
  private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATIONS = 1;
  private static DatabaseReference mfirebaseDatabase;
  public static String DATABASE_ID = "DATABASE_ID";
  private static String IS_RANDOM_COLOR_ENABLED = "IS_RANDOM_COLOR_ENABLED";
  private static String JOINED_USER_SECONDARY_ID = "JOINED_USER_SECONDARY_ID";
  private static String IS_SESSION_STARTED = "IS_SESSION_STARTED";
  public static String USERNAME = "USERNAME";
  public static String DESTINATION_LATITUDE = "DESTINATION_LATITUDE";
  public static String DESTINATION_LONGITUDE = "DESTINATION_LONGITUDE";
  public static String MAP_UPDATE_TIMER = "MAP_UPDATE_TIMER";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    welcomeScreen = new WelcomeHelper(this, MyEtaWelcome.class);
    welcomeScreen.show(savedInstanceState);

    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    setSupportActionBar(toolbar);

    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);

    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    toggle.syncState();

    initializeVariables();

    navigationView.setNavigationItemSelectedListener(this);

    isPreviouslyStoredSession = prefs.getBoolean(IS_SESSION_STARTED, false);
    if (isPreviouslyStoredSession) {
      setUpQuerycallBacks();
      hideFloatingActionButton();
      placesLinearLayout.setVisibility(View.GONE);
      shareImageButton.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    this.googleMap = googleMap;
    if (PermissionUtil.isLocationPermissionsOn(this)) {
      setUpLocationCallback();
    } else if (!PermissionUtil.isLocationPermissionsOn(this)) {
      PermissionUtil.checkLocationPermissions(this);
    }
  }

  public void bindAdapterToRecycler(ArrayList<Session> list) {
    RecyclerView rvSession = (RecyclerView) findViewById(R.id.rvSessions);
    recyclerViewLinearLayout.setVisibility(View.VISIBLE);
    sessionadapter = new SessionAdapter(this, list);
    rvSession.setAdapter(sessionadapter);
    rvSession.setLayoutManager(new LinearLayoutManager(this));
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
    if (item.getItemId() == R.id.action_settings) {
      drawer.openDrawer(GravityCompat.START);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt("isRecyclerVisible", recyclerViewLinearLayout.getVisibility());
    outState.putParcelableArrayList("savedSessionList", listOfSession);
    outState.putParcelable("currentLocationMarker", currentLocation);
  }

  @SuppressWarnings("WrongConstant")
  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    if (savedInstanceState != null) {
      recyclerViewLinearLayout.setVisibility(savedInstanceState.getInt("isRecyclerVisible"));
      listOfSession = savedInstanceState.getParcelableArrayList("savedSessionList");
      bindAdapterToRecycler(listOfSession);
      currentLocation = savedInstanceState.getParcelable("currentLocationMarker");
      if (currentLocation != null) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
      }
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mfirebaseDatabase != null){
      mfirebaseDatabase.removeEventListener(queryListener);
      queryListener = null;
    }
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
      setupUpdateTimeDialog();
    } else if (id == R.id.nav_color) {
      setupRandomColorDialog();
    } else if (id == R.id.nav_permissions) {
      PermissionUtil.checkLocationPermissions(this);
    } else if (id == R.id.nav_share) {
      createShareSessionIntent(MainActivity.this, prefs);
    } else if (id == R.id.nav_playstore) {
      createPlaystoreIntent(MainActivity.this);
    }
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  private void setupRandomColorDialog() {
    randomColorDialog = new Dialog(this);
    randomColorDialog.setContentView(R.layout.random_color_dialog);
    dialogManager.expandWindow(randomColorDialog);
    final Switch colorSwitch = (Switch) randomColorDialog.findViewById(R.id.color_switch);

    colorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        prefs.edit().putBoolean(IS_RANDOM_COLOR_ENABLED, isChecked).apply();
      }
    });

    Button canceldialogButton = (Button) randomColorDialog.findViewById(R.id.color_close);
    canceldialogButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        randomColorDialog.dismiss();
      }
    });
    randomColorDialog.show();
  }

  private void setupUpdateTimeDialog() {
    mapUpdateDialog = new Dialog(this);
    mapUpdateDialog.setContentView(R.layout.update_time_dialog);
    dialogManager.expandWindow(mapUpdateDialog);
    mapUpdateDialog.show();

    Button confirmdialogButton = (Button) mapUpdateDialog.findViewById(R.id.time_update_confirm);
    updateTimeListener(mapUpdateDialog, confirmdialogButton);
    Button canceldialogButton = (Button) mapUpdateDialog.findViewById(R.id.time_update_cancel);
    canceldialogButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mapUpdateDialog.dismiss();
      }
    });
  }

  private void updateTimeListener(final Dialog dialog, Button confirmdialogButton) {
    confirmdialogButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        DiscreteSeekBar slidebar = (DiscreteSeekBar) dialog.findViewById(R.id.update_seek_bar);
        long updateTimer = slidebar.getProgress() * 60 * 1000;
        prefs.edit().putLong(MAP_UPDATE_TIMER, updateTimer).apply();
        dialog.dismiss();
      }
    });
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         String permissions[], int[] grantResults) {
    switch (requestCode) {
      case MY_PERMISSIONS_REQUEST_FINE_LOCATIONS: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          setUpLocationCallback();
        } else {
            CharSequence text = getString(R.string.please_enable_permissions);
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
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
        if (currentMarker != null) {
          currentMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions()
            .position(currentLocation)
            .title(getString(R.string.current_location));
        currentMarker = googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
      }
    };
    locationHandler.post(task);
  }

  private void initializeVariables() {
    prefs = this.getSharedPreferences("com.myeta", Context.MODE_PRIVATE);
    listOfMarkers = new ArrayList();
    listOfSession = new ArrayList();
    dialogManager = new DialogManager();
    locationAlarmManager = new LocationAlarmManager();

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
        if (googleMap != null) {
          if (mfirebaseDatabase == null) {
            mfirebaseDatabase = FirebaseDatabase.getInstance().getReference();
          }
          String user = getString(R.string.Destination);
          endLocation = new Session();
          endLocation.setUser(user);

          com.myeta.Location location = new com.myeta.Location();

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
        if (!PermissionUtil.isLocationPermissionsOn(MainActivity.this)){
          denyStartSessionToast();
          dialog.dismiss();
          return;
        }
        if (mfirebaseDatabase == null) {
          mfirebaseDatabase = FirebaseDatabase.getInstance().getReference();
        }
        EditText editText = (EditText) startDialog.findViewById(R.id.session_edit_Text);
        String user = editText.getText().toString();
        sessionId = mfirebaseDatabase.push().getKey();

        //add user location
        Session session = new Session();
        session.setUser(user);
        com.myeta.Location location = new com.myeta.Location();

        if (currentUserLocation != null) {
          location.setLatitude(currentUserLocation.getLatitude());
          location.setLongitude(currentUserLocation.getLongitude());
        }
        session.setLocation(location);

        mfirebaseDatabase.child(getString(R.string.session) + sessionId)
            .child(sessionId)
            .setValue(session);

        prefs.edit().putString(DATABASE_ID, sessionId).apply();
        prefs.edit().putBoolean(IS_SESSION_STARTED, true).apply();
        prefs.edit().putString(USERNAME, user).apply();
        prefs.edit().putLong(DESTINATION_LATITUDE,
            Double.doubleToRawLongBits(endLocation.getLocation().getLatitude())).apply();
        prefs.edit().putLong(DESTINATION_LONGITUDE,
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
        shareImageButton.setVisibility(View.VISIBLE);
        if (autocompleteFragment != null) {
          placesLinearLayout.setVisibility(View.GONE);
        }
        dialog.dismiss();
        hideFloatingActionButton();
      }
    });
  }

  private void denyStartSessionToast() {
    CharSequence text = getString(R.string.you_cannot_share);
    int duration = Toast.LENGTH_LONG;
    Toast toast = Toast.makeText(MainActivity.this, text, duration);
    toast.show();
  }

  private void addStartDestinationSteps(Location currentUserLocation,
                                        com.myeta.Location location) {
    if (location == null || currentUserLocation == null){
      CharSequence text = getString(R.string.please_check_connection);
      int duration = Toast.LENGTH_LONG;
      Toast toast = Toast.makeText(MainActivity.this, text, duration);
      toast.show();
      return;
    }
    GoogleDirection.withServerKey("AIzaSyDaMLLRbHXqa1UB7U_dLXYnr6DuvTvaQYk")
        .from(new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude()))
        .to(new LatLng(location.getLatitude(), location.getLongitude()))
        .avoid(AvoidType.FERRIES)
        .execute(new DirectionCallback() {
          @Override
          public void onDirectionSuccess(Direction direction, String rawBody) {
            String status = direction.getStatus();
            if (direction.isOK()) {
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

              if (directionSize > 0) {
                for (LatLng latlong : originalDirections) {
                  customDirectionList.add(new CustomLatLng(latlong.latitude, latlong.longitude));
                }
              }
              tripInfo.setCustomDirectionList(customDirectionList);
              tripInfo.setDistance(distance);
              tripInfo.setDuration(duration);
              tripInfo.setStartAddress(startAddress);
              childUpdates.put("/tripInfo/", tripInfo);

              //if User clicked join session initially
              if (isFirstJoin) {
                String tempSessionId = prefs.getString(JOINED_USER_SECONDARY_ID,
                    getString(R.string.nullValue));
                String parentSessionId = prefs.getString(DATABASE_ID, null);
                mfirebaseDatabase.child(getString(R.string.session) + parentSessionId)
                    .child(tempSessionId).updateChildren(childUpdates);
                isFirstJoin = false;
              } else {
                mfirebaseDatabase.child(getString(R.string.session) + sessionId)
                    .child(sessionId).updateChildren(childUpdates);
              }
            } else {
            }
          }

          @Override
          public void onDirectionFailure(Throwable throwable) {
            CharSequence text = getString(R.string.please_check_connection);
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(MainActivity.this, text, duration);
            toast.show();
          }
        });
  }

  private void dialogEndSessionListener(final Dialog enddialog, Button confirmdialogButton) {
    confirmdialogButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        clearAllSettings();
        autocompleteFragment.setHint(getString(R.string.Destination));
        enddialog.dismiss();
      }
    });
  }

  private void clearAllSettings() {
    if (queryListener != null && mfirebaseDatabase != null) {
      mfirebaseDatabase.removeEventListener(queryListener);
    }

    startSessionButton.setVisibility(View.VISIBLE);
    joinSessionButton.setVisibility(View.VISIBLE);
    endSessionButton.setVisibility(View.GONE);
    locationAlarmManager.cancelAlarm(getApplication());
    String tempSessionId = prefs.getString(JOINED_USER_SECONDARY_ID, getString(R.string.nullValue));
    String parentSessionId = prefs.getString(DATABASE_ID, null);

    setUpLocationCallback();
    if (!tempSessionId.equals(getString(R.string.nullValue)) && parentSessionId != null) {
      mfirebaseDatabase.child(getString(R.string.session) + parentSessionId)
          .child(tempSessionId).removeValue();
    } else {
      if (parentSessionId != null) {
        mfirebaseDatabase.child(getString(R.string.session) + parentSessionId)
            .child(parentSessionId).removeValue();
      }
    }
    prefs.edit().clear().apply();
    listOfSession.clear();
    if (sessionadapter != null) {
      sessionadapter.notifyDataSetChanged();
    }
    recyclerViewLinearLayout.setVisibility(View.GONE);
    listOfMarkers.clear();
    googleMap.clear();
    placesLinearLayout.setVisibility(View.VISIBLE);
    shareImageButton.setVisibility(View.GONE);
  }

  private void dialogJoinSessionListener(final Dialog dialog, Button dialogButton) {
    dialogButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (!PermissionUtil.isLocationPermissionsOn(MainActivity.this)){
          denyStartSessionToast();
          dialog.dismiss();
          return;
        }
        mfirebaseDatabase = FirebaseDatabase.getInstance().getReference();
        String newSessionId = ((EditText)
            joinDialog.findViewById(R.id.join_sessionID_editText)).getText().toString();
        String newSessionName = ((EditText)
            joinDialog.findViewById(R.id.join_sessionUserName_editText)).getText().toString();

        prefs.edit().putString(DATABASE_ID, newSessionId).apply();
        prefs.edit().putString(USERNAME, newSessionName).apply();
        isFirstJoin = true;

        Session newSession = new Session();
        newSession.setUser(newSessionName);
        com.myeta.Location location = new com.myeta.Location();

        if (currentUserLocation != null) {
          location.setLatitude(currentUserLocation.getLatitude());
          location.setLongitude(currentUserLocation.getLongitude());
        }
        newSession.setLocation(location);

        String newUserKey = mfirebaseDatabase.push().getKey();
        mfirebaseDatabase.child(getString(R.string.session)
            + newSessionId).child(newUserKey).setValue(newSession);

        prefs.edit().putString(JOINED_USER_SECONDARY_ID, newUserKey).apply();
        setUpQuerycallBacks();

        prefs.edit().putBoolean(IS_SESSION_STARTED, true).apply();
        locationAlarmManager.scheduleAlarm(getApplication(), newSessionId, newUserKey);

        bindAdapterToRecycler(listOfSession);
        shareImageButton.setVisibility(View.VISIBLE);
        if (autocompleteFragment != null) {
          placesLinearLayout.setVisibility(View.GONE);
        }
        hideFloatingActionButton();
        dialog.dismiss();
      }
    });
  }

  private void hideFloatingActionButton() {
    startSessionButton.setVisibility(View.GONE);
    joinSessionButton.setVisibility(View.GONE);
    endSessionButton.setVisibility(View.VISIBLE);
  }

  private void setUpQuerycallBacks() {
    String storedSessionId = prefs.getString(DATABASE_ID, getString(R.string.nullValue));
    final String currentUser = prefs.getString(USERNAME, null);
    if (storedSessionId.equals(getString(R.string.nullValue))) {
      return;
    }
    if (mfirebaseDatabase == null) {
      mfirebaseDatabase = FirebaseDatabase.getInstance().getReference();
    }
    Query matchingSession = mfirebaseDatabase.child(getString(R.string.session)
        + storedSessionId);

    queryListener = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        Boolean isPreviouslyStoredSession = prefs.getBoolean(IS_SESSION_STARTED, false);
        if (!isPreviouslyStoredSession) {
          return;
        }
        googleMap.clear();
        listOfMarkers.clear();
        listOfSession.clear();
        Iterable<DataSnapshot>  allUsers = dataSnapshot.getChildren();
        Iterator userIterator = allUsers.iterator();

        while (userIterator.hasNext()) {
          DataSnapshot user = (DataSnapshot) userIterator.next();
          Session savedSession =  user.getValue(Session.class);
          if (!savedSession.getUser().equals(getString(R.string.Destination))) {
            listOfSession.add(savedSession);
          }

          MapsUtil.createMarkers(googleMap, listOfMarkers, savedSession,
              currentUser.equals(savedSession.getUser()) ? true : false);
          getDirections(savedSession);
          if (isFirstJoin && savedSession.getUser().equals(getString(R.string.Destination))) {
            prefs.edit().putLong(DESTINATION_LATITUDE,
                Double.doubleToRawLongBits(savedSession.getLocation().getLatitude())).apply();
            prefs.edit().putLong(DESTINATION_LONGITUDE,
                Double.doubleToRawLongBits(savedSession.getLocation().getLongitude())).apply();

            //if the first call when joined : the user has no idea about destination.
            //we have to add destination from firebase into our app
            addStartDestinationSteps(currentUserLocation,
                new com.myeta.Location(savedSession.getLocation().getLatitude(),
                    savedSession.getLocation().getLongitude()));
          }
        }
        if (listOfMarkers.size() > 1) {
          MapsUtil.correctZoom(googleMap, listOfMarkers);
        }
        if (sessionadapter != null) {
          sessionadapter.notifyDataSetChanged();
        }
        if (isPreviouslyStoredSession && listOfSession.size() > 0) {
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
    if (tripInfo == null) {
      return;
    }
    ArrayList<CustomLatLng> steplist = tripInfo.getCustomDirectionList();
    if (steplist != null && !steplist.isEmpty()) {
      final boolean useRandomColor = prefs.getBoolean(IS_RANDOM_COLOR_ENABLED, true);
      PolylineOptions directionOptions = new PolylineOptions();
      if (useRandomColor) {
        directionOptions.color(ColorUtil.generateRandomColor());
      } else {
        directionOptions.color(Color.RED);
      }
      directionOptions.width(15);
      for (CustomLatLng step : steplist) {
        directionOptions.add(new LatLng(step.getLat(), step.getLong()));
      }
      googleMap.addPolyline(directionOptions);
    }
  }

  @OnClick(R.id.shareButton)
  void shareSessionId() {
    createShareSessionIntent(MainActivity.this, prefs);
  }


  @OnClick(R.id.fab_start_session)
  void clickStartSession() {
    floatingActionMenu.close(true);
    startDialog = new Dialog(this);
    if (endLocation == null) {
      startDialog.setContentView(R.layout.non_selected_location_dialog);
      Button dialogButton = (Button) startDialog.findViewById(R.id.non_selected_location_button);
      dialogButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          startDialog.dismiss();
        }
      });
      startDialog.show();
      return;
    }
    startDialog.setContentView(R.layout.start_dialog);
    dialogManager.expandWindow(startDialog);
    Button dialogButton = (Button) startDialog.findViewById(R.id.start_dialog_confirm_button);
    dialogStartSessionListener(startDialog, dialogButton);
    startDialog.show();
  }

  @OnClick(R.id.fab_join_session)
  void clickJoinSession() {
    floatingActionMenu.close(true);
    joinDialog = new Dialog(this);
    joinDialog.setContentView(R.layout.join_dialog);
    dialogManager.expandWindow(joinDialog);
    Button dialogButton = (Button) joinDialog.findViewById(R.id.join_dialog_confirm_button);
    dialogJoinSessionListener(joinDialog, dialogButton);
    joinDialog.show();
  }

  @OnClick(R.id.fab_end_session)
  void clickEndSession() {
    floatingActionMenu.close(true);
    endDialog = new Dialog(this);
    endDialog.setContentView(R.layout.end_dialog);
    dialogManager.expandWindow(endDialog);
    Button confirmdialogButton = (Button) endDialog.findViewById(R.id.end_dialog_confirm_button);
    dialogEndSessionListener(endDialog, confirmdialogButton);
    Button canceldialogButton = (Button) endDialog.findViewById(R.id.end_dialog_cancel_button);
    canceldialogButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        endDialog.dismiss();
      }
    });
    endDialog.show();
  }
}