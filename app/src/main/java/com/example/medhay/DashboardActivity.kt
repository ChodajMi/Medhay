package com.example.medhay

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.NonNull
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class DashboardActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var myuid: String
    private lateinit var actionBar: ActionBar
    private lateinit var navigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        actionBar = supportActionBar!!
        actionBar.title = "Profile Activity"
        firebaseAuth = FirebaseAuth.getInstance()

        navigationView = findViewById(R.id.navigation)
        navigationView.setOnNavigationItemSelectedListener(selectedListener)
        actionBar.title = "Home"

        // When we open the application first time the fragment should be shown to the user
        // in this case it is home fragment
        val fragment = HomeFragment()
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.content, fragment, "")
        fragmentTransaction.commit()
    }

    private val selectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
        when (menuItem.itemId) {
            R.id.nav_home -> {
                actionBar.title = "Home"
                val fragment = HomeFragment()
                val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.content, fragment, "")
                fragmentTransaction.commit()
                true
            }
            R.id.nav_profile -> {
                actionBar.title = "Profile"
                val fragment1 = ProfileFragment()
                val fragmentTransaction1: FragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction1.replace(R.id.content, fragment1)
                fragmentTransaction1.commit()
                true
            }
            R.id.nav_users -> {
                actionBar.title = "Users"
                val fragment2 = UsersFragment()
                val fragmentTransaction2: FragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction2.replace(R.id.content, fragment2, "")
                fragmentTransaction2.commit()
                true
            }
            R.id.nav_addblogs -> {
                actionBar.title = "Add Blogs"
                val fragment4 = AddBlogsFragment()
                val fragmentTransaction4: FragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction4.replace(R.id.content, fragment4, "")
                fragmentTransaction4.commit()
                true
            }
            else -> false
        }
    }
}
