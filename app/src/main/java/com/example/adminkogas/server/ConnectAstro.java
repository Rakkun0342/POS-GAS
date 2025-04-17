package com.example.adminkogas.server;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectAstro {
    public Connection connection(Context context) {
        ApplicationInfo ai = null;
        try {
            ai = context.getApplicationContext().getPackageManager()
                    .getApplicationInfo(context.getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        Bundle metaData = ai.metaData;
        String ip = metaData.getString("sqlIpAstro");
        String user = metaData.getString("sqlUserAstro");
        String db = metaData.getString("sqlDbAstro");
        String pass = metaData.getString("sqlPassAstro");
        StrictMode.ThreadPolicy p = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(p);
        Connection con = null;
        String url = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            url = "jdbc:jtds:sqlserver://" + ip + ";" + "databaseName=" + db + ";" + "user=" + user + ";" + "password=" + pass + ";";
            con = DriverManager.getConnection(url);
        } catch (SQLException a){
            Log.e("Error SQL:", a.getMessage());
        } catch (Exception e) {
            Log.e("Error :", e.getMessage());
        }
        return con;
    }
}
