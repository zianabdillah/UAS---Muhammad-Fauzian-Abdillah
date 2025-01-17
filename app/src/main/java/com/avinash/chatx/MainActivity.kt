package com.avinash.chatx

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.avinash.chatx.auth.AuthenticationActivity
import com.avinash.chatx.databinding.ActivityMainBinding
import com.avinash.chatx.util.UserUtil
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*
        * If the user is not logged In, then taking to AuthenticationActivity
        * */
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, AuthenticationActivity::class.java))
        }

        UserUtil.getCurrentUser()

        /*
        * Default Fragment
        * */
        setFragment(FeedFragment())

        /*
        * Bottom Navigation View
        * */
        binding.navigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.feed_item -> {
                    setFragment(FeedFragment())
                }
                R.id.search_item -> {
                    setFragment(SearchFragment())
                }
                R.id.chatroom_item -> {
                    setFragment(ChatroomFragment())
                }
                R.id.profile_item -> {
                    setFragment(ProfileFragment())
                }
            }
            true
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setFragmentWithBackStack(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

}

//Nama-nama Anggota:
//1. Muhtar - 411211083
//2. Simon Andika Simanungkalit - 411211203
//3. Muhammad Fauzian Abdillah - 411211082