package com.neo.fbrules.main.presenter.adapter

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.neo.fbrules.R
import com.neo.fbrules.databinding.ItemAppBinding
import com.neo.fbrules.main.presenter.model.NeoUtilsApp
import com.neo.fbrules.util.goToApp
import com.neo.fbrules.util.goToUrl
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
            }

            val packageInstalled = isPackageInstalled(app.packageName, context.packageManager)

            binding.ivDownload.visibility(!packageInstalled)

            itemView.alpha = if (
                packageInstalled
            ) 1f else 0.5f

            itemView.setOnClickListener {
                if (packageInstalled) {
                    goToApp(context, app.packageName)
                } else {
                    goToUrl(context, app.url)
                }
            }
        }

        private fun isPackageInstalled(
            packageName: String,
            packageManager: PackageManager
        ): Boolean {
            return try {
                packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

}
