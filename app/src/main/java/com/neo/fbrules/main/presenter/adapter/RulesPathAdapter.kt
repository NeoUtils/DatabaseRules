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
import com.neo.fbrules.main.presenter.model.PathModel
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

    private var paths: MutableList<PathModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            binding = PathRulesView.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            onRulePathListener = pathListener
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val rule = paths[position]

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

    override fun getItemCount() = paths.size

    @SuppressLint("NotifyDataSetChanged")
    fun setRules(paths: MutableList<PathModel>) {
        this.paths = paths
        updateAll()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addRule(path: PathModel) {
        paths.add(path)
        updateAll()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun editRule(path: PathModel, position: Int) {
        val oldPath = paths[position].rootPath

        paths[position].rules = path.rules
        paths[position].rootPath = path.rootPath

        if (oldPath != path.rootPath) {

            val newPath = path.rootPath

            paths.filter { it.rootPath.startsWith(oldPath) }.forEach {
                it.rootPath = it.rootPath.replaceFirst(oldPath, newPath)
            }
        }

        updateAll()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun jsonFormat() {

        if (paths.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            val readRulesJson = ReadRulesJson()
            val rulesString = readRulesJson.getRulesString(paths)

            paths = readRulesJson.getRulesModel(JSONObject(rulesString))

            withContext(Dispatchers.Main) {
                notifyDataSetChanged()
            }
        }
    }

    fun getRules(): MutableList<PathModel> {
        return paths
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        paths.clear()
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

        private val ruleConditionAdapter: RulesAdapter
                by setupRulesConditionAdapter()

        lateinit var pathModel: PathModel

        private fun setupRulesConditionAdapter() = lazy {
            RulesAdapter(onRulePathListener?.let {
                object : RulesAdapter.OnRuleClickListener{
                    override fun onRuleEdit(rule: RuleModel, position: Int) {
                        it.onEditRule(rule, adapterPosition, position)
                    }

                    override fun onRuleRemove(rule: RuleModel, position: Int) {
                        it.onRemoveRule(adapterPosition, position)
                    }
                }
            }) { pathModel }.apply {
                binding.rvConditions.adapter = this
            }
        }

        fun bind(path: PathModel, isLastItem: Boolean) {
            this.pathModel = path

            ruleConditionAdapter.updateAll()
            binding.tvPath.text = path.rootPath.replaceFirst("rules/", "/")

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
        fun onEditPath(path: PathModel, position: Int)
        fun onEditRule(rule: RuleModel, pathPosition: Int, rulePosition: Int)
        fun onRemoveRule(pathPosition: Int, rulePosition: Int)
        fun onRemovePath(pathPosition: Int)
    }
}
