package us.xingkong.streamsdk.network;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import us.xingkong.streamsdk.model.AppsResult;
import us.xingkong.streamsdk.model.Result;
import us.xingkong.streamsdk.model.StatusResult;

import static android.content.ContentValues.TAG;

/**
 * Created by 饶翰新 on 2017/12/20.
 * <p>
 * 本类模拟浏览器环境实现API对接
 */

public class Client {

    private Context context;
    /**
     * 默认API的URL接口
     */
    private static final String API_HOST_DEFAULT = "http://live.xingkong.us/?s=/index/user/";

    /**
     * 接口方法数组
     */
    private static final String API_PART[] = new String[]{"apps", "app", "login", "signin", "createapp", "appupdate", "user"};

    /**
     * 内部Handler
     */
    private Handler handler;

    /**
     * 内部缓存线程池
     */
    private ThreadPoolExecutor pool;

    /**
     * 存放Cookie
     */
    private Map<String, String> cookie;

    /**
     * 默认构造函数
     */
    public Client(Context context) {
        this.context = context;
        this.handler = new Handler();
        this.cookie = new HashMap<>();
        this.pool = (ThreadPoolExecutor) Executors.newCachedThreadPool();//初始化缓存线程池
        this.pool.setCorePoolSize(10);//设置核心池大小
        this.pool.setMaximumPoolSize(20);//设置最大池大小

    }

    /**
     * 登陆方法
     *
     * @param username 用户名
     * @param password 密码
     * @param listener 回调监听器
     */
    public void login(String username, String password, final ResultListener<Result> listener) {

        HttpRequest request = new HttpRequest(API_HOST_DEFAULT + API_PART[2], context);//构造请求对象
        request.post("username", username);//请求对象设置POST数据
        request.post("password", password);//请求对象设置POST数据
        request.setCookie(getCookieString());//请求对象设置Cookie

        //构造Http执行器
        HttpRunnable hr = new HttpRunnable(request) {
            @Override
            public void onDone(final String result, final Exception e) {
                Log.d(TAG, "onDone: this.getRequest().getCookie():" + this.getRequest().getCookie());
                setCookie(this.getRequest().getCookie());//每次执行完请求后都要将本次请求获得的Cookie保存。

                //本onDone方法环境尚处于非UI线程环境中,使用handler在UI线程触发listener的事件。
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        //本run方法已经处于UI线程，故可以触发onDone方法后，onDone方法的UI操作不会受影响
                        if (e != null) {
                            listener.onDone(null, e);
                        } else {
                            try {
                                listener.onDone(new Result(result), null);
                            } catch (Exception e1) {
                                listener.onDone(null, e1);
                            }
                        }

                    }

                });
            }
        };

        pool.execute(hr);//将Http执行器递给线程池
    }

    /**
     * 注册方法
     *
     * @param username 用户名
     * @param password 密码
     * @param nickname 昵称
     * @param listener 回调监听器
     */
    public void signin(String username, String password, String nickname, final ResultListener<Result> listener) {
        HttpRequest request = new HttpRequest(API_HOST_DEFAULT + API_PART[3], context);//构造请求对象
        request.post("username", username);//请求对象设置POST数据
        request.post("password", password);//请求对象设置POST数据
        request.post("nickname", nickname);//请求对象设置POST数据
        request.setCookie(getCookieString());//请求对象设置Cookie

        //构造Http执行器
        HttpRunnable hr = new HttpRunnable(request) {
            @Override
            public void onDone(final String result, final Exception e) {
                setCookie(this.getRequest().getCookie());//每次执行完请求后都要将本次请求获得的Cookie保存。

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (e != null) {
                            listener.onDone(null, e);
                        } else {
                            try {
                                listener.onDone(new Result(result), null);
                            } catch (Exception e1) {
                                listener.onDone(null, e1);
                            }
                        }
                    }
                });

            }
        };

        pool.execute(hr);
    }

    /**
     * 检查登陆状态
     *
     * @param listener
     */
    public void checkLoginStatus(final ResultListener<StatusResult> listener) {
        HttpRequest request = new HttpRequest(API_HOST_DEFAULT + API_PART[6], context);
        request.setCookie(getCookieString());
        HttpRunnable hr = new HttpRunnable(request) {
            @Override
            public void onDone(final String result, final Exception e) {
                setCookie(this.getRequest().getCookie());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (e != null) {
                            listener.onDone(null, e);
                        } else {
                            try {
                                listener.onDone(new StatusResult(result), null);
                            } catch (Exception e1) {
                                listener.onDone(null, e1);
                            }
                        }
                    }
                });
            }
        };
        pool.execute(hr);
    }

    /**
     * 创建直播的方法
     *
     * @param appname
     * @param apptitle
     * @param maintext
     * @param listener
     */
    public void createApp(String appname, String apptitle, String maintext, final ResultListener<Result> listener) {
        HttpRequest request = new HttpRequest(API_HOST_DEFAULT + API_PART[4], context);
        request.post("appname", appname);//请求对象设置POST数据
        request.post("apptitle", apptitle);//请求对象设置POST数据
        request.post("maintext", maintext);//请求对象设置POST数据
        request.setCookie(getCookieString());
        HttpRunnable hr = new HttpRunnable(request) {
            @Override
            public void onDone(final String result, final Exception e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (e != null) {
                            listener.onDone(null, e);
                        } else {
                            try {
                                listener.onDone(new StatusResult(result), null);
                            } catch (Exception e1) {
                                listener.onDone(null, e1);
                            }
                        }
                    }
                });
            }
        };
        pool.execute(hr);
    }

    /**
     * 获取某个用户信息
     *
     * @param username
     * @param listener
     */
    public void getUser(String username, final ResultListener<StatusResult> listener) {
        HttpRequest request = new HttpRequest(API_HOST_DEFAULT + API_PART[6] + username, context);
        request.setCookie(getCookieString());
        HttpRunnable hr = new HttpRunnable(request) {
            @Override
            public void onDone(final String result, final Exception e) {
                setCookie(this.getRequest().getCookie());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (e != null) {
                            listener.onDone(null, e);
                        } else {
                            try {
                                listener.onDone(new StatusResult(result), null);
                            } catch (Exception e1) {
                                listener.onDone(null, e1);
                            }
                        }
                    }
                });
            }
        };
        pool.execute(hr);
    }

    /**
     * 获取所有直播信息
     *
     * @param listener 回调监听器
     */
    public void getApps(final ResultListener<AppsResult> listener) {
        HttpRequest request = new HttpRequest(API_HOST_DEFAULT + API_PART[0], context);
        request.setCookie(getCookieString());
        HttpRunnable hr = new HttpRunnable(request) {
            @Override
            public void onDone(final String result, final Exception e) {
                setCookie(this.getRequest().getCookie());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (e != null) {
                            listener.onDone(null, e);
                        } else {
                            try {
                                listener.onDone(new AppsResult(result), null);
                            } catch (Exception e1) {
                                listener.onDone(null, e1);
                            }
                        }

                    }
                });
            }
        };

        pool.execute(hr);
    }

    /**
     * 获取当前登陆用户创建的所有直播信息
     *
     * @param listener
     */
    public void getUserApps(final ResultListener<AppsResult> listener) {
        HttpRequest request = new HttpRequest(API_HOST_DEFAULT + API_PART[0] + "&q=1", context);
        request.setCookie(getCookieString());
        HttpRunnable hr = new HttpRunnable(request) {
            @Override
            public void onDone(final String result, final Exception e) {
                setCookie(this.getRequest().getCookie());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (e != null) {
                            listener.onDone(null, e);
                        } else {
                            try {
                                listener.onDone(new AppsResult(result), null);
                            } catch (Exception e1) {
                                listener.onDone(null, e1);
                            }
                        }

                    }
                });
            }
        };

        pool.execute(hr);
    }

    /**
     * 获取单个直播信息
     *
     * @param appname  直播唯一标识名
     * @param listener 回调监听器
     */
    public void getApp(String appname, final ResultListener<AppsResult> listener) {
        HttpRequest request = new HttpRequest(API_HOST_DEFAULT + API_PART[1], context);
        request.post("app", appname);
        request.setCookie(getCookieString());
        HttpRunnable hr = new HttpRunnable(request) {
            @Override
            public void onDone(final String result, final Exception e) {
                setCookie(this.getRequest().getCookie());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (e != null) {
                            listener.onDone(null, e);
                        } else {
                            try {
                                listener.onDone(new AppsResult(result), null);
                            } catch (Exception e1) {
                                listener.onDone(null, e1);
                            }
                        }

                    }
                });
            }
        };

        pool.execute(hr);
    }

    /**
     * 将Http头部的Set-Cookie数据存进本地Cookie
     *
     * @param cookieString Cookie字符串
     */
    private void setCookie(String cookieString) {
        if (cookieString == null)
            return;
        Log.d(TAG, "setCookie: " + cookieString);
        String[] strs = cookieString.split(";");
        for (String str : strs) {
            String[] kv = str.split("=", 2);
            if (kv.length == 2)
                this.cookie.put(kv[0].trim(), kv[1].trim());
        }
    }

    /**
     * 存入Cookie
     *
     * @param key   Cookie Key
     * @param value Cookie Value
     */
    public void setCookie(String key, String value) {
        cookie.put(key, value);
    }

    /**
     * 将Cookie转成Http头部的Cookie字符串
     *
     * @return Cookie字符串
     */
    private String getCookieString() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> es : cookie.entrySet()) {
            if (result.length() > 0)
                result.append("; ");
            result.append(es.getKey()).append("=").append(es.getValue());
        }
        return result.toString();
    }

    /**
     * 获取Cookie的对象
     *
     * @return
     */
    public Map<String, String> getCookie() {
        return cookie;
    }

    /**
     * 获取Cookie某个值
     *
     * @param key Cookie Key
     * @return Cookie Value
     */
    public String getCookie(String key) {
        return this.cookie.get(key);
    }

}
