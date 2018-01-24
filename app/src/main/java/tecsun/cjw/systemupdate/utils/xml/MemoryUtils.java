package tecsun.cjw.systemupdate.utils.xml;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MemoryUtils {

	public static String getTotalMemory(Context context) {
		String str1 = "/proc/meminfo";
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;

		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			for (String num : arrayOfString) {  
                Log.i("cd", num + "\t");  
            } 			
			initial_memory = Integer.valueOf(arrayOfString[1]) ; //系统总内存，单位KB
			localBufferedReader.close();				
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		int mem_g = (int)initial_memory/(1024*1024)+1;
		return mem_g+"G";
	}
	
//	public static String getTotalMemory(Context context) {
//		String str1 = "/proc/meminfo";
//		String str2;
//		String[] arrayOfString;
//		long initial_memory = 0;
//
//		try {
//			FileReader localFileReader = new FileReader(str1);
//			BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
//			str2 = localBufferedReader.readLine();
//			arrayOfString = str2.split("\\s+");
//			for (String num : arrayOfString) {  
//                Log.i("cd", num + "\t");  
//            } 			
//			initial_memory = Integer.valueOf(arrayOfString[1]) * 1024; //系统总内存，单位KB，乘以1024转换为byte
//			localBufferedReader.close();				
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return Formatter.formatFileSize(context, initial_memory);
//	}
}
