package com.hxbreak.animalcrossingtools.ui.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.ItemViewBinder
import com.hxbreak.animalcrossingtools.GlideApp
import com.hxbreak.animalcrossingtools.data.source.entity.MessageEntity
import com.hxbreak.animalcrossingtools.databinding.ItemChatMessageBinding

class ChatMessageItemViewDelegate: ItemViewBinder<MessageEntity, ChatMessageItemViewDelegate.ViewBinder>() {

    inner class ViewBinder(val item: ItemChatMessageBinding): RecyclerView.ViewHolder(item.root){
        @SuppressLint("SetTextI18n")
        fun bind(entity: MessageEntity){
            item.content.text = entity.description
            val isImage = (entity.mimeType.startsWith("image/") && entity.path != null)
            item.image.isGone = !isImage
            if (isImage){
                GlideApp.with(item.image).load(entity.path?.toUri())
                    .centerCrop()
                    .into(item.image)
            }
            if (entity.description == null && !isImage){
//                item.content.text = buildSpannedString {
//                    "收到了文件" + color(Color.BLUE){ "${entity.path}" } + "\n" +
//                    "类型" + bold { entity.mimeType } + "\n" +
//                    "点击进行浏览"
//                }
                item.content.text = "收到了文件" + " ${entity.path} " + "\n" +
                            "类型" +  " ${entity.mimeType} " + "\n" +
                            "点击进行浏览"
            }
            itemView.setOnClickListener {
                if (entity.path != null){
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(entity.path.toUri(), entity.mimeType)
                    intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)
                    itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewBinder, item: MessageEntity) {
        holder.bind(item)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewBinder {
        return ViewBinder(ItemChatMessageBinding.inflate(inflater, parent, false))
    }
}