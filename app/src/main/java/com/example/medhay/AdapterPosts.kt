package com.example.medhay

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import com.bumptech.glide.request.target.Target
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.medhay.databinding.RowPostsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class AdapterPosts(
    private val context: Context,
    private val modelPosts: List<ModelPost>
) : RecyclerView.Adapter<AdapterPosts.MyHolder>() {

    private val myuid: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val likeRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Likes")
    private val postRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Posts")
    private var mProcessLike = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = RowPostsBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyHolder(binding)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val post = modelPosts[position]
        val uid = post.uid ?: ""
        val name = post.uname ?: "Unknown"
        val title = post.title ?: "No Title"
        val description = post.description ?: "No Description"
        val ptime = post.ptime ?: ""
        val dp = post.profileImageUrl ?: ""
        val plike = post.plike ?: "0"
        val image = post.uimage ?: ""
        val comments = post.pcomments ?: "0"
        val pid = post.ptime ?: ""

        // Fetching the profile image from the 'Users' node
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userProfileImage = snapshot.child("profileImageUrl").value as? String
                // Load user profile image using Glide
                Glide.with(context).load(userProfileImage ?: dp)
                    .placeholder(R.drawable.ic_default_img) // Placeholder while loading
                    .error(R.drawable.ic_default_img) // In case of error
                    .into(holder.binding.picturetv)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors if needed
            }
        })
        // Formatting post time to "dd/MM/yyyy hh:mm aa"
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = ptime.toLong()
        val timedate = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString()

        // Bind data to views
        holder.binding.apply {
            unametv.text = name
            ptitletv.text = title
            descript.text = description
            utimetv.text = timedate
            plikeb.text = "$plike Likes"
            pcommentco.text = "$comments Comments"

            setLikes(holder, ptime)

            // Load post image
            // Load post image if available
            if (image.isNotEmpty()) {
                pimagetv.visibility = View.VISIBLE
                Glide.with(context)
                    .load(image)
                    .dontAnimate()  // Disable animations
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) // Retain original size
                    .placeholder(R.drawable.ic_default_img) // Placeholder while loading
                    .error(R.drawable.ic_default_img) // In case of error
                    .into(pimagetv)
            } else {
                pimagetv.visibility = View.GONE
            }


            // Click listeners for Like, Comment, and More options
            plikeb.setOnClickListener {
                val intent = Intent(context, PostLikedByActivity::class.java)
                intent.putExtra("pid", pid)
                context.startActivity(intent)
            }

            like.setOnClickListener {
                handleLikeClick(holder, position)
            }

            morebtn.setOnClickListener {
                showMoreOptions(morebtn, uid, myuid, ptime, image)
            }

            comment.setOnClickListener {
                val intent = Intent(context, PostDetailsActivity::class.java)
                intent.putExtra("pid", ptime)
                context.startActivity(intent)
            }
        }
    }

    private fun handleLikeClick(holder: MyHolder, position: Int) {
        val post = modelPosts[position]
        val postId = post.ptime ?: ""
        val plike = post.plike?.toIntOrNull() ?: 0

        mProcessLike = true
        likeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (mProcessLike) {
                    if (snapshot.child(postId).hasChild(myuid)) {
                        postRef.child(postId).child("plike").setValue((plike - 1).toString())
                        likeRef.child(postId).child(myuid).removeValue()
                    } else {
                        postRef.child(postId).child("plike").setValue((plike + 1).toString())
                        likeRef.child(postId).child(myuid).setValue("Liked")
                    }
                    mProcessLike = false
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showMoreOptions(more: ImageButton, uid: String, myuid: String, pid: String, image: String) {
        val popupMenu = PopupMenu(context, more, Gravity.END)
        if (uid == myuid) {
            popupMenu.menu.add(Menu.NONE, 0, 0, "DELETE")
        }

        popupMenu.setOnMenuItemClickListener { item ->
            if (item.itemId == 0) {
                deletePostWithImage(pid, image)
            }
            false
        }
        popupMenu.show()
    }

    private fun deletePostWithImage(pid: String, image: String) {
        val pd = ProgressBar(context) // ProgressBar instead of ProgressDialog
        pd.visibility = View.VISIBLE // Show ProgressBar

        if (image.isNotEmpty()) {
            val picRef: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(image)
            picRef.delete().addOnSuccessListener {
                deletePostFromDatabase(pid, pd)
            }.addOnFailureListener {
                pd.visibility = View.GONE // Hide ProgressBar
                Toast.makeText(context, "Failed to delete post image", Toast.LENGTH_LONG).show()
            }
        } else {
            deletePostFromDatabase(pid, pd)
        }
    }

    private fun deletePostFromDatabase(pid: String, pd: ProgressBar) {
        postRef.child(pid).removeValue().addOnSuccessListener {
            pd.visibility = View.GONE // Hide ProgressBar
            Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            pd.visibility = View.GONE // Hide ProgressBar
            Toast.makeText(context, "Failed to delete post", Toast.LENGTH_LONG).show()
        }
    }

    private fun setLikes(holder: MyHolder, pid: String) {
        likeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(pid).hasChild(myuid)) {
                    holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0)
                    holder.binding.like.text = "Liked"
                } else {
                    holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unliked, 0, 0, 0)
                    holder.binding.like.text = "Like"
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun getItemCount(): Int = modelPosts.size

    inner class MyHolder(val binding: RowPostsBinding) : RecyclerView.ViewHolder(binding.root)
}
