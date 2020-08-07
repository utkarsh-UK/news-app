package com.utkarshkore.realnewsdaily.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.utkarshkore.realnewsdaily.models.Article

/**
 * Created by Utkarsh Kore on 8/2/2020, Aug, 2020
 * UK Solutions Pvt. Ltd.
 * utkarshkore@gmail.com
 * 8693886401
 **/
@Dao
interface ArticleDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertArticle(article: Article): Long

    @Query("SELECT  * FROM article")
    fun getSavedArticles(): LiveData<List<Article>>

    @Delete
    suspend fun deleteArticle(article: Article)
}