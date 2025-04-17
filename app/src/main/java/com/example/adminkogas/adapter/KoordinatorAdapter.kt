package com.example.adminkogas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.adminkogas.R
import com.example.adminkogas.model.DetailPembayaran
import com.example.adminkogas.model.Koordinator
import com.example.adminkogas.utils.Utils

class KoordinatorAdapter(private val detail: MutableList<Koordinator>):
    RecyclerView.Adapter<KoordinatorAdapter.ViewHolder>() {

    private var onItemClickCallback: OnItemClickCallback? = null

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(items: Koordinator)
    }

    fun setSwipeRemove(position: Int): Koordinator {
        return detail.removeAt(position)
    }

    inner class ViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        private val tvId = itemview.findViewById<TextView>(R.id.tvIdKoordinator)
        private val tvNama = itemview.findViewById<TextView>(R.id.tvNamaKoordinator)
        private val tvJumlah = itemview.findViewById<TextView>(R.id.tvJumlahKoordinator)
        private val tvPeriode = itemview.findViewById<TextView>(R.id.tvPeriodeKoordinator)
        private val tvNomor = itemview.findViewById<TextView>(R.id.tvNomorKoordinator)
        fun bind(item: Koordinator, position: Int) {
            tvNomor.text = (1 + position).toString()
            tvNama.text = item.namaPelanggan
            tvId.text = item.idPelanggan.toString()
            tvPeriode.text = item.periode
            tvJumlah.text = Utils.formatRupiah().format(item.total).replace(",00", "")
            itemView.setOnClickListener { onItemClickCallback?.onItemClicked(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_koordinator, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = detail.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(detail[position], position)
    }
}