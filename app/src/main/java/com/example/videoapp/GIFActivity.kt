package com.example.videoapp

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.example.videoapp.databinding.ActivityGifBinding


class GIFActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGifBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGifBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setGif()

    }

    private fun setGif(){
        Glide.with(this)
            .asGif()
            .load(R.drawable.splash_video_ezgif_com_resize)
            .into(object : com.bumptech.glide.request.target.ImageViewTarget<GifDrawable>(binding.imageView) {
                override fun setResource(resource: GifDrawable?) {
                    binding.imageView.setImageDrawable(resource)

                    resource?.setLoopCount(1)
                    resource?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                        override fun onAnimationEnd(drawable: Drawable?) {
                            startActivity(Intent(this@GIFActivity, MainActivity::class.java))
                            finish()
                        }
                    })
                }
            })
    }
}