package com.zkteco.zkfinger;

import android.graphics.Bitmap;

import com.zkteco.zkfinger.util.FingerListener;
import com.zkteco.zkfinger.util.FingerStatus;
import com.zkteco.zkfinger.util.FingerStatusType;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * ZkFingerPlugin
 */
public class ZkFingerPlugin implements MethodCallHandler, FingerListener {
    private static final String METHOD_FINGER_OPEN_CONNECTION =
            "openConnection";
    private static final String METHOD_FINGER_CLOSE_CONNECTION =
            "closeConnection";
    private static final String METHOD_FINGER_START_LISTEN =
            "startListen";
    private static final String METHOD_FINGER_STOP_LISTEN =
            "stopListen";
    private static final String METHOD_FINGER_ENROLL =
            "enroll";
    private static final String METHOD_FINGER_VERIFY =
            "verify";

    private static final String METHOD_FINGER_REGISTER =
            "register";
    private static final String METHOD_FINGER_CLEAR =
            "clear";
    private static final String METHOD_FINGER_IS__DEVICE_SUPPORTED =
            "isDeviceSupported";

    private static final String CHANNEL_FINGER_STATUS_CHANGE = "com.zkteco.zkfinger/status_change";
    private static final String CHANNEL_FINGER_IMAGE = "com.zkteco.zkfinger/finger_image";
    private static PublishSubject<FingerStatus> fingerStatusSubject = PublishSubject.create();
    private static PublishSubject<byte[]> fingerImageSubject = PublishSubject.create();
    private final Registrar registrar;
    private ZKFingerPrintHelper zkFingerPrintHelper;
    private Result result;

    private ZkFingerPlugin(Registrar registrar) {
        this.registrar = registrar;
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "zkfinger");
        initFingerStatusChangeListener(registrar);
        initFingerImageListener(registrar);

        final ZkFingerPlugin instance = new ZkFingerPlugin(registrar);
        channel.setMethodCallHandler(instance);
    }

    private static void initFingerStatusChangeListener(Registrar registrar) {
        final EventChannel statusChangeEventChannel = new EventChannel(registrar.messenger(), CHANNEL_FINGER_STATUS_CHANGE);
        statusChangeEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object o, final EventChannel.EventSink eventSink) {
                fingerStatusSubject.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<FingerStatus>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(FingerStatus status) {
                        HashMap<String, Object> statusMap = new HashMap<>();
                        statusMap.put("id", status.getId());
                        statusMap.put("message", status.getMessage());
                        statusMap.put("data", status.getData());
                        statusMap.put("fingerStatus", status.getFingerStatusType().ordinal());
                        eventSink.success(statusMap);
//                        eventSink.success(status);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }

            @Override
            public void onCancel(Object o) {

            }
        });
    }

    private static void initFingerImageListener(Registrar registrar) {
        final EventChannel imageEventChannel = new EventChannel(registrar.messenger(), CHANNEL_FINGER_IMAGE);
        imageEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object o, final EventChannel.EventSink eventSink) {
                fingerImageSubject.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<byte[]>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(byte[] imageBytes) {
                        eventSink.success(imageBytes);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }

            @Override
            public void onCancel(Object o) {

            }
        });
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        this.result = result;
        if (zkFingerPrintHelper == null)
            zkFingerPrintHelper = new ZKFingerPrintHelper(registrar.activity(), this);


        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            case METHOD_FINGER_OPEN_CONNECTION:
                openConnection(isLogEnabled(call));
                break;
            case METHOD_FINGER_CLOSE_CONNECTION:
                closeConnection();
                break;
            case METHOD_FINGER_START_LISTEN:
                startFingerListen(getUserId(call));
                break;
            case METHOD_FINGER_STOP_LISTEN:
                stopFingerListen();
                break;
            case METHOD_FINGER_ENROLL:
                enrollFinger(getUserId(call));
                break;
            case METHOD_FINGER_VERIFY:
                verifyFinger(getUserId(call));
                break;
            case METHOD_FINGER_REGISTER:
                registerFinger(getUserId(call), getFingerData(call));
                break;
            case METHOD_FINGER_CLEAR:
                clearFinger();
                break;
            case METHOD_FINGER_IS__DEVICE_SUPPORTED:
                isDeviceSupported();
                break;
            default:
                result.notImplemented();
        }
    }

    private String getUserId(MethodCall call) {
        return call.argument("id");
    }

    private boolean isLogEnabled(MethodCall call) {
        return call.argument("isLogEnabled");
    }

    private String getFingerData(MethodCall call) {
        return call.argument("data");
    }

    private void openConnection(boolean isLogEnabled) {
        zkFingerPrintHelper.openConnection(isLogEnabled);
        result.success(true);
    }

    private void closeConnection() {
        zkFingerPrintHelper.closeConnection();
        result.success(true);
    }


    private void startFingerListen(String userId) {
        zkFingerPrintHelper.startFingerSensor(userId);
        result.success(true);
    }

    private void stopFingerListen() {
        zkFingerPrintHelper.stopFingerSensor();
        result.success(true);
    }

    private void enrollFinger(String userId) {
        zkFingerPrintHelper.enrollFinger(userId);
        result.success(true);
    }

    private void verifyFinger(String userId) {
        zkFingerPrintHelper.verifyFinger(userId);
        result.success(true);
    }

    private void registerFinger(String userId, String fingerData) {
        zkFingerPrintHelper.register(userId, fingerData);
        result.success(true);
    }

    private void clearFinger() {
        zkFingerPrintHelper.clear();
        result.success(true);
    }

    private void isDeviceSupported() {
        boolean isSupported = zkFingerPrintHelper.isDeviceSupported();
        result.success(isSupported);
    }


    @Override
    public void onStatusChange(String message, FingerStatusType fingerStatusType, String id, String data) {
        fingerStatusSubject.onNext(new FingerStatus(message, fingerStatusType, id, data));
    }

    @Override
    public void onCaptureFinger(Bitmap fingerBitmap) {
        //b is the Bitmap

        //calculate how many bytes our image consists of.
//        int bytes = fingerBitmap.getByteCount();
//        //or we can calculate bytes this way. Use a different value than 4 if you don't use 32bit images.
//        //int bytes = b.getWidth()*b.getHeight()*4;
//        ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
//        fingerBitmap.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
//        byte[] array = buffer.array(); //Get the underlying array containing the data.

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        fingerBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        fingerBitmap.recycle();

        fingerImageSubject.onNext(byteArray);
    }

}


