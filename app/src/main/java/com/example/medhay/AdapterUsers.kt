package com.example.medhay

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView


class AdapterUsers(var context: Context, var list: List<ModelUsers>) :
    RecyclerView.Adapter<AdapterUsers.MyHolder>() {
    var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    var uid: String? = firebaseAuth.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_users, parent, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val hisuid = list[position].uid
        val userImage = list[position].profileImageUrl
        val username = list[position].name
        val usermail = list[position].email
        holder.name.text = username
        holder.email.text = usermail

        //Load the profile image using glide
        try {
            Glide.with(context).load(userImage).placeholder(R.drawable.profile_image).into(holder.profiletv)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profiletv: CircleImageView = itemView.findViewById(R.id.imagep)
        var name: TextView = itemView.findViewById(R.id.namep)
        var email: TextView = itemView.findViewById(R.id.emailp)
    }
}