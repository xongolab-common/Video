package com.example.videoapp

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.videoapp.databinding.RawExoplayerBinding


@SuppressLint("NotifyDataSetChanged")
class RealStoriesAdapter(var context: Context, var clickListener: View.OnClickListener) : RecyclerView.Adapter<RealStoriesAdapter.Holder>() {

    var objList: ArrayList<VideoImageModel.VideoImage> = ArrayList()

    var onItemClick: ((position: Int, item: VideoImageModel.VideoImage) -> Unit)? = null

    inner class Holder(val binding: RawExoplayerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = RawExoplayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return objList.size
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = objList[position]
        holder.binding.apply {
            tvRealStoriesName.text = item.title
          //  ivRealStories.setImageURI(item.thumbnail)

            ivRealStories.setOnClickListener {
                onItemClick?.invoke(position, item)
            }
        }
    }

    fun addData(mObj: ArrayList<VideoImageModel.VideoImage>) {
        objList = ArrayList()
        objList.addAll(mObj)
        this.notifyDataSetChanged()
    }
}


data class VideoImageModel(
    var _id: String = "",
    var title: String = "",
    var code: String = "",
    var videoImage: ArrayList<VideoImage> = ArrayList()
) {

    data class VideoImage(
        var _id: String = "",
        var video: String = "",
        var title: String = "",
        var description: String = "",
      //  var thumbnail: String = "",
    )
}