package org.sweetinsanity.a2dpmetadatafix;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Main implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!(lpparam.packageName.equals("com.spotify.mobile.android.ui") || lpparam.packageName.equals("tunein.player")))
            return;

        if (lpparam.packageName.equals("com.spotify.mobile.android.ui")) {
            findAndHookMethod("com.spotify.mobile.android.ui.widget.SpotifyWidget", lpparam.classLoader, "onReceive", Context.class, Intent.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Context context = (Context) param.args[0];
                    Intent intent = (Intent) param.args[1];

                    Intent avrcp = new Intent("com.android.music.metachanged");
                    avrcp.putExtra("track", intent.getStringExtra("track_name"));
                    avrcp.putExtra("artist", intent.getStringExtra("artist_name"));
                    avrcp.putExtra("album", intent.getStringExtra("album_name"));

                    context.sendBroadcast(avrcp);
                }
            });
        } else {
            findAndHookMethod("tunein.library.common.k", lpparam.classLoader, "c", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String track  = (String)param.thisObject.getClass().getDeclaredField("j").get(param.thisObject);
                    String artist = (String)param.thisObject.getClass().getDeclaredField("k").get(param.thisObject);
                    String album  = (String)param.thisObject.getClass().getDeclaredField("g").get(param.thisObject);

                    Application tunein = (Application)param.thisObject.getClass().getDeclaredField("b").get(param.thisObject);

                    Intent avrcp = new Intent("com.android.music.metachanged");
                    avrcp.putExtra("track", track);
                    avrcp.putExtra("artist", artist);
                    avrcp.putExtra("album", album);

                    tunein.getApplicationContext().sendBroadcast(avrcp);
                }
            });
        }
    }
}
