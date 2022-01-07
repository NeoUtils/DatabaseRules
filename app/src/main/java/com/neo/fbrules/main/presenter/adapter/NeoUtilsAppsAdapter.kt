package com.neo.fbrules.main.presenter.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.neo.fbrules.R
import com.neo.fbrules.databinding.ItemAppBinding
import com.neo.fbrules.main.presenter.model.NeoUtilsApp
import com.neo.fbrules.util.goToApp
import com.neo.fbrules.util.goToUrl
import com.neo.fbrules.util.isInstalled
import com.neo.fbrules.util.visibility

class NeoUtilsAppsAdapter : RecyclerView.Adapter<NeoUtilsAppsAdapter.Holder>() {

    private var apps: List<NeoUtilsApp> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemAppBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val app = apps[position]

        holder.bind(app)
    }

    override fun getItemCount() = apps.size

    @SuppressLint("NotifyDataSetChanged")
    fun setApps(apps: List<NeoUtilsApp>) {
        this.apps = apps
        notifyDataSetChanged()
    }

    class Holder(private val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root) {
        private val context = itemView.context

        init {
            itemView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        fun bind(app: NeoUtilsApp) {

            binding.name.text = app.name

            binding.icon.load(app.iconUrl) {
                placeholder(R.drawable.ic_android)
                memoryCacheKey(app.iconUrl)
            }

            val isInstalled = isInstalled(app.packageName, context.packageManager)

            binding.ivDownload.visibility(!isInstalled)

            itemView.alpha = if (
                isInstalled
            ) 1f else 0.5f

            itemView.setOnClickListener {
                if (isInstalled) {

                    Firebase.analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
                        param(FirebaseAnalytics.Param.ITEM_ID, it.id.toString())
                        param(
                            FirebaseAnalytics.Param.ITEM_NAME,
                            "open ${app.packageName}"
                        )
                        param(FirebaseAnalytics.Param.CONTENT_TYPE, "button")
                    }

                    goToApp(context, app.packageName)
                } else {

                    Firebase.analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
                        param(FirebaseAnalytics.Param.ITEM_ID, it.id.toString())
                        param(
                            FirebaseAnalytics.Param.ITEM_NAME,
                            "download ${app.packageName}"
                        )
                        param(FirebaseAnalytics.Param.CONTENT_TYPE, "button")
                    }

                    goToUrl(context, app.url)
                }
            }
        }

    }

}
