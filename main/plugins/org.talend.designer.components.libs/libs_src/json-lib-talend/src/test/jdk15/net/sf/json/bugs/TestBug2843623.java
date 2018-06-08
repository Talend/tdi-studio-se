package net.sf.json.bugs;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

public class TestBug2843623 extends TestCase {
    public void testSample() {
        Container orig = new Container();
        List<String> strings = new ArrayList<String>();
        strings.add("one");
        strings.add("two");
        strings.add("three");
        orig.setMyList(strings);
        
        JSONObject jobj = JSONObject.fromObject(orig);
        Container root = new Container();
        Container res = (Container)JSONObject.toBean(jobj, root, new JsonConfig());
        assertTrue(res.getMyList().size() > 0);
        assertTrue(root.getMyList().size() > 0);
    }
    
    public static class Container {
        private List<String> myList = new ArrayList<String>();
        
        public List<String> getMyList() {
            return myList;
        }
        
        public void setMyList(List<String> strings) {
            this.myList.addAll(strings);
        }
        
    }
}
