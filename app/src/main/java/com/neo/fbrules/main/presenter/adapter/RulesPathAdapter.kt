package com.neo.fbrules.main.presenter.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neo.fbrules.R
import com.neo.fbrules.core.Expression
import com.neo.fbrules.databinding.ItemPathRulesBinding
import com.neo.fbrules.main.presenter.components.ReadRulesJson
import com.neo.fbrules.main.presenter.model.RuleCondition
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
    private val pathListener: RulesPathListener? = null
) : RecyclerView.Adapter<RulesPathAdapter.Holder>() {

    private var rules: MutableList<RuleModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            binding = PathRulesView.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            onRulePathListener = pathListener
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val rule = rules[position]

        val lastItemPosition = itemCount - 1

        holder.bind(rule, lastItemPosition == position)
        holder.setupHighlight()

        holder.addConditionBtn.setOnClickListener {
            pathListener?.onAddRule(position)
        }

        holder.itemView.setOnClickListener {
            pathListener?.onEditPath(rule, position)
        }

        holder.itemView.setOnLongClickListener {
            pathListener?.onRemovePath(position)
            true
        }
    }

    override fun getItemCount() = rules.size

    @SuppressLint("NotifyDataSetChanged")
    fun setRules(rules: MutableList<RuleModel>) {
        this.rules = rules
        updateAll()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addRule(rule: RuleModel) {
        rules.add(rule)
        updateAll()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun editRule(rule: RuleModel, position: Int) {
        rules[position] = rule
        updateAll()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun jsonFormat() {

        if (rules.isEmpty()) return

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

    @SuppressLint("NotifyDataSetChanged")
    fun updateAll() {
        notifyDataSetChanged()
        jsonFormat()
    }

    class Holder(
        private val binding: PathRulesView,
        private var onRulePathListener: RulesPathListener? = null
    ) : RecyclerView.ViewHolder(binding.root) {

        val addConditionBtn = binding.mbAddConditionBtn

        private val context get() = itemView.context

        private val ruleConditionAdapter: RuleConditionsAdapter
                by setupRulesConditionAdapter()

        lateinit var ruleModel: RuleModel

        private fun setupRulesConditionAdapter() = lazy {
            RuleConditionsAdapter(onRulePathListener?.let {
                object : RuleConditionsAdapter.OnRuleClickListener{
                    override fun edit(rule: RuleCondition, position: Int) {
                        it.onEditRule(rule, adapterPosition, position)
                    }

                    override fun remove(rule: RuleCondition, position: Int) {
                        it.onRemoveRule(adapterPosition, position)
                    }
                }
            }) { ruleModel }.apply {
                binding.rvConditions.adapter = this
            }
        }

        fun bind(rule: RuleModel, isLastItem: Boolean) {
            this.ruleModel = rule

            ruleConditionAdapter.updateAll()
            binding.tvPath.text = rule.path.replaceFirst("rules/", "/")

            configBottomMargin(isLastItem)
        }

        fun setupHighlight() {
            Highlight().apply {
                addScheme(
                    ColorScheme(
                        Expression.variableInProperty,
                        context.requestColor(R.color.syntax_variable)
                    )
                )

                setSpan(binding.tvPath)
            }
        }

        private fun configBottomMargin(lastItem: Boolean) =
            with(itemView.layoutParams as ViewGroup.MarginLayoutParams) {
                bottomMargin = context.dp(if (lastItem) 6 else 0)
                itemView.layoutParams = this
            }
    }

    interface RulesPathListener {
        fun onAddRule(pathPosition: Int)
        fun onEditPath(rule: RuleModel, position: Int)
        fun onEditRule(rule: RuleCondition, pathPosition: Int, rulePosition: Int)
        fun onRemoveRule(pathPosition: Int, rulePosition: Int)
        fun onRemovePath(pathPosition: Int)
    }
}
