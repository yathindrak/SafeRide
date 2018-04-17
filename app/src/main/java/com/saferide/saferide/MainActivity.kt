package com.saferide.saferide

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private var mMap: GoogleMap? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLastLocation: Location? = null
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mUserRef: DatabaseReference

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mToolbar: Toolbar

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                Log.i("MapsActivity", "Location: " + location.latitude + " " + location.longitude)
                mLastLocation = location
                /*if (currentLocationMarker != null) {
                    currentLocationMarker.remove();
                }
                // Get the id of the user which currently logged in
                String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                // make a db ref with  DriverAvailable
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriverAvailable");

                // Save the current location using GeoFire
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(user_id, new GeoLocation(location.getLatitude(), location.getLongitude()));



                //Place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Position");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                currentLocationMarker = mMap.addMarker(markerOptions);*/

                //move map camera
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
        }

    }


    private var mUserMarker: Marker? = null
    private var userLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Adding the action bar
        mToolbar = findViewById(R.id.main_page_toolbar) as Toolbar
        //signoutBtn = findViewById(R.id.signoutBtn)
        setSupportActionBar(mToolbar)
        getSupportActionBar()!!.setTitle("YK Sample Chat")

        // Initialize Firebase auth
        mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser != null) {
            mUserRef = FirebaseDatabase.getInstance().reference.child("Users").child(mAuth.currentUser!!.uid)
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        /*signoutBtn.setOnClickListener {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP)

            FirebaseAuth.getInstance().signOut()
            sendToStart()
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.map_main_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        if (item.itemId == R.id.main_logout_btn) {

            mUserRef.child("online").setValue(ServerValue.TIMESTAMP)

            FirebaseAuth.getInstance().signOut()
            sendToStart()

        }

        /*if (item.itemId == R.id.main_settings_btn) {

            val settingsIntent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(settingsIntent)

        }

        if (item.itemId == R.id.main_all_btn) {

            val settingsIntent = Intent(this@MainActivity, UsersActivity::class.java)
            startActivity(settingsIntent)

        }*/

        return true
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        getUserCurrentLocation()
        // Check permissions
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //this method should call before setMyLocationEnabled(true)
            buildGoogleApiClient()
            mMap!!.isMyLocationEnabled = true
        }
    }

    protected fun buildGoogleApiClient() {
        //create a GoogleApiClient and connect it
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        mGoogleApiClient!!.connect()
    }

    override fun onLocationChanged(location: Location) {

    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }

    @SuppressLint("RestrictedApi")
    override fun onConnected(bundle: Bundle?) {
        //request location in evey 1000ms
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 1000
        Toast.makeText(this, "dfnafs", Toast.LENGTH_SHORT)

        mLocationRequest.fastestInterval = 1000
        //this can be caused to use battery because its high priority
        // can set to PRIORITY_BALANCED_POWER_ACCURACY instead
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        /*if(ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Commenting because deprecated
            //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            // Instead adding below lines
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);
        }
*/
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
    }

    // Get their current updated location
    private fun getUserCurrentLocation() {
        val userLocationRef = FirebaseDatabase.getInstance().reference
                .child("Users").child(mAuth.currentUser!!.uid).child("l")

        userLocationRef.addValueEventListener(object : ValueEventListener {

            //A DataSnapshot instance contains data from a Firebase Database location.
            // Any time you read Database data, you receive the data as a DataSnapshot

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Below line is adding to prevent crashing the app
                if (dataSnapshot.exists()) {
                    val map = dataSnapshot.value as List<Any>?
                    var locationLat = 0.0
                    var locationLng = 0.0


                    if (map!![0] != null) {
                        locationLat = java.lang.Double.parseDouble(map[0].toString())
                    }
                    if (map[0] != null) {
                        locationLng = java.lang.Double.parseDouble(map[1].toString())
                    }

                    userLatLng = LatLng(locationLat, locationLng)

                    // To avoid a lot of markers on the map
                    if (mUserMarker != null) {
                        mUserMarker!!.remove()
                    }

                    mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))

                    mUserMarker = mMap!!.addMarker(MarkerOptions().position(userLatLng!!).title("You"))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }


    override fun onConnectionSuspended(i: Int) {

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onMyLocationClick(location: Location) {

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null)
        val currentUser = mAuth.currentUser

        if (currentUser == null) {
            sendToStart()
        } else {
            mUserRef.child("online").setValue("true")
        }
    }

    // Send to start if not logged in successfully
    private fun sendToStart() {
        val startIntent = Intent(this@MainActivity, StartActivity::class.java)
        startActivity(startIntent)
        // When user go there we dont need to cme him back to this page. So finish that
        finish()
    }

    override fun onStop() {
        super.onStop()

        val currentUser = mAuth.currentUser

        if (currentUser != null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP)
        }
    }
}

