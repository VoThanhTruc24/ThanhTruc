package com.example.uthsmarttasks

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.uthsmarttasks.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        if (user != null) {
            // Gán thông tin user lên giao diện
            binding.txtName.text = user.displayName ?: "No Name"
            binding.txtEmail.text = user.email ?: "No Email"

            // Hiển thị ảnh đại diện nếu có
            val photoUrl = user.photoUrl
            if (photoUrl != null) {
                Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .into(binding.imgProfile)
            } else {
                binding.imgProfile.setImageResource(R.drawable.avatar_placeholder)
            }
        }

        // Nút đăng xuất
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
