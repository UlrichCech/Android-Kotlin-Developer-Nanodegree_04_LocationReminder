package com.udacity.project4.authentication

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.LiveData

object CurrentUserLiveData : LiveData<FirebaseUser?>() {
    private val firebaseAuthenticator = FirebaseAuth.getInstance()


    private val authenticationStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        value = firebaseAuth.currentUser
    }

    override fun onActive() {
        firebaseAuthenticator.addAuthStateListener(authenticationStateListener)
    }

    override fun onInactive() {
        firebaseAuthenticator.removeAuthStateListener(authenticationStateListener)
    }
}