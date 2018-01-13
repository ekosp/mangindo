package com.bigscreen.mangindo.chapter

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.bigscreen.mangindo.R
import com.bigscreen.mangindo.base.BaseActivity
import com.bigscreen.mangindo.common.IntentKey
import com.bigscreen.mangindo.content.MangaContentActivity
import com.bigscreen.mangindo.databinding.ActivityChapterListBinding
import com.bigscreen.mangindo.info.MangaInfoActivity
import com.bigscreen.mangindo.listener.OnListItemClickListener
import com.bigscreen.mangindo.listener.OnLoadDataListener
import com.bigscreen.mangindo.network.model.Manga
import com.bigscreen.mangindo.network.service.MangaApiService
import com.bigscreen.mangindo.stored.StoredDataService
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import javax.inject.Inject


class ChapterListActivity : BaseActivity(), OnLoadDataListener, OnListItemClickListener {

    @Inject lateinit var storedDataService: StoredDataService
    @Inject lateinit var apiService: MangaApiService

    private lateinit var binding: ActivityChapterListBinding
    private lateinit var chaptersAdapter: ChaptersAdapter
    private lateinit var manga: Manga

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appDeps.inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chapter_list)

        if (intent.hasExtra(IntentKey.MANGA_DATA)) {
            manga = intent.getParcelableExtra(IntentKey.MANGA_DATA)
        } else {
            finish()
        }

        chaptersAdapter = ChaptersAdapter(this, this, manga.hiddenComic, storedDataService, apiService)
        setCollapsingToolbarContent()
        initRecyclerView()
        setToolbarTitle(manga.title, true)
        chaptersAdapter.loadChapters()
    }

    override fun onDestroy() {
        chaptersAdapter.onParentDestroyed()
        super.onDestroy()
    }

    private fun setCollapsingToolbarContent() {
        binding.toolbarCollapsing.title = manga.title
        binding.imageMangaCover.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimaryOverlay_50))
        Glide.with(this).load(manga.comicIcon)
                .error(R.drawable.ic_image_error)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(binding.imageMangaCover)
        binding.buttonInfo.setOnClickListener {
            val intent = Intent(this@ChapterListActivity, MangaInfoActivity::class.java)
            intent.putExtra(IntentKey.MANGA_DATA, manga)
            startActivity(intent)
        }
    }

    private fun initRecyclerView() {
        binding.listChapters.setHasFixedSize(true)
        binding.listChapters.layoutManager = LinearLayoutManager(this)
        binding.listChapters.adapter = chaptersAdapter
        chaptersAdapter.listItemClickListener = this
    }

    override fun onListItemClick(position: Int) {
        chaptersAdapter.getItem(position)?.let {
            val intent = Intent(this@ChapterListActivity, MangaContentActivity::class.java)
            intent.putExtra(IntentKey.MANGA_KEY, manga.hiddenComic)
            intent.putExtra(IntentKey.MANGA_TITLE, manga.title)
            intent.putExtra(IntentKey.CHAPTER_KEY, it.hiddenChapter)
            startActivity(intent)
        }
    }

    override fun onPrepare() {
        binding.progressLoading.visibility = View.VISIBLE
    }

    override fun onSuccess() {
        binding.progressLoading.visibility = View.GONE
    }

    override fun onError(errorMessage: String) {
        binding.progressLoading.visibility = View.GONE
        showAlert("Error", errorMessage, "Reload") { _, _ ->
            chaptersAdapter.loadChapters()
        }
    }
}