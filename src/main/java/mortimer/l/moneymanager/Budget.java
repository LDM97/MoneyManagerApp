
    /* Class to represent a budget */

    package mortimer.l.moneymanager;

    import java.util.List;

    public class Budget
    {
        // Budget data
        String budgetName;
        Integer budgetMax;
        List<Transaction> transactions; // List of any transactions which have been made in this budget

        Budget()
        {
            // Blank constructor, used by Firebase for retrieving objects
        }

        Budget( String budgetName, int budgetMax )
        {
            this.budgetName = budgetName;
            this.budgetMax = new Integer( budgetMax );
        }
    }
