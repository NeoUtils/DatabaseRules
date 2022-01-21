package com.neo.fbrules.main.presenter.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neo.fbrules.R
import com.neo.fbrules.core.Expression
import com.neo.fbrules.core.constants.Highlighting
import com.neo.fbrules.databinding.ItemRuleConditionBinding
import com.neo.fbrules.main.presenter.model.RuleCondition
import com.neo.fbrules.main.presenter.model.RuleModel
import com.neo.fbrules.util.dp
import com.neo.fbrules.util.requestColor
import com.neo.highlight.core.Highlight
import com.neo.highlight.util.scheme.ColorScheme
import java.util.regex.Pattern

private typealias RuleConditionView = ItemRuleConditionBinding

class RuleConditionsAdapter(
    private val onRuleClickListener: OnRuleClickListener? = null,
    private val getRule: () -> RuleModel
) : RecyclerView.Adapter<RuleConditionsAdapter.Holder>() {

    private val rule get() = getRule()
    private val conditions get() = rule.conditions
    private val path get() = rule.rootPath

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            RuleConditionView
                .inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val condition = conditions[position]

        val lastItemPosition = itemCount - 1
        val isLastItem = lastItemPosition == position
        holder.bind(condition, isLastItem)
        holder.setupHighlight(path)

        setupListeners(holder)
    }

    private fun setupListeners(holder: Holder) {
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            val rule = conditions[position]
            onRuleClickListener?.edit(rule, position)
        }

        holder.itemView.setOnLongClickListener {
            val position = holder.adapterPosition
            val rule = conditions[position]
            onRuleClickListener?.remove(rule, position)?.let { true } ?: false
        }
    }

    class Holder(private val binding: RuleConditionView) : RecyclerView.ViewHolder(binding.root) {

        private val context get() = itemView.context

        fun bind(condition: RuleCondition, isLastItem: Boolean) {
            binding.tvProperty.text = condition.property.substringAfter("rules/")
            binding.tvCondition.text = condition.condition

            configBottomMargin(isLastItem)
        }


        fun setupHighlight(path: String) {
            val highlighting = Highlighting(context)

            Highlight().apply {

                schemes = highlighting.propertySyntax

                setSpan(binding.tvProperty)
            }

            Highlight().apply {

                schemes = highlighting.conditionSyntax

                val matcher = Expression.variableInProperty.matcher(path)
                while (matcher.find()) {
                    val variable = matcher.group()
                    addScheme(
                        ColorScheme(
                            Pattern.compile("\\$variable\\b"),
                            context.requestColor(R.color.syntax_variable)
                        )
                    )
                }

                setSpan(binding.tvCondition)
            }
        }

        private fun configBottomMargin(lastItem: Boolean) =
            with(itemView.layoutParams as ViewGroup.MarginLayoutParams) {
                bottomMargin = context.dp(if (lastItem) 6 else 0)
                itemView.layoutParams = this
            }
    }

    interface OnRuleClickListener {
        fun edit(rule: RuleCondition, position: Int)
        fun remove(rule: RuleCondition, position: Int)
    }

    override fun getItemCount() = conditions.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateAll() {
        notifyDataSetChanged()
    }
}
