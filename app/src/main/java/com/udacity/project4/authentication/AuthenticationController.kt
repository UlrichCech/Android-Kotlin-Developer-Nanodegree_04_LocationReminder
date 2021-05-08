package com.udacity.project4.authentication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map

object AuthenticationController {

    val inLogoutState = MutableLiveData<Boolean>()

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }

    val authenticationState = CurrentUserLiveData.map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    fun getCurrentUsername(): String {
        return CurrentUserLiveData.value?.displayName?:""
    }

}