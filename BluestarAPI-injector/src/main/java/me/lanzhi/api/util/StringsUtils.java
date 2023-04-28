package me.lanzhi.api.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StringsUtils {
    public static List<String> toStrings(Object o) {
        if (o instanceof Collection) {
            List<String> list = new ArrayList<>();
            for (var obj : (Collection<?>) o) {
                list.addAll(toStrings(obj));
            }
            return list;
        } else if (o != null && o.getClass().isArray()) {
            List<String> list = new ArrayList<>();
            for (int i = 0; i < Array.getLength(o); i++) {
                list.addAll(toStrings(Array.get(o, i)));
            }
            return list;
        } else if (o != null) {
            var list = new ArrayList<String>();
            list.add(o.toString());
            return list;
        } else {
            return new ArrayList<>();
        }
    }
}