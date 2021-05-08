package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "AuthenticationActivity"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthenticationController.authenticationState.observe(this, { authenticationState ->
            when (authenticationState) {
                AuthenticationController.AuthenticationState.AUTHENTICATED -> {
                    if (AuthenticationController.inLogoutState.value == null  || AuthenticationController.inLogoutState.value == false) {
                        Log.i(
                            TAG,
                            "User <${AuthenticationController.getCurrentUsername()}> was successfully logged in."
                        )
                        navigateToRemindersList()
                    } else {
                        AuthenticationController.inLogoutState.postValue(false)
                    }
                }
                AuthenticationController.AuthenticationState.UNAUTHENTICATED -> {
                    Log.i(TAG, "User logged out successfully!")
                }
                else -> Log.e(
                        TAG, "New $authenticationState state that doesn't require any UI change"
                )
            }
        })

        setContentView(R.layout.activity_authentication)
//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

        val loginButton = findViewById<Button>(R.id.login_btn)
        loginButton.setOnClickListener {
            startFirebaseSignInFlow()
        }
    }

    private fun navigateToRemindersList() {
        val intent = Intent(this, RemindersActivity::class.java)
        startActivity(intent)
    }


    private fun startFirebaseSignInFlow() {
        val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                        providers
                ).build(), SIGN_IN_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "Successfully signed in user " + "${FirebaseAuth.getInstance().currentUser?.displayName}!")
                navigateToRemindersList()
            } else {
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

}
