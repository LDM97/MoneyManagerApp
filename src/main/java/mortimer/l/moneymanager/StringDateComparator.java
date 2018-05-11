    package mortimer.l.moneymanager;

    // Java imports
    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.Comparator;

    // Used to sort the dates of the events
    class StringDateComparator implements Comparator<String>
    {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        public int compare(String lhs, String rhs)
        {
            try
            { // Compare the dates given
                return dateFormat.parse(lhs).compareTo(dateFormat.parse(rhs));
            } catch (ParseException e)
            {
                e.printStackTrace();
            }
            // Default return on failure
            return 0;
        }

    }
