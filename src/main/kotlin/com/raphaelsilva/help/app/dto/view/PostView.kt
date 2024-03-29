package com.raphaelsilva.help.app.dto.view

import com.raphaelsilva.help.app.model.PostStatus
import org.apache.catalina.User
import java.time.LocalDateTime

data class PostView (
    val id: Long?,
    val title: String,
    val tags: List<String>,
    val status: PostStatus,
    var answerQuantity: Int? = 0,
    val createdAt: LocalDateTime,
    val createdBy: UserView
)
