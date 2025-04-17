package com.example.adminkogas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.adminkogas.R
import com.example.adminkogas.model.UserFirebase
import com.example.adminkogas.utils.Utils
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute.Use

class ExportAdapter(private val userFirebase: MutableList<UserFirebase>):
    RecyclerView.Adapter<ExportAdapter.ViewHolder>() {

    private var onItemClickCallback: OnItemClickCallback? = null

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(items: UserFirebase)
    }

    inner class ViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        private val tvN = itemview.findViewById<TextView>(R.id.tvTglDetail)
        private val tvNama = itemview.findViewById<TextView>(R.id.tvNamaData)
        private val tvSaldo = itemview.findViewById<TextView>(R.id.tvSaldo)
        fun bind(item: UserFirebase) {
            tvN.text = "Periode"
            tvNama.text = item.nama
            tvSaldo.visibility = View.VISIBLE
            tvSaldo.text = Utils.formatRupiah().format(item.deposit).replace(",00","")
            itemView.setOnClickListener { onItemClickCallback?.onItemClicked(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_user, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = userFirebase.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userFirebase[position])
    }
}