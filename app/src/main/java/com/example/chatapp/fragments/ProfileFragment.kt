package com.example.chatapp.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatapp.R
import com.example.chatapp.activities.SplashActivity
import com.example.chatapp.databinding.FragmentProfileBinding
import com.example.chatapp.models.User
import com.example.chatapp.utils.FirebaseUtil
import com.example.chatapp.utils.FirebaseUtil.logout
import com.google.firebase.messaging.FirebaseMessaging
import de.hdodenhof.circleimageview.CircleImageView


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var profilePic: ImageView

    private lateinit var profileImageView: CircleImageView // Declare this variable at the top
    private var imageUri: Uri? = null
    private val GALLERY_REQUEST_CODE = 1001



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

        binding.updateProfileButton.setOnClickListener{
            showUpdateUserDialog()

        }



    }

    private fun showUpdateUserDialog() {
        // Inflate the dialog view
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_update_user, null)

        // Initialize dialog views
        val usernameEditText = dialogView.findViewById<EditText>(R.id.editTextUsername)
        val aboutEditText = dialogView.findViewById<EditText>(R.id.editTextAbout)
        // Initialize dialog views
        profileImageView = dialogView.findViewById(R.id.profile_image)
        val selectImageLayout = dialogView.findViewById<RelativeLayout>(R.id.selectImage)

        // Set up image click listener to open gallery
        selectImageLayout.setOnClickListener {
            openGallery() // This will launch the gallery to select the image
        }

        // Show the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Update Profile")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val username = usernameEditText.text.toString()
                val about = aboutEditText.text.toString()

                // When "Update" is clicked, handle image upload (if selected) and Firestore update
                if (imageUri != null) {
                    uploadImageToFirebaseStorage(username, about) // Handle the upload and Firestore update
                } else {
                    // If no image is selected, update only username and about
                    updateUserInFirestore(username, null, about)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            imageUri = data?.data // Store the selected image URI locally

            // Show the selected image in the ImageView in the dialog
            if (imageUri != null) {
                Glide.with(this)
                    .load(imageUri)
                    .circleCrop() // Optional: Apply circle crop if you want a circular image
                    .into(profileImageView) // profileImageView should be the ImageView inside the dialog
            }
        }
    }



    private fun uploadImageToFirebaseStorage(username: String, about: String) {
        val storageRef = FirebaseUtil.getCurrentProfilePicStorageRef()

        // Upload the image
        imageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val profilePictureUrl = downloadUri.toString()
                        // Now update Firestore with the image URL
                        updateUserInFirestore(username, profilePictureUrl, about)
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateUserInFirestore(username: String?, profilePictureUrl: String?, about: String?) {
        val userId = FirebaseUtil.currentUserId()

        if (userId != null) {
            val updatedFields = mutableMapOf<String, Any>()

            // Only update fields that have values
            if (!username.isNullOrEmpty()) {
                updatedFields["username"] = username
            }
            if (!about.isNullOrEmpty()) {
                updatedFields["about"] = about
            }
            if (!profilePictureUrl.isNullOrEmpty()) {
                updatedFields["profilePictureUrl"] = profilePictureUrl
            }

            // Update Firestore with the new values
            if (updatedFields.isNotEmpty()) {
                FirebaseUtil.currentUserDetails().update(updatedFields)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        getUserData()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), "Failed to update profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "No fields to update", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // show user data
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

    override fun onResume() {
        super.onResume()
        // Refresh user data when the fragment resumes
        getUserData()
    }



}