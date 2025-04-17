package com.example.adminkogas.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.adminkogas.R
import com.example.adminkogas.model.Keuntungan
import com.example.adminkogas.server.ConnectAstro
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.android.synthetic.main.fragment_montly.lineChart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.sql.SQLException

class MontlyFragment : Fragment() {

    private lateinit var connectAstro: ConnectAstro

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_montly, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        connectAstro = ConnectAstro()

        lifecycleScope.launch {
            val async = async(Dispatchers.IO) {
                getBulanan()
            }
            val arrayTgl: ArrayList<String> = arrayListOf()
            val entries = arrayListOf<Entry>()
            val deff = async.await()
            var i = 0
            deff.forEach {
                arrayTgl.add(it.tanggal.toString())
                entries.add(Entry(i.toFloat(), it.keuntungan!!.toFloat()))
                i++
            }

            val lineDataSet = LineDataSet(entries, "Laporan Harian")
            lineDataSet.color = resources.getColor(R.color.black)
            lineDataSet.setCircleColor(resources.getColor(R.color.red_dark))
            lineDataSet.circleSize = 7f
            lineDataSet.valueTextSize = 12f
            lineDataSet.lineWidth = 2f

            val dataSets = arrayListOf<ILineDataSet>()
            dataSets.add(lineDataSet)

            val lineData = LineData(dataSets)

            lineChart.data = lineData
            lineChart.description = Description().apply {
                text = "Grafik Garis"
            }
            lineChart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(arrayTgl)
                position = XAxis.XAxisPosition.BOTTOM
                labelCount = 4
                granularity = 1f
            }
            lineChart.axisRight.isEnabled = false
            lineChart.setTouchEnabled(true)
            lineChart.setPinchZoom(true)
            lineChart.isDoubleTapToZoomEnabled = false
            lineChart.animateX(500)
            lineChart.invalidate()
        }
    }
    private fun getBulanan():MutableList<Keuntungan>{
        val conn = connectAstro.connection(requireContext())
        val item = mutableListOf<Keuntungan>()
        if (conn != null){
            try {
                val query = "SELECT FORMAT(CONVERT(DATE,Tanggal), 'yyyy-MM') AS Tanggal, COUNT(*) AS Coun, SUM(Jumlah) AS Jumlah " +
                        "FROM JS_HISTPROPER WHERE VoidByAdmin IS NULL " +
                        "GROUP BY FORMAT(CAST(Tanggal AS DATE), 'yyyy-MM')"
                val statemen = conn.createStatement()
                val resultSet = statemen.executeQuery(query)
                while (resultSet.next()){
                    val tanggal = resultSet.getString("Tanggal")
                    val pay = resultSet.getInt("Jumlah")
                    val count = resultSet.getInt("Coun")
                    item.add(Keuntungan(tanggal, count, pay))
                }
            }catch (e: SQLException){
                e.printStackTrace()
            }
        }
        return item
    }
}