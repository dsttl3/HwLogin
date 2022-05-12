package cn.dsttl3.hwlogin;

import com.huawei.hms.accountsdk.exception.ApiException;
import com.huawei.hms.accountsdk.support.account.AccountAuthManager;
import com.huawei.hms.accountsdk.support.account.tasks.OnFailureListener;
import com.huawei.hms.accountsdk.support.account.tasks.OnSuccessListener;
import com.huawei.hms.accountsdk.support.account.tasks.Task;
import ohos.aafwk.ability.AbilityPackage;

public class MyApplication extends AbilityPackage {
    @Override
    public void onInitialize() {
        super.onInitialize();
        // 调用示例initHuaweiAccountSDK方法，在HarmonyOS应用初始化方法onInitialize中进行华为帐号SDK初始化
        initHuaweiAccountSDK();
    }

    private void initHuaweiAccountSDK() {
        Task<Void> task;
        try {
            // 调用AccountAuthManager.init方法初始化
            task = AccountAuthManager.init(this);
        } catch (ApiException apiException) {
            apiException.getStatusCode();
            return;
        }
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void v) {
                //初始化成功
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // SDK初始化失败
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    // SDK初始化失败，status code标识了失败的原因，请参见API参考中的错误码了解详细错误原因
                    apiException.getStatusCode();
                }
            }
        });
    }
}
