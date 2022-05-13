package cn.dsttl3.hwlogin.slice;

import cn.dsttl3.hwlogin.ResourceTable;
import cn.dsttl3.hwlogin.utils.OkUtil;
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
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class MainAbilitySlice extends AbilitySlice {

    Text text;
    Button buttonLogin;
    Button btnSignOut;
    Button btnCancelAuthorization;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        text = findComponentById(ResourceTable.Id_text_helloworld);
        buttonLogin = findComponentById(ResourceTable.Id_btn_login);
        btnSignOut = findComponentById(ResourceTable.Id_btn_sign_out);
        btnCancelAuthorization = findComponentById(ResourceTable.Id_btn_cancelAuthorization);

        btnSignOut.setVisibility(Component.VERTICAL);
        btnCancelAuthorization.setVisibility(Component.VERTICAL);


        buttonLogin.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                huaweiIdSignIn();
            }
        });

        btnSignOut.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                signOut();
            }
        });

        btnCancelAuthorization.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                cancelAuthorization();
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

                text.setText(authAccount.getDisplayName() + "\n登录成功。\nAuthorizationCode = "
                        + authAccount.getAuthorizationCode());
                // 更新按钮显示
                buttonLogin.setVisibility(Component.VERTICAL);
                btnSignOut.setVisibility(Component.VISIBLE);
                btnCancelAuthorization.setVisibility(Component.VISIBLE);

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
                            String code = account.getAuthorizationCode();
                            //获取凭证Access Token  （获取Access Token操作需在服务器端操作，这里只是为了演示操作过程）
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    RequestBody requestBody = new FormBody.Builder()
                                            .add("grant_type", "authorization_code")
                                            .add("client_id", "106205123")
                                            .add("client_secret", "fe0e68f057eb4c6e5a2071ebec4a0beff7642c731140bc8ebccdb1238383e85c")
                                            .add("code", code)
                                            .add("redirect_uri", "https://api.dsttl3.cn/hwlogin")
                                            .build();
                                    String json = OkUtil.post("https://oauth-login.cloud.huawei.com/oauth2/v3/token", requestBody);
                                    System.out.println(json);
                                    getUITaskDispatcher().asyncDispatch(new Runnable() {
                                        @Override
                                        public void run() {
                                            text.setText(account.getDisplayName() + "\n登录成功。" + json);
                                            // 更新按钮显示
                                            buttonLogin.setVisibility(Component.VERTICAL);
                                            btnSignOut.setVisibility(Component.VISIBLE);
                                            btnCancelAuthorization.setVisibility(Component.VISIBLE);
                                        }
                                    });
                                }
                            }).start();
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

    private void signOut() {
        AccountAuthService accountAuthService;
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
        Task<Void> signOutTask = accountAuthService.signOut();
        signOutTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
//                Log.i(TAG, "signOut Success");
                text.setText("账号退出成功");
                // // 更新按钮显示
                buttonLogin.setVisibility(Component.VISIBLE);
                btnSignOut.setVisibility(Component.VERTICAL);
                btnCancelAuthorization.setVisibility(Component.VERTICAL);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
//                Log.i(TAG, "signOut fail");
                text.setText("账号退出失败：" + e.getLocalizedMessage());
            }
        });
    }

    private void cancelAuthorization() {
        AccountAuthService accountAuthService;
        AccountAuthParams accountAuthParams = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setEmail()
                .createParams();
        try {
            accountAuthService = AccountAuthManager.getService(accountAuthParams);
        } catch (ApiException e) {
            // 处理初始化登录授权服务失败，status code标识了失败的原因，请参见API参考中的错误码了解详细错误原因
            e.getStatusCode();
            return;
        }
        // 调用取消授权接口
        Task<Void> task = accountAuthService.cancelAuthorization();
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void v) {
                // 取消授权成功
                text.setText("取消授权成功");
                // 更新按钮显示
                buttonLogin.setVisibility(Component.VISIBLE);
                btnSignOut.setVisibility(Component.VERTICAL);
                btnCancelAuthorization.setVisibility(Component.VERTICAL);
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // 取消授权失败
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    // 华为帐号取消授权失败，status code标识了失败的原因，请参见API参考中的错误码了解详细错误原因
                    apiException.getStatusCode();
                    text.setText("取消授权失败：" + apiException.getStatusCode());
                }
            }
        });
    }

}
