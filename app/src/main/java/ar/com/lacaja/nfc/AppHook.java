package ar.com.lacaja.nfc;

import android.Manifest;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.RemoteException;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * Created by Boykier on 04/01/2017.
 */
public class AppHook extends Application implements BootstrapNotifier, RangeNotifier {
    private static final String TAG = "AppHook";
    public static final long BACKGROUND_SCAN_PERIOD = 10000l;
    private RegionBootstrap regionBootstrap;
    private BeaconManager beaconManager;
    public static final int ID=12321;
    public int i=0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "App started up");

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Granted");
            hookBeacon();
        }

    }

    public void hookBeacon() {

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.setBackgroundScanPeriod(BACKGROUND_SCAN_PERIOD);

//        Set<MonitorNotifier> noti = beaconManager.getMonitoringNotifiers();
//        Log.i(TAG, noti.toString());

        Region region = new Region("Welcome", null, null, null);

        regionBootstrap = new RegionBootstrap(this, region);
        Log.i(TAG, "Enter Enabled " + region.getUniqueId());

        long[] pattern = {0, 100, 500, 100};
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(pattern, -1);

    }

    @Override
    public void didEnterRegion(Region region) {
        Log.i(TAG, "Enter Region: " + region.getUniqueId());
        SharedPreferences sp=getSharedPreferences(MainActivity.PREFS_NAME, 0);
        long lastVisit=sp.getLong("lastVisit", 0);
        long now=new Date().getTime();

        Log.i(TAG, "Last=" + (now-lastVisit));

        if (now-lastVisit >= MainActivity.SECONDS_BETWEEN_VISITS * 1000) {

            regionBootstrap.disable();
            regionBootstrap = new RegionBootstrap(this, region);
            Log.i(TAG, "Disabled");

//            beaconManager.removeAllRangeNotifiers();
            beaconManager.addRangeNotifier(new RangeNotifier() {
                @Override
                public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                    if (beacons.size() > 0) {
                        Log.i(TAG, "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away.");
                    }
                }
            });

            try {
                beaconManager.startRangingBeaconsInRegion(new Region("com.hb.range", null, null, null));
            } catch (RemoteException e) {    }

            SharedPreferences.Editor editor = sp.edit();
            editor.putLong("lastVisit", now);
            editor.commit();


            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int notifyID = 1;
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.logocaja)
                            .setContentTitle("Bienvenido a La Caja")
                            .setContentText("Para obtener un turno presione aqui")
                            .setAutoCancel(true)
                            .setSound(soundUri)
                            .setVibrate(new long[]{1000, 1000});
            Intent resultIntent = new Intent(this, WelcomeActivity.class);
            Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(WelcomeActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            mNotificationManager.notify(notifyID, mBuilder.build());
        }
    }

    @Override
    public void didExitRegion(Region region) {
        Log.i(TAG, "Exit Region");
        long[] pattern = {0, 100, 500, 100, 500, 100};
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(pattern, -1);
/*
        try {
            beaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
*/
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {
        Log.i(TAG, "Determine=" + i + " " + region.getUniqueId());
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
        Log.i(TAG, "Beacons In Region");
    }

}