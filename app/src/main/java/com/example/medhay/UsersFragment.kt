package com.example.medhay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


/**
 * A simple [Fragment] subclass.
 */
class UsersFragment : Fragment() {
    var recyclerView: RecyclerView? = null
    var adapterUsers: AdapterUsers? = null
    var usersList: MutableList<ModelUsers?>? = null
    var firebaseAuth: FirebaseAuth? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_users, container, false)
        recyclerView = view.findViewById(R.id.recyclep)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.setLayoutManager(LinearLayoutManager(activity))

        usersList = ArrayList()
        firebaseAuth = FirebaseAuth.getInstance()
        allUsers
        return view
    }

    private val allUsers: Unit
        get() {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val reference = FirebaseDatabase.getInstance().getReference("Users")
            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    usersList!!.clear()
                    for (dataSnapshot1 in dataSnapshot.children) {
                        val modelUsers = dataSnapshot1.getValue(ModelUsers::class.java)
                        if (modelUsers!!.uid != null && modelUsers.uid != firebaseUser!!.uid) {
                            usersList!!.add(modelUsers)
                        }
                        adapterUsers = AdapterUsers(activity!!, usersList as List<ModelUsers>)
                        recyclerView!!.adapter = adapterUsers
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }
}