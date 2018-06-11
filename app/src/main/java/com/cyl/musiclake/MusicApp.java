package com.cyl.musiclake;

import android.app.Application;
import android.content.Context;

import com.cyl.musiclake.bean.Playlist;
import com.cyl.musiclake.bean.SearchHistoryBean;
import com.cyl.musiclake.common.Constants;
import com.cyl.musiclake.di.component.ApplicationComponent;
import com.cyl.musiclake.di.component.DaggerApplicationComponent;
import com.cyl.musiclake.di.module.ApplicationModule;
import com.cyl.musiclake.player.PlayManager;
import com.cyl.musiclake.utils.LogUtil;
import com.cyl.musiclake.utils.SPUtils;
import com.cyl.musiclake.utils.UpdateUtils;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;
import com.liulishuo.filedownloader.util.FileDownloadLog;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.tauth.Tencent;

import org.litepal.LitePal;

import java.net.Proxy;

import static com.cyl.musiclake.common.Constants.PLAYLIST_QUEUE_ID;

public class MusicApp extends Application {
    private static MusicApp sInstance;
    private PlayManager.ServiceToken mToken;
    public static Context mContext;
    private Tencent mTencent;

    private ApplicationComponent mApplicationComponent;

    public static synchronized MusicApp getInstance() {
        return sInstance;
    }

    public static Context getAppContext() {
        return sInstance.getApplicationContext();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        mContext = this;
        initApplicationComponent();
        LitePal.initialize(this);
        UpdateUtils.init(this);
        SPUtils.init(this);
        mTencent = Tencent.createInstance(Constants.APP_ID, this);
        initBugly();
        initDB();
        initFileDownload();
    }

    /**
     * 初始化文件下载
     */
    private void initFileDownload() {
        FileDownloadLog.NEED_LOG = true;
        FileDownloader.setupOnApplicationOnCreate(this)
                .connectionCreator(new FileDownloadUrlConnection
                        .Creator(new FileDownloadUrlConnection.Configuration()
                        .connectTimeout(15_000) // set connection timeout.
                        .readTimeout(15_000) // set read timeout.
                        .proxy(Proxy.NO_PROXY) // set proxy
                ))
                .commit();
    }

    /**
     * 初始化bugly
     */
    private void initBugly() {
        Bugly.init(getApplicationContext(), Constants.BUG_APP_ID, true);
        Beta.checkUpgrade(false, false);
    }


    /**
     * 初始化ApplicationComponent
     */
    private void initApplicationComponent() {
        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    private void initDB() {
        Playlist playlist = new Playlist(String.valueOf(PLAYLIST_QUEUE_ID), getString(R.string.playlist_queue));
        if (playlist.save()) {
            LogUtil.d("存储成功");
        } else {
            LogUtil.d("失败");
        }
    }

    public ApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }

}
