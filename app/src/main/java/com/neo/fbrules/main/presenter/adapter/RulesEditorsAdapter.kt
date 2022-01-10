package com.neo.fbrules.main.presenter.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.neo.fbrules.main.presenter.contract.RulesEditor
import com.neo.fbrules.main.presenter.fragment.VisualEditorFragment
import com.neo.fbrules.main.presenter.fragment.content.TextEditorFragment
import java.lang.IllegalArgumentException

class RulesEditorsAdapter(
    manager: FragmentManager, lifecycle: Lifecycle,
    private val viewPagerListener: ViewPagerListener
) : FragmentStateAdapter(manager, lifecycle), RulesEditor {

    private val editors: Array<RulesEditor?> = Array(2) { null }

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                TextEditorFragment()
            }
            else -> {
                VisualEditorFragment()
            }
        }.apply {
            editors[position] = this
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

    interface ViewPagerListener {
        fun getPosition(): Int
    }
}