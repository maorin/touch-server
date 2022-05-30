package touch.server;

import android.graphics.Rect;
import android.media.MediaCodecInfo;
import android.os.BatteryManager;
import android.os.Build;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public final class Server {

    private Server() {
        // not instantiable
    }

    

   private static void scrcpy(Options options) throws IOException {
        ///Ln.i("Device: " + Build.MANUFACTURER + " " + Build.MODEL + " (Android " + Build.VERSION.RELEASE + ")");
        final Device device = new Device(options);
        //List<CodecOption> codecOptions = options.getCodecOptions();

        Thread initThread = startInitThread(options);

        boolean tunnelForward = options.isTunnelForward();
        boolean control = options.getControl();
        boolean sendDummyByte = options.getSendDummyByte();

        try (DesktopConnection connection = DesktopConnection.open(tunnelForward, control, sendDummyByte)) {



            Thread controllerThread = null;
            Thread deviceMessageSenderThread = null;
            if (control) {
                //final Controller controller = new Controller(device, connection, options.getClipboardAutosync(), options.getPowerOn());

                // asynchronous
                //controllerThread = startController(controller);
                //deviceMessageSenderThread = startDeviceMessageSender(controller.getSender());


            }

 
      
        }
    }

    private static Thread startInitThread(final Options options) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //initAndCleanUp(options);
            }
        });
        thread.start();
        return thread;
    }
    /**
    private static Thread startController(final Controller controller) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    controller.control();
                } catch (IOException e) {
                    // this is expected on close
                    Ln.d("Controller stopped");
                }
            }
        });
        thread.start();
        return thread;
    }*/

    private static Thread startDeviceMessageSender(final DeviceMessageSender sender) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sender.loop();
                } catch (IOException | InterruptedException e) {
                    // this is expected on close
                    Ln.d("Device message sender stopped");
                }
            }
        });
        thread.start();
        return thread;
    }

    private static Options createOptions(String... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Missing client version");
        }

        String clientVersion = args[0];
        if (!clientVersion.equals(BuildConfig.VERSION_NAME)) {
            throw new IllegalArgumentException(
                    "The server version (" + BuildConfig.VERSION_NAME + ") does not match the client " + "(" + clientVersion + ")");
        }

        Options options = new Options();

        for (int i = 1; i < args.length; ++i) {
            String arg = args[i];
            int equalIndex = arg.indexOf('=');
            if (equalIndex == -1) {
                throw new IllegalArgumentException("Invalid key=value pair: \"" + arg + "\"");
            }
            String key = arg.substring(0, equalIndex);
            String value = arg.substring(equalIndex + 1);
            switch (key) {
                case "log_level":
                    Ln.Level level = Ln.Level.valueOf(value.toUpperCase(Locale.ENGLISH));
                    options.setLogLevel(level);
                    break;
                case "max_size":
                    int maxSize = Integer.parseInt(value) & ~7; // multiple of 8
                    options.setMaxSize(maxSize);
                    break;
                case "bit_rate":
                    int bitRate = Integer.parseInt(value);
                    options.setBitRate(bitRate);
                    break;
                case "max_fps":
                    int maxFps = Integer.parseInt(value);
                    options.setMaxFps(maxFps);
                    break;
                case "lock_video_orientation":
                    int lockVideoOrientation = Integer.parseInt(value);
                    options.setLockVideoOrientation(lockVideoOrientation);
                    break;
                case "tunnel_forward":
                    boolean tunnelForward = Boolean.parseBoolean(value);
                    options.setTunnelForward(tunnelForward);
                    break;
                case "control":
                    boolean control = Boolean.parseBoolean(value);
                    options.setControl(control);
                    break;
                case "display_id":
                    int displayId = Integer.parseInt(value);
                    options.setDisplayId(displayId);
                    break;
                case "show_touches":
                    boolean showTouches = Boolean.parseBoolean(value);
                    options.setShowTouches(showTouches);
                    break;
                case "stay_awake":
                    boolean stayAwake = Boolean.parseBoolean(value);
                    options.setStayAwake(stayAwake);
                    break;
                case "encoder_name":
                    if (!value.isEmpty()) {
                        options.setEncoderName(value);
                    }
                    break;
                case "power_off_on_close":
                    boolean powerOffScreenOnClose = Boolean.parseBoolean(value);
                    options.setPowerOffScreenOnClose(powerOffScreenOnClose);
                    break;
                case "clipboard_autosync":
                    boolean clipboardAutosync = Boolean.parseBoolean(value);
                    options.setClipboardAutosync(clipboardAutosync);
                    break;
                case "downsize_on_error":
                    boolean downsizeOnError = Boolean.parseBoolean(value);
                    options.setDownsizeOnError(downsizeOnError);
                    break;
                case "cleanup":
                    boolean cleanup = Boolean.parseBoolean(value);
                    options.setCleanup(cleanup);
                    break;
                case "power_on":
                    boolean powerOn = Boolean.parseBoolean(value);
                    options.setPowerOn(powerOn);
                    break;
                case "send_device_meta":
                    boolean sendDeviceMeta = Boolean.parseBoolean(value);
                    options.setSendDeviceMeta(sendDeviceMeta);
                    break;
                case "send_frame_meta":
                    boolean sendFrameMeta = Boolean.parseBoolean(value);
                    options.setSendFrameMeta(sendFrameMeta);
                    break;
                case "send_dummy_byte":
                    boolean sendDummyByte = Boolean.parseBoolean(value);
                    options.setSendDummyByte(sendDummyByte);
                    break;
                case "raw_video_stream":
                    boolean rawVideoStream = Boolean.parseBoolean(value);
                    if (rawVideoStream) {
                        options.setSendDeviceMeta(false);
                        options.setSendFrameMeta(false);
                        options.setSendDummyByte(false);
                    }
                    break;
                default:
                    Ln.w("Unknown server option: " + key);
                    break;
            }
        }

        return options;
    }



    public static void main(String... args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Ln.e("Exception on thread " + t, e);
                //suggestFix(e);
            }
        });

        Options options = createOptions(args);

        Ln.initLogLevel(options.getLogLevel());

        scrcpy(options);
    }
}
