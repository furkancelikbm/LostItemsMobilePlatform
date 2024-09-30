package com.example.mycompose.model

import java.util.UUID

class MessageModel (
    val messageId :String=UUID.randomUUID().toString(),
    val senderId:String="",
    val receiverId :String="",
    val messageContent : String="",
    val timestamp : Long = System.currentTimeMillis(),
    val adId:String="",
    val roomId:String="",
)
