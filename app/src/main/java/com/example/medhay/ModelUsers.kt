package com.example.medhay


class ModelUsers {
    var name: String? = null

    constructor()


    constructor(
        name: String?,
        onlineStatus: String?,
        email: String?,
        profileImageUrl: String?,
        uid: String?
    ) {
        this.name = name
        this.email = email
        this.profileImageUrl = profileImageUrl
        this.uid = uid
    }

    var email: String? = null

    var profileImageUrl: String? = null

    var uid: String? = null
}