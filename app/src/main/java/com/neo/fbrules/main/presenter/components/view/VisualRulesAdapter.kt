package com.neo.fbrules.main.presenter.components.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.RecyclerView
import com.neo.fbrules.databinding.ItemPathRulesBinding
import com.neo.fbrules.main.presenter.model.RuleModel
import com.neo.fbrules.util.dp

private typealias PathRulesView = ItemPathRulesBinding

class VisualRulesAdapter : RecyclerView.Adapter<VisualRulesAdapter.Holder>() {

    private var rules: MutableList<RuleModel> = mutableListOf()

    class Holder(
        private val binding: PathRulesView
    ) : RecyclerView.ViewHolder(binding.root) {

        private val context = itemView.context

        fun bind(rule: RuleModel) {
            binding.title.text = rule.path
            binding.condition.text = rule.condition.toString()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            PathRulesView.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val rule = rules[position]

        holder.bind(rule)
    }

    override fun getItemCount() = rules.size

    @SuppressLint("NotifyDataSetChanged")
    fun setRules(rules: List<RuleModel>) {
        this.rules.clear()
        this.rules.addAll(rules)
        notifyDataSetChanged()
    }
}
