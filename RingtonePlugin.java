package com.riemankojo.mp3dop;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

@CapacitorPlugin(name = "Ringtone")
public class RingtonePlugin extends Plugin {

    @PluginMethod
    public void allFiles(PluginCall call) {
        boolean ok = Build.VERSION.SDK_INT < 30 || Environment.isExternalStorageManager();
        if (!ok) {
            Intent i = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:" + getContext().getPackageName()));
            getActivity().startActivity(i);
        }
        JSObject r = new JSObject();
        r.put("granted", ok);
        call.resolve(r);
    }

    @PluginMethod
    public void setRingtone(PluginCall call) {
        String path = call.getString("path");
        if (path == null) { call.reject("No file"); return; }
        if (Build.VERSION.SDK_INT < 29) { call.reject("Needs Android 10 or newer"); return; }
        if (!Settings.System.canWrite(getContext())) {
            Intent i = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:" + getContext().getPackageName()));
            getActivity().startActivity(i);
            call.reject("permission");
            return;
        }
        File src = path.startsWith("/") ? new File(path)
            : new File(Environment.getExternalStorageDirectory(), path);
        if (!src.exists()) { call.reject("File not found"); return; }
        try {
            String name = src.getName();
            String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toLowerCase() : "mp3";
            String mime = "audio/mpeg";
            if (ext.equals("wav")) mime = "audio/x-wav";
            else if (ext.equals("m4a")) mime = "audio/mp4";
            else if (ext.equals("aac")) mime = "audio/aac";
            else if (ext.equals("ogg") || ext.equals("opus")) mime = "audio/ogg";
            else if (ext.equals("flac")) mime = "audio/flac";
            ContentValues v = new ContentValues();
            v.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            v.put(MediaStore.MediaColumns.TITLE, name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name);
            v.put(MediaStore.MediaColumns.MIME_TYPE, mime);
            v.put(MediaStore.MediaColumns.RELATIVE_PATH, "Ringtones/");
            v.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            v.put(MediaStore.Audio.Media.IS_MUSIC, false);
            ContentResolver cr = getContext().getContentResolver();
            Uri uri = cr.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, v);
            if (uri == null) { call.reject("Could not create ringtone"); return; }
            InputStream in = new FileInputStream(src);
            OutputStream out = cr.openOutputStream(uri);
            byte[] buf = new byte[65536];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            out.close();
            in.close();
            RingtoneManager.setActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_RINGTONE, uri);
            call.resolve();
        } catch (Exception e) {
            call.reject(e.getMessage() == null ? "Failed" : e.getMessage());
        }
    }
}