package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.models.ResultItem
import com.yalantis.ucrop.UCrop
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File

/**
 * The main activity of the application responsible for image selection and analysis.
 *
 * This activity allows users to select an image from the device gallery and analyze it
 * using an image classifier. Once the analysis is complete, the results are displayed
 * in the ResultActivity.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    // URI of the currently selected image
    private var currentImageUri: Uri? = null

    // Helper class for image classification
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.galleryButton.setOnClickListener{ startGallery() }
        binding.analyzeButton.setOnClickListener {
            currentImageUri?.let {
                analyzeImage(it)
            } ?: run {
                showToast(getString(R.string.empty_image_warning))
            }
        }
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
     * the URI of the selected image to the [currentImageUri] property. It then triggers the start
     * of the uCrop activity with the selected image URI. If no media is selected, a debug log message
     * is printed.
     */
    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            startUCrop(uri)
        } else {
            // Prints a debug log message if no media is selected
            Log.d("Photo Picker", "No media selected")
        }
    }


    /**
     * Displays the selected image.
     *
     * This method sets the provided image URI to the ImageView for preview.
     * If the URI is not null, it logs the URI and updates the ImageView with the image.
     *
     * @param imageUri The URI of the selected image to be displayed.
     */
    private fun showImage(imageUri: Uri) {
        currentImageUri = imageUri

        // Checks if the currentImageUri is not null
        currentImageUri?.let {
            // Logs the URI of the captured image
            Log.d("Image URI", "showImage: $it")
            // Sets the captured image URI to the ImageView for preview
            binding.previewImageView.setImageURI(it)
        }
    }

    /**
     * Initiates the image analysis process using the selected image URI.
     *
     * This function starts the image analysis process by initializing an [ImageClassifierHelper]
     * instance and calling its [ImageClassifierHelper.classifyStaticImage] method with the provided
     * image URI. It also sets up a listener to handle classification results and errors.
     *
     * @param imageUri The URI of the selected image to be analyzed.
     */
    private fun analyzeImage(imageUri: Uri) {
        // Show progress indicator while processing the image
        binding.progressIndicator.visibility = View.VISIBLE

        // Initialize the ImageClassifierHelper
        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {

                /**
                 * Callback function invoked when an error occurs during image classification.
                 * Hides the progress indicator and displays an error message.
                 *
                 * @param error The error message describing the failure.
                 */
                override fun onError(error: String) {
                    // Hide progress indicator
                    binding.progressIndicator.visibility = View.GONE
                    // Show error message
                    showToast(error)
                }

                /**
                 * Callback function invoked when classification results are available.
                 * Hides the progress indicator and moves to the ResultActivity to display the results.
                 *
                 * @param results The list of classification results.
                 */
                override fun onResults(results: List<Classifications>?) {
                    // Hide progress indicator
                    binding.progressIndicator.visibility = View.GONE
                    // Move to ResultActivity with classification results
                    moveToResult(results)
                }
            }
        )

        // Classify the static image
        imageClassifierHelper.classifyStaticImage(imageUri)
    }

    /**
     * Navigates to the ResultActivity with classification results.
     *
     * This function creates an intent to navigate from the MainActivity to the ResultActivity.
     * It packages the classification results and the URI of the analyzed image as extras in the intent,
     * then starts the ResultActivity to display the results.
     *
     * @param results The list of classification results obtained from analyzing the image.
     *                Each result contains the predicted class label and confidence score.
     */
    private fun moveToResult(results: List<Classifications>?) {
        // Extracts class labels and confidence scores from the classification results
        val resultList = results?.map { classification ->
            ResultItem(classification.categories[0].label, classification.categories[0].score)
        }

        // Create an intent to move to the ResultActivity
        val intent = Intent(this, ResultActivity::class.java)

        // Passes the classification results and image URI as extras in the intent
        intent.putParcelableArrayListExtra(ResultActivity.EXTRA_CLASSIFICATION_RESULTS, resultList as ArrayList<ResultItem>)
        intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, currentImageUri)

        // Start the ResultActivity
        startActivity(intent)
    }

    /**
     * Displays a toast message.
     *
     * This function shows a toast message with the provided message string.
     *
     * @param message The message string to be displayed in the toast.
     */
    private fun showToast(message: String) {
        // Shows a toast message with the provided message string
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Starts the uCrop activity to perform image cropping.
     *
     * This function initiates the uCrop activity with the selected image URI for cropping.
     * It also allows customization of uCrop options such as crop aspect ratio, image format, etc.
     *
     * @param imageUri The URI of the selected image to be cropped.
     */
    private fun startUCrop(imageUri: Uri) {
        // Configure uCrop options as needed
        val options = UCrop.Options().apply {
            // Set desired crop aspect ratio, image format, etc.
        }

        // Start uCrop activity with the selected image URI
        UCrop.of(imageUri, Uri.fromFile(File.createTempFile("cropped", ".jpg", cacheDir)))
            .withOptions(options)
            .start(this)
    }

    /**
     * Handles the result from the uCrop activity.
     *
     * This method is invoked when the uCrop activity returns a result.
     * It checks if the requestCode matches the UCrop.REQUEST_CROP code and the resultCode is RESULT_OK,
     * indicating a successful cropping operation. If successful, it retrieves the cropped image URI
     * and handles it accordingly (e.g., displaying it in an ImageView).
     * If there was an error during cropping, it retrieves the error message and displays a toast message.
     *
     * @param requestCode The request code passed to the activity for identification.
     * @param resultCode The result code returned by the activity to indicate success or failure.
     * @param data The intent containing the result data from the activity.
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            val croppedUri = UCrop.getOutput(data!!)
            // Handle the cropped image URI
            showImage(croppedUri!!)
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            // Handle any errors that occurred during cropping
            showToast("Error cropping image: $cropError")
        }
    }
}