package com.kenz.almuslim.data.services;

import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.kenz.almuslim.data.utils.SharedPref;


public class GifWallpaperService extends WallpaperService {
    public static final String WALLPAPER_KEY = "set_wallpaper";

    public Engine onCreateEngine() {
        try {
            File file = new File(SharedPref.getSharedPref(getApplicationContext()).read(WALLPAPER_KEY));
            final int readLimit = 16 * 1024;
            InputStream mInputStream = new BufferedInputStream(new FileInputStream(file), readLimit);
            mInputStream.mark(readLimit);
            return new GIFWallpaperEngine(Movie.decodeStream(mInputStream));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private class GIFWallpaperEngine extends Engine {
        private SurfaceHolder holder;
        private boolean visible;
        private final Handler handler;
        private final Movie movie;
        private float scaleRatio;
        private float y;
        private float x;

        private GIFWallpaperEngine(Movie movie) {
            this.movie = movie;
            handler = new Handler();
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            this.holder = surfaceHolder;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            handler.removeCallbacks(drawGIF);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            if ((float) width / movie.width() > (float) height / movie.height()) {
                scaleRatio = (float) width / movie.width();
            } else {
                scaleRatio = (float) height / movie.height();
            }
            this.x = (width - (movie.width() * scaleRatio)) / 2;
            this.y = (height - (movie.height() * scaleRatio)) / 2;
            x = x / scaleRatio;
            y = y / scaleRatio;
        }


        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
        }

        private Runnable drawGIF = new Runnable() {
            public void run() {
                draw();
            }
        };

        private void draw() {
            if (visible) {
                Canvas canvas = holder.lockCanvas();
                canvas.save();
                canvas.scale(scaleRatio, scaleRatio);
                movie.draw(canvas, x, y);
                canvas.restore();
                holder.unlockCanvasAndPost(canvas);
                movie.setTime((int) (System.currentTimeMillis() % movie.duration()));
                handler.removeCallbacks(drawGIF);
                handler.postDelayed(drawGIF, 0);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                handler.post(drawGIF);
            } else {
                handler.removeCallbacks(drawGIF);
            }
        }

    }
}
