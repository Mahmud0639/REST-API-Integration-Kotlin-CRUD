package com.manuni.studentsinfoapi.activity

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.dhaval2404.imagepicker.ImagePicker
import com.manuni.studentsinfoapi.R
import com.manuni.studentsinfoapi.api.ProgressTracker
import com.manuni.studentsinfoapi.api.RetrofitClient
import com.manuni.studentsinfoapi.databinding.ActivityInsertStudentBinding
import com.manuni.studentsinfoapi.model.ResultResponse
import com.manuni.studentsinfoapi.model.SubjectInfo
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File

class InsertStudentActivity:AppCompatActivity() {
    private lateinit var binding: ActivityInsertStudentBinding
    var selected = "CSE"

    private var mProfileUri:String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsertStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cseList = arrayListOf<String>()
        val eeeList = arrayListOf<String>()
        val bbaList = arrayListOf<String>()
        val engList = arrayListOf<String>()

        val departments = listOf("CSE", "EEE", "BBA", "ENG")

        val deptAdapter = ArrayAdapter(
            this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            departments
        )
        binding.deptSpinner.adapter = deptAdapter


        var subRes: List<SubjectInfo> = emptyList()

        lifecycleScope.launch {
            subRes = RetrofitClient.retrofit.getSubjects()
            // Optional: default selection show korar jonno
            handleSubjects(subRes, selected, cseList, eeeList, bbaList, engList)
        }

        binding.deptSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                selected = departments[position]
                Toast.makeText(this@InsertStudentActivity, "$selected selected", Toast.LENGTH_SHORT).show()

                // 🔁 Ekhane call korchi selected value diye
                handleSubjects(subRes, selected, cseList, eeeList, bbaList, engList)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        //image pick
        binding.imagePick.setOnClickListener{
            ImagePicker.with(this@InsertStudentActivity)
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
        var credits = ""

        //button click
        binding.saveBtn.setOnClickListener {

            //for image validation
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

            if (binding.projectDescET.text.isNullOrEmpty()){
                binding.projectDescET.error = "Field can't be empty."
                return@setOnClickListener
            }else{
                proDesc = binding.projectDescET.text.toString().trim()
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

            if (binding.userPhoneET.text.isNullOrEmpty()){
                binding.userPhoneET.error = "Field can't be empty."
                return@setOnClickListener
            }else{
                userPhone = binding.userPhoneET.text.toString().trim()
                binding.projectTitle.error = null
            }

            //for checkbox
            val selectedSubjects = mutableListOf<String>()
            val checkBoxes = listOf(
                binding.checkbox1,
                binding.checkbox2,
                binding.checkbox3,
                binding.checkbox4,
                binding.checkbox5,
                binding.checkbox6
            )

            for (checkbox in checkBoxes){
                if (checkbox.isChecked){
                    selectedSubjects.add(checkbox.text.toString())
                }
            }


            val selectedIds = selectedSubjects.map { subjectText ->
                subjectText.split("-")[0].trim().toInt()
            }

            Toast.makeText(this@InsertStudentActivity,"$selectedIds is here",Toast.LENGTH_SHORT).show()


            val sSubjects = checkBoxes.filter { it.isChecked }.map { it.text.toString() }

            if (sSubjects.isEmpty()){
                Toast.makeText(this, "At least one subject should be taken.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (binding.creditET.text.isNullOrEmpty()){
                binding.creditET.error = "Field can't be empty."
                return@setOnClickListener
            }else{
                credits = binding.creditET.text.toString().trim()

                binding.projectTitle.error = null
            }

            Toast.makeText(this@InsertStudentActivity,"$mProfileUri-$proTitle-$proDesc-$userName-$userEmail-$userPhone-$selected-$selectedIds-$credits",Toast.LENGTH_SHORT).show()


            saveInfo(userName,userEmail,userPhone,credits.toInt(),selected,selectedIds.toTypedArray(),proTitle,proDesc,mProfileUri!!, success = {
                Toast.makeText(this@InsertStudentActivity,"${it.Result}",Toast.LENGTH_SHORT).show()
            }, saveSuccess = {
                Toast.makeText(this@InsertStudentActivity,"${it.Result}",Toast.LENGTH_SHORT).show()
            })


        }

    }

    //save data to database
    private fun saveInfo(mName: String,mEmail: String,mPhone: String,totCredit: Int,mDept: String,mSubjects: Array<Int>,proTitle: String, proDesc: String, mProfile:String?, success: (ResultResponse) -> Unit,saveSuccess: (ResultResponse)->Unit){

        binding.progressBar.visibility = View.VISIBLE
        binding.progressValue.visibility = View.VISIBLE

        //students_profile

        val mapData = HashMap<String,Any>().apply {
            put("name",mName)
            put("email",mEmail)
            put("phone",mPhone)
            put("total_credits",totCredit)
            put("dept_name",mDept)
            put("subjects",mSubjects)
        }

        val formData = MultipartBody.Builder().apply {
            setType(MultipartBody.FORM)//kon type er input..form naki raw

            addFormDataPart("title",proTitle)
            addFormDataPart("description",proDesc)
            addFormDataPart("profile_id", "")

            val profileFile = mProfile?.let {
                File(it)
            }



            if (profileFile != null){
                addPart(MultipartBody.Part.createFormData("my_file",profileFile.name,ProgressTracker(profileFile,object : ProgressTracker.UploadCallback{
                    override fun onProgressUpdate(percentage: Int) {
                        runOnUiThread {
                            binding.progressBar.progress = percentage
                            binding.progressValue.text = "$percentage%"
                        }

                    }

                    override fun onError() {
                        runOnUiThread {

                        }
                    }

                    override fun onFinish() {
                        runOnUiThread {
                            binding.progressBar.visibility = View.GONE
                            binding.progressValue.visibility = View.GONE

                            binding.projectTitleET.text.clear()
                            binding.projectDescET.text.clear()
                            binding.userNameET.text.clear()
                            binding.userEmailET.text.clear()
                            binding.userPhoneET.text.clear()

                            val checkBoxes = listOf(
                                binding.checkbox1,
                                binding.checkbox2,
                                binding.checkbox3,
                                binding.checkbox4,
                                binding.checkbox5,
                                binding.checkbox6

                            )
                            for (cb in checkBoxes) {
                                cb.isChecked = false
                            }

                            binding.creditET.text.clear()

                            mProfileUri = null
                            binding.profilePic.setImageResource(R.drawable.avatar)

                        }
                    }
                })))
            }

        }.build()




        lifecycleScope.launch {
            try {

                val saveResponse = RetrofitClient.retrofit.saveStudents(mapData)

                saveSuccess(saveResponse)

                val uploadResponse = RetrofitClient.retrofit.uploadFile(formData.parts())

                success(uploadResponse)

                HomeActivity.shouldRefresh = true


            } catch (e: Exception) {
                Log.e("UPLOAD", "Error in upload or save: ${e.message}", e)
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

    private fun handleSubjects(
        subRes: List<SubjectInfo>,
        selected: String,
        cseList: ArrayList<String>,
        eeeList: ArrayList<String>,
        bbaList: ArrayList<String>,
        engList: ArrayList<String>
    ) {

        cseList.clear()
        eeeList.clear()
        bbaList.clear()
        engList.clear()

        subRes.forEach {
            when (selected) {
                "CSE" -> {
                    if (it.subjectName == "Data Structure") cseList.add("101 - Data Structure")
                    if (it.subjectName == "Algorithm") cseList.add("102 - Algorithm")
                    if (it.subjectName == "Operating Systems") cseList.add("103 - Operating Systems")
                }
                "EEE" -> {
                    if (it.subjectName == "Signals and Systems") eeeList.add("201 - Signals and Systems")
                    if (it.subjectName == "Circuit Theory") eeeList.add("202 - Circuit Theory")
                    if (it.subjectName == "Digital Electronics") eeeList.add("203 - Digital Electronics")
                }
                "BBA" -> {
                    if (it.subjectName == "Principles of Manage") bbaList.add("301 - Principles of Manage")
                    if (it.subjectName == "Business Study") bbaList.add("302 - Business Study")
                    if (it.subjectName == "Marketing Fundamenta") bbaList.add("303 - Marketing Fundamenta")
                }
                "ENG" -> {
                    if (it.subjectName == "British Literature") engList.add("401- British Literature")
                    if (it.subjectName == "Fiction") engList.add("402 - Fiction")
                    if (it.subjectName == "Poetry") engList.add("403 - Poetry")
                }
            }
        }



        // Spinner update
//        val subAdapter = when (selected) {
//            "CSE" -> ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, cseList)
//            "EEE" -> ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, eeeList)
//            "BBA" -> ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, bbaList)
//            "ENG" -> ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, engList)
//            else -> null
//        }

        val subList = when (selected) {
            "CSE" -> cseList
            "EEE" -> eeeList
            "BBA" -> bbaList
            "ENG" -> engList
            else -> null
        }

        subList?.let {
            setSubjectsToCheckBox(subList)
            //binding.subjectSpinner.adapter = it
        }
    }


    private fun setSubjectsToCheckBox(subjects: List<String>){
        val checkboxes = listOf(
            binding.checkbox1,
            binding.checkbox2,
            binding.checkbox3,
            binding.checkbox4,
            binding.checkbox5,
            binding.checkbox6
        )


        for (i in checkboxes.indices){
            if (i < subjects.size){
                checkboxes[i].visibility = View.VISIBLE
                checkboxes[i].text = subjects[i]
            }else{
                checkboxes[i].visibility = View.GONE
            }
        }

    }
}