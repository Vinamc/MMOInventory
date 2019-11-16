package net.Indyuce.inventory.version;

public class ServerVersion {
	private final String version;
	private final int[] integers;

	public ServerVersion(Class<?> clazz) {
		version = clazz.getPackage().getName().replace(".", ",").split(",")[3];
		String[] split = version.substring(1).split("\\_");
		integers = new int[] { Integer.parseInt(split[0]), Integer.parseInt(split[1]) };
	}

	public boolean isBelowOrEqual(int... version) {
		return version[0] > integers[0] ? true : version[1] >= integers[1];
	}

	public boolean isStrictlyHigher(int... version) {
		return version[0] < integers[0] ? true : version[1] < integers[1];
		// return !isBelowOrEqual(version);
	}

	public int getRevisionNumber() {
		return Integer.parseInt(version.split("\\_")[2].replaceAll("[^0-9]", ""));
	}

	public int[] toNumbers() {
		return integers;
	}

	@Override
	public String toString() {
		return version;
	}
}
