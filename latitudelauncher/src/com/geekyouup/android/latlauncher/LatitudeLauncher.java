package com.geekyouup.android.latlauncher;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class LatitudeLauncher extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        	//sort out the DB values
        	fixDB();
        	
            //and launch GMaps
            launchMaps();

    }
    
    private void launchMaps()
    {
        Intent i = new Intent("android.intent.action.VIEW",Uri.parse("geo:0,0?z=2"));
        startActivity(i);
        finish();
    }
    
    private void fixDB()
    {
    	
    	Cursor c = null;
    	boolean keyExists = false;
    	boolean valueCorrect = false;
    	Uri gServicesUri= Uri.parse("content://settings/gservices");
    	ContentResolver cr = getContentResolver();
    	String rowId = null;
        try
        {
        	c = cr.query(gServicesUri, null, null,null, null);
        	if(c.moveToFirst())
        	{
                do {
              	  try
              	  {
	                 if("maps_enable_friend_finder".equals(c.getString(1)))
	                 {
	                	 keyExists = true;
	                	 if("1".equals(c.getString(2)))
	                	 {
	                		 valueCorrect=true;
	                	 }else
	                	 {
	                		 rowId = c.getString(0);
	                	 }
                		 break;
	                 }
              	  }catch(Exception e)
              	  {}
                } while (c.moveToNext());
        	}
        }catch(Exception e)
        {
        	e.printStackTrace();
        }finally{
        	if(c != null) c.close();
        }
        	
        try
        {
        	//now have to write to the DB to add the value
        	if(keyExists && valueCorrect)
        	{
        		//do nothing as value in DB is correct
        	}else
        	{
        		fixDB(rowId);
        	}
        	
        }catch(Exception e)
        {
        	e.printStackTrace();
        }
    }
    
    private void fixDB(String rowId) throws Exception {
        Process process = Runtime.getRuntime().exec("su");
        DataInputStream is = new DataInputStream(process.getInputStream());
        DataOutputStream os = new DataOutputStream(process.getOutputStream());
        os.writeBytes("cd /data/data/com.android.providers.settings/databases\n");
       // os.writeBytes("echo .dump gservices | sqlite3 settings.db\n");
        if(rowId != null) os.writeBytes("echo \"DELETE FROM gservices WHERE _id="+rowId + ";\" | sqlite3 settings.db\n");
        os.writeBytes("echo \"INSERT INTO gservices (name,value) VALUES ('maps_enable_friend_finder','1');\" | sqlite3 settings.db\n");
        os.writeBytes("exit\n");
        os.flush();
        os.close();
        
        if(is != null && is.available()>0)
        {
        	while(is.available()>0)
        	{
	        	byte[] allBytes = new byte[is.available()];
	        	is.read(allBytes);
	        	Log.d("OUTPUT: ",new String(allBytes));
        	}
        	is.close();
        }
        
        process.waitFor();
    }  
    
    public static boolean runRootCommand(String command) {
        Process process = null;
        DataOutputStream os = null;
            try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command+"\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            } catch (Exception e) {
                    Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "+e.getMessage());
                    return false;
            }
            finally {
                    try {
                            if (os != null) {
                                    os.close();
                            }
                            process.destroy();
                    } catch (Exception e) {
                            // nothing
                    }
            }
            return true;
    }

}