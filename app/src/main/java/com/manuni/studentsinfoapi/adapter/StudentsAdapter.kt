package com.manuni.studentsinfoapi.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.manuni.studentsinfoapi.R
import com.manuni.studentsinfoapi.api.ApiEndPoints
import com.manuni.studentsinfoapi.databinding.StudentProfileItemBinding
import com.manuni.studentsinfoapi.model.Students
import com.squareup.picasso.Picasso

class StudentsAdapter(var items: ArrayList<Students>):RecyclerView.Adapter<StudentsAdapter.StudentViewHolder>() {


    private lateinit var onViewOrEditListener: (Students)-> Unit
    private lateinit var onDeleteClickListener: (Students,Int) -> Unit

    fun setOnViewOrEditListener(action: (Students)->Unit){
        onViewOrEditListener = action
    }

    fun setOnDeleteClickListener(action: (Students,Int) -> Unit){
        onDeleteClickListener = action
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = StudentProfileItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val item = items[position]
        val binding = holder.binding

        binding.projectTitle.text = item.projectTitle
        binding.descriptionTxt.text = item.projectDesc

//http://localhost/ourapi/uploads/9d556766e0060444c1991c1ba0a70cc83989d5d1.jpg
        Picasso.get().load(ApiEndPoints.BASE_URL+"ourapi"+item.imageUrl).placeholder(R.drawable.avatar).into(binding.studentImg)
        
    }

    inner class StudentViewHolder(var binding:StudentProfileItemBinding):ViewHolder(binding.root){
        init {



            binding.viewOrEditBtn.setOnClickListener {
                val position = adapterPosition
                onViewOrEditListener(items[position])
            }

            binding.deleteBtn.setOnClickListener {
                val position = adapterPosition
                onDeleteClickListener(items[position],position)
            }
        }
    }


}