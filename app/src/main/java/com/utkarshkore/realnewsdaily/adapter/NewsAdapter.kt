package com.utkarshkore.realnewsdaily.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.utkarshkore.realnewsdaily.R
import com.utkarshkore.realnewsdaily.models.Article
import kotlinx.android.synthetic.main.news_item.view.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Created by Utkarsh Kore on 8/3/2020, Aug, 2020
 * UK Solutions Pvt. Ltd.
 * utkarshkore@gmail.com
 * 8693886401
 **/
class NewsAdapter : RecyclerView.Adapter<ArticleViewHolder>() {
    private val differCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    private var itemClickListener: ((Article, View, View) -> Unit)? = null

    fun setOnItemClickListener(listener: (Article, View, View) -> Unit) {
        itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.news_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(differ.currentList[position], itemClickListener)
    }
}

class ArticleViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    private val todaysDate = Date()
    var currentVelocity = 0f

    val rotation: SpringAnimation = SpringAnimation(view, SpringAnimation.ROTATION)
        .setSpring(SpringForce()
            .setFinalPosition(0f)
            .setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY)
            .setStiffness(SpringForce.STIFFNESS_HIGH))
        .addUpdateListener { _, _, velocity ->
            currentVelocity = velocity
        }

    val translationY = SpringAnimation(view, SpringAnimation.TRANSLATION_Y)
        .setSpring(
            SpringForce()
                .setFinalPosition(0f)
                .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
                .setStiffness(SpringForce.STIFFNESS_MEDIUM)
        )

    fun bind(article: Article, listener: ((Article, View, View) -> Unit)?) {
        view.apply {
            image_article.transitionName = article.url
            title_article.transitionName = article.title

            Glide.with(this)
                .load(article.urlToImage)
                .into(image_article)

            title_article.text = article.title

            if (article.publishedAt.contains("ago")) {
                article_date.text = article.publishedAt
            } else {
                val removedT = article.publishedAt.replace("T", " ")
                val removedZ = removedT.replace("Z", "")
                val parsedDate = SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(removedZ)
                val publishedDate = SimpleDateFormat("EEE dd MMM ").format(parsedDate)
                val diff: Long = todaysDate.time - parsedDate.time
                val hours = (diff % (24 * 3600)) / 3600

                article_date.text = if (hours < 25) "$hours h ago" else publishedDate
            }
            setOnClickListener {
                it?.let {
                    listener?.let { it(article, image_article, title_article) }
                }
            }


        }
    }
}