package com.example.todo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class DaysAdapter(
    private val days: List<Day>, // Haftalik kunlar ro'yxati
    private val onDayClick: (Day) -> Unit // Kunni tanlash callback
) : RecyclerView.Adapter<DaysAdapter.DaysViewHolder>() {

    private var selectedPosition: Int = -1 // Tanlangan kunni belgilash uchun

    class DaysViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.dateTextView) // Sana
        val dayTextView: TextView = view.findViewById(R.id.dayTextView) // Kunning nomi
        val dayLayout: View = view.findViewById(R.id.dayLayout) // Orqa fon uchun view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DaysViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_days, parent, false)
        return DaysViewHolder(view)
    }

    override fun onBindViewHolder(holder: DaysViewHolder, position: Int) {
        val day = days[position]
        holder.dateTextView.text = day.date
        holder.dayTextView.text = day.day

        // Bugungi yoki tanlangan kun uchun rangni belgilash
        if (day.isToday || position == selectedPosition) {
            holder.dayLayout.setBackgroundResource(R.drawable.circular_background)
            holder.dayTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.main_color))
            holder.dateTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
        } else {
            holder.dayLayout.setBackgroundResource(R.drawable.graphite_background)
            holder.dayTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
            holder.dateTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
        }

        // Element bosilganda
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            onDayClick(day)
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    override fun getItemCount(): Int = days.size
}
