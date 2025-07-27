package com.manuni.studentsinfoapi.api

import com.manuni.studentsinfoapi.model.ResultResponse
import com.manuni.studentsinfoapi.model.Students
import com.manuni.studentsinfoapi.model.SubjectInfo
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query


@JvmSuppressWildcards
interface ApiInterface {

    @GET(ApiEndPoints.USER)
    suspend fun getStudents(@Query("currentPage") currentPage: Int, @Query("limit") limit: Int) : List<Students>

    @GET(ApiEndPoints.SUBJECTS)
    suspend fun getSubjects() : List<SubjectInfo>

    @Multipart
    @POST(ApiEndPoints.UPLOADS)
    suspend fun uploadFile(@Part formData: List<MultipartBody.Part>):ResultResponse

    @POST(ApiEndPoints.USER)
    suspend fun saveStudents(@Body reqBody: Map<String,Any?>):ResultResponse

    @PUT(ApiEndPoints.USER)
    suspend fun updateStudents(@Body reqBody: Map<String, Any?>):ResultResponse

    @PUT(ApiEndPoints.UPLOADS)
    suspend fun updateProfile(@Body reqBody: Map<String, Any?>):ResultResponse

    /* If your API requires a body, use @HTTP with hasBody = true. ✅
   If your API only needs an ID as a query parameter, use @DELETE with @Query.*/

    /*@DELETE(AllApi.USER)
    suspend fun deleteUser(@Query("id") userId: Int): retrofit2.Response<ResultModel>*/


    @HTTP(method = "DELETE", path = ApiEndPoints.USER, hasBody = true)
    suspend fun deleteStudent(@Body body: Map<String, Any?>):ResultResponse


    @HTTP(method = "DELETE", path = ApiEndPoints.UPLOADS, hasBody = true)
    suspend fun deleteProfile(@Body body: Map<String, Any?>):ResultResponse

}

//@JvmSuppressWildcards
//interface ApiInterface {
//
//    @GET(ApiEndPoints.USER)
//    suspend fun getStudents(@Query("currentPage") currentPage: Int, @Query("limit") limit: Int) : List<Students>
//
//    @GET(ApiEndPoints.SUBJECTS)
//    suspend fun getSubjects():List<SubjectInfo>
//
//    @POST(ApiEndPoints.USER)
//    suspend fun saveStudents(@Body reqBody: Map<String, Any?>):ResultResponse
//
//    @Multipart
//    @POST(ApiEndPoints.UPLOADS)
//    suspend fun uploadFile(@Part formData: List<MultipartBody.Part>):ResultResponse
//
//    @PUT(ApiEndPoints.USER)
//    suspend fun updateStudents(@Body reqBody: Map<String, Any?>):ResultResponse
//
//    @PUT(ApiEndPoints.UPLOADS)
//    suspend fun updateProfile(@Body reqBody: Map<String,Any?>):ResultResponse
//
//    //query string parameter
//   /* @DELETE(ApiEndPoints.USER)
//    suspend fun deleteUser(@Query("student_id") userId: Int):ResultResponse*/
//
//    //without query string parameter
//    @HTTP(method = "DELETE", path = ApiEndPoints.USER, hasBody = true)
//    suspend fun deleteStudent(@Body body: Map<String,Any?>):ResultResponse
//
//    @HTTP(method = "DELETE", path = ApiEndPoints.USER, hasBody = true)
//    suspend fun deleteProfile(@Body body: Map<String, Any?>):ResultResponse
//}