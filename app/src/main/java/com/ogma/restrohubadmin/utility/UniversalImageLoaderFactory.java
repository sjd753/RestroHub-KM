package com.ogma.restrohubadmin.utility;

import android.content.Context;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by User on 02-09-2016.
 */
public class UniversalImageLoaderFactory {

    public UniversalImageLoaderFactory(Builder builder) {

    }

    public static DisplayImageOptions getDefaultOptions(int placeHolder) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showStubImage(placeHolder)
                .cacheInMemory()
                .cacheOnDisc()
                .build();

        return options;
    }

    public static class Builder {
        private ImageLoader imageLoader;
        private ImageLoaderConfiguration config;
        private Context context;

        public Builder(Context context) {
            this.context = context;
        }

        private void createAdapterConfig() {
            config = new ImageLoaderConfiguration.Builder(context)
                    .threadPoolSize(10)
                    .threadPriority(Thread.MIN_PRIORITY + 3)
                    .denyCacheImageMultipleSizesInMemory()
                    // 1MB=1048576
                    .memoryCacheSize(1048576 * 5)
                    .discCacheSize(104857600)
                    .build();
        }

        public Builder getInstance() {
            if (imageLoader == null) {
                imageLoader = ImageLoader.getInstance();
            }
            return this;
        }

        public Builder initWithDefaultConfig() {
            if (!imageLoader.isInited()) {
                imageLoader.init(ImageLoaderConfiguration.createDefault(context));
            }
            return this;
        }

        public Builder initForAdapter() {
            if (!imageLoader.isInited()) {
                if (config == null) {
                    createAdapterConfig();
                }
                imageLoader.init(config);
            }
            return this;
        }

        public ImageLoader build() {
            return imageLoader;
        }
    }


}
