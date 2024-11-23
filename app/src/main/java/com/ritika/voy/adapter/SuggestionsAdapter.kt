package com.ritika.voy.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.ritika.voy.R

class SuggestionsAdapter(private val onItemClick: (AutocompletePrediction) -> Unit) :
    RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder>() {

    private val predictions = mutableListOf<AutocompletePrediction>()

    fun submitList(newPredictions: List<AutocompletePrediction>) {
        predictions.clear()
        predictions.addAll(newPredictions)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val prediction = predictions[position]
        holder.bind(prediction)
    }

    override fun getItemCount(): Int = predictions.size

    inner class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.suggestion_text)

        fun bind(prediction: AutocompletePrediction) {
            textView.text = prediction.getPrimaryText(null).toString()
            itemView.setOnClickListener {
                onItemClick(prediction)
            }

        }
    }
}
