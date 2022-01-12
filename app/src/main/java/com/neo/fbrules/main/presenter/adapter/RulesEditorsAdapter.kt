package com.neo.fbrules.main.presenter.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.neo.fbrules.main.presenter.contract.RulesEditor
import com.neo.fbrules.main.presenter.fragment.TextEditorFragment
import com.neo.fbrules.main.presenter.fragment.VisualEditorFragment

class RulesEditorsAdapter(
    manager: FragmentManager, lifecycle: Lifecycle,
    private val viewPagerListener: ViewPagerListener
) : FragmentStateAdapter(manager, lifecycle), RulesEditor {

    private val editors: Array<RulesEditor?> = Array(2) { null }

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                VisualEditorFragment()
            }
            else -> {
                TextEditorFragment()
            }
        }.apply {
            editors[position] = this

            val old = if (position == 0) 1 else 0

            editors[old]?.let {
                arguments = Bundle().apply {
                    putString("rules", it.getRules())
                }
            }
        }
    }

    override fun getRules(): String {
        val position = viewPagerListener.getPosition()

        return editors[position]?.getRules()
            ?: throw IllegalArgumentException("position $position is null")
    }

    override fun setRules(rules: String) {
        val position = viewPagerListener.getPosition()

        editors[position]?.setRules(rules)
            ?: throw IllegalArgumentException("position $position is null")
    }

    private fun onChange(old: Int, position: Int) {
        editors[old]?.let { editors[position]?.setRules(it.getRules()) }
    }

    fun onChangeTo(position: Int) {
        onChange(if (position == 0) 1 else 0, position)
    }

    interface ViewPagerListener {
        fun getPosition(): Int
    }
}
