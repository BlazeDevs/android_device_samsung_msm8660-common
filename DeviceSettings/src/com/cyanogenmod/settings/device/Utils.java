/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.settings.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.DataOutputStream;
import java.lang.InterruptedException;
import java.lang.Process;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Utils {

    private static final String DEVICE_SETTINGS_TAG = "D-SETTINGS";

    /**
     * Write a string value to the specified file.
     * @param filename      The filename
     * @param value         The value
     */
    public static void writeValue(String filename, String value) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(filename));
            fos.write(value.getBytes());
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            try {
                String[] cmds = {"echo \"" + value + "\" > \"" + filename + "\""};
                runAsRoot(cmds, false, false);
            } catch (Exception e1) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Read a string value from the specified file.
     * @param filename        The filename
     */
    public static String readValue(String filename) {
        try {
            InputStream in = new FileInputStream(filename);
            InputStreamReader instr = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(instr);
            String line = null;
            while ((line = reader.readLine()) != null) {
                return line.replace("\n", "");
            }
        } catch (FileNotFoundException e) {
            try {
                String[] cmds = {"cat \"" + filename + "\""};
                String[] val = runAsRoot(cmds, false, true);
                return Arrays.toString(val);
            } catch (Exception e1) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return "";
    }

    /**
     * Write the "color value" to the specified file. The value is scaled from
     * an integer to an unsigned integer by multiplying by 2.
     * @param filename      The filename
     * @param value         The value of max value Integer.MAX
     */
    public static void writeColor(String filename, int value) {
        writeValue(filename, String.valueOf((long) value * 2));
    }
    
    /**
     * Check if screencast mirroring is supported.
     *
     */
    public static boolean mirroringIsSupported() {
        File submixFile = new File(DisplaySettings.SUBMIX_FILE);
        
        if (submixFile.exists()){
            return true;
        }
        
        return false;
    }
    
    /**
     * Initialize GSFDB for screencast.
     */
    public static void initializeGSFDB() {
        boolean gsfMirroringEnabledExists = false;
        boolean gsfRemoteDisplayEnabledExists = false;
        String[] cmds = {"sqlite3 " + DisplaySettings.GSF_DB_FILE + " \"SELECT count(name) FROM " + DisplaySettings.GSF_OVERRIDES_TABLE + " WHERE name='" + DisplaySettings.GSF_MIRRORING_ENABLED + "';\"", "sqlite3 " + DisplaySettings.GSF_DB_FILE + " \"SELECT count(name) FROM " + DisplaySettings.GSF_OVERRIDES_TABLE + " WHERE name='" + DisplaySettings.GSF_REMOTE_DISPLAY_ENABLED + "';\""};
        String[] results = new String[cmds.length];
        
        try {
            results = runAsRoot(cmds, true, true);
        } catch (Exception e) {
        }
        
        gsfMirroringEnabledExists = results[0].equals("1") ? true : false;
        gsfRemoteDisplayEnabledExists = results[1].equals("1") ? true : false;
            
        if (!gsfMirroringEnabledExists) {
            cmds[0] = "sqlite3 " + DisplaySettings.GSF_DB_FILE + " \"INSERT INTO " + DisplaySettings.GSF_OVERRIDES_TABLE +" (name, value) VALUES ('" + DisplaySettings.GSF_MIRRORING_ENABLED + "', 'false');\"";
        }
        else {
            cmds[0] = "";
        }
        
        if (!gsfRemoteDisplayEnabledExists) {
            cmds[1] = "sqlite3 " + DisplaySettings.GSF_DB_FILE + " \"INSERT INTO " + DisplaySettings.GSF_OVERRIDES_TABLE +" (name, value) VALUES ('" + DisplaySettings.GSF_REMOTE_DISPLAY_ENABLED + "', 'false');\"";
        }
        else {
            cmds[1] = "";
        }
        
        if (!gsfMirroringEnabledExists || !gsfRemoteDisplayEnabledExists) {
            try {
                runAsRoot(cmds, true, false);
            } catch (Exception e) {
            }
        }
     }
     
     public static String[] runAsRoot(String[] cmds, boolean terminateApps) {
        String[] result = new String[1];
        result[0] = null;
        try {
            return runAsRoot(cmds, terminateApps, false);
        } catch (Exception e) {
        }
        return result;
     }
     
     public static String[] runAsRoot(String[] cmds, boolean terminateApps,
        boolean readVal) throws Exception {
         Process process = Runtime.getRuntime().exec("su");
         DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
         InputStream inputStream = process.getInputStream();
         String[] results = new String[cmds.length];
         
         int i = 0;
         for (String tmpCmd : cmds) {
             String result = "";
                 
             
             if (!tmpCmd.equals("")) {
                 outputStream.writeBytes(tmpCmd+"\n");
                 
                 if (readVal) {
                     int bytesRead = 0;
                     byte[] buffer = new byte[4096];
                     
                     while( inputStream.available() <= 0) {
                         try { Thread.sleep(500); } catch(Exception ex) {}
                     }

                     while( inputStream.available() > 0) {
                        bytesRead = inputStream.read(buffer);
                        if ( bytesRead <= 0 ) {
                            break;
                        }
                        else {
                            String retVal = new String(buffer,0,bytesRead);   
                            result = retVal.replace(" ", "").replace("\n", "");
                        }
                     }
                     
                     results[i] = result;
                 }
             }
             i++;
         }
             
         if (terminateApps) {
            outputStream.writeBytes("am force-stop " + DisplaySettings.GSF_PACKAGE + "\n");
            outputStream.writeBytes("am force-stop " + DisplaySettings.GMS_PACKAGE + "\n");
            outputStream.writeBytes("am force-stop " + DisplaySettings.CHROMECAST_PACKAGE + "\n");
         }
         
         outputStream.writeBytes("exit\n");
         outputStream.flush();
         process.waitFor();
         
         return results;
     }
     
     /**
      * Check if screencast override is enabled.
      */
     public static boolean overrideEnabled(String name) {
        String[] cmds = {"sqlite3 " + DisplaySettings.GSF_DB_FILE + " \"SELECT value FROM " + DisplaySettings.GSF_OVERRIDES_TABLE + " WHERE name='" + name + "';\""};
        String[] results = new String[1];
        
        try {
            results = runAsRoot(cmds, false, true);
        } catch (Exception e) {
        }
        
        return results[0].equals("true") ? true : false;
     }
     
     /**
     * Set screencast override value.
     */
     public static boolean setOverride(String name, boolean enabled) {
        String[] cmds = {"sqlite3 " + DisplaySettings.GSF_DB_FILE + " \"UPDATE " + DisplaySettings.GSF_OVERRIDES_TABLE + " SET value='" + Boolean.toString(enabled) + "' WHERE name='" + name + "';\""};
        String[] results = new String[cmds.length];
        
        try  {
            results = runAsRoot(cmds, true, false);
        } catch (Exception e) {
        }
        
        return true;
     }

    /**
     * Check if the specified file exists.
     * @param filename      The filename
     * @return              Whether the file exists or not
     */
    public static boolean fileExists(String filename) {
        return new File(filename).exists();
    }


    public static void showDialog(Context ctx, String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int which) {
              alertDialog.dismiss();
           }
        });
        alertDialog.show();
    }
    
    public static void showToast(Context ctx, String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
    }
    
    public static String[] getAvailableFrequencies() {
        String[] mAvailableFrequencies = new String[0];

        String availableFrequenciesLine = readValue(DisplaySettings.FILE_CPU_FREQS);
        if (availableFrequenciesLine != null) {
            mAvailableFrequencies = availableFrequenciesLine.split(" ");
            Arrays.sort(mAvailableFrequencies, new Comparator<String>() {
                @Override
                public int compare(String object1, String object2) {
                    return Integer.valueOf(object1).compareTo(Integer.valueOf(object2));
                }
            });
        }
        
        return mAvailableFrequencies;
    }
    
    public static int CPUFreqToIndex(int cpuFreq) {
        String[] mAvailableFrequencies = getAvailableFrequencies();
        
        return Arrays.asList(mAvailableFrequencies).indexOf(Integer.toString(cpuFreq));
    }
    
    public static int IndexToCPUFreq(int index) {
        String[] mAvailableFrequencies = getAvailableFrequencies();
        
        return Integer.valueOf(mAvailableFrequencies[index]);
    }
}
