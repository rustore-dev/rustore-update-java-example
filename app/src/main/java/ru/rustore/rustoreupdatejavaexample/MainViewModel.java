package ru.rustore.rustoreupdatejavaexample;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import ru.rustore.sdk.appupdate.listener.InstallStateUpdateListener;
import ru.rustore.sdk.appupdate.manager.RuStoreAppUpdateManager;
import ru.rustore.sdk.appupdate.manager.factory.RuStoreAppUpdateManagerFactory;
import ru.rustore.sdk.appupdate.model.AppUpdateOptions;
import ru.rustore.sdk.appupdate.model.AppUpdateType;
import ru.rustore.sdk.appupdate.model.InstallStatus;
import ru.rustore.sdk.appupdate.model.UpdateAvailability;


public class MainViewModel extends ViewModel {

    private static final String TAG = "MainViewModel";

    private RuStoreAppUpdateManager ruStoreAppUpdateManager;

    private final MutableLiveData<Event> events = new MutableLiveData<>();

    public LiveData<Event> getEvents() {
        return events;
    }

    private void emitEvent() {
        events.postValue(Event.UpdateCompleted);
    }

    private final InstallStateUpdateListener installStateUpdateListener = installState -> {
        switch (installState.getInstallStatus()) {
            case InstallStatus.DOWNLOADED:
                emitEvent();
                break;
            case InstallStatus.DOWNLOADING:
                long totalBytes = installState.getTotalBytesToDownload();
                long bytesDownloaded = installState.getBytesDownloaded();
                // Здесь можно отобразить прогресс скачивания
                break;
            case InstallStatus.FAILED:
                Log.e(TAG, "Downloading error");
                break;
        }
    };

    @Override
    protected void onCleared() {
        super.onCleared();
        ruStoreAppUpdateManager.unregisterListener(installStateUpdateListener);
    }

    public void init(Context context) {
        ruStoreAppUpdateManager = RuStoreAppUpdateManagerFactory.INSTANCE.create(context);
        ruStoreAppUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {
                    if (appUpdateInfo.getUpdateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        ruStoreAppUpdateManager.registerListener(installStateUpdateListener);
                        ruStoreAppUpdateManager
                                .startUpdateFlow(appUpdateInfo, new AppUpdateOptions.Builder().build())
                                .addOnSuccessListener(resultCode -> {
                                    if (resultCode == Activity.RESULT_CANCELED) {
                                        // Пользователь отказался от скачивания
                                    }
                                })
                                .addOnFailureListener(throwable ->
                                        Log.e(TAG, "startUpdateFlow error", throwable)
                                );
                    }
                })
                .addOnFailureListener(throwable ->
                        Log.e(TAG, "getAppUpdateInfo error", throwable)
                );
    }

    public void completeUpdateRequested() {
        int type = AppUpdateType.SILENT;
        ruStoreAppUpdateManager.completeUpdate(new AppUpdateOptions.Builder().appUpdateType(type).build())
                .addOnFailureListener(throwable -> {
                    Log.d("RuStoreUpdate", "Throwable: " + throwable);
                });
    }

    public enum Event {
        UpdateCompleted
    }
}
