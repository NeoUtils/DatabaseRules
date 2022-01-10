package com.neo.fbrules.main.presenter.fragment.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.neo.fbrules.databinding.ContentRulesEditorBinding
import com.neo.fbrules.main.presenter.adapter.RulesEditorsAdapter
import com.neo.fbrules.main.presenter.contract.RulesEditor

private typealias RulesEditorView = ContentRulesEditorBinding

class RulesEditorFragment : Fragment(), RulesEditor {

    private lateinit var binding: RulesEditorView
    private val rulesEditorsAdapter: RulesEditorsAdapter by setupRulesEditorsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = RulesEditorView.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        setupListener()
    }

    private fun setupListener() {
        binding.vpRulesEditors.registerOnPageChangeCallback(
            object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    rulesEditorsAdapter.onChange(if (position == 0) 1 else 0, position)
                }
            }
        )
    }

    private fun setupView() = with(binding) {
        vpRulesEditors.adapter = rulesEditorsAdapter

        TabLayoutMediator(tlTabs, vpRulesEditors) { tab, position ->
            tab.text = when (position) {
                0 -> "Visual Editor"
                else -> "Text Editor"
            }
        }.attach()
    }

    override fun getRules(): String {
        return rulesEditorsAdapter.getRules()
    }

    override fun setRules(rules: String) {
        rulesEditorsAdapter.setRules(rules)
    }

    private fun setupRulesEditorsAdapter() = lazy {
        RulesEditorsAdapter(
            childFragmentManager,
            lifecycle,
            object : RulesEditorsAdapter.ViewPagerListener {
                override fun getPosition(): Int {
                    return binding.vpRulesEditors.currentItem
                }
            }
        )
    }

}