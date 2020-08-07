package com.utkarshkore.realnewsdaily.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.EdgeEffect
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.utkarshkore.realnewsdaily.R
import com.utkarshkore.realnewsdaily.adapter.ArticleViewHolder
import com.utkarshkore.realnewsdaily.adapter.NewsAdapter
import com.utkarshkore.realnewsdaily.databinding.FragmentTopNewsBinding
import com.utkarshkore.realnewsdaily.db.ArticleDatabase
import com.utkarshkore.realnewsdaily.models.Article
import com.utkarshkore.realnewsdaily.presentation.HomeActivity
import com.utkarshkore.realnewsdaily.presentation.NewsViewModel
import com.utkarshkore.realnewsdaily.presentation.NewsViewModelProviderFactory
import com.utkarshkore.realnewsdaily.repository.NewsRepository
import com.utkarshkore.realnewsdaily.utils.Resource
import com.utkarshkore.realnewsdaily.utils.forEachVisibleHolder
import kotlinx.android.synthetic.main.fragment_top_news.*

/**
 * Created by Utkarsh Kore on 8/1/2020, Aug, 2020
 * UK Solutions Pvt. Ltd.
 * utkarshkore@gmail.com
 * 8693886401
 **/
class TopNewsFragment : Fragment(R.layout.fragment_top_news) {
    private lateinit var binding: FragmentTopNewsBinding
    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter

    private val TAG = "TopNewsFragment"
    private lateinit var topArticle: Article

    companion object {
        /** The magnitude of rotation while the list is scrolled. */
        private const val SCROLL_ROTATION_MAGNITUDE = 0.25f

        /** The magnitude of rotation while the list is over-scrolled. */
        private const val OVERSCROLL_ROTATION_MAGNITUDE = -10

        /** The magnitude of translation distance while the list is over-scrolled. */
        private const val OVERSCROLL_TRANSLATION_MAGNITUDE = 0.5f

        /** The magnitude of translation distance when the list reaches the edge on fling. */
        private const val FLING_TRANSLATION_MAGNITUDE = 0.5f
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_top_news, container, false)
        return binding.apply {
            viewModel = (activity as HomeActivity).viewModel
            binding.lifecycleOwner = this@TopNewsFragment
            binding.newsViewModel = viewModel
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()

        newsAdapter.setOnItemClickListener { article, imageView, titleView ->
            val bundle = Bundle().apply {
                putSerializable("article", article)
            }
            findNavController().navigate(
                R.id.action_topNewsFragment_to_articleDetailsFragment,
                bundle,
                null,
                FragmentNavigatorExtras(imageView to article.url,
                    titleView to article.title
                )
            )
        }

        binding.imgBreakingNews.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", topArticle)
            }
            findNavController().navigate(
                R.id.action_topNewsFragment_to_articleDetailsFragment,
                bundle
            )
        }

        viewModel.topHeadlines.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    binding.shimmerViewContainer.visibility = View.GONE
                    isLoading = false
                    response.data?.let {
                        topArticle = it.articles.first()
                        val newList = it.articles.subList(1, it.articles.size).toList()
                        newsAdapter.differ.submitList(newList)
                        val totalPages = it.totalResults / 20 + 2
                        isLastPage = viewModel.headlinesPage == totalPages
                        Glide.with(this)
                            .load(it.articles[0].urlToImage)
                            .into(binding.imgBreakingNews)
                    }
                }

                is Resource.Error -> {
                    binding.shimmerViewContainer.visibility = View.GONE
                    response.message?.let {
                        Log.e(TAG, it)
                    }
                }

                is Resource.Loading -> {
                    binding.shimmerViewContainer.visibility = View.VISIBLE
                    isLoading = true
                }
            }
        })
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            recyclerView.forEachVisibleHolder { holder: ArticleViewHolder ->
                holder.rotation
                    // Update the velocity.
                    // The velocity is calculated by the vertical scroll offset.
                    .setStartVelocity(holder.currentVelocity - dx * SCROLL_ROTATION_MAGNITUDE)
                    // Start the animation. This does nothing if the animation is already running.
                    .start()
            }

            val layoutManager = binding.topNewsRecyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val totalVisibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isLastItem = firstVisibleItemPosition + totalVisibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= 19
            val shouldPaginate =
                isNotLoadingAndNotLastPage && isLastItem && isNotAtBeginning && isTotalMoreThanVisible
                        && isScrolling
            if (shouldPaginate) {
                viewModel.getTopHeadlines("in")
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    private fun setUpRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.topNewsRecyclerView.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@TopNewsFragment.scrollListener)
        }

        binding.topNewsRecyclerView.edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
            override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
                return object : EdgeEffect(view.context) {
                    override fun onRelease() {
                        super.onRelease()

                        view.forEachVisibleHolder { holder: ArticleViewHolder ->
//                            holder.rotation.start()
                            holder.translationY.start()
                        }
                    }

                    override fun onAbsorb(velocity: Int) {
                        super.onAbsorb(velocity)

                        val sign = if (direction == DIRECTION_BOTTOM) -1 else 1
                        // The list has reached the edge on fling.
                        val translationVelocity = sign * velocity * FLING_TRANSLATION_MAGNITUDE
                        view.forEachVisibleHolder { holder: ArticleViewHolder ->
                            holder.translationY
                                .setStartVelocity(translationVelocity)
                                .start()
                        }
                    }

                    override fun onPull(deltaDistance: Float) {
                        super.onPull(deltaDistance)
                        handlePull(deltaDistance)
                    }

                    override fun onPull(deltaDistance: Float, displacement: Float) {
                        super.onPull(deltaDistance, displacement)
                        handlePull(deltaDistance)
                    }

                    private fun handlePull(deltaDistance: Float) {
                        val sign = if (direction == DIRECTION_BOTTOM) -1 else 1
                        val rotationData = sign * deltaDistance * OVERSCROLL_ROTATION_MAGNITUDE
                        val translationYDelta =
                            sign * view.width * deltaDistance * OVERSCROLL_TRANSLATION_MAGNITUDE
                        view.forEachVisibleHolder { holder: ArticleViewHolder ->
                            holder.apply {
                                translationY.cancel()
                                itemView.translationY += translationYDelta
                            }
                        }
                    }
                }
            }
        }
    }
}