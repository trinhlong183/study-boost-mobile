package com.example.miniproject;

import android.content.Context;

import java.util.Map;

import io.appwrite.Client;
import io.appwrite.ID;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.models.Session;
import io.appwrite.models.User;
import io.appwrite.services.Account;

public class AppwriteHelper {
    private static AppwriteHelper instance;
    private Client client;
    private Account account;

    private AppwriteHelper(Context context) {
        client = new Client(context)
                .setEndpoint("https://Frankfurt.cloud.appwrite.io/v1")
                .setProject("684459d500016ad059df");

        account = new Account(client);
    }

    public static synchronized AppwriteHelper getInstance(Context context) {
        if (instance == null) {
            instance = new AppwriteHelper(context.getApplicationContext());
        }
        return instance;
    }

    public interface AuthCallback<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }

    public void login(String email, String password, final AuthCallback<Session> callback) {
        account.createEmailPasswordSession(email, password, new CoroutineCallback<>(result -> {
            callback.onSuccess(result);
            return null;
        }, error -> {
            callback.onError(error);
            return null;
        }));
    }

    public void register(String email, String password, final AuthCallback<User<Map<String, Object>>> callback) {
        account.create(
                ID.unique(),
                email,
                password,
                null,
                new CoroutineCallback<>(result -> {
                    callback.onSuccess(result);
                    return null;
                }, error -> {
                    callback.onError(error);
                    return null;
                })
        );
    }

    public void logout(final AuthCallback<Object> callback) {
        account.deleteSession("current", new CoroutineCallback<>(result -> {
            callback.onSuccess(result);
            return null;
        }, error -> {
            callback.onError(error);
            return null;
        }));
    }
}
