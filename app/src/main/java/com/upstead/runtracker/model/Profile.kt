package com.upstead.runtracker.model

data class Profile(
    val id: Int = 0,
    val name: String,
    val heightCm: Int,
    val gender: Gender
)
