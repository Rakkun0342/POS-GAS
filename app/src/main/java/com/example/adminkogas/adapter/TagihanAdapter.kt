package com.example.adminkogas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.adminkogas.R
import com.example.adminkogas.model.Tagihan

class TagihanAdapter(private var tagihan: MutableList<Tagihan>): RecyclerView.Adapter<TagihanAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        private val meter  = itemView.findViewById<TextView>(R.id.tvM3)
        private val biaya  = itemView.findViewById<TextView>(R.id.tvBiaya)
        private val denda  = itemView.findViewById<TextView>(R.id.tvDenda)
        private val periode = itemView.findViewById<TextView>(R.id.tvPeriode)
        fun bind (mTagihan: Tagihan){
            meter.text = mTagihan.m3.toString()
            biaya.text = mTagihan.biaya.toString()
            denda.text = mTagihan.denda.toString()
            periode.text = mTagihan.periode
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_items, parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = tagihan.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tagihan[position])
    }
}