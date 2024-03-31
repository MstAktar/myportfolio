package com.example.myportfolio.model

import com.google.firebase.firestore.PropertyName

data class User(val email: String, val password: String)

data class UserData(
    @get:PropertyName("name") @set:PropertyName("name") var name: String? = null,
    @get:PropertyName("email") @set:PropertyName("email") var email: String? = null,
    @get:PropertyName("address") @set:PropertyName("address") var address: String? = null,
    @get:PropertyName("bio") @set:PropertyName("bio") var bio: String? = null
) {
    constructor() : this("", "", "", "")
}

