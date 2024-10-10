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
        udp: String?,
        uemail: String?,
        uid: String?,
        uimage: String?,
        uname: String?,
        plike: String?
    ) {
        this.description = description
        this.pid = pid
        this.ptime = ptime
        this.pcomments = pcomments
        this.title = title
        this.udp = udp
        this.uemail = uemail
        this.uid = uid
        this.uimage = uimage
        this.uname = uname
        this.plike = plike
    }

    var ptime: String? = null
    var pcomments: String? = null

    var title: String? = null

    var udp: String? = null
    var uemail: String? = null
    var uid: String? = null
    var uimage: String? = null

    var uname: String? = null
    var plike: String? = null
}