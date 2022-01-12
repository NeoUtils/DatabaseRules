package com.neo.fbrules.main.presenter.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neo.fbrules.databinding.ItemRuleBinding
import com.neo.fbrules.util.dp

class RuleConditionsAdapter : RecyclerView.Adapter<RuleConditionsAdapter.Holder>() {

    private val conditions = mutableListOf<Pair<String, String>>()

    fun setConditions(conditions: MutableList<Pair<String, String>>) {
        this.conditions.clear()
        this.conditions.addAll(conditions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            ItemRuleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val condition = conditions[position]

        val lastItemPosition = itemCount - 1
        val isLastItem = lastItemPosition == position
        holder.bind(condition, isLastItem)
    }


    class Holder(private val binding: ItemRuleBinding) : RecyclerView.ViewHolder(binding.root) {

        private val context get() = itemView.context

        fun bind(condition: Pair<String, String>, isLastItem: Boolean) {
            binding.tvRule.text = condition.first
            binding.tvCondition.text = condition.second

            configBottomMargin(isLastItem)
        }

        private fun configBottomMargin(lastItem: Boolean) =
            with(itemView.layoutParams as ViewGroup.MarginLayoutParams) {
                bottomMargin = context.dp(if (lastItem) 6 else 0)
                itemView.layoutParams = this
            }
    }

    override fun getItemCount() = conditions.size
}
