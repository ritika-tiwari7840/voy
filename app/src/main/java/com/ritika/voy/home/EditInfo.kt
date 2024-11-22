package com.ritika.voy.home

import android.Manifest
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.ritika.voy.R
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.databinding.FragmentEditInfoBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.*
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

class EditInfo : Fragment() {
    private var _binding: FragmentEditInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private var selectedGender: String? = null
    private var imageUri: Uri? = null
    private var uploadJob: Job? = null

    companion object {
        const val PICK_IMAGE_REQUEST = 1
        const val CAPTURE_IMAGE_REQUEST = 2
        const val STORAGE_PERMISSION_CODE = 3
        private const val TAG = "EditInfo"
        private const val MAX_RETRIES = 3
        private const val INITIAL_BACKOFF_MS = 1000L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEditInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        checkAndRequestPermissions()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        with(binding) {
            btnBack.setOnClickListener {
                navController.navigate(R.id.action_editInfo_to_profile)
            }

            setProfile.setOnClickListener {
                pickImageFromGallery()
            }

            takeImageUsingCamera.setOnClickListener {
                captureImageWithCamera()
            }

            nameLayout.setOnClickListener {
                if (editGenderPopup.visibility == View.VISIBLE) {
                    editGenderPopup.visibility = View.GONE
                }
                slideInPopup(editNamePopup)
            }

            genderLayout.setOnClickListener {
                if (editNamePopup.visibility == View.VISIBLE) {
                    editNamePopup.visibility = View.GONE
                }
                slideInPopup(editGenderPopup)
            }

            editNameSaveChanges.setOnClickListener {
                saveNameChanges()
            }
        }

        setupGenderSelection()
    }

    private fun saveNameChanges() {
        binding.apply {
            editNamePopup.visibility = View.GONE
            val firstName: Editable = firstName.text
            val lastName: Editable = lastName.text
            name.text = "$firstName $lastName"
        }
    }

    private fun setupGenderSelection() {
        binding.apply {
            setExclusiveSelection(male, "Male", female, other)
            setExclusiveSelection(female, "Female", male, other)
            setExclusiveSelection(other, "Other", male, female)

            editGenderSaveChanges.setOnClickListener {
                handleGenderSave()
            }
        }
    }

    private fun handleGenderSave() {
        if (selectedGender != null) {
            binding.editGenderPopup.visibility = View.GONE
            showToast("Selected Gender: $selectedGender")
        } else {
            showToast("Please select a gender")
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(
                requireContext(), it
            ) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(), permissionsToRequest.toTypedArray(), STORAGE_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        when (requestCode) {
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    showToast("Storage permissions granted")
                } else {
                    showToast("Storage permissions required to upload images")
                }
            }
        }
    }

    private fun pickImageFromGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
            startActivityForResult(it, PICK_IMAGE_REQUEST)
        }
    }

    private fun captureImageWithCamera() {
        val file = createImageFile()
        imageUri = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.fileprovider", file
        )

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            intent.resolveActivity(requireContext().packageManager)?.let {
                startActivityForResult(intent, CAPTURE_IMAGE_REQUEST)
            }
        }
    }

    private fun createImageFile(): File {
        return File(
            requireContext().getExternalFilesDir(null),
            "profile_image_${System.currentTimeMillis()}.jpg"
        )
    }

    private fun copyFileToAppStorage(sourceUri: Uri): File? = runCatching {
        val destinationFile = createImageFile()
        requireContext().contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destinationFile).use { output ->
                input.copyTo(output)
            }
        }
        destinationFile
    }.getOrNull()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> handleGalleryResult(data)
                CAPTURE_IMAGE_REQUEST -> handleCameraResult()
            }
        } else {
            showToast("Action canceled!")
        }
    }

    private fun handleGalleryResult(data: Intent?) {
        val selectedImageUri = data?.data ?: return
        try {
            copyFileToAppStorage(selectedImageUri)?.let { file ->
                updateImageAndUpload(Uri.fromFile(file), file.absolutePath)
            } ?: showToast("Failed to process image")
        } catch (e: Exception) {
            Log.e(TAG, "Error processing gallery image", e)
            showToast("Error processing image: ${e.message}")
        }
    }

    private fun handleCameraResult() {
        imageUri?.let { uri ->
            try {
                val file = File(uri.path ?: return)
                updateImageAndUpload(uri, file.absolutePath)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing camera image", e)
                showToast("Error processing image: ${e.message}")
            }
        }
    }

    private fun updateImageAndUpload(uri: Uri, filePath: String) {
        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        val resizedBitmap = resizeBitmap(bitmap, 200, 200)
        binding.setProfile.setImageBitmap(resizedBitmap)

        uploadJob?.cancel() // Cancel any existing upload
        uploadJob = lifecycleScope.launch {
            try {
                val accessToken = DataStoreManager.getToken(requireContext(), "access").first()
                if (accessToken != null) {
                    updateUserWithRetry(requireContext(), filePath, accessToken)
                } else {
                    showToast("Access token not found")
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private suspend fun updateUserWithRetry(
        context: Context,
        filePath: String,
        token: String,
        retryCount: Int = 0,
    ) = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath).apply {
                if (!exists()) throw FileNotFoundException("File not found: $filePath")
            }

            val response = RetrofitInstance.api.updateUserData(
                token = "Bearer $token",
                profile_photo = createMultipartBody(file, "profile_photo"),
                firstName = createRequestBody("Ritika"),
                lastName = createRequestBody("Tiwari"),
                gender = createRequestBody("FEMALE"),
                emergencyContactPhone = createRequestBody("9540309305")
            )

            withContext(Dispatchers.Main) {
                if (response.success) {
                    showToast("Profile updated successfully")
                    Log.d(TAG, "Update response: $response")
                } else {
                    showToast("Error updating profile: ${response.message}")
                }
            }
        } catch (e: Exception) {
            handleUploadError(e, context, filePath, token, retryCount)
        }
    }

    private suspend fun handleUploadError(
        error: Exception,
        context: Context,
        filePath: String,
        token: String,
        retryCount: Int,
    ) {
        when (error) {
            is SocketException,
            is SocketTimeoutException,
            is TimeoutException,
            is IOException,
            -> {
                if (retryCount < MAX_RETRIES) {
                    val backoffTime = INITIAL_BACKOFF_MS * (retryCount + 1)
                    delay(backoffTime)
                    updateUserWithRetry(context, filePath, token, retryCount + 1)
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("Failed to upload after $MAX_RETRIES attempts")
                    }
                }
            }

            is HttpException -> {
                withContext(Dispatchers.Main) {
                    showToast("Server error: ${error.code()}")
                }
            }

            else -> {
                withContext(Dispatchers.Main) {
                    showToast("Error: ${error.message}")
                }
            }
        }
    }

    private fun createMultipartBody(file: File, key: String): MultipartBody.Part {
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(key, file.name, requestBody)
    }

    private fun createRequestBody(value: String) =
        value.toRequestBody("text/plain".toMediaTypeOrNull())

    private fun resizeBitmap(bitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap =
        Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

    private fun setExclusiveSelection(
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

    private fun slideInPopup(popupView: View) {
        if (popupView.visibility != View.VISIBLE) {
            popupView.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(popupView, "translationY", 1000f, 0f).apply {
                duration = 300
                start()
            }
        }
        binding.root.setOnTouchListener { _, event ->
            handleOutsideClick(popupView, event)
        }
    }

    private fun handleOutsideClick(popupView: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && popupView.visibility == View.VISIBLE) {
            val outRect = Rect()
            popupView.getGlobalVisibleRect(outRect)
            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                animateAndHidePopup(popupView)
            }
        }
        return false
    }

    private fun animateAndHidePopup(popupView: View) {
        ObjectAnimator.ofFloat(popupView, "translationY", 0f, 1000f).apply {
            duration = 300
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    popupView.visibility = View.GONE
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            start()
        }
    }

    private fun handleError(error: Exception) {
        Log.e(TAG, "Error in EditInfo", error)
        showToast("Error: ${error.message}")
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        uploadJob?.cancel() // Cancel any ongoing upload when the view is destroyed
        _binding = null
    }
}