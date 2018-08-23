package com.cyl.musiclake.data.download

import android.text.TextUtils
import android.util.SparseArray
import com.cyl.musiclake.data.DownloadLoader
import com.cyl.musiclake.ui.music.download.DownloadManagerFragment
import com.cyl.musiclake.ui.music.download.TaskItemAdapter
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadConnectListener
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.model.FileDownloadStatus
import java.lang.ref.WeakReference


/**
 * Created by yonglong on 2018/1/23.
 * 下载任务管理
 */
object TasksManager {
    private var modelList = DownloadLoader.getDownloadingList()

    private val taskSparseArray = SparseArray<BaseDownloadTask>()

    private var listener: FileDownloadConnectListener? = null

    val isReady by lazy { FileDownloader.getImpl().isServiceConnected }

    fun addTaskForViewHolder(task: BaseDownloadTask) {
        taskSparseArray.put(task.id, task)
    }

    fun removeTaskForViewHolder(id: Int) {
        taskSparseArray.remove(id)
    }

    fun updateViewHolder(id: Int, holder: TaskItemAdapter.TaskItemViewHolder) {
        if (taskSparseArray.get(id) != null) {
            taskSparseArray.get(id).tag = holder
        }
    }

    fun releaseTask() {
        taskSparseArray.clear()
    }

    /**
     * 注册监听
     */
    private fun registerServiceConnectionListener(activityWeakReference: WeakReference<DownloadManagerFragment>?) {
        if (listener != null) {
            FileDownloader.getImpl().removeServiceConnectListener(listener)
        }

        listener = object : FileDownloadConnectListener() {

            override fun connected() {
                if (activityWeakReference?.get() == null) {
                    return
                }
                activityWeakReference.get()?.postNotifyDataChanged()
            }

            override fun disconnected() {
                if (activityWeakReference?.get() == null) {
                    return
                }
                activityWeakReference.get()?.postNotifyDataChanged()
            }
        }

        FileDownloader.getImpl().addServiceConnectListener(listener)
    }

    private fun unregisterServiceConnectionListener() {
        FileDownloader.getImpl().removeServiceConnectListener(listener)
        listener = null
    }

    fun onCreate(activityWeakReference: WeakReference<DownloadManagerFragment>) {
        if (!FileDownloader.getImpl().isServiceConnected) {
            FileDownloader.getImpl().bindService()
            registerServiceConnectionListener(activityWeakReference)
        }
    }

    fun onDestroy() {
        unregisterServiceConnectionListener()
        releaseTask()
    }

    /**
     * 根据位置获取
     */
    operator fun get(position: Int): TasksManagerModel {
        return modelList[position]
    }

    /**
     * 根据model id获取对象
     */
    private fun getById(id: Int): TasksManagerModel? {
        for (model in modelList) {
            if (model.id == id) {
                return model
            }
        }
        return null
    }

    /**
     * @param status Download Status
     * @return has already downloaded
     * @see FileDownloadStatus
     */
    fun isDownloaded(status: Int): Boolean {
        return status == FileDownloadStatus.completed.toInt()
    }

    fun getStatus(id: Int, path: String): Int {
        return FileDownloader.getImpl().getStatus(id, path).toInt()
    }

    fun getTotal(id: Int): Long {
        return FileDownloader.getImpl().getTotal(id)
    }

    fun getSoFar(id: Int): Long {
        return FileDownloader.getImpl().getSoFar(id)
    }


    fun getModelList(): List<TasksManagerModel> {
        return modelList
    }


    fun finishTask(id: Int) {
        DownloadLoader.updateTask(id)
        modelList = DownloadLoader.getDownloadingList()
    }

    fun addTask(id: Int, mid: String, name: String, url: String, path: String): TasksManagerModel? {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(path)) {
            return null
        }

        val model = getById(id)
        if (model != null) {
            return model
        }
        val newModel = DownloadLoader.addTask(mid, name, url, path)
        if (newModel != null) {
            modelList.add(newModel)
        }
        return newModel
    }

}
