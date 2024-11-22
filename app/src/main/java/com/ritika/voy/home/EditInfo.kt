package com.ritika.voy.home

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.ritika.voy.R
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.UpdateUserRequest
import com.ritika.voy.databinding.FragmentEditInfoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class EditInfo : Fragment() {
    private var _binding: FragmentEditInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private var selectedGender: String? = null
    private var imageUri: Uri? = null

    companion object {
        const val PICK_IMAGE_REQUEST = 1
        const val CAPTURE_IMAGE_REQUEST = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEditInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_editInfo_to_profile)
        }

        // Handle "Set Profile" button click
        binding.setProfile.setOnClickListener {
            pickImageFromGallery()
        }

        // Handle "Camera" button click
        binding.takeImageUsingCamera.setOnClickListener {
            captureImageWithCamera()

        }

        binding.nameLayout.setOnClickListener {
            if (binding.editGenderPopup.visibility == View.VISIBLE) {
                binding.editGenderPopup.visibility = View.GONE
            }
            slideInPopup(binding.editNamePopup)
        }
        binding.genderLayout.setOnClickListener {
            if (binding.editNamePopup.visibility == View.VISIBLE) {
                binding.editNamePopup.visibility = View.GONE
            }
            slideInPopup(binding.editGenderPopup)
        }

        binding.editNameSaveChanges.setOnClickListener {
            binding.editNamePopup.visibility = View.GONE
            val firstName: Editable = binding.firstName.text
            val lastName: Editable = binding.lastName.text
            Log.d("popup", "onViewCreated: $firstName $lastName")
            binding.name.text = "$firstName $lastName"
        }

        val maleCheckBox = binding.male
        val femaleCheckBox = binding.female
        val otherCheckBox = binding.other

        setExclusiveSelection(maleCheckBox, "Male", femaleCheckBox, otherCheckBox)
        setExclusiveSelection(femaleCheckBox, "Female", maleCheckBox, otherCheckBox)
        setExclusiveSelection(otherCheckBox, "Other", maleCheckBox, femaleCheckBox)

        val saveButton = binding.editGenderSaveChanges
        saveButton.setOnClickListener {
            if (selectedGender != null) {
                binding.editGenderPopup.visibility = View.GONE
                Toast.makeText(
                    requireContext(), "Selected Gender: $selectedGender", Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(requireContext(), "No gender selected!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUser(context: Context, filePath: String, token: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {

                    val userUpdateRequest = UpdateUserRequest(
                        profilePhoto = createMultipartBody(filePath, "profile_photo"),
                        firstName = "Ritika",
                        lastName = "Tiwari",
                        gender = "Female",
                        emergencyContactPhone = "8756256565"
                    )
                    Log.d(
                        "profileApi",
                        "profile photo: ${userUpdateRequest.profilePhoto} ,token $token"
                    )

                    RetrofitInstance.api.updateUserData(
                        token = "Bearer $token", userUpdateRequest
                    )
                }

                if (response.success) {
                    val userData = response.data
                    Toast.makeText(
                        context,
                        "User updated successfully: ${userData.first_name}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Error updating user: ${response.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Exception: ${e.message}", Toast.LENGTH_LONG).show()

            }
        }
    }

    private fun createMultipartBody(filePath: String, key: String): MultipartBody.Part {
        val file = File("path_to_your_file")
        val requestBody =
            file.asRequestBody("application/octet-stream".toMediaTypeOrNull()) // Or any appropriate MIME type
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        return part
    }


    // Handle gallery selection
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Handle camera capture
    private fun captureImageWithCamera() {
        val file = File(
            requireContext().getExternalFilesDir(null),
            "profile_image_${System.currentTimeMillis()}.jpg"
        )
        imageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        val bitmap = MediaStore.Images.Media.getBitmap(
                            requireContext().contentResolver,
                            selectedImageUri
                        )
                        val resizedBitmap =
                            resizeBitmap(bitmap, 200, 200)
                        binding.setProfile.setImageBitmap(resizedBitmap)
                        val filePath = getFilePathFromUri(selectedImageUri)
                        Log.d("profileApi", "File path: $filePath")
                        lifecycleScope.launch {
                            val accessToken =
                                DataStoreManager.getToken(requireContext(), "access").first()
                            updateUser(requireContext(), filePath.toString(), accessToken!!)
                        }
                    }
                }

                CAPTURE_IMAGE_REQUEST -> {
                    if (imageUri != null) {
                        val bitmap = MediaStore.Images.Media.getBitmap(
                            requireContext().contentResolver,
                            imageUri
                        )
                        val resizedBitmap =
                            resizeBitmap(bitmap, 200, 200)
                        binding.setProfile.setImageBitmap(resizedBitmap)
                        val filePath = imageUri?.path
                        Log.d("profileApi", "file path is $filePath")
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "Action canceled!", Toast.LENGTH_SHORT).show()
        }
    }

    fun resizeBitmap(bitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun getFilePathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireContext().contentResolver.query(uri, projection, null, null, null)
        cursor?.let {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            val path = it.getString(columnIndex)
            it.close()
            return path
        }
        return null
    }


    fun setExclusiveSelection(
        selectedCheckBox: CheckBox,
        gender: String,
        vararg otherCheckBoxes: CheckBox,
    ) {
        selectedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                otherCheckBoxes.forEach { it.isChecked = false }
                selectedGender = gender
            } else if (!otherCheckBoxes.any { it.isChecked }) {
                selectedGender = null
            }
        }
    }

    fun slideInPopup(editPopupLayout: View) {
        if (editPopupLayout.visibility != View.VISIBLE) {
            editPopupLayout.visibility = View.VISIBLE
            val animator = ObjectAnimator.ofFloat(editPopupLayout, "translationY", 1000f, 0f)
            animator.duration = 300
            animator.start()
        }
        binding.root.setOnTouchListener { _, event ->
            outsideClickEvent(editPopupLayout, event)
        }
    }

    fun outsideClickEvent(editPopupLayout: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && editPopupLayout.visibility == View.VISIBLE) {
            val outRect = Rect()
            editPopupLayout.getGlobalVisibleRect(outRect)
            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                val animator = ObjectAnimator.ofFloat(editPopupLayout, "translationY", 0f, 1000f)
                animator.duration = 300
                animator.start()
                animator.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationEnd(animation: Animator) {
                        editPopupLayout.visibility = View.GONE
                    }

                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                })
            }
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
