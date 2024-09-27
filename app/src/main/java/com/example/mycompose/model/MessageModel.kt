package com.example.mycompose.model

class MessageModel (
    val messageId:String="",
    val senderId:String="",
    val receiverId :String="",
    val messageContent : String="",
    val timestamp : Long = System.currentTimeMillis()
)
