package com.manuni.studentsinfoapi.activity

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.dhaval2404.imagepicker.ImagePicker
import com.manuni.studentsinfoapi.R
import com.manuni.studentsinfoapi.api.ApiEndPoints
import com.manuni.studentsinfoapi.api.ProgressTracker
import com.manuni.studentsinfoapi.api.RetrofitClient
import com.manuni.studentsinfoapi.databinding.ActivityStudentsDetailsBinding
import com.manuni.studentsinfoapi.model.ResultResponse
import com.manuni.studentsinfoapi.model.Students
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File

class StudentsDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentsDetailsBinding
    private var mProfileUri:String? = ""
    private var mPic:String? = ""

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentsDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // val studentData = intent.getParcelableExtra<Students>("STUDENTS")
        //val studentData = intent.getParcelableExtra("STUDENTS") as Students?
        val stu = intent.getParcelableExtra("STUDENTS", Students::class.java)

        mPic = stu?.imageUrl




        //eta edit text er khetre setText use korte hobe
        binding.projectTitle.setText(stu?.projectTitle)

        binding.proDescription.setText(stu?.projectDesc)

        binding.userNameET.setText(stu?.name)
        binding.userEmailET.setText(stu?.email)
        binding.userMobileET.setText(stu?.phone)

        val credits = stu?.totalCredits.toString()
        binding.creditET.setText(credits)



        val subjects = arrayListOf<String>()


        stu?.subjects?.forEach {
            subjects.add(
                "${it.subjectId} - ${it.subjectName}"
            )
        }

        binding.motivationTxt.text =
            "Hi, ${stu?.name}. Your department is ${stu?.departments?.deptName} and taken subjects are ${subjects}"

        val subAdapter = ArrayAdapter(
            this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            subjects
        )

        //    binding.subjectSpinner.adapter = subAdapter


        //try/catch shortcut ctrl+alt+T
        try {
            Picasso.get().load(ApiEndPoints.BASE_URL + "ourapi" + stu?.imageUrl)
                .placeholder(R.drawable.avatar).into(binding.profilePic)
        } catch (e: Exception) {
            e.printStackTrace()
        }


        //image picking
        binding.imagePick.setOnClickListener{
            ImagePicker.with(this@StudentsDetailsActivity)
                .crop()
                .compress(1024)
                .maxResultSize(512, 512)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }

        var proTitle = ""
        var proDesc = ""
        var userName = ""
        var userEmail = ""
        var userPhone = ""
        var earnedCredit = ""
        binding.updateBtn.setOnClickListener {
            if (mProfileUri == null){
                return@setOnClickListener
            }

            if (binding.projectTitle.text.isNullOrEmpty()){
                binding.projectTitle.error = "Field can't be empty."
                return@setOnClickListener
            }else{
                proTitle = binding.projectTitle.text.toString().trim()
                binding.projectTitle.error = null
            }

            if (binding.proDescription.text.isNullOrEmpty()){
                binding.proDescription.error = "Field can't be empty."
                return@setOnClickListener
            }else{
                proDesc = binding.proDescription.text.toString().trim()
                binding.projectTitle.error = null
            }

            if (binding.userNameET.text.isNullOrEmpty()){
                binding.userNameET.error = "Field can't be empty."
                return@setOnClickListener
            }else{
                userName = binding.userNameET.text.toString().trim()
                binding.projectTitle.error = null
            }

            if (binding.userEmailET.text.isNullOrEmpty()){
                binding.userEmailET.error = "Field can't be empty."
                return@setOnClickListener
            }else{
                userEmail = binding.userEmailET.text.toString().trim()
                binding.projectTitle.error = null
            }

            if (binding.userMobileET.text.isNullOrEmpty()){
                binding.userMobileET.error = "Field can't be empty."
                return@setOnClickListener
            }else{
                userPhone = binding.userMobileET.text.toString().trim()
                binding.projectTitle.error = null
            }

            if (binding.creditET.text.isNullOrEmpty()){
                binding.creditET.error = "Field can't be empty."
                return@setOnClickListener
            }else{
                earnedCredit = binding.creditET.text.toString().trim()

                binding.creditET.error = null
            }
            val subIds = stu?.subjects?.mapNotNull {
                it.subjectId
            }

            updateInfo(stu?.studentId,stu?.profileId,userName,userEmail,userPhone,earnedCredit.toInt(),stu?.departments?.deptName,subIds,proTitle,proDesc,mProfileUri,
                success = {
                Toast.makeText(this@StudentsDetailsActivity,"Students data updated.",Toast.LENGTH_SHORT).show()
            }, saveSuccess = {
                Toast.makeText(this@StudentsDetailsActivity,"${it.Result}",Toast.LENGTH_SHORT).show()
            })
        }

    }
    private fun updateInfo(
        stId: Int?,
        profileId: String?,
        mName: String,
        mEmail: String,
        mPhone: String,
        totCredit: Int,
        mDept: String?,
        subjects: List<Int>?,
        proTitle: String,
        proDesc: String,
        mProfile: String?,
        success: (ResultResponse) -> Unit,
        saveSuccess: (ResultResponse) -> Unit
    ) {
        binding.progressBar.visibility = View.VISIBLE
        binding.progressValue.visibility = View.VISIBLE

        val isLocalFile = mProfileUri?.startsWith("/storage") == true || mProfileUri?.startsWith("/data") == true

        val mapData = hashMapOf<String, Any?>(
            "student_id" to stId,
            "name" to mName,
            "email" to mEmail,
            "phone" to mPhone,
            "total_credits" to totCredit,
            "dept_name" to mDept,
            "subjects" to subjects
        )

        if (isLocalFile) {
            val profileFile = mProfile?.let { File(it) }

            val formData = MultipartBody.Builder().setType(MultipartBody.FORM).apply {
                addFormDataPart("profile_id", profileId!!)
                addFormDataPart("title", proTitle)
                addFormDataPart("description", proDesc)
                addPart(
                    MultipartBody.Part.createFormData(
                        "my_file",
                        profileFile?.name,
                        ProgressTracker(profileFile!!, object : ProgressTracker.UploadCallback {
                            override fun onProgressUpdate(percentage: Int) {
                                runOnUiThread {
                                    binding.progressBar.progress = percentage
                                    binding.progressValue.text = "$percentage%"
                                }
                            }

                            override fun onError() {
                                runOnUiThread {
                                    binding.progressBar.visibility = View.GONE
                                    binding.progressValue.visibility = View.GONE
                                }
                            }

                            override fun onFinish() {
                                runOnUiThread {
                                    binding.progressBar.visibility = View.GONE
                                    binding.progressValue.visibility = View.GONE
                                    binding.projectTitle.text.clear()
                                    binding.proDescription.text.clear()
                                    binding.userNameET.text.clear()
                                    binding.userEmailET.text.clear()
                                    binding.userMobileET.text.clear()
                                    binding.creditET.text.clear()
                                    mProfileUri = null
                                    binding.profilePic.setImageResource(R.drawable.avatar)
                                }
                            }
                        })
                    )
                )
            }.build()

            lifecycleScope.launch {
                try {
                    val uploadResponse = RetrofitClient.retrofit.uploadFile(formData.parts())
                    val saveResponse = RetrofitClient.retrofit.updateStudents(mapData)

                    saveSuccess(saveResponse)
                    success(uploadResponse)



                    HomeActivity.shouldRefresh = true

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            val profileMap = hashMapOf<String, Any?>(
                "profile_id" to profileId,
                "project_title" to proTitle,
                "project_desc" to proDesc,
                "photoUrl" to mPic
            )

            lifecycleScope.launch {
                try {
                   val profileResponse = RetrofitClient.retrofit.updateProfile(profileMap)
                    val response = RetrofitClient.retrofit.updateStudents(mapData)

                    saveSuccess(profileResponse)
                    success(response)



                    HomeActivity.shouldRefresh = true

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@StudentsDetailsActivity,"${e.message}",Toast.LENGTH_SHORT).show()
                    Log.d("TAG", "updateInfo: ${e.message}")
                } finally {
                    binding.progressBar.visibility = View.GONE
                    binding.progressValue.visibility = View.GONE

                    binding.projectTitle.text.clear()
                    binding.proDescription.text.clear()
                    binding.userNameET.text.clear()
                    binding.userEmailET.text.clear()
                    binding.userMobileET.text.clear()
                    binding.creditET.text.clear()
                    mProfileUri = null
                    binding.profilePic.setImageResource(R.drawable.avatar)
                }
            }
        }
    }




    //for image
    @RequiresApi(Build.VERSION_CODES.O)
    private val startForProfileImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK) {
            // Image Uri will not be null for RESULT_OK
            val fileUri = data?.data
            //this below code portion was not given in the above same function for that we faced error
            if (fileUri != null) {
                mProfileUri = getRealPathFromUri(fileUri)
                Toast.makeText(this, "Image path: $mProfileUri", Toast.LENGTH_SHORT).show()
                Log.d("IMAGE_PATH", "Storage path: $mProfileUri")
                if (mProfileUri != null) {
                    binding.profilePic.setImageURI(fileUri)
                } else {
                    Toast.makeText(this, "Failed to get image path", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getRealPathFromUri(contentUri: Uri): String? {
        var result: String? = null
        val cursor = contentResolver.query(contentUri, null, null, null, null)
        if (cursor == null) {
            result = contentUri.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }

        // Debug the result
        Log.d("FileUploadActivity", "Real Path: $result")
        return result
    }
}