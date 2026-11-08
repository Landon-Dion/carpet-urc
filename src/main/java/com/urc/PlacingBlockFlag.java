package com.urc;

/** Marks the dynamic scope of a player block placement so observers can ignore it. */
public final class PlacingBlockFlag {
	private static boolean placing;

	private PlacingBlockFlag() {
	}

	public static void begin() {
		placing = true;
	}

	public static void end() {
		placing = false;
	}

	public static boolean isPlacing() {
		return placing;
	}
}
