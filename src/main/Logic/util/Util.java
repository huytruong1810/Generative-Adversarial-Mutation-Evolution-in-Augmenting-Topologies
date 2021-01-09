package Logic.util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ravi Mohan
 * @author Ruediger Lunde
 * 
 */
public class Util {
	
	/**
	 * Get the first element from a list.
	 * 
	 * @param l
	 *            the list the first element is to be extracted from.
	 * @return the first element of the passed in list.
	 */
	public static <T> T first(List<T> l) {
		return l.get(0);
	}

	/**
	 * Get a sublist of all of the elements in the list except for first.
	 * 
	 * @param l
	 *            the list the rest of the elements are to be extracted from.
	 * @return a list of all of the elements in the passed in list except for
	 *         the first element.
	 */
	public static <T> List<T> rest(List<T> l) {
		return l.subList(1, l.size());
	}

	/**
	 * Create a Map<K, V> with the passed in keys having their values
	 * initialized to the passed in value.
	 * 
	 * @param keys
	 *            the keys for the newly constructed map.
	 * @param value
	 *            the value to be associated with each of the maps keys.
	 * @return a map with the passed in keys initialized to value.
	 */
	public static <K, V> Map<K, V> create(Collection<K> keys, V value) {
		Map<K, V> map = new LinkedHashMap<>();

		for (K k : keys) {
			map.put(k, value);
		}

		return map;
	}
	
	/**
	 * Create a set for the provided values.
	 * @param values
	 *        the sets initial values.
	 * @return a Set of the provided values.
	 */
	@SafeVarargs
	public static <V> Set<V> createSet(V... values) {
		Set<V> set = new LinkedHashSet<>();
		Collections.addAll(set, values);
		return set;
	}

}