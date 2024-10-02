package com.example.mycompose.model

data class MessageBoxState(
    val rooms: List<Pair<MessageModel, UserProfile>> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)