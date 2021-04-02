package com.udacity.project4.locationreminders

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import kotlinx.android.synthetic.main.activity_reminders.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private val androidRuntimeQorLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    private lateinit var remindersActivityLayout: ConstraintLayout;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)
        remindersActivityLayout = findViewById(R.id.remindersActivityLayout)
        if (checkLocationPermissionsGranted()) {
            checkDeviceLocationSetting()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    @TargetApi(29)
    private fun checkLocationPermissionsGranted(): Boolean {
        val fineLocationGranted = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        ))
        val backgroundLocationGranted =
                if (androidRuntimeQorLater) {
                    PackageManager.PERMISSION_GRANTED ==
                            ActivityCompat.checkSelfPermission(
                                    this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            )
                } else {
                    true
                }
        return fineLocationGranted && backgroundLocationGranted
    }

    @TargetApi(29 )
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (checkLocationPermissionsGranted())
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            androidRuntimeQorLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ReminderListFragment.REQUEST_FINE_LOCATION_AND_BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
            }
            else -> ReminderListFragment.REQUEST_FINE_LOCATION_ONLY_PERMISSIONS_REQUEST_CODE
        }
        Log.d(ReminderListFragment.TAG, "Request only FINE_LOCATION permission")
        ActivityCompat.requestPermissions(this, permissionsArray, resultCode)
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d(ReminderListFragment.TAG, "onRequestPermissionResult")
        if (
                grantResults.isEmpty() ||
                grantResults[ReminderListFragment.LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
                (requestCode == ReminderListFragment.REQUEST_FINE_LOCATION_AND_BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE &&
                        grantResults[ReminderListFragment.BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                        PackageManager.PERMISSION_DENIED)) {
            Snackbar
                    .make(remindersActivityLayout, R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE)
//                .setAction(R.string.settings) {
//                    startActivity(Intent().apply {
//                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
//                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    })
//                }
                    .show()
//        } else {
//            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    private fun checkDeviceLocationSetting(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
                settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(this,
                            ReminderListFragment.REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(ReminderListFragment.TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(remindersActivityLayout,
                        R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSetting()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                addGeofenceForReminders()
            }
        }
    }

    private fun addGeofenceForReminders() {
        // TODO: implement this
    }

}
