package com.manuni.studentsinfoapi.api

object ApiEndPoints {
    val BASE_URL = "http://192.168.0.103/"

    private const val v1 = "ourapi/v1/"

    const val USER = "${v1}students.php"
    const val SUBJECTS = "${v1}subjects.php"

    const val UPLOADS = "${v1}uploads.php"

}