
    /* Class to represent and individual transaction */

    package mortimer.l.moneymanager;

    public class Transaction
    {
        // Transaction data
        String transactionName;
        Integer transactionPrice;
        boolean expenditure; // Is this an expenditure or income?

        Transaction()
        {
            // Blank constructor, used by Firebase for retrieving objects
        }

        Transaction( String transactionName, int transactionPrice, boolean expenditure )
        {
            this.transactionName = transactionName;
            this.transactionPrice = transactionPrice;
            this.expenditure = expenditure;
        }

    }
