package cn.dsttl3.hwlogin.slice;

import cn.dsttl3.hwlogin.ResourceTable;
import com.huawei.hms.accountsdk.exception.ApiException;
import com.huawei.hms.accountsdk.support.account.AccountAuthManager;
import com.huawei.hms.accountsdk.support.account.request.AccountAuthParams;
import com.huawei.hms.accountsdk.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.accountsdk.support.account.result.AuthAccount;
import com.huawei.hms.accountsdk.support.account.service.AccountAuthService;
import com.huawei.hms.accountsdk.support.account.tasks.OnFailureListener;
import com.huawei.hms.accountsdk.support.account.tasks.OnSuccessListener;
import com.huawei.hms.accountsdk.support.account.tasks.Task;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.Text;

public class MainAbilitySlice extends AbilitySlice {

    Text text;
    Button button;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        text = findComponentById(ResourceTable.Id_text_helloworld);
        button = findComponentById(ResourceTable.Id_btn_login);

        button.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                huaweiIdSignIn();
            }
        });

    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }


    private void huaweiIdSignIn() {
        AccountAuthService accountAuthService;
        // 1、配置登录请求参数AccountAuthParams，包括请求用户的id(openid、unionid)、email、profile(昵称、头像)等;
        // 2、DEFAULT_AUTH_REQUEST_PARAM默认包含了id和profile（昵称、头像）的请求;
        // 3、如需要再获取用户邮箱，需要setEmail();
        // 4、通过setAuthorizationCode()来选择使用code模式，最终所有请求的用户信息都可以调服务器的接口获取；
        AccountAuthParams accountAuthParams = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setAuthorizationCode()
                .createParams();
        try {
            accountAuthService = AccountAuthManager.getService(accountAuthParams);
        } catch (ApiException e) {
            // 处理初始化登录授权服务失败，status code标识了失败的原因，请参见API参考中的错误码了解详细错误原因
            e.getStatusCode();
            return;
        }
        // 调用静默登录接口。
        // 如果华为系统帐号已经登录，并且已经授权，会登录成功；
        // 否则静默登录失败，需要在失败监听中，显式调用前台登录授权接口，完成登录授权。
        Task<AuthAccount> task = accountAuthService.silentSignIn();
        // 添加静默登录成功处理监听
        task.addOnSuccessListener(new OnSuccessListener<AuthAccount>() {
            @Override
            public void onSuccess(AuthAccount authAccount) {
                // 静默登录成功后，根据结果中获取到的帐号基本信息更新UI

                text.setText(authAccount.getDisplayName()+"\n登录成功。");

            }
        });
        // 添加静默登录失败监听
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    // 静默登录失败，显式调用前台登录授权接口，完成登录授权。
                    Task task = accountAuthService.signIn();
                    if (task == null) {
                        return;
                    }
                    task.addOnSuccessListener(new OnSuccessListener<AuthAccount>() {
                        @Override
                        public void onSuccess(AuthAccount account) {
                            // 从account中获取授权码code
                            account.getAuthorizationCode();

                            text.setText("授权code：" + account.getAuthorizationCode());

                        }
                    });
                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            if (e instanceof ApiException) {
                                ApiException apiException = (ApiException) e;
                                // 登录失败，status code标识了失败的原因，请参见API参考中的错误码了解详细错误原因
                                apiException.getStatusCode();

                                text.setText("登录失败 code：" + apiException.getStatusCode());
                            }
                        }
                    });
                }
            }
        });
    }

}
