package com.manuni.studentsinfoapi.activity

import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.manuni.studentsinfoapi.R
import com.manuni.studentsinfoapi.adapter.StudentsAdapter
import com.manuni.studentsinfoapi.api.RetrofitClient
import com.manuni.studentsinfoapi.databinding.ActivityHomeBinding
import com.manuni.studentsinfoapi.model.ResultResponse
import com.manuni.studentsinfoapi.model.Students
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var studentsAdapter: StudentsAdapter
    private var studentsList: ArrayList<Students> = arrayListOf()

    private var tempPageNumber = 1
    private val PER_PAGE_DATA = 5

    companion object{
        var shouldRefresh = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.progressBar.visibility = View.VISIBLE


        studentsAdapter = StudentsAdapter(studentsList)
        binding.studentsRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.studentsRV.adapter = studentsAdapter


        //pagination
        binding.studentsRV.addOnScrollListener(object : OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0){
                    recyclerView.layoutManager?.let {
                        val lm = it as LinearLayoutManager
                        val totalCount = lm.itemCount

                        if (!binding.swipeRefresh.isRefreshing && totalCount == lm.findLastVisibleItemPosition() + 1 && (totalCount % PER_PAGE_DATA)==0){
                            tempPageNumber++
                            loadStudents(tempPageNumber)
                        }
                    }
                }

            }
        })

//        binding.searchET.addTextChangedListener(object : TextWatcher{
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                studentsAdapter.filter.filter(s)
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//
//            }
//        })


        studentsAdapter.setOnViewOrEditListener { students ->
            //Toast.makeText(this,"${students.projectTitle} clicked",Toast.LENGTH_SHORT).show()
            val intent = Intent(this, StudentsDetailsActivity::class.java)
            intent.putExtra("STUDENTS",students)
            startActivity(intent)
        }

        studentsAdapter.setOnDeleteClickListener { deleteStudent, pos ->

            AlertDialog.Builder(this@HomeActivity).apply {
                setTitle("Delete student ${deleteStudent.name}?")
                setMessage("Student data will delete permanently.")
                setPositiveButton("Delete",object : OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        performDelete(deleteStudent, successStudentDelete = {
                            Toast.makeText(this@HomeActivity,"${deleteStudent.name} deleted",Toast.LENGTH_SHORT).show()

                        }, successProfileDelete = {
                            Toast.makeText(this@HomeActivity,"${deleteStudent.name} profile deleted",Toast.LENGTH_SHORT).show()
                            studentsList.removeAt(pos)
                            studentsAdapter.notifyItemRemoved(pos)
                            loadStudents(tempPageNumber)
                        })
                    }
                })



                setNeutralButton("Cancel",object : OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                    }
                })
            }.show()

        }

        binding.swipeRefresh.setOnRefreshListener {
            tempPageNumber = 1
            loadStudents(tempPageNumber)
        }

        binding.swipeRefresh.post {
            loadStudents(tempPageNumber)
        }

        binding.addStudentCardView.setOnClickListener {
            startActivity(Intent(this,InsertStudentActivity::class.java))
        }


        //   loadStudents()

    }

    private fun performDelete(student: Students,successStudentDelete: (ResultResponse)->Unit,successProfileDelete:(ResultResponse)->Unit){
        lifecycleScope.launch {


            val deleteMap = HashMap<String,Any?>().apply {
                put("student_id",student.studentId)
            }

            val deleteProfileMap = HashMap<String,Any?>().apply {
                put("profile_id",student.profileId)
            }



            try {
                val successStudent = RetrofitClient.retrofit.deleteStudent(deleteMap)
                val successProfile = RetrofitClient.retrofit.deleteProfile(deleteProfileMap)


                successStudentDelete(successStudent)
                successProfileDelete(successProfile)

            } catch (e: Exception) {
                e.printStackTrace()
            }


        }
    }

    private fun loadStudents(pageNumber: Int) {
        lifecycleScope.launch {
            try {
                binding.swipeRefresh.isRefreshing = true
                val result = RetrofitClient.retrofit.getStudents(pageNumber, PER_PAGE_DATA)
                Log.d("Ret_Data", "onStart: $result")


                if (pageNumber == 1) {
                    studentsList.clear() // Only clear on refresh or first load
                }

                studentsList.addAll(result)
                studentsAdapter.notifyDataSetChanged()

                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false

            } catch (e: Exception) {
                binding.swipeRefresh.isRefreshing = false
               // Toast.makeText(this@HomeActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                Log.e("API_ERROR", "Error fetching data", e)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (shouldRefresh){
            loadStudents(tempPageNumber)
            shouldRefresh = false
        }
    }


}