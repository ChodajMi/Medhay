package com.example.medhay

class ModelPost {
    constructor()

    var description: String? = null

    var pid: String? = null

    constructor(
        description: String?,
        pid: String?,
        ptime: String?,
        pcomments: String?,
        title: String?,
        uimage: String?,
        uemail: String?,
        uid: String?,
        profileImageUrl: String?,
        uname: String?,
        plike: String?
    ) {
        this.description = description
        this.pid = pid
        this.ptime = ptime
        this.pcomments = pcomments
        this.title = title
        this.profileImageUrl = profileImageUrl
        this.uemail = uemail
        this.uid = uid
        this.uimage = uimage
        this.uname = uname
        this.plike = plike
    }

    var ptime: String? = null
    var pcomments: String? = null

    var title: String? = null

    var profileImageUrl: String? = null
    var uemail: String? = null
    var uid: String? = null
    var uimage: String? = null

    var uname: String? = null
    var plike: String? = null
}