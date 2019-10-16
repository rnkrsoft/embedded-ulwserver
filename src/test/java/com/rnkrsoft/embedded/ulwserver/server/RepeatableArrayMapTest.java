package com.rnkrsoft.embedded.ulwserver.server;

import com.rnkrsoft.embedded.ulwserver.server.header.RepeatableArrayMap;
import com.rnkrsoft.embedded.ulwserver.server.header.RepeatableMap;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by rnkrsoft.com on 2019/10/15.
 */
public class RepeatableArrayMapTest {

    @Test
    public void testIsEmpty() throws Exception {
        RepeatableMap<String, String> map = new RepeatableArrayMap<String, String>(4);
        Assert.assertEquals(true, map.isEmpty());
        map.put("1", "1");
        Assert.assertEquals(false, map.isEmpty());
        map.put("1", "2");
        map.put("1", "3");
        map.put("1", "4");
        map.put("1", "5");
        Assert.assertEquals(false, map.isEmpty());
    }

    @Test
    public void testContainsKey() throws Exception {
        RepeatableMap<String, String> map = new RepeatableArrayMap<String, String>(4);
        Assert.assertEquals(0, map.containsKey("1"));
        map.put("1", "1");
        Assert.assertEquals(1, map.containsKey("1"));
        map.put("1", "2");
        Assert.assertEquals(2, map.containsKey("1"));
        map.put("1", "2");
        Assert.assertEquals(3, map.containsKey("1"));
    }

    @Test
    public void testContainsValue() throws Exception {
        RepeatableMap<String, String> map = new RepeatableArrayMap<String, String>(4);
        Assert.assertEquals(0, map.containsValue("1"));
        map.put("1", "1");
        Assert.assertEquals(1, map.containsValue("1"));
        Assert.assertEquals(0, map.containsValue("2"));
        map.put("1", "2");
        Assert.assertEquals(1, map.containsValue("2"));
        map.put("1", "2");
        Assert.assertEquals(2, map.containsValue("2"));
    }

    @Test
    public void testGet() throws Exception {
        RepeatableMap<String, String> map = new RepeatableArrayMap<String, String>(4);
        map.put("1", "1");
        map.put("1", "2");
        map.put("1", "3");
        map.put("2", "4");
        map.put("2", "5");
        map.put("3", "6");
        Assert.assertEquals(3, map.get("1").size());
        Assert.assertEquals(2, map.get("2").size());
        Assert.assertEquals(1, map.get("3").size());
    }

    @Test
    public void testPut() throws Exception {
        RepeatableMap<String, String> map = new RepeatableArrayMap<String, String>(4);
        map.put("1", "1");
        map.put("1", "2");
        map.put("1", "3");
        map.put("2", "4");
        map.put("2", "5");
        map.put("3", "6");
    }

    @Test
    public void testKeys() throws Exception {
        RepeatableMap<String, String> map = new RepeatableArrayMap<String, String>(4);
        map.put("1", "1");
        map.put("1", "2");
        map.put("1", "3");
        map.put("2", "4");
        map.put("2", "5");
        map.put("3", "6");
        System.out.println(map.keys());
    }

    @Test
    public void testValues() throws Exception {
        RepeatableMap<String, String> map = new RepeatableArrayMap<String, String>(4);
        map.put("1", "1");
        map.put("1", "2");
        map.put("1", "3");
        map.put("2", "4");
        map.put("2", "5");
        map.put("3", "6");
        System.out.println(map.values());
    }

    @Test
    public void testEntrySet() throws Exception {
        RepeatableMap<String, String> map = new RepeatableArrayMap<String, String>(4);
        map.put("1", "1");
        map.put("1", "2");
        map.put("1", "3");
        map.put("2", "4");
        map.put("2", "5");
        map.put("3", "6");
        System.out.println(map.entries());
    }
}