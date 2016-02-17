package com.ctflab.locker.utils;

/**
 * Created by wuwei on 2016/1/27.
 */
public class CharacterCompareUtil {

    /**
     * 比较两个字符
     *
     * @param one
     * @param two
     * @return
     */
    public static int singleSort(String one, String two) {
        int[] left = stringToAscii(one);
        int[] right = stringToAscii(two);
        int size = left.length < right.length ? left.length : right.length;
        for (int i = 0; i < size; i++) {
            // 大于10000说明是汉字 并且在判断一下是否相等 不相等在判断 减少判断次数
            if (left[i] > 10000 && right[i] > 10000 && left[i] != right[i]) {
                if (chineseCompare(one, two, i) != 0) {
                    return chineseCompare(one, two, i);
                }
            } else {
                if (intCompare(left[i], right[i]) != 0) {
                    return intCompare(left[i], right[i]);
                }
            }
        }
        return intCompare(left.length, right.length);
    }

    /**
     * 汉字比较
     *
     * @param one
     * @param two
     * @param i
     * @return
     */
    private static int chineseCompare(String one, String two, int i) {
        try {
            String substringleft;
            String substringright;
            if (i > 0) {
                substringleft = one.substring(i - 1, i);
                substringright = two.substring(i - 1, i);
            } else {
                substringleft = one.substring(0, i);
                substringright = two.substring(0, i);
            }
            // 获得汉字拼音首字母的ASCII码
            // 把他里面的CharacterParser.convert方法 改成 public static 不然会报错
            int subLeft = stringToAscii(CharacterParser.convert(substringleft)
                    .substring(0, 1))[0];
            int subRight = stringToAscii(CharacterParser.convert(substringright)
                    .substring(0, 1))[0];
            System.out.println(CharacterParser.convert(substringleft).substring(0,
                    1));
            return intCompare(subLeft, subRight);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 数字比较
     *
     * @param subLeft
     * @param subRight
     * @return
     */
    private static int intCompare(int subLeft, int subRight) {
        if (subLeft > subRight) {
            return 1;
        } else if (subLeft < subRight) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * 获得ASCII码
     *
     * @param value
     * @return
     */
    public static int[] stringToAscii(String value) {
        char[] chars = value.toCharArray();
        int j = chars.length;
        int[] array = new int[j];
        for (int i = 0; i < chars.length; i++) {
            array[i] = (int) chars[i];
        }
        return array;
    }
}

