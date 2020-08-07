package com.utkarshkore.realnewsdaily.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.utkarshkore.realnewsdaily.R
import com.utkarshkore.realnewsdaily.databinding.FragmentArticleDetailsBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Utkarsh Kore on 8/6/2020, Aug, 2020
 * UK Solutions Pvt. Ltd.
 * utkarshkore@gmail.com
 * 8693886401
 **/
class ArticleDetailsFragment : Fragment(R.layout.fragment_article_details) {
    private lateinit var binding: FragmentArticleDetailsBinding
    private val args: ArticleDetailsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_article_details, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val article = args.article
        ViewCompat.setTransitionName(binding.imageView, article.url )
        ViewCompat.setTransitionName(binding.articleTitle, article.title)

        binding.articleTitle.text = article.title
        binding.articleContent.text = article.description
        binding.authorName.text = "- " + (article.author ?: "Unknown Author")
        Glide.with(this)
            .load(article.urlToImage)
            .into(binding.imageView)

        if (!article.publishedAt.contains("ago")) {
            val removedT = article.publishedAt.replace("T", " ")
            val removedZ = removedT.replace("Z", "")
            val parsedDate = SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(removedZ)
            val publishedDate = SimpleDateFormat("EEE dd MMM ").format(parsedDate)
            val diff: Long = Date().time - parsedDate.time
            val hours = (diff % (24 * 3600)) / 3600
            binding.articleDate.text = if (hours < 25) "$hours h ago" else publishedDate
        } else {
            binding.articleDate.text = article.publishedAt
        }
    }
}