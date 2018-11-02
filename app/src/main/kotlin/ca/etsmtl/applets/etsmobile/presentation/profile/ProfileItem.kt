package ca.etsmtl.applets.etsmobile.presentation.profile

import ca.etsmtl.applets.etsmobile.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_profile.tvInfoProfileItemLabel
import kotlinx.android.synthetic.main.item_profile.tvProfileItemValue

/**
 * Created by Sonphil on 27-10-18.
 */

class ProfileItem(
    private val label: String,
    private val value: String
) : Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        with (viewHolder) {
            tvInfoProfileItemLabel.text = label
            tvProfileItemValue.text = value
        }
    }

    override fun getLayout() = R.layout.item_profile

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        return if (other !is ProfileItem) {
            false
        } else {
            label == other.label && value == other.value
        }
    }

    override fun hashCode(): Int {
        var result = label.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}