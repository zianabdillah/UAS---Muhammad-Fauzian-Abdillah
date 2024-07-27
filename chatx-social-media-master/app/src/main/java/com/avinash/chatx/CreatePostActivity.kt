package com.avinash.chatx

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.avinash.chatx.databinding.ActivityCreatePostBinding
import com.avinash.chatx.models.Post
import com.avinash.chatx.models.User
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class CreatePostActivity : AppCompatActivity() {

    companion object {
        const val TAG = "CreatePostActivity"
    }

    private lateinit var binding: ActivityCreatePostBinding
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.postImage.setOnClickListener {
            pickImage()
        }

        binding.btnPost.setOnClickListener {
            val text = binding.postText.text.toString()

            if (TextUtils.isEmpty(text)) {
                Toast.makeText(
                    this,
                    "Description cannot be empty.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (imageUri == null) {
                Toast.makeText(
                    this,
                    "Please select an image.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            Log.d(TAG, "Text: $text, ImageUri: $imageUri")
            addPost(text)
        }
    }

    private fun pickImage() {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start()
    }

    private fun addPost(text: String) {
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_LONG).show()
            return
        }

        Log.d(TAG, "User: ${currentUser.uid}")
        firestore.collection("Users")
            .document(currentUser.uid).get()
            .addOnCompleteListener {
                if (!it.isSuccessful || it.result == null) {
                    Toast.makeText(this, "Failed to get user data!", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                val user = it.result?.toObject(User::class.java)
                if (user == null) {
                    Toast.makeText(this, "User data is null!", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                Log.d(TAG, "User Data: $user")
                Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show()

                val storage = FirebaseStorage.getInstance().reference.child("Images")
                    .child("${currentUser.email}_${System.currentTimeMillis()}.jpg")

                Log.d(TAG, "Starting upload task")
                val uploadTask = storage.putFile(imageUri!!)
                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        Log.d(TAG, "Upload Task Failed: ${task.exception}")
                    }
                    storage.downloadUrl
                }.addOnCompleteListener { urlTaskCompleted ->
                    if (urlTaskCompleted.isSuccessful) {
                        val downloadUri = urlTaskCompleted.result
                        Log.d(TAG, "Download URL: $downloadUri")
                        Toast.makeText(this, "Image uploaded. Creating post...", Toast.LENGTH_SHORT).show()
                        val post = Post(text, downloadUri.toString(), user, System.currentTimeMillis())
                        firestore.collection("Posts")
                            .add(post)
                            .addOnCompleteListener { posted ->
                                if (posted.isSuccessful) {
                                    Log.d(TAG, "Post added successfully")
                                    Toast.makeText(this, "Posted Successfully", Toast.LENGTH_LONG).show()
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                } else {
                                    Log.d(TAG, "Error occurred while adding post: ${posted.exception}")
                                    Toast.makeText(
                                        this,
                                        "Error occurred! Please Try again.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    } else {
                        Log.d(TAG, "URL Task Failed: ${urlTaskCompleted.exception}")
                        Toast.makeText(this, "Failed to get download URL!", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                val fileUri = data?.data
                Log.d(TAG, "Image URI: $fileUri")
                if (fileUri != null) {
                    binding.postImage.setImageURI(fileUri)
                    imageUri = fileUri
                } else {
                    Toast.makeText(
                        this,
                        "Failed to get image.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(
                    this,
                    ImagePicker.getError(data),
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                Toast.makeText(
                    this,
                    "Task Cancelled",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
