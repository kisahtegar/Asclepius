package com.dicoding.asclepius.view

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.models.ResultItem
import java.text.NumberFormat

/**
 * Activity to display the result of image classification.
 *
 * This activity displays the analyzed image along with the predicted class labels and confidence scores.
 * It receives the classification results and the URI of the analyzed image as extras in the intent.
 */
@Suppress("DEPRECATION")
class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private var resultList: List<ResultItem>? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from the intent
        resultList = intent.getParcelableArrayListExtra(EXTRA_CLASSIFICATION_RESULTS)
        imageUri = intent.getParcelableExtra(EXTRA_IMAGE_URI)

        // Display the image in the ImageView if URI is not null
        imageUri?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, it)
                val bitmap = ImageDecoder.decodeBitmap(source)
                binding.resultImage.setImageBitmap(bitmap)
            } else {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                binding.resultImage.setImageBitmap(bitmap)
            }
        }

        // Display prediction results (assuming resultList is not null)
        resultList?.let { results ->
            val stringBuilder = StringBuilder()
            for (item in results) {
                stringBuilder.append(" ${item.name}  ${NumberFormat.getPercentInstance().format(item.score).trim()}\n")
            }
            binding.resultText.text = stringBuilder.toString()
        }

    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_CLASSIFICATION_RESULTS = "extra_classification_results"
    }
}