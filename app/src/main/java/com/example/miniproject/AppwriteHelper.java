package com.example.miniproject;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.Map;

import io.appwrite.Client;
import io.appwrite.ID;
import io.appwrite.coroutines.Callback;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.Session;
import io.appwrite.models.User;
import io.appwrite.services.Account;

public class AppwriteHelper {
    private static AppwriteHelper instance;
    private Client client;
    private Account account;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    private AppwriteHelper(Context context) {
//        client = new Client(context, "https://syd.cloud.appwrite.io/v1")
//                .setProject("683087d10014a6af0d7d");

        client = new Client(context, "https://fra.cloud.appwrite.io/v1")
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
        // First check if there's already an active session
        account.getSession("current", new CoroutineCallback<>(new Callback<Session>() {
            @Override
            public void onComplete(Session currentSession, Throwable sessionError) {
                if (currentSession != null) {
                    // Session already exists, delete it first
                    account.deleteSession("current", new CoroutineCallback<>(new Callback<Object>() {
                        @Override
                        public void onComplete(Object deleteResult, Throwable deleteError) {
                            // Whether deletion succeeds or fails, try to create new session
                            createNewSession(email, password, callback);
                        }
                    }));
                } else {
                    // No active session, proceed with login
                    createNewSession(email, password, callback);
                }
            }
        }));
    }

    private void createNewSession(String email, String password, final AuthCallback<Session> callback) {
        account.createEmailPasswordSession(email, password, new CoroutineCallback<>(new Callback<>() {
            @Override
            public void onComplete(Session result, Throwable error) {
                mainHandler.post(() -> {
                    if (error != null) {
                        callback.onError((Exception) error);
                    } else if (result != null) {
                        callback.onSuccess(result);
                    }
                });
            }
        }));
    }

    public void register(String email, String password, final AuthCallback<User<Map<String, Object>>> callback) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            mainHandler.post(() -> callback.onError(new IllegalArgumentException("Email and password cannot be empty")));
            return;
        }
        try {
            account.create(
                    ID.Companion.unique(7),
                    email,
                    password,
                    "", // Empty string instead of null
                    new CoroutineCallback<>(new Callback<User<Map<String, Object>>>() {
                        @Override
                        public void onComplete(User<Map<String, Object>> result, Throwable error) {
                            if (error != null) {
                                mainHandler.post(() -> callback.onError((Exception) error));
                            } else if (result != null) {
                                mainHandler.post(() -> callback.onSuccess(result));
                            }
                        }
                    })
            );
        } catch (Exception e) {
            mainHandler.post(() -> callback.onError(e));
        }
    }

    // Đăng ký với name, email, password
    public void registerWithName(String name, String email, String password, final AuthCallback<User<Map<String, Object>>> callback) {
        if (name == null || name.isEmpty() || email == null || email.isEmpty() || password == null || password.isEmpty()) {
            mainHandler.post(() -> callback.onError(new IllegalArgumentException("Name, email and password cannot be empty")));
            return;
        }
        try {
            account.create(
                    ID.Companion.unique(7),
                    email,
                    password,
                    name,
                    new CoroutineCallback<>(new Callback<User<Map<String, Object>>>() {
                        @Override
                        public void onComplete(User<Map<String, Object>> result, Throwable error) {
                            if (error != null) {
                                mainHandler.post(() -> callback.onError((Exception) error));
                            } else if (result != null) {
                                mainHandler.post(() -> callback.onSuccess(result));
                            }
                        }
                    })
            );
        } catch (Exception e) {
            mainHandler.post(() -> callback.onError(e));
        }
    }

    public void logout(final AuthCallback<Object> callback) {
        account.getSession("current", new CoroutineCallback<>(new Callback<Session>() {
            @Override
            public void onComplete(Session result, Throwable error) {
                if (error != null) {
                    mainHandler.post(() -> callback.onError((Exception) error));
                } else if (result != null) {
                    account.deleteSession("current", new CoroutineCallback<>(new Callback<Object>() {
                        @Override
                        public void onComplete(Object result, Throwable error) {
                            if (error != null) {
                                mainHandler.post(() -> callback.onError((Exception) error));
                            } else {
                                mainHandler.post(() -> callback.onSuccess(result));
                            }
                        }
                    }));
                } else {
                    mainHandler.post(() -> callback.onError(new Exception("No active session found")));
                }
            }
        }));
    }

    // Check if user is currently logged in
    public void getCurrentSession(final AuthCallback<Session> callback) {
        account.getSession("current", new CoroutineCallback<>(new Callback<Session>() {
            @Override
            public void onComplete(Session result, Throwable error) {
                mainHandler.post(() -> {
                    if (error != null) {
                        callback.onError((Exception) error);
                    } else if (result != null) {
                        callback.onSuccess(result);
                    } else {
                        callback.onError(new Exception("No active session"));
                    }
                });
            }
        }));
    }

    // Get current user information
    public void getCurrentUser(final AuthCallback<User<Map<String, Object>>> callback) {
        try {
            account.get(new CoroutineCallback<>(new Callback<User<Map<String, Object>>>() {
                @Override
                public void onComplete(User<Map<String, Object>> result, Throwable error) {
                    mainHandler.post(() -> {
                        if (error != null) {
                            callback.onError((Exception) error);
                        } else if (result != null) {
                            callback.onSuccess(result);
                        }
                    });
                }
            }));
        } catch (Exception e) {
            mainHandler.post(() -> callback.onError(e));
        }
    }
}
