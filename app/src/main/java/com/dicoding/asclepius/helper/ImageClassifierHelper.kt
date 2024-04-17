package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.dicoding.asclepius.R
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

/**
 * A helper class for performing image classification using a TensorFlow Lite model.
 *
 * @property threshold The minimum confidence threshold for classification results. Default is 0.1.
 * @property maxResults The maximum number of classification results to return. Default is 1.
 * @property modelName The name of the TensorFlow Lite model file. Default is "cancer_classification.tflite".
 * @property context The context used for accessing resources and content resolver.
 * @property classifierListener The listener interface for handling classification results and errors.
 */
class ImageClassifierHelper(
    private var threshold: Float = 0.1f,
    private var maxResults: Int = 1,
    private val modelName: String = "cancer_classification.tflite",
    val context: Context,
    val classifierListener: ClassifierListener?
) {
    private var imageClassifier: ImageClassifier? = null

    /**
     * Initializes the image classifier by setting up the required options and loading the model.
     */
    init {
        setupImageClassifier()
    }

    /**
     * Sets up the image classifier by creating an instance with the specified options.
     */
    private fun setupImageClassifier() {
        // Build options for the image classifier
        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResults)

        // Build base options for the image classifier
        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(4)

        // Set the base options for the image classifier
        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        try {
            // Create an image classifier instance from the model file and options
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                modelName,
                optionsBuilder.build()
            )
        } catch (e: IllegalStateException) {
            // Handle the exception if image classifier creation fails
            classifierListener?.onError(context.getString(R.string.image_classifier_failed))
            Log.e(TAG, e.message.toString())
        }
    }

    /**
     * Performs image classification on a static image.
     *
     * This function loads the image from the provided URI, preprocesses it,
     * and then performs classification using the initialized image classifier.
     * It notifies the listener with the classification results or any errors encountered.
     *
     * @param imageUri The URI of the image to be classified.
     */
    fun classifyStaticImage(imageUri: Uri) {
        Log.d(TAG, "classifyStaticImage: Running...")

        // Ensure the image classifier is initialized
        if (imageClassifier == null) {
            setupImageClassifier()
        }

        // Get the content resolver for accessing image data
        val contentResolver = context.contentResolver

        // Load the image bitmap from the URI
        val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Decode bitmap from URI source (API level 28 and higher)
            val source = ImageDecoder.createSource(contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
                .copy(Bitmap.Config.ARGB_8888, true) // Optional conversion based on model requirements
        } else {
            // Decode bitmap from URI using deprecated method (API level below 28)
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                .copy(Bitmap.Config.ARGB_8888, true) // Optional conversion based on model requirements
        }

        // Check if bitmap loading is successful
        if (bitmap == null) {
            // Notify listener about the failure to load the image
            classifierListener?.onError("Failed to load image from URI")
            return
        }

        // Create an ImageProcessor for preprocessing the image
        val imageProcessor = ImageProcessor.Builder()
            .build()

        // Preprocess the image bitmap using the image processor
        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))

        // Perform classification on the preprocessed image
        val results = imageClassifier?.classify(tensorImage)

        Log.d(TAG, "classifyStaticImage: results = $results")

        // Notify the listener with the classification results
        classifierListener?.onResults(results)

        // Check if classification is unsuccessful
        if (results == null) {
            classifierListener?.onError("Failed to classify image")
        }
    }

    /**
     * An interface for handling classification results and errors.
     */
    interface ClassifierListener {

        /**
         * Called when an error occurs during classification.
         *
         * @param error The error message.
         */
        fun onError(error: String)

        /**
         * Called when classification results are available.
         *
         * @param results The list of classification results.
         */
        fun onResults(
            results: List<Classifications>?,
        )
    }

    companion object {
        private const val TAG = "ImageClassifierHelper"
    }
}