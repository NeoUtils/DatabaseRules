package com.neo.fbrules.main.presenter.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neo.fbrules.R
import com.neo.fbrules.core.Expression
import com.neo.fbrules.core.constants.Highlighting
import com.neo.fbrules.core.constants.setupConditions
import com.neo.fbrules.core.constants.setupProperties
import com.neo.fbrules.databinding.ItemRuleBinding
import com.neo.fbrules.main.presenter.model.RuleModel
import com.neo.fbrules.main.presenter.model.PathModel
import com.neo.fbrules.util.dp
import com.neo.fbrules.util.requestColor
import com.neo.highlight.core.Highlight
import com.neo.highlight.util.scheme.BackgroundScheme
import com.neo.highlight.util.scheme.ColorScheme
import java.util.regex.Pattern

private typealias RuleConditionView = ItemRuleBinding

class RulesAdapter(
    private val onRuleClickListener: OnRuleClickListener,
    private val getPath: () -> PathModel,
    private val getShowCode: () -> Boolean
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
                ),
            getShowCode = getShowCode
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
            onRuleClickListener.onRuleEdit(rule, position)
        }

        holder.itemView.setOnLongClickListener {
            val position = holder.adapterPosition
            val rule = conditions[position]

            onRuleClickListener.onRuleRemove(rule, position)
            true
        }
    }

    class Holder(
        private val binding: RuleConditionView,
        private val getShowCode: () -> Boolean
    ) : RecyclerView.ViewHolder(binding.root) {

        private val context get() = itemView.context

        private val properties by setupProperties { context.resources.getBoolean(R.bool.portuguese) }
        private val conditions by setupConditions { context.resources.getBoolean(R.bool.portuguese) }

        private lateinit var rule: RuleModel

        fun bind(rule: RuleModel, isLastItem: Boolean) {
            this.rule = rule

            binding.tvProperty.text = getProperty()
            binding.tvCondition.text = geCondition()

            configBottomMargin(isLastItem)
        }

        private fun getProperty(): CharSequence {
            return if (getShowCode()) {
                rule.property
            } else {
                properties.find {
                    it.second == rule.property.trim()
                }?.first ?: rule.property
            }
        }

        private fun geCondition(): CharSequence {
            return if (getShowCode()) {
                rule.condition
            } else {
                conditions.find {
                    it.second.removeSpaces() == rule.condition.removeSpaces()
                }?.first ?: rule.condition
            }
        }

        fun setupHighlight(path: String) {
            val highlighting = Highlighting(context)

            Highlight().apply {

                schemes = highlighting.propertySyntax

                properties.forEach { _ ->
                    addScheme(
                        BackgroundScheme(
                            Pattern.compile(
                                properties.joinToString(
                                    prefix = "(",
                                    separator = ")|(",
                                    postfix = ")"
                                ) { it.first }
                            ),
                            Color.GRAY
                        )
                    )
                }

                setSpan(binding.tvProperty)
            }

            Highlight().apply {

                schemes = highlighting.conditionSyntax

                conditions.forEach { _ ->
                    addScheme(
                        BackgroundScheme(
                            Pattern.compile(
                                conditions.joinToString(
                                    prefix = "(",
                                    separator = ")|(",
                                    postfix = ")"
                                ) { it.first }
                            ),
                            Color.GRAY
                        )
                    )
                }

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

        private fun String.removeSpaces() = this.replace(" ", "")
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
