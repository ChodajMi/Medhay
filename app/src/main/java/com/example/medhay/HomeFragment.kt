package com.example.medhay

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medhay.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth // Use lateinit for better safety
    private var posts: MutableList<ModelPost> = mutableListOf() // Remove nullable type
    private lateinit var adapterPosts: AdapterPosts // Use lateinit for better safety
    private var _binding: FragmentHomeBinding? = null // Declare ViewBinding variable
    private val binding get() = _binding!! // Get the binding reference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using ViewBinding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()

        // Setup RecyclerView with ViewBinding
        binding.postRecyclerview.setHasFixedSize(true)
        binding.postRecyclerview.layoutManager = LinearLayoutManager(activity).apply {
            reverseLayout = true
            stackFromEnd = true
        }

        loadPosts() // Load posts
        return binding.root // Return the root view of the binding
    }

    private fun loadPosts() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Posts")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                posts.clear() // Clear the existing posts
                for (dataSnapshot1 in dataSnapshot.children) {
                    val modelPost = dataSnapshot1.getValue(ModelPost::class.java)
                    if (modelPost != null) {
                        posts.add(modelPost) // Only add non-null posts
                    }
                }
                adapterPosts = AdapterPosts(requireActivity(), posts) // Use requireActivity()
                binding.postRecyclerview.adapter = adapterPosts // Set adapter using ViewBinding
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(activity, databaseError.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    // Search post code
    private fun searchPosts(search: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Posts")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                posts.clear() // Clear the existing posts
                for (dataSnapshot1 in dataSnapshot.children) {
                    val modelPost = dataSnapshot1.getValue(ModelPost::class.java)
                    if (modelPost != null &&
                        (modelPost.title?.lowercase(Locale.getDefault())?.contains(search.lowercase(Locale.getDefault())) == true ||
                                modelPost.description?.lowercase(Locale.getDefault())?.contains(search.lowercase(Locale.getDefault())) == true)) {
                        posts.add(modelPost) // Only add matching posts
                    }
                }
                adapterPosts = AdapterPosts(requireActivity(), posts) // Use requireActivity()
                binding.postRecyclerview.adapter = adapterPosts // Set adapter using ViewBinding
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(activity, databaseError.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true) // Enable options menu
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView // Directly get actionView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!TextUtils.isEmpty(query)) {
                    searchPosts(query!!)
                } else {
                    loadPosts()
                }
                return true // Changed to true to indicate query has been handled
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!TextUtils.isEmpty(newText)) {
                    searchPosts(newText!!)
                } else {
                    loadPosts()
                }
                return true // Changed to true to indicate query has been handled
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    // Logout functionality
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            firebaseAuth.signOut()
            startActivity(Intent(context, SplashScreen::class.java))
            requireActivity().finish()
            return true // Indicate that the event was handled
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks by releasing the binding reference
    }
}
