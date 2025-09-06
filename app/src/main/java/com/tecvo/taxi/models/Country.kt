package com.tecvo.taxi.models

data class Country(
    val name: String, // e.g. "United States"
    val code: String, // e.g. "US"
    val dialCode: String, // e.g. "+1"
    val flagEmoji: String
)