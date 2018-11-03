package ca.etsmtl.applets.etsmobile.presentation.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ca.etsmtl.applets.etsmobile.R
import kotlinx.android.extensions.LayoutContainer

/**
 * Created by Sonphil on 02-11-18.
 */

class ProfileAdapter : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {
    private val differ = AsyncListDiffer<ProfileItem<out ProfileViewHolder>>(this, object : DiffUtil.ItemCallback<ProfileItem<out ProfileViewHolder>>() {
        override fun areItemsTheSame(oldItem: ProfileItem<out ProfileViewHolder>, newItem: ProfileItem<out ProfileViewHolder>): Boolean {
            return when {
                oldItem is ProfileValueItem && newItem is ProfileValueItem ->
                    oldItem.label == newItem.label && oldItem.label == newItem.label
                oldItem is ProfileHeaderItem && newItem is ProfileHeaderItem ->
                    oldItem.title == newItem.title
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: ProfileItem<out ProfileViewHolder>, newItem: ProfileItem<out ProfileViewHolder>): Boolean = oldItem == newItem
    })

    var items: List<ProfileItem<out ProfileViewHolder>> = emptyList()
        set(value) {
            field = value

            differ.submitList(value)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val containerView = LayoutInflater.from(parent.context)
                .inflate(viewType, parent, false)

        return if (viewType == R.layout.item_profile_header) {
            ProfileViewHolder.HeaderViewHolder(containerView)
        } else {
            ProfileViewHolder.ItemViewHolder(containerView)
        }
    }

    override fun getItemCount() = differ.currentList.count()

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val item = differ.currentList[position]

        if (item is ProfileValueItem) {
            item.bind(holder as ProfileViewHolder.ItemViewHolder,  position)
        } else if (item is ProfileHeaderItem) {
            item.bind(holder as ProfileViewHolder.HeaderViewHolder,  position)
        }
    }

    override fun getItemViewType(position: Int) = items[position].getLayout()

    sealed class ProfileViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {
        class ItemViewHolder(override val containerView: View): ProfileViewHolder(containerView)

        class HeaderViewHolder(override val containerView: View): ProfileViewHolder(containerView)
    }
}