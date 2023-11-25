package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Video(
    val id: Int? = null,
    val name: String
)
