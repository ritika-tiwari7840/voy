package com.ritika.voy.home

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.ritika.voy.R
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.GetUserResponse
import com.ritika.voy.api.dataclasses.UserResponseData
import com.ritika.voy.databinding.FragmentEditInfoBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.*
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException
import kotlin.math.log

@Suppress("UNUSED_EXPRESSION")
class EditInfo : Fragment() {
    private var _binding: FragmentEditInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private var selectedGender: String? = null
    private var imageUri: Uri? = null
    private var uploadJob: Job? = null
    private val firstName: String = ""
    private val lastName: String = ""
    private val emergencyContactNo: String = ""
    private val filePath: String = ""

    companion object {
        const val PICK_IMAGE_REQUEST = 1
        const val CAPTURE_IMAGE_REQUEST = 2
        const val STORAGE_PERMISSION_CODE = 100
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
        writeUserDataOnView(binding)
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
                if (editGenderPopup.visibility == View.VISIBLE || editEmergencyPopup.visibility == View.VISIBLE) {
                    editGenderPopup.visibility = View.GONE
                    editEmergencyPopup.visibility = View.GONE
                }
                slideInPopup(editNamePopup)
            }

            genderLayout.setOnClickListener {
                if (editNamePopup.visibility == View.VISIBLE || editEmergencyPopup.visibility == View.VISIBLE) {
                    editNamePopup.visibility = View.GONE
                    editEmergencyPopup.visibility = View.GONE
                }
                slideInPopup(editGenderPopup)
            }
            emergencyContactLayout.setOnClickListener {
                if (editNamePopup.visibility == View.VISIBLE || editGenderPopup.visibility == View.VISIBLE) {
                    editNamePopup.visibility = View.GONE
                    editGenderPopup.visibility = View.GONE
                }
                slideInPopup(editEmergencyPopup)
            }

            editNameSaveChanges.setOnClickListener {
                saveNameChanges()
            }

            editEmergencySaveChanges.setOnClickListener {
                saveEmergencyContact()
            }
        }
        setupGenderSelection()
    }

    private fun writeUserDataOnView(
        binding: FragmentEditInfoBinding,

        ) {
        lifecycleScope.launch {

            DataStoreManager.getUserData(requireContext(), "email").first().let {
                val email = it.toString()
                binding.email.text = email
            }
            DataStoreManager.getUserData(requireContext(), "fullName").first().let {
                val fullName = it.toString()
                if (fullName.isNotEmpty()) {
                    binding.name.text = fullName
                    binding.firstName.setText(fullName.split(" ")[0])
                    binding.lastName.setText(fullName.split(" ")[1])
                } else {
                    binding.name.text = "User Name"
                    binding.firstName.setText("Update First Name")
                    binding.lastName.setText("Update Last Name")
                }
            }
            DataStoreManager.getUserData(requireContext(), "phoneNo").first().let {
                val phoneNo = it.toString()
                binding.phoneNumber.text = phoneNo
                Log.d(TAG, "writeUserDataOnView: $phoneNo")
            }
            DataStoreManager.getUserData(requireContext(), "gender").first().let {
                val gender = it.toString()
                if (gender.isNotEmpty()) {
                    binding.gender.text = gender
                    selectedGender = gender
                    if (selectedGender != null) {
                        if (selectedGender == "FEMALE") {
                            binding.female.isChecked = true
                        } else if (selectedGender == "MALE") {
                            binding.male.isChecked = true
                        } else if (selectedGender == "OTHER") {
                            binding.other.isChecked = true
                        }
                    }
                } else {
                    binding.gender.text = "Not Specified"
                }
            }

            DataStoreManager.getUserData(requireContext(), "emergencyContact").first().let {
                    val emergencyContactPhone = it.toString()
                    if (emergencyContactPhone.isNotEmpty() && emergencyContactPhone != "null") {
                        binding.emergencyContact.text = emergencyContactPhone
                    } else {
                        binding.emergencyContact.text = "Help is here"
                    }
                }
            DataStoreManager.getUserData(requireContext(), "emergencyContact").first().let {
                val emergencyContactPhone = it.toString()
                if (emergencyContactPhone.isNotEmpty() && emergencyContactPhone != "null") {
                    binding.emergencyContact.text = emergencyContactPhone
                } else {
                    binding.emergencyContact.text = "Help is here"
                }
            }
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with camera action
                    captureImageWithCamera()
                } else {
                    // Permission denied, show message or handle as needed
                    Toast.makeText(
                        requireContext(),
                        "Camera permission is required to take photos",
                        Toast.LENGTH_SHORT
                    ).show()
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
        // Check if camera permission is granted
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request camera permission if not granted
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.CAMERA), STORAGE_PERMISSION_CODE
            )
        } else {
            // If permission is already granted, proceed to capture image
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
                copyFileToAppStorage(uri)?.let { file ->
                    updateImageAndUpload(Uri.fromFile(file), file.absolutePath)
                } ?: showToast("Failed to process image")
            } catch (e: Exception) {
                Log.e(TAG, "Error processing camera image", e)
                showToast("Error processing image: ${e.message}")
            }
        }
    }


    private fun updateImageAndUpload(uri: Uri, filePath: String) {
//        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
//        val resizedBitmap = resizeBitmap(bitmap, 200, 200)
//        binding.setProfile.setImageBitmap(resizedBitmap)

        loadImageWithGlide(requireContext(), binding.setProfile, filePath)
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        uploadJob?.cancel() // Cancel any existing upload
        uploadJob = lifecycleScope.launch {
            try {
                updateUserWithRetry(requireContext(), filePath)
            } catch (e: Exception) {
                handleError(e)
            } finally {
                progressDialog.dismiss()
            }
        }
    }

    private fun loadImageWithGlide(
        context: android.content.Context,
        imageView: ImageView,
        filePath: String,
        backgroundColor: Int = android.R.color.transparent,
        cornerRadius: Int = 30,
        imageWidth: Int = 300,
        imageHeight: Int = 300,
    ) {
        Glide.with(context).load(java.io.File(filePath)).apply(
                RequestOptions().override(imageWidth, imageHeight)
                    .transform(RoundedCorners(cornerRadius)).transform(CircleCrop())

            ).into(imageView)
        Log.d(TAG, "loadImageWithGlide: $filePath")
        imageView.setBackgroundColor(ContextCompat.getColor(context, backgroundColor))
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


    private fun saveNameChanges() {
        binding.apply {
            editNamePopup.visibility = View.GONE
            val firstName: Editable = firstName.text
            val lastName: Editable = lastName.text
            name.text = "$firstName $lastName"
            val progressDialog = ProgressDialog(requireContext())
            progressDialog.setMessage("Loading...")
            progressDialog.setCancelable(false)
            progressDialog.show()
            lifecycleScope.launch {
                try {
                    updateUserName(requireContext(), firstName.toString(), lastName.toString())
                } catch (e: Exception) {
                    handleError(e)
                } finally {
                    delay(1000)
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun setupGenderSelection() {
        binding.apply {
            setExclusiveSelection(male, "MALE", female, other)
            setExclusiveSelection(female, "FEMALE", male, other)
            setExclusiveSelection(other, "OTHER", male, female)

            editGenderSaveChanges.setOnClickListener {
                if (handleGenderSave()) {
                    val progressDialog = ProgressDialog(requireContext())
                    progressDialog.setMessage("Loading...")
                    progressDialog.setCancelable(false)
                    progressDialog.show()
//                    uploadJob?.cancel() // Cancel any existing upload
                    uploadJob = lifecycleScope.launch {
                        try {
                            updateGender(
                                requireContext(), selectedGender!!
                            )
                        } catch (e: Exception) {
                            handleError(e)
                        } finally {
                            delay(1000)
                            progressDialog.dismiss()
                        }
                    }
                }
            }
        }
    }

    private fun handleGenderSave(): Boolean {
        if (selectedGender != null) {
            binding.editGenderPopup.visibility = View.GONE
            showToast("Selected Gender: $selectedGender")
            return true
        } else {
            showToast("Please select a gender")
            return false
        }
    }

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


    private fun saveEmergencyContact() {
        binding.apply {
            editEmergencyPopup.visibility = View.GONE
            val emergencyContactNo: Editable = emergency.text
            emergencyContact.text = emergencyContactNo
            val progressDialog = ProgressDialog(requireContext())
            progressDialog.setMessage("Loading...")
            progressDialog.setCancelable(false)
            progressDialog.show()
            lifecycleScope.launch {
                try {
                    updateEmergencyContactNo(requireContext(), emergencyContactNo.toString())
                } catch (e: Exception) {
                    handleError(e)
                } finally {
                    delay(1000)
                    progressDialog.dismiss()
                }
            }
        }
    }


    private suspend fun updateUserWithRetry(
        context: Context,
        filePath: String = "",
        retryCount: Int = 0,
    ) = withContext(Dispatchers.IO) {
        var token: String? = null
        try {
            val file = File(filePath).apply {
                if (!exists()) throw FileNotFoundException("File not found: $filePath")
            }
            token = DataStoreManager.getToken(requireContext(), "access").first()
            if (token != null) {
                val response = RetrofitInstance.api.updateUserData(
                    token = "Bearer $token",
                    profile_photo = createMultipartBody(file, "profile_photo"),
                )

                withContext(Dispatchers.Main) {
                    if (response.success) {
                        showToast("Profile updated successfully")
                        saveUserDataInDataStore(context, response)
                        Log.d(TAG, "Update response: $response \n file path is $filePath")
                    } else {
                        showToast("Error updating profile: ${response.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "updateUserWithRetry: $e ,$filePath")
            handleUploadError(e, context, filePath, retryCount)
        }
    }


    private suspend fun handleUploadError(
        error: Exception,
        context: Context,
        filePath: String,
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
                    updateUserWithRetry(
                        context,
                        filePath,
                    )
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

    private suspend fun updateGender(context: Context, gender: String?) =
        withContext(Dispatchers.IO) {
            try {
                val token = DataStoreManager.getToken(context, "access").first()
                if (!token.isNullOrEmpty()) {
                    val response = RetrofitInstance.api.updateGender(
                        token = "Bearer $token", gender = gender
                    )
                    withContext(Dispatchers.Main) {
                        if (response.success) {
                            showToast("Gender updated successfully")
                            saveUserDataInDataStore(context, response)
                            Log.d(TAG, "Update response: $response")
                        } else {
                            Log.e(TAG, "Error updating gender: ${response.message} ")
                            showToast("Error updating gender: ${response.message}")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("Token is missing")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating gender $gender", e)
                withContext(Dispatchers.Main) {
                    showToast("An error occurred: ${e.message}")
                }
            }
        }


    private suspend fun updateUserName(context: Context, firstName: String?, lastName: String?) =
        withContext(Dispatchers.IO) {
            try {
                val token = DataStoreManager.getToken(context, "access").first()
                if (!token.isNullOrEmpty()) {
                    val response = RetrofitInstance.api.updateUserName(
                        token = "Bearer $token", firstName = firstName, lastName = lastName
                    )
                    withContext(Dispatchers.Main) {
                        if (response.success) {
                            showToast("UserName updated successfully")
                            saveUserDataInDataStore(context, response)
                            Log.d(TAG, "Update response: $response")
                        } else {
                            Log.e(TAG, "Error updating gender: ${response.message} ")
                            showToast("Error updating gender: ${response.message}")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("Token is missing")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating gender $firstName $lastName", e)
                withContext(Dispatchers.Main) {
                    showToast("An error occurred: ${e.message}")
                }
            }
        }


    private suspend fun updateEmergencyContactNo(context: Context, emergencyContactNo: String?) =
        withContext(Dispatchers.IO) {
            try {
                val token = DataStoreManager.getToken(context, "access").first()
                if (!token.isNullOrEmpty()) {
                    val response = RetrofitInstance.api.updateEmergencyContactNo(
                        token = "Bearer $token", emergencyContactNo = emergencyContactNo
                    )
                    withContext(Dispatchers.Main) {
                        if (response.success) {
                            showToast("Emergency Contact updated successfully")
                            saveUserDataInDataStore(context, response)
                            Log.d(TAG, "Update response: $response, $emergencyContactNo")
                        } else {
                            Log.e(TAG, "Error Adding Emergency Contact: ${response.message} ")
                            showToast("Error Adding Emergency Contact: ${response.message}")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("Token is missing")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error Adding Emergency Contact $emergencyContactNo", e)
                withContext(Dispatchers.Main) {
                    showToast("An error occurred: ${e.message}")
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


    private fun handleError(error: Exception) {
        Log.e(TAG, "Error in EditInfo", error)
        showToast("Error: ${error.message}")
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    private fun saveUserDataInDataStore(
        context: Context,
        response: UserResponseData,
    ) {
        lifecycleScope.launch {
            DataStoreManager.SaveUserData(
                context,
                response.user.id.toString(),
                response.user.email,
                response.user.first_name.toString(),
                response.user.last_name.toString(),
                response.user.full_name.toString(),
                response.user.created_at.toString(),
                response.user.phone_number.toString(),
                response.user.gender.toString(),
                response.user.emergency_contact_phone,
                filePath,
                response.user.rating_as_driver.toString(),
                response.user.rating_as_passenger.toString()
            )
            writeUserDataOnView(binding)
        }
    }


    override fun onResume() {
        super.onResume()
        writeUserDataOnView(binding)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        uploadJob?.cancel() // Cancel any ongoing upload when the view is destroyed
        _binding = null
    }
}