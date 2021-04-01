import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.core.view.isGone
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.ItemViewBinder
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.components.selections.ActionModeListener
import com.hxbreak.animalcrossingtools.components.selections.ViewHolderItemDetails
import com.hxbreak.animalcrossingtools.data.source.entity.FishEntityMix
import com.hxbreak.animalcrossingtools.data.source.entity.monthArray
import com.hxbreak.animalcrossingtools.databinding.ItemFishBinding
import com.hxbreak.animalcrossingtools.i18n.toLocaleName
import com.hxbreak.animalcrossingtools.ui.fish.FishViewModel
import com.hxbreak.animalcrossingtools.view.SlideSection


class FishViewBinder(
    private val viewModel: FishViewModel,
    private val tracker: SelectionTracker<Long>
) : ItemViewBinder<FishEntityMix, FishViewBinder.ViewBinder>() {

    @SuppressLint("UnsafeOptInUsageError")
    inner class ViewBinder(
        private val binding: ItemFishBinding
    ) : RecyclerView.ViewHolder(binding.root), ActionModeListener, ViewHolderItemDetails<Long> {

        private var detail: ItemDetailsLookup.ItemDetails<Long>? = null

        private val badgeDrawable: BadgeDrawable = BadgeDrawable.create(itemView.context)

        init {
            badgeDrawable.isVisible = true
            badgeDrawable.badgeGravity = BadgeDrawable.TOP_END

            binding.image.doOnLayout {
                BadgeUtils.attachBadgeDrawable(badgeDrawable, binding.image, binding.imageContainer)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: FishEntityMix) {
            detail = object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition() = bindingAdapterPosition
                override fun getSelectionKey() = item.fish.id.toLong()
            }
            binding.donatedIcon.isGone = !(item.saved?.donated ?: false)
            binding.foundIcon.isGone = !(item.saved?.owned ?: false)
            binding.stub.checkBox.setOnCheckedChangeListener(null)
            binding.stub.checkBox.isChecked = tracker.isSelected(item.fish.id.toLong())
            binding.root.setOnClickListener {
                tracker.select(item.fish.id.toLong())
            }
            binding.stub.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    tracker.deselect(item.fish.id.toLong())
                } else {
                    tracker.select(item.fish.id.toLong())
                }
            }
            GlideApp.with(binding.image)
                .load(item.fish.icon_uri)
                .into(binding.image)
            val now = viewModel.preferenceStorage.timeInNow
            val monthValue = now.monthValue
            val hour = now.hour
            val i = item.fish
            binding.availability.background = ColorDrawable(Color.TRANSPARENT)
            if (monthValue.toShort() in i.availability.monthArray(viewModel.hemisphere)){
                if (i.availability.timeArray.orEmpty().contains(hour.toShort())){
                    badgeDrawable.backgroundColor = Color.GREEN
                }else{
                    badgeDrawable.backgroundColor = Color.BLUE
                }
                badgeDrawable.alpha = 255
            }else{
                badgeDrawable.backgroundColor = Color.RED
                badgeDrawable.alpha = 0
            }
            binding.title.setText("${item.fish.name.toLocaleName(viewModel.locale)}-\$${item.fish.price}")
            binding.subtitle.setText(
                "${
                    if (item.fish.availability.isAllDay) "All Day" else
                        item.fish.availability.time
                }-${item.fish.availability.location}"
            )
        }

        override fun mode(mode: Boolean) {
            if (mode) {
                binding.fishItem.setState(SlideSection.EXPANDED, false)
            } else {
                binding.fishItem.setState(SlideSection.COLLAPSED, false)
            }
        }

        override fun detail(): ItemDetailsLookup.ItemDetails<Long>? = detail
    }

    override fun onBindViewHolder(holder: ViewBinder, item: FishEntityMix) {
        holder.bind(item)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewBinder {
        return ViewBinder(ItemFishBinding.inflate(inflater, parent, false))
    }
}