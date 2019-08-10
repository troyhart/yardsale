package com.myco.user.query

import com.myco.jpa.JacksonAttributeConverter
import com.myco.user.api.UserProfile

data class StatusSet(
    val set: MutableSet<UserProfile.Status> = HashSet()
)

class StatusSetConverter : JacksonAttributeConverter<StatusSet>(StatusSet::class.java)
