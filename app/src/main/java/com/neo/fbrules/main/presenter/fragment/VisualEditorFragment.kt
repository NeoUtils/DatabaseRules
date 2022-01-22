package com.neo.fbrules.main.presenter.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.neo.fbrules.R
import com.neo.fbrules.core.ERROR
import com.neo.fbrules.core.handlerError
import com.neo.fbrules.databinding.FragmentVisualRulesEditorBinding
import com.neo.fbrules.main.presenter.components.ReadRulesJson
import com.neo.fbrules.main.presenter.adapter.PathsAdapter
import com.neo.fbrules.main.presenter.contract.RulesEditor
import com.neo.fbrules.main.presenter.fragment.dialog.RuleDialog
import com.neo.fbrules.main.presenter.fragment.dialog.PathDialog
import com.neo.fbrules.main.presenter.model.RuleModel
import com.neo.fbrules.main.presenter.model.PathModel
import com.neo.fbrules.util.showAlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

private typealias VisualEditorView = FragmentVisualRulesEditorBinding

/**
 * Visual rule editor
 * @author Irineu A. Silva
 */
class VisualEditorFragment : Fragment(),
    RulesEditor,
    PathsAdapter.RulesPathListener {

    private lateinit var binding: VisualEditorView
    private val pathsAdapter: PathsAdapter by setupVisualRulesAdapter()
    private val paths get() = pathsAdapter.getPaths()

    //setups

    private fun setupVisualRulesAdapter() = lazy {
        PathsAdapter(this).apply {
            binding.rvRules.adapter = this
        }
    }

    //override Fragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = VisualEditorView.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupArguments()
        setupListeners()
    }

    //override RulesPathListener

    override fun onAddRule(pathPosition: Int) {
        showRuleDialog(pathPosition, null)
    }

    override fun onEditPath(path: PathModel, position: Int) {
        showPathDialog(path, position)
    }

    override fun onEditRule(rule: RuleModel, pathPosition: Int, rulePosition: Int) {
        showRuleDialog(pathPosition, rule, rulePosition)
    }

    override fun onRemovePath(pathPosition: Int) {

        val pathToRemove = paths[pathPosition].rootPath

        val allRulesToRemove = paths.filter {
            it.parentPath == pathToRemove || it.rootPath == pathToRemove
        }

        val listedPaths: String = allRulesToRemove
            .joinToString(prefix = "\n", separator = ",\n") { it.rootPath }

        showAlertDialog(
            getString(R.string.text_visualEditor_onRemovePath_title),
            getString(R.string.text_visualEditor_onRemovePath_message, listedPaths)
        ) {
            positiveButton(getString(R.string.btn_remove)) {

                paths.removeAll { allRulesToRemove.contains(it) }
                pathsAdapter.updateAll()

            }
            negativeButton(getString(R.string.btn_cancel))
        }
    }

    override fun onRemoveRule(pathPosition: Int, rulePosition: Int) {
        val path = paths[pathPosition]
        val rule = path.rules[rulePosition]
        showAlertDialog(
            getString(R.string.text_visualEditor_onRemoveRule_title),
            getString(R.string.text_visualEditor_onRemoveRule_message, rule)
        ) {
            positiveButton(getString(R.string.btn_remove)) {
                path.rules.removeAt(rulePosition)
                pathsAdapter.updateAll()
            }
            negativeButton(getString(R.string.btn_cancel))
        }
    }

    //override RulesEditor

    override fun getRules(): String? {

        if (paths.isEmpty()) return null

        return ReadRulesJson().getRulesString(paths)
    }

    override fun setRules(rules: String) {
        var tryFix = false

        runCatching {

            pathsAdapter.clear()

            if (rules.isEmpty()) {
                return@runCatching
            }

            val rulesJson = runCatching {
                JSONObject(rules)
            }.getOrElse {
                tryFix = true
                tryFixRules(rules)
            }

            if (!rulesJson.has("rules")) {
                handlerError(
                    ERROR.INVALID_RULES,
                    IllegalArgumentException("rules not found")
                )
                return@runCatching
            }

            if (tryFix) {
                showFixedRulesAlert()
            }

            readRulesJson(rulesJson)
        }.onFailure {
            handlerError(
                ERROR.INVALID_JSON,
                it
            )
        }
    }

    //member functions

    private fun setupListeners() {
        binding.fbAddPathBtn.setOnClickListener {
            showPathDialog()
        }
    }

    private fun setupArguments() {
        arguments?.getString("rules", null)?.let { setRules(it) }
    }

    private fun showRuleDialog(
        pathPosition: Int,
        rule: RuleModel? = null,
        rulePosition: Int = -1
    ) {

        registerAddConditionListener()

        val dialog = RuleDialog()

        dialog.arguments = Bundle().apply {
            putInt("path_position", pathPosition)
            putInt("rule_position", rulePosition)

            rule?.let {
                putSerializable(RuleModel::class.java.simpleName, rule)
            }
        }

        dialog.show(
            parentFragmentManager,
            RuleDialog.TAG
        )
    }

    private fun registerAddConditionListener() {
        setFragmentResultListener(RuleDialog.TAG) { _, bundle ->
            val condition =
                bundle.getParcelable<RuleModel>(RuleModel::class.java.simpleName)

            val pathPosition = bundle.getInt("path_position", -1)

            if (condition != null && pathPosition != -1) {

                val rulePosition = bundle.getInt("rule_position", -1)

                if (rulePosition != -1) {
                    editRule(condition, pathPosition, rulePosition)
                } else {
                    addRule(condition, pathPosition)
                }
            }
        }
    }

    private fun editRule(
        rule: RuleModel,
        pathPosition: Int,
        rulePosition: Int
    ) {
        paths[pathPosition].rules[rulePosition] = rule
        pathsAdapter.updateAll()
    }

    private fun addRule(
        rule: RuleModel,
        position: Int
    ) {
        val path = paths[position]
        val foundEqualsProperty = path.rules.find { it.property == rule.property }

        foundEqualsProperty?.let {
            showAlertDialog(
                "Error",
                getString(R.string.text_visualEditor_addRuleError_hasPropertyError, it.property)
            ) {
                positiveButton()
            }
        } ?: run {
            path.rules.add(rule)
            pathsAdapter.setPaths(paths)
        }
    }


    private fun showPathDialog(
        path: PathModel? = null,
        position: Int? = null
    ) {

        registerAddPathListener()

        val dialog = PathDialog()

        if (path != null && position != null) {
            dialog.arguments = Bundle().apply {
                putString(PathModel::class.java.simpleName, Gson().toJson(path))
                putInt("position", position)
            }
        }

        dialog.show(
            parentFragmentManager,
            PathDialog.TAG
        )
    }

    private fun registerAddPathListener() {
        setFragmentResultListener(PathDialog.TAG) { _, bundle ->
            val rule = bundle.getParcelable<PathModel>(PathModel::class.java.simpleName)
            rule?.let {
                val position = bundle.getInt("position", -1)
                if (position != -1) {
                    editPath(rule, position)
                } else {
                    addPath(rule)
                }
            }
        }
    }

    private fun addPath(path: PathModel) {

        val foundEqualsPath = paths.find { it.rootPath == path.rootPath }

        foundEqualsPath?.let {
            showAlertDialog(
                "Error",
                getString(R.string.text_visualEditor_addPathError_hasPathError, it.actualPath)
            ) {
                positiveButton()
            }
        } ?: runCatching {
            pathsAdapter.addPath(path)
        }.onFailure {
            handlerError(ERROR.INVALID_JSON, it)
        }
    }

    private fun editPath(path: PathModel, position: Int) {
        runCatching {
            pathsAdapter.editPath(path, position)
        }.onFailure {
            handlerError(ERROR.INVALID_JSON, it)
        }
    }

    private fun readRulesJson(rulesJson: JSONObject): Result<Any> = runCatching {
        pathsAdapter.clear()

        lifecycleScope.launch(Dispatchers.IO) {
            val rules = ReadRulesJson().getRulesModel(rulesJson)
            withContext(Dispatchers.Main) {
                pathsAdapter.setPaths(rules)
            }
        }
    }.onFailure {
        handlerError(
            ERROR.UNRECOGNIZED_RULES, it
        )
    }

    private fun showFixedRulesAlert() {
        showAlertDialog(
            getString(R.string.text_visualEditor_fixedRules_title),
            getString(R.string.text_visualEditor_fixedRules_message)
        ) {
            positiveButton()
        }
    }

    private fun tryFixRules(rules: String): JSONObject {
        val detectCommaAtEndRegex = Regex(",(?=[^{}\\w]*\\})")
        val fixedCommaAtEnd = rules.replace(detectCommaAtEndRegex, "")
        return JSONObject(fixedCommaAtEnd)
    }
}
