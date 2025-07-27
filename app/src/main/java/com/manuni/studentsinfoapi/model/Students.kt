package com.manuni.studentsinfoapi.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Students (

  @SerializedName("student_id"    ) var studentId    : Int?                = null,
  @SerializedName("name"          ) var name         : String?             = null,
  @SerializedName("email"         ) var email        : String?             = null,
  @SerializedName("phone"         ) var phone        : String?             = null,
  @SerializedName("profile_id"    ) var profileId    : String?             = null,
  @SerializedName("project_title" ) var projectTitle : String?             = null,
  @SerializedName("project_desc"  ) var projectDesc  : String?             = null,
  @SerializedName("imageUrl"      ) var imageUrl     : String?             = null,
  @SerializedName("total_credits" ) var totalCredits : Int?                = null,
  @SerializedName("departments"   ) var departments  : Departments?        = Departments(),
  @SerializedName("subjects"      ) var subjects     : ArrayList<Subjects> = arrayListOf()

):Parcelable