package com.example.todo

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val tasks: List<Task>, // Vazifalar ro'yxati
    private val onDeleteClick: (Task) -> Unit, // O'chirish callback
    private val onStatusChange: (Task, Boolean) -> Unit // Holatni o'zgartirish callback
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.textViewTime)
        val timeTextView: TextView = view.findViewById(R.id.textViewTask)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
        val deleteIcon: View = view.findViewById(R.id.ic_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        holder.titleTextView.text = task.title
        holder.timeTextView.text = task.time

        // Checkbox'ni faqat hozirgi vazifa holatiga sinxronlashtirish
        holder.checkBox.setOnCheckedChangeListener(null) // Eski listener'ni tozalash
        holder.checkBox.isChecked = task.completed

        // Ustiga chiziq qo'yish yoki olib tashlash
        if (task.completed) {
            holder.titleTextView.paintFlags = holder.titleTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.timeTextView.paintFlags = holder.timeTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.titleTextView.paintFlags = holder.titleTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.timeTextView.paintFlags = holder.timeTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        // Checkbox bosilganda holatni o'zgartirish
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onStatusChange(task, isChecked)
        }

        // Vazifani o'chirish
        holder.deleteIcon.setOnClickListener {
            onDeleteClick(task)
        }
    }

    override fun getItemCount(): Int = tasks.size
}
