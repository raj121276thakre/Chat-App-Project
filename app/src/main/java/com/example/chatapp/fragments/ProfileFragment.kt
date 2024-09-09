package com.example.chatapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatapp.activities.SplashActivity
import com.example.chatapp.databinding.FragmentProfileBinding
import com.example.chatapp.models.User
import com.example.chatapp.utils.FirebaseUtil
import com.example.chatapp.utils.FirebaseUtil.logout
import com.google.firebase.messaging.FirebaseMessaging


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var profilePic : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)


        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profilePic = binding.profileImage
        getUserData();


        binding.logoutBtn.setOnClickListener {
            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    logout()
                    val intent = Intent(
                        context,
                        SplashActivity::class.java
                    )
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
            }

        }

    }


    private fun getUserData() {
        setInProgress(true)

//        FirebaseUtil.getCurrentProfilePicStorageRef().downloadUrl
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val uri = task.result
//                   // Utils.setProfilePic(requireContext(), uri, profilePic)
//                }
//            }

        FirebaseUtil.currentUserDetails().get()
            .addOnCompleteListener { task ->
                setInProgress(false)
                if (task.isSuccessful) {
                    val currentUserModel = task.result?.toObject(User::class.java)
                    binding.profileName.setText(currentUserModel?.username)
                    binding.profilePhone.setText(currentUserModel?.phone)
                    binding.profileAbout.setText(currentUserModel?.about)

                    Glide.with(requireContext())
                        .load(currentUserModel?.profilePictureUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(profilePic)
                }
            }
    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.progressBar.visibility = View.VISIBLE
            binding.updateProfileButton.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.updateProfileButton.visibility = View.VISIBLE
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }


}