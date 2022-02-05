package com.thejunglegiant.teslastations.data.model

import com.google.gson.annotations.SerializedName

data class ContactDTO(
    @SerializedName("label") val label: String,
    @SerializedName("number") val number: String
)
