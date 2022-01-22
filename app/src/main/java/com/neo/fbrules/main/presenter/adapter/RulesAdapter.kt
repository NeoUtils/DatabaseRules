package com.neo.fbrules.main.presenter.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neo.fbrules.R
import com.neo.fbrules.core.Expression
import com.neo.fbrules.core.constants.Highlighting
import com.neo.fbrules.databinding.ItemRuleBinding
import com.neo.fbrules.main.presenter.model.RuleModel
import com.neo.fbrules.main.presenter.model.PathModel
import com.neo.fbrules.util.dp
import com.neo.fbrules.util.requestColor
import com.neo.highlight.core.Highlight
import com.neo.highlight.util.scheme.ColorScheme
import java.util.regex.Pattern

private typealias RuleConditionView = ItemRuleBinding

class RulesAdapter(
    private val onRuleClickListener: OnRuleClickListener? = null,
    private val getPath: () -> PathModel
) : RecyclerView.Adapter<RulesAdapter.Holder>() {

    private val rule get() = getPath()
    private val conditions get() = rule.rules
    private val path get() = rule.rootPath

    //override RecyclerView.Adapter

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

    override fun getItemCount() = conditions.size

    //member functions

    private fun setupListeners(holder: Holder) {
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            val rule = conditions[position]
            onRuleClickListener?.onRuleEdit(rule, position)
        }

        holder.itemView.setOnLongClickListener {
            val position = holder.adapterPosition
            val rule = conditions[position]
            onRuleClickListener?.onRuleRemove(rule, position)?.let { true } ?: false
        }
    }

    class Holder(private val binding: RuleConditionView) : RecyclerView.ViewHolder(binding.root) {

        private val context get() = itemView.context

        fun bind(rule: RuleModel, isLastItem: Boolean) {
            binding.tvProperty.text = rule.property
            binding.tvCondition.text = rule.condition

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
        fun onRuleEdit(rule: RuleModel, position: Int)
        fun onRuleRemove(rule: RuleModel, position: Int)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAll() {
        notifyDataSetChanged()
    }
}
