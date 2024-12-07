package com.example.todo

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val db = FirebaseFirestore.getInstance()
    private val tasks = mutableListOf<Task>()
    private val days = mutableListOf<Day>()
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var daysAdapter: DaysAdapter
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addTaskContainer.setOnClickListener {
            if (selectedDate.isNotEmpty()) {
                showAddTaskDialog(selectedDate)
            } else {
                Toast.makeText(this, "Iltimos, kunni tanlang", Toast.LENGTH_SHORT).show()
            }
        }

        setupDaysRecyclerView()
        setupTasksRecyclerView()
        populateDays()
    }

    private fun setupDaysRecyclerView() {
        daysAdapter = DaysAdapter(days) { day ->
            selectedDate = day.date
            loadTasksFromFirestore(selectedDate)
        }
        binding.recyclerViewDays.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewDays.adapter = daysAdapter
    }

    private fun populateDays() {
        val calendar = Calendar.getInstance()
        val sdfDate = SimpleDateFormat("d", Locale.getDefault())
        val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())
        val today = calendar.get(Calendar.DAY_OF_MONTH)

        days.clear()

        for (i in 0 until 7) {
            val date = sdfDate.format(calendar.time)
            val day = sdfDay.format(calendar.time)
            val isToday = today == calendar.get(Calendar.DAY_OF_MONTH)
            days.add(Day(date, day, isToday))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        selectedDate = days.firstOrNull { it.isToday }?.date ?: ""
        loadTasksFromFirestore(selectedDate)

        daysAdapter.notifyDataSetChanged()
    }

    private fun setupTasksRecyclerView() {
        taskAdapter = TaskAdapter(tasks, { task -> deleteTask(task) }, { task, isChecked ->
            updateTaskStatus(task, isChecked)
        })
        binding.todaysTasksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.todaysTasksRecyclerView.adapter = taskAdapter
    }

    private fun loadTasksFromFirestore(date: String) {
        db.collection("tasks").whereEqualTo("date", date).addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(this, "Xatolik: ${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            tasks.clear()
            value?.documents?.forEach { document ->
                val task = document.toObject(Task::class.java)
                if (task != null) {
                    tasks.add(task)
                }
            }
            taskAdapter.notifyDataSetChanged()
        }
    }

    private fun showAddTaskDialog(date: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val taskTimeEditText = dialogView.findViewById<EditText>(R.id.taskTimeEditText)
        val taskTitleEditText = dialogView.findViewById<EditText>(R.id.taskTitleEditText)
        val setTaskButton = dialogView.findViewById<Button>(R.id.setTaskButton)

        taskTimeEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                taskTimeEditText.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
            }, hour, minute, true).show()
        }

        setTaskButton.setOnClickListener {
            val title = taskTitleEditText.text.toString()
            val time = taskTimeEditText.text.toString()

            if (title.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Barcha maydonlarni to'ldiring", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveTaskToFirestore(title, time, date)
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun saveTaskToFirestore(title: String, time: String, date: String) {
        val task = hashMapOf(
            "title" to title,
            "time" to time,
            "date" to date,
            "completed" to false,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("tasks").add(task)
            .addOnSuccessListener {
                Toast.makeText(this, "Vazifa qo'shildi", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Xatolik: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteTask(task: Task) {
        db.collection("tasks").whereEqualTo("title", task.title).whereEqualTo("date", task.date)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("tasks").document(document.id).delete()
                }
                Toast.makeText(this, "Vazifa o'chirildi", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Xatolik: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTaskStatus(task: Task, isChecked: Boolean) {
        db.collection("tasks").whereEqualTo("title", task.title).whereEqualTo("date", task.date)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("tasks").document(document.id)
                        .update("completed", isChecked)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Xatolik: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
