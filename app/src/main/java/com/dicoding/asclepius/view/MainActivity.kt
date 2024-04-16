package com.dicoding.asclepius.view

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.galleryButton.setOnClickListener{ startGallery() }
    }

    /**
     * Starts the gallery to select an image.
     *
     * This function launches the system gallery app to allow the user to select an image.
     * The selected image will be used for further processing, such as uploading or analysis.
     */
    private fun startGallery() {
        // Launches the system gallery app with the option to select an image only
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    /**
     * Launcher for the gallery activity result.
     *
     * This variable registers an activity result launcher for picking visual media, such as images,
     * from the gallery. When an image is selected from the gallery, the launcher callback assigns
     * the URI of the selected image to the currentImageUri property. It then triggers the display
     * of the selected image. If no media is selected, a debug log message is printed.
     */
    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // Assigns the URI of the selected image to the currentImageUri property
            currentImageUri = uri
            // Displays the selected image
            showImage()
        } else {
            // Prints a debug log message if no media is selected
            Log.d("Photo Picker", "No media selected")
        }
    }


    /**
     * Displays the captured image.
     *
     * This function sets the captured image URI to the ImageView for preview.
     * If the URI is not null, it logs the URI and updates the ImageView with the captured image.
     */
    private fun showImage() {
        // Checks if the currentImageUri is not null
        currentImageUri?.let {
            // Logs the URI of the captured image
            Log.d("Image URI", "showImage: $it")
            // Sets the captured image URI to the ImageView for preview
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage() {
        // TODO: Menganalisa gambar yang berhasil ditampilkan.
    }

    private fun moveToResult() {
        val intent = Intent(this, ResultActivity::class.java)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}