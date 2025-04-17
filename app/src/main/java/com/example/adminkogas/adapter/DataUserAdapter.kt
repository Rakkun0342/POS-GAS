package com.example.adminkogas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.adminkogas.R
import com.example.adminkogas.model.UserFirebase
import com.example.adminkogas.utils.Utils.formatRupiah

class DataUserAdapter(private val userFirebase: MutableList<UserFirebase>,val kondisi: Boolean):RecyclerView.Adapter<DataUserAdapter.ViewHolder>() {

    private var onItemClickCallback: OnItemClickCallback? = null
    var harian = false

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(items: UserFirebase)
    }

    inner class ViewHolder(itemview: View):RecyclerView.ViewHolder(itemview) {
        private val tvN = itemview.findViewById<TextView>(R.id.tvTglDetail)
        private val tvNama = itemview.findViewById<TextView>(R.id.tvNamaData)
        private val tvSaldo = itemview.findViewById<TextView>(R.id.tvSaldo)
        fun bind (item: UserFirebase){
            if (harian){
                tvN.text = "Tanggal"
            }
            val nama = item.nama
            tvNama.text = item.nama
            if (nama == null){
                tvNama.text = item.key
            }
            tvSaldo.visibility = View.GONE
            if (kondisi){
                tvSaldo.visibility = View.VISIBLE
                tvSaldo.text = formatRupiah().format(item.deposit).replace(",00","")
            }
            itemView.setOnClickListener{onItemClickCallback?.onItemClicked(item)}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_user, parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = userFirebase.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userFirebase[position])
    }
}