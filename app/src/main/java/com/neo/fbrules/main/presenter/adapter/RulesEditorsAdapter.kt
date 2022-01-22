package com.neo.fbrules.main.presenter.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.neo.fbrules.main.presenter.fragment.TextEditorFragment
import com.neo.fbrules.main.presenter.fragment.VisualEditorFragment
import java.lang.IllegalArgumentException

class RulesEditorsAdapter(
    manager: FragmentManager, lifecycle: Lifecycle,
    private val onCreateFragmentListener: OnCreateFragmentListener
) : FragmentStateAdapter(manager, lifecycle) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                VisualEditorFragment()
            }
            1 -> {
                TextEditorFragment()
            }
            else -> throw IllegalArgumentException("position $position is invalid")
        }.apply {
            onCreateFragmentListener.createFragment(position, this)
        }
    }

    interface OnCreateFragmentListener {
        fun createFragment(position: Int, fragment: Fragment)
    }
}
