package com.neo.fbrules.main.presenter.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neo.fbrules.R
import com.neo.fbrules.core.Expression
import com.neo.fbrules.databinding.ItemPathRulesBinding
import com.neo.fbrules.main.presenter.components.ReadRulesJson
import com.neo.fbrules.main.presenter.model.RuleModel
import com.neo.fbrules.util.dp
import com.neo.fbrules.util.requestColor
import com.neo.highlight.core.Highlight
import com.neo.highlight.util.scheme.ColorScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

private typealias PathRulesView = ItemPathRulesBinding

class RulesPathAdapter(
    private val pathListener: RulesPathListener
) : RecyclerView.Adapter<RulesPathAdapter.Holder>() {

    private var rules: MutableList<RuleModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            PathRulesView.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val rule = rules[position]

        val lastItemPosition = itemCount - 1

        holder.bind(rule, lastItemPosition == position)
        holder.setupHighlight()

        holder.addConditionBtn.setOnClickListener {
            pathListener.onAddCondition(position)
        }

        holder.itemView.setOnClickListener {
            pathListener.onEditPath(rule, position)
        }
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

        CoroutineScope(Dispatchers.IO).launch {
            val readRulesJson = ReadRulesJson()
            val rulesString = readRulesJson.getRulesString(rules)

            rules = readRulesJson.getRulesModel(JSONObject(rulesString))

            withContext(Dispatchers.Main) {
                notifyDataSetChanged()
            }
        }

    }

    fun getRules(): MutableList<RuleModel> {
        return rules
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        rules.clear()
        notifyDataSetChanged()
    }

    class Holder(
        private val binding: PathRulesView
    ) : RecyclerView.ViewHolder(binding.root) {

        val addConditionBtn = binding.mbAddConditionBtn

        private val context get() = itemView.context

        private val ruleConditionAdapter: RuleConditionsAdapter
                by setupRulesConditionAdapter()

        private fun setupRulesConditionAdapter() = lazy {
            RuleConditionsAdapter().apply {
                binding.rvConditions.adapter = this
            }
        }

        fun bind(rule: RuleModel, isLastItem: Boolean) {
            binding.tvPath.text = rule.path.replaceFirst("rules/", "/")
            ruleConditionAdapter.setConditions(rule.conditions, rule.path)

            configBottomMargin(isLastItem)
        }

        private fun configBottomMargin(lastItem: Boolean) =
            with(itemView.layoutParams as ViewGroup.MarginLayoutParams) {
                bottomMargin = context.dp(if (lastItem) 6 else 0)
                itemView.layoutParams = this
            }

        fun setupHighlight() {
            Highlight().apply {
                addScheme(
                    ColorScheme(
                        Expression.variable,
                        context.requestColor(R.color.syntax_variable)
                    )
                )

                setSpan(binding.tvPath)
            }
        }
    }

    interface RulesPathListener {
        fun onAddCondition(position: Int)
        fun onEditPath(rule: RuleModel, position: Int)
    }
}
