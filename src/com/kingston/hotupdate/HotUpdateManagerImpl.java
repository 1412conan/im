package com.kingston.hotupdate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class HotUpdateManagerImpl {

	DynamicClassLoader dc =null; 

	Long lastModified = 0l; 
	Class c = null; 
	//�����࣬ ������ļ��޸Ĺ����أ����û���޸ģ����ص�ǰ�� 
	public Class loadClass(String name) throws ClassNotFoundException, IOException{ 
		if (isClassModified(name)){ 
			dc =  new DynamicClassLoader(); 
			return c = dc.findClass(getBytes(name)); 
		} 
		return c; 
	} 
	
	//�ж��Ƿ��޸Ĺ� 
	private boolean isClassModified(String filename) { 
		boolean returnValue = false; 
		File file = new File(filename); 
		if (file.lastModified() > lastModified) { 
			returnValue = true; 
		} 
		return returnValue; 
	} 
	
	// �ӱ��ض�ȡ�ļ� 
	private byte[] getBytes(String filename) throws IOException { 
		File file = new File(filename); 
		long len = file.length(); 
		lastModified = file.lastModified(); 
		byte raw[] = new byte[(int) len]; 
		FileInputStream fin = new FileInputStream(file); 
		try{
			int r = fin.read(raw); 
			if (r  != len) { 
				throw new IOException("Can't read all, " + r + " != " + len); 
			} 
		}finally{
			fin.close(); 
		}
		return raw; 
	} 
}
