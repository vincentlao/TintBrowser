package org.tint.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.tint.R;

import android.content.Context;
import android.os.Environment;

public class IOUtils {
	
	/**
	 * Get the list of xml files in the bookmark export folder.
	 * @return The list of xml files in the bookmark export folder.
	 */
	public static List<String> getExportedBookmarksFileList() {
		List<String> result = new ArrayList<String>();
		
		File folder = Environment.getExternalStorageDirectory();		
		
		if (folder != null) {
			
			FileFilter filter = new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					if ((pathname.isFile()) &&
							(pathname.getPath().endsWith(".xml"))) {
						return true;
					}
					return false;
				}
			};
			
			File[] files = folder.listFiles(filter);
			
			for (File file : files) {
				result.add(file.getName());
			}			
		}
		
		Collections.sort(result, new Comparator<String>() {

			@Override
			public int compare(String arg0, String arg1) {				
				return arg1.compareTo(arg0);
			}    		
    	});
		
		return result;
	}
	
	public static String checkCardState(Context context) {
		// Check to see if we have an SDCard
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
        	// Check to see if the SDCard is busy, same as the music app
            if (status.equals(Environment.MEDIA_SHARED)) {
                return context.getString(R.string.SDCardErrorSDUnavailable);
            } else {
                return context.getString(R.string.SDCardErrorNoSDMsg);
            }
        }
		
		return null;		
	}

}
