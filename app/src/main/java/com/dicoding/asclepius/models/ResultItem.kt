package com.dicoding.asclepius.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents an item in the classification result.
 *
 * This data class encapsulates the name and score of a classification result item.
 *
 * @property name The name or label of the classification result item.
 * @property score The confidence score or probability associated with the classification result item.
 */
@Parcelize
data class ResultItem(
    val name: String,
    val score: Float,
) : Parcelable