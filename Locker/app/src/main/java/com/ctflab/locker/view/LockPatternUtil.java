package com.ctflab.locker.view;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class LockPatternUtil {
	public static final String TAG = "LockPatternUtil";
	public static final int MIN_LOCK_PATTERN_SIZE = 4;
    /**
     * The maximum number of incorrect attempts before the user is prevented
     * from trying again for {@link #FAILED_ATTEMPT_TIMEOUT_MS}.
     */
    public static final int FAILED_ATTEMPTS_BEFORE_TIMEOUT = 5;
    
    /**
     * The minimum number of dots the user must include in a wrong pattern
     * attempt for it to be counted against the counts that affect
     * {@link #FAILED_ATTEMPTS_BEFORE_TIMEOUT}
     */
    public static final int MIN_PATTERN_REGISTER_FAIL = 3;
    
    /**
     * The interval of the countdown for showing progress of the lockout.
     */
    public static final long FAILED_ATTEMPT_COUNTDOWN_INTERVAL_MS = 1000L;
    
    /**
     * How long the user is prevented from trying again after entering the
     * wrong pattern too many times.
     */
    public static final long FAILED_ATTEMPT_TIMEOUT_MS = 30000L;
    
	/**
	 * Deserialize a pattern.
	 * 
	 * @param string
	 *            The pattern serialized with {@link #patternToString}
	 * @return The pattern.
	 */
	public static List<LockPatternView.Cell> stringToPattern(String string) {
		List<LockPatternView.Cell> result = new ArrayList<LockPatternView.Cell>();

		final byte[] bytes = string.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			result.add(LockPatternView.Cell.of(b / 3, b % 3));
		}
		return result;
	}

	/**
	 * Serialize a pattern.
	 * 
	 * @param pattern
	 *            The pattern.
	 * @return The pattern in string form.
	 */
	public static String patternToString(List<LockPatternView.Cell> pattern) {
		if (pattern == null) {
			return "";
		}
		final int patternSize = pattern.size();

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < patternSize; i++) {
			LockPatternView.Cell cell = pattern.get(i);
			builder.append(String.valueOf((byte) (cell.getRow() * 3 + cell.getColumn() + 1)));
		}

		return builder.toString();
	}

	/*
	 * Generate an SHA-1 hash for the pattern. Not the most secure, but it is at
	 * least a second level of protection. First level is that the file is in a
	 * location only readable by the system process.
	 * 
	 * @param pattern the gesture pattern.
	 * 
	 * @return the hash of the pattern in a byte array.
	 */
	public static byte[] patternToHash(List<LockPatternView.Cell> pattern) {
		// if (pattern == null) {
		// return null;
		// }
		//
		// final int patternSize = pattern.size();
		// byte[] res = new byte[patternSize];
		// for (int i = 0; i < patternSize; i++) {
		// LockPatternView.Cell cell = pattern.get(i);
		// res[i] = (byte) (cell.getRow() * 3 + cell.getColumn());
		// }
		// try {
		// MessageDigest md = MessageDigest.getInstance("SHA-1");
		// byte[] hash = md.digest(res);
		// return hash;
		// } catch (NoSuchAlgorithmException nsa) {
		// return res;
		// }
		return null;
	}
	
//    /**
//     * Set and store the lockout deadline, meaning the user can't attempt his/her unlock
//     * pattern until the deadline has passed.
//     * @return the chosen deadline.
//     */
//    public long setLockoutAttemptDeadline() {
//        final long deadline = SystemClock.elapsedRealtime() + FAILED_ATTEMPT_TIMEOUT_MS;
//        setLong(LOCKOUT_ATTEMPT_DEADLINE, deadline);
//        return deadline;
//    }
//
//    /**
//     * @return The elapsed time in millis in the future when the user is allowed to
//     *   attempt to enter his/her lock pattern, or 0 if the user is welcome to
//     *   enter a pattern.
//     */
//    public long getLockoutAttemptDeadline() {
//        final long deadline = getLong(LOCKOUT_ATTEMPT_DEADLINE, 0L);
//        final long now = SystemClock.elapsedRealtime();
//        if (deadline < now || deadline > (now + FAILED_ATTEMPT_TIMEOUT_MS)) {
//            return 0L;
//        }
//        return deadline;
//    }
	
	public static boolean comparePatterns(List<LockPatternView.Cell> pattern1,
			List<LockPatternView.Cell> pattern2) {
		return comparePatterns(patternToString(pattern1),
				patternToString(pattern2));
	}

	public static boolean comparePatterns(String pattern1, String pattern2) {
		//Log.v(TAG, pattern1 + ", " + pattern2);
		if (null == pattern1 || null == pattern2) return false;
		
		int length1 = pattern1.length();
		int length2 = pattern2.length();
		if (length1 != length2 || length1 <= 0
				|| length2 <= 0)
			return false;

		if (length1 == 1)
			return pattern1.compareTo(pattern2) == 0;
		
		MinPath[] paths1 = stringToPaths(pattern1);
		MinPath[] paths2 = stringToPaths(pattern2);
		
		for (int i = 0; i < paths1.length; i++) {
			MinPath path = paths1[i];
			boolean bFound = false;
			for (int j = 0; j < paths2.length; j++) {
				if (path.equals(paths2[j])) {
					bFound = true;
					break;
				}
			}
			if (!bFound)
				return false;
		}

		return true;
	}

	private static MinPath[] stringToPaths(String pattern) {
		int length = pattern.length();
		if (length <= 1)
			return null;
		MinPath[] paths = new MinPath[length - 1];
		int i = 0;
		while (i < length) {
			char b = pattern.charAt(i);
			if (++i < length) {
				char e = pattern.charAt(i);
				paths[i - 1] = new MinPath((byte) b, (byte) e);
			}
		}
		return paths;
	}

	private static class MinPath {
		private byte b;
		private byte e;

		MinPath(byte b, byte e) {
			this.b = b;
			this.e = e;
		}

		@Override
		public boolean equals(Object obj) {
			MinPath path = (MinPath) obj;
			if(path!=null){				
				if ((path.b == this.b && path.e == this.e)
						|| (path.b == this.e && path.e == this.b)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public LockPatternUtil(Context context){
		
	}
}
