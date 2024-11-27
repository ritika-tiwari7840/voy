package com.ritika.voy.home

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.marginTop
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.ritika.voy.R
import com.ritika.voy.api.DataStoreManager
import com.ritika.voy.api.RetrofitInstance
import com.ritika.voy.api.dataclasses.UserResponseData
import com.ritika.voy.databinding.FragmentHomeBinding
import com.ritika.voy.databinding.FragmentUploadLicenseBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException


class UploadLicenseFragment : Fragment() {
    lateinit var _binding: FragmentUploadLicenseBinding
    private val binding get() = _binding!!
    lateinit var navController: NavController
    private var isDiverVerified: Boolean? = null ?: false
    private var imageUri: Uri? = null
    private val filePath: String = ""
    private var uploadJob: Job? = null

    companion object {
        const val PICK_IMAGE_REQUEST = 1
        const val CAPTURE_IMAGE_REQUEST = 2
        const val STORAGE_PERMISSION_CODE = 100
        private const val TAG = "EditInfo"
        private const val MAX_RETRIES = 3
        private const val INITIAL_BACKOFF_MS = 1000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentUploadLicenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)

        checkAndRequestPermissions()

        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_uploadLicenseFragment_to_driverVerificationFragment)
        }
        binding.capturePhoto.setOnClickListener {
            var layoutParams =
                binding.driverVerificationLayoutImage.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = 150
            binding.verifyYourDrivingLicense.text = "Front side of Driving License"
            binding.verifyYourDrivingLicenseText.text =
                "Make sure the lighting is good and letters are clearly visible."
            binding.driverVerificationLayoutImage.setImageResource(R.drawable.license_demo)
            binding.driverVerificationLayoutImage.layoutParams = layoutParams
            layoutParams = binding.capturePhoto.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = 200
            binding.capturePhoto.layoutParams = layoutParams
            binding.uploadFromGallery.visibility = View.GONE
            captureImageWithCamera()
        }
        binding.uploadFromGallery.setOnClickListener {
            var layoutParams =
                binding.driverVerificationLayoutImage.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = 150
            binding.verifyYourDrivingLicense.text = "Front side of Driving License"
            binding.verifyYourDrivingLicenseText.text =
                "Make sure the lighting is good and letters are clearly visible."
            binding.driverVerificationLayoutImage.setImageResource(R.drawable.license_demo)
            binding.driverVerificationLayoutImage.layoutParams = layoutParams
            layoutParams = binding.uploadFromGallery.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = 200
            binding.uploadFromGallery.layoutParams = layoutParams
            binding.capturePhoto.visibility = View.GONE
            pickImageFromGallery()
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
            "drivers_license_image${System.currentTimeMillis()}.jpg"
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

        loadImageWithGlide(requireContext(), binding.driverVerificationLayoutImage, filePath)
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
                val response = RetrofitInstance.api.VerifyUser(
                    token = "Bearer $token",
                    drivers_license_image = createMultipartBody(file, "drivers_license_image"),
                )

                withContext(Dispatchers.Main) {
                    if (response.success) {
                        showToast("License updated successfully")
                        saveUserDataInDataStore(context, response)
                        Log.d(TAG, "Update response: $response \n file path is $filePath")
                    } else {
                        showToast("Error uploading License: ${response.message}")
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
                response.user.rating_as_passenger.toString(),
                response.user.is_driver_verified.toString()
            )
        }
    }


}