package com.example.adminkogas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.adminkogas.R
import com.example.adminkogas.model.DetailPembayaran
import com.example.adminkogas.model.UserFirebase
import com.example.adminkogas.utils.Utils

class DetailAdapter(private val detail: MutableList<DetailPembayaran>):
    RecyclerView.Adapter<DetailAdapter.ViewHolder>() {

    private var onItemClickCallback: OnItemClickCallback? = null

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(items: DetailPembayaran)
    }

    fun setSwipeRemove(position: Int): DetailPembayaran {
        return detail.removeAt(position)
    }

    inner class ViewHolder(itemview: View): RecyclerView.ViewHolder(itemview) {
        private val tvTgl = itemview.findViewById<TextView>(R.id.tvTglDetail)
        private val tvNama = itemview.findViewById<TextView>(R.id.tvNamaData)
        private val tvSaldo = itemview.findViewById<TextView>(R.id.tvSaldo)
        private val tvPeriode = itemview.findViewById<TextView>(R.id.tvPeriode)
        fun bind (item: DetailPembayaran){
            val nama = item.nama
            tvTgl.text = item.tanggal
            tvNama.text = item.nama
            if (nama == null){
                tvNama.text = item.key
            }
            tvPeriode.visibility = View.VISIBLE
            tvSaldo.visibility = View.VISIBLE
            tvPeriode.text = item.periode
            tvSaldo.text = Utils.formatRupiah().format(item.biaya).replace(",00","")
            itemView.setOnClickListener{onItemClickCallback?.onItemClicked(item)}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_user, parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = detail.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(detail[position])
    }
}