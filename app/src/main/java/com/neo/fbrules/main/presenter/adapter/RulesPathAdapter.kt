package com.neo.fbrules.main.presenter.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neo.fbrules.R
import com.neo.fbrules.core.Expression
import com.neo.fbrules.databinding.ItemPathRulesBinding
import com.neo.fbrules.main.presenter.model.RuleModel
import com.neo.fbrules.util.dp
import com.neo.fbrules.util.requestColor
import com.neo.highlight.core.Highlight
import com.neo.highlight.util.scheme.ColorScheme

private typealias PathRulesView = ItemPathRulesBinding

class RulesPathAdapter : RecyclerView.Adapter<RulesPathAdapter.Holder>() {

    private var rules: MutableList<RuleModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            PathRulesView.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val rule = rules[position]

        val lastItemPosition = itemCount - 1

        holder.bind(rule, lastItemPosition == position)
        holder.setupHighlight()
    }

    override fun getItemCount() = rules.size

    @SuppressLint("NotifyDataSetChanged")
    fun setRules(rules: MutableList<RuleModel>) {
        this.rules = rules
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addRule(rule: RuleModel) {
        rules.add(rule)
        notifyDataSetChanged()
    }

    fun getRules(): MutableList<RuleModel> {
        return rules
    }

    class Holder(
        private val binding: PathRulesView
    ) : RecyclerView.ViewHolder(binding.root) {

        private val context get() = itemView.context

        private val ruleConditionAdapter: RuleConditionsAdapter
                by setupRulesConditionAdapter()

        fun bind(rule: RuleModel, isLastItem : Boolean) {
            binding.tvPath.text = rule.path
            ruleConditionAdapter.setConditions(rule.conditions, rule.path)

            configBottomMargin(isLastItem)
        }

        private fun configBottomMargin(lastItem: Boolean) =
            with(itemView.layoutParams as ViewGroup.MarginLayoutParams) {
                bottomMargin = context.dp(if (lastItem) 6 else 0)
                itemView.layoutParams = this
            }

        private fun setupRulesConditionAdapter() = lazy {
            RuleConditionsAdapter().apply {
                binding.rvConditions.adapter = this
            }
        }

        fun setupHighlight() {
            Highlight().apply {
                addScheme(
                    ColorScheme(
                        Expression.variable,
                        context.requestColor(R.color.bg_variable)
                    )
                )

                setSpan(binding.tvPath)
            }
        }

    }
}
