package com.manuni.studentsinfoapi.model

import com.google.gson.annotations.SerializedName


data class SubjectInfo (

  @SerializedName("sub_id"   ) var subjectId   :     Int?    = null,
  @SerializedName("subject_name" ) var subjectName : String? = null

)