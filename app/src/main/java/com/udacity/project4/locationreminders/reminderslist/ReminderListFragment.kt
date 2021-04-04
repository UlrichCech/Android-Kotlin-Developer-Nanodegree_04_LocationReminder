package com.udacity.project4.locationreminders.reminderslist

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResultListener
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {


    companion object {
        const val TAG = "ReminderListFragment"
        const val REQUEST_FINE_LOCATION_AND_BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 11
        const val REQUEST_FINE_LOCATION_ONLY_PERMISSIONS_REQUEST_CODE = 12
        const val LOCATION_PERMISSION_INDEX = 0
        const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
        const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
        const val ACTION_GEOFENCE_EVENT =
            "RemindersListViewModel.remindersLocation.action.ACTION_GEOFENCE_EVENT"
        const val GEOFENCE_RADIUS_IN_METERS = 100f
    }


    private lateinit var geofencingClient: GeofencingClient
    private lateinit var binding: FragmentRemindersBinding


    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()


    private val geofencePendingIntent: PendingIntent by lazy {  // <-- this property is initialized upon first access online
        val intent = Intent(requireContext().applicationContext, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener("KEY-NEW_REMINDER") { requestKey, bundle ->
            val result = bundle.getString("newReminderId")
            Log.i(TAG, result?:"")
            // Do something with the result
        }
        _viewModel.remindersList.observe(requireActivity(), {
            createGeofencesForReminders(it)
        })
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        binding.viewModel = _viewModel
//        binding.viewModel!!.deleteAllReminders()
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))
        binding.refreshLayout.setOnRefreshListener {
            binding.viewModel!!.loadReminders()
            binding.refreshLayout.isRefreshing = false
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        binding.viewModel!!.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }
        // setup the recycler view using the extension function
        binding.remindersRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                AuthUI.getInstance().signOut(requireContext())
                requireActivity().finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }


    fun createGeofencesForReminders(reminderList: List<ReminderDataItem>) {
        if (! binding.viewModel!!.isReminderListEmpty()) {
            for (reminder in reminderList) {
                createGeofence(reminder)
            }
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            geofencingClient.removeGeofences(geofencePendingIntent).run {
                addOnCompleteListener {
                    geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent)
                        .run {
                            addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(), R.string.geofences_added,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            addOnFailureListener {
                                Log.e(TAG, it.message!!)
                                Toast.makeText(
                                    requireContext(), R.string.geofences_not_added,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }

        }
    }

    fun createGeofence(reminder: ReminderDataItem) {
        if (! geofenceAlreadyExist(reminder)) {
            binding.viewModel!!.geofenceList.add(
                Geofence.Builder()
                    .setRequestId(reminder.id)
                    .setCircularRegion(
                        reminder.latitude!!,
                        reminder.longitude!!,
                        GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()
            )
        }
    }

    private fun geofenceAlreadyExist(reminder: ReminderDataItem): Boolean {
        for (gf in binding.viewModel!!.geofenceList) {
            if (gf.requestId.equals(reminder.id)) {
                return true
            }
        }
        return false
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(binding.viewModel!!.geofenceList)
        }.build()
    }

}
