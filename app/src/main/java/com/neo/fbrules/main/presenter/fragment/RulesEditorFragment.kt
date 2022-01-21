package com.neo.fbrules.main.presenter.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.neo.fbrules.databinding.FragmentRulesEditorBinding
import com.neo.fbrules.main.presenter.adapter.RulesEditorsAdapter
import com.neo.fbrules.main.presenter.contract.RulesEditor
import kotlin.math.absoluteValue

private typealias RulesEditorView = FragmentRulesEditorBinding

class RulesEditorFragment : Fragment(), RulesEditor {

    private lateinit var binding: RulesEditorView
    private val rulesEditorsAdapter: RulesEditorsAdapter by setupRulesEditorsAdapter()

    private fun setupRulesEditorsAdapter() = lazy {
        RulesEditorsAdapter(
            childFragmentManager,
            lifecycle,
            object : RulesEditorsAdapter.OnCreateFragmentListener {
                override fun createFragment(position: Int, fragment: Fragment) {
                    val oldPosition = 1 - position

                    val oldFragment = childFragmentManager
                        .findFragmentByTag("f$oldPosition")
                            as? RulesEditor

                    oldFragment?.let {
                        fragment.arguments = Bundle().apply {
                            putString("rules", oldFragment.getRules())
                        }
                    }
                }

            }
        )
    }

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
                    val oldPosition = 1 - position

                    val oldFragment = childFragmentManager
                        .findFragmentByTag("f" + oldPosition.absoluteValue)
                            as? RulesEditor

                    val newFragment = childFragmentManager
                        .findFragmentByTag("f" +  position.absoluteValue)
                            as? RulesEditor

                    oldFragment?.let {
                        oldFragment.getRules()?.let { rules ->
                            newFragment?.setRules(rules)
                        }
                    }
                }
            }
        )
    }

    private fun setupView() = with(binding) {
        vpRulesEditors.adapter = rulesEditorsAdapter
        vpRulesEditors.isUserInputEnabled = false

        TabLayoutMediator(tlTabs, vpRulesEditors) { tab, position ->
            tab.text = when (position) {
                0 -> "Visual Editor"
                else -> "Text Editor"
            }
        }.attach()
    }

    override fun getRules(): String? {
        val myFragment = childFragmentManager
            .findFragmentByTag("f" + binding.vpRulesEditors.currentItem)
        as RulesEditor

        return myFragment.getRules()
    }

    override fun setRules(rules: String) {
        val myFragment = childFragmentManager
            .findFragmentByTag("f" + binding.vpRulesEditors.currentItem)
                as RulesEditor

        myFragment.setRules(rules)
    }

}