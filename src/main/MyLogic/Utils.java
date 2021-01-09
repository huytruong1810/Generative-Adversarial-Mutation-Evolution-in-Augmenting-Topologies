package MyLogic;

public class Utils {

    /**
     * Checks if the inputted string only contains alphabetical character
     *
     * @param s the string to be checked
     * @return true if it is and false otherwise
     */
    public static boolean isAlpha (String s) {

        int n = s.length();
        for (int i = 0; i < n; ++i) {
            if (s.charAt(i) == '~') // not symbol can be accepted as an alphabetical symbol in logic
                continue;
            if (!Character.isLetter(s.charAt(i)))
                return false;
        }
        return true;

    }

    /**
     * Checks if the inputted string is parenthesized properly
     * Ex: ()(()) and empty string are acceptable but )( or ( are not
     *
     * @param s the string to be checked
     * @return true if the input string is properly parenthesized, false otherwise
     */
    public static boolean isBalanced (String s) {

        int count = 0, n = s.length();
        for (int i = 0; i < n; ++i) {

            char c = s.charAt(i);
            if (c == '(')
                count++;
            else if (c == ')') {
                // there should always be at least 1 '(' to have a ')'
                if (count < 1)
                    return false;
                count--;
            }

        }
        return count == 0;

    }

}
