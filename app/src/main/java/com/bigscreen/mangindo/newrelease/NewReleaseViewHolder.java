package com.bigscreen.mangindo.newrelease;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigscreen.mangindo.R;
import com.bigscreen.mangindo.network.model.Manga;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

public class NewReleaseViewHolder extends RecyclerView.ViewHolder {

    private Context context;
    private TextView textTitle;
    private TextView textChapter;
    private ImageView imageCover;
    private OnMangaClickListener clickListener;

    public NewReleaseViewHolder(View itemView, Context context, OnMangaClickListener clickListener) {
        super(itemView);
        this.context = context;
        this.clickListener = clickListener;
        inflateView();
    }

    private void inflateView() {
        textTitle = (TextView) itemView.findViewById(R.id.text_title);
        textChapter = (TextView) itemView.findViewById(R.id.text_chapter);
        imageCover = (ImageView) itemView.findViewById(R.id.image_cover);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getAdapterPosition() != NO_POSITION)
                    clickListener.onMangaClick(getAdapterPosition());
            }
        });
    }

    public void bindData(Manga manga) {
        textTitle.setText(manga.getTitle());
        textChapter.setText(String.format(context.getString(R.string.chapter_), manga.getHiddenNewChapter()));
        Glide.with(context).load(manga.getComicIcon())
                .placeholder(R.drawable.ic_load_image)
                .error(R.drawable.ic_image_error)
                .override(200, 200).centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(imageCover);
    }

    public void bindDataSearch(Manga manga, String keyword) {
        bindData(manga);
        textTitle.setText(getSearchSpannedTitle(manga.getTitle(), keyword));
    }

    private SpannableStringBuilder getSearchSpannedTitle(String title, String keyword) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(title);
        String regex = String.format("(%s)", keyword.toLowerCase());
        Matcher matcher = Pattern.compile(regex).matcher(title.toLowerCase());
        if (matcher.find()) {
            spannableStringBuilder.setSpan(new BackgroundColorSpan(Color.YELLOW), matcher.start(1), matcher.end(1), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return spannableStringBuilder;
    }

    public interface OnMangaClickListener {
        void onMangaClick(int position);
    }
}
