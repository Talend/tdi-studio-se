package org.talend.designer.components.hashfile.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import org.talend.designer.components.hashfile.memory.AdvancedMemoryHashFile;

public class MapHashFile {
	//use this map instead of globalMap
	private Map<String, AdvancedMemoryHashFile> resourceMap = Collections.synchronizedMap(new HashMap<String, AdvancedMemoryHashFile>());
	//keep the present key of AdvancedMemoryHashFile as key and the previous key as value
	private Map<String, String> keyMap = Collections.synchronizedMap(new HashMap<String, String>());
	//singleton
	private static final MapHashFile mhf = new MapHashFile();

    public static TalendMultiThreadLockMap resourceLockMap = new TalendMultiThreadLockMap();

    public static class TalendMultiThreadLockMap {

        private Map<Object, Object> tMultiTheadLockMap = new HashMap<Object, Object>();

        public synchronized Object get(Object key) {
            if (tMultiTheadLockMap.get(key) == null) {
                tMultiTheadLockMap.put(key, new Object());
            }
            return tMultiTheadLockMap.get(key);
        }
        
        public synchronized void remove(Object key){
            tMultiTheadLockMap.remove(key);
        }
    }
	
	private MapHashFile() {
	}

	public static MapHashFile getMapHashFile() {
		return mhf;
	}
	
	//get the linked AdvancedMemoryHashFile
	public AdvancedMemoryHashFile getAdvancedMemoryHashFile(String key) {
		AdvancedMemoryHashFile amhf = resourceMap.get(key);
		String prekey = keyMap.get(key);
		//if present AdvancedMemoryHashFile is null get the AdvancedMemoryHashFile before present.
		int size = keyMap.size();
		while(amhf==null && (size--)>0){
			amhf = resourceMap.get(prekey);
			prekey = keyMap.get(prekey);
		}
		return amhf;
	}

	public Map<String, AdvancedMemoryHashFile> getResourceMap() {
		return resourceMap;
	}

	public Map<String, String> getKeyMap() {
		return keyMap;
	}
	public void clearCache(String key){
		clearChildCache(getRootCache(key));
	}
	public void clearChildCache(String root){
		Set<String> set = keyMap.keySet();
		synchronized(keyMap) {
			Iterator<String> it = set.iterator();
			while(it.hasNext()){
				String key = it.next();
				if(keyMap.get(key).equals(root)){
					this.resourceMap.remove(key);
					clearChildCache(key);
				}
			}
		}
		this.resourceMap.remove(root);
	}
	
	public String getRootCache(String cache){
		String root;
		while((root = keyMap.get(cache))!=null){
			cache=root;
		}
		return cache;
	}
}