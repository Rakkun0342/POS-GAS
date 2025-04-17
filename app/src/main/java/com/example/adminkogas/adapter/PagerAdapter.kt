package com.example.adminkogas.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.adminkogas.fragment.DayFragment
import com.example.adminkogas.fragment.MontlyFragment

class PagerAdapter(fragment: FragmentActivity): FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = DayFragment()
            1 -> fragment = MontlyFragment()
        }
        return fragment as Fragment
    }
    override fun getItemCount(): Int {
        return 2
    }
}