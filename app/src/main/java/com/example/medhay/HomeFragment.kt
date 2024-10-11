package com.example.medhay

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medhay.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth // FirebaseAuth instance
    private lateinit var adapterPosts: AdapterPosts // Adapter for the RecyclerView
    private var posts: MutableList<ModelPost> = mutableListOf() // List to store posts
    private var _binding: FragmentHomeBinding? = null // ViewBinding for fragment layout
    private val binding get() = _binding!! // Safe access to binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false) // Initialize ViewBinding
        firebaseAuth = FirebaseAuth.getInstance() // Initialize FirebaseAuth

        // Set up RecyclerView with LinearLayoutManager
        binding.postRecyclerview.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity).apply {
                reverseLayout = true // Reverse layout to show newest posts first
                stackFromEnd = true // Stack from end
            }
        }

        loadPosts() // Call function to load posts from Firebase

        return binding.root // Return the root view of the binding
    }

    // Load all posts from Firebase
    private fun loadPosts() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Posts")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Ensure the fragment is still attached before updating UI
                if (isAdded) {
                    posts.clear() // Clear previous posts
                    for (snapshot in dataSnapshot.children) {
                        val modelPost = snapshot.getValue(ModelPost::class.java)
                        if (modelPost != null) {
                            posts.add(modelPost) // Add non-null posts to the list
                        }
                    }
                    activity?.let { // Safe activity access
                        adapterPosts = AdapterPosts(it, posts) // Set adapter with posts
                        binding.postRecyclerview.adapter = adapterPosts // Bind adapter to RecyclerView
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(activity, databaseError.message, Toast.LENGTH_LONG).show() // Error handling
                }
            }
        })
    }

    // Search for posts by title or description
    private fun searchPosts(query: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Posts")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Ensure the fragment is still attached before updating UI
                if (isAdded) {
                    posts.clear() // Clear previous search results
                    for (snapshot in dataSnapshot.children) {
                        val modelPost = snapshot.getValue(ModelPost::class.java)
                        if (modelPost != null &&
                            (modelPost.title?.lowercase(Locale.getDefault())?.contains(query.lowercase(Locale.getDefault())) == true ||
                                    modelPost.description?.lowercase(Locale.getDefault())?.contains(query.lowercase(Locale.getDefault())) == true)) {
                            posts.add(modelPost) // Add matching posts to the list
                        }
                    }
                    activity?.let { // Safe activity access
                        adapterPosts = AdapterPosts(it, posts) // Set adapter with search results
                        binding.postRecyclerview.adapter = adapterPosts // Bind adapter to RecyclerView
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(activity, databaseError.message, Toast.LENGTH_LONG).show() // Error handling
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Enable options menu
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu) // Inflate the menu
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView // Get the SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!TextUtils.isEmpty(query)) {
                    searchPosts(query!!) // Search posts if query is not empty
                } else {
                    loadPosts() // Reload all posts if query is empty
                }
                return true // Query handled
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!TextUtils.isEmpty(newText)) {
                    searchPosts(newText!!) // Search posts as text changes
                } else {
                    loadPosts() // Reload all posts if text is cleared
                }
                return true // Query handled
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            firebaseAuth.signOut() // Sign out from Firebase
            startActivity(Intent(context, SplashScreen::class.java)) // Redirect to SplashScreen
            requireActivity().finish() // Close the current activity
            return true // Event handled
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks by clearing binding
    }
}
