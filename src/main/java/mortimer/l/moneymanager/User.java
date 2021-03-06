
    /* Class to represent a user, and any budgets they have on their account */

    package mortimer.l.moneymanager;

    import java.math.BigDecimal;
    import java.math.RoundingMode;
    import java.util.Collections;
    import java.util.HashMap;
    import java.util.LinkedList;
    import java.util.List;
    import java.util.Map;

    public class User
    {
        // Store the user data
        private String userId;
        private String name;
        private String accountBalance;
        private String monthlyBudget;
        List<Transaction> transactions;

        User()
        {
            // Blank constructor, used by Firebase for retrieving objects
        }

        User( String userId, String name )
        {
            this.userId = userId;
            this.name = name;
            // Initialise account with no money
            this.accountBalance = "0.00";
        }

        // Transaction get and setters
        public List<Transaction> getTransactions() {
            return transactions;
        }

        public void setTransactions(List<Transaction> transactions) {
            this.transactions = transactions;
        }

        public void addTransaction( Transaction transaction )
        { // Add a new transaction
            if( this.transactions == null )
            {
                // Create new list if first transaction on this account
                this.transactions = new LinkedList<>();
                transactions.add( transaction );

            } else { // Else add transaction to the existing list
                this.transactions.add( transaction );
            }

            // Edit the account balance accordingly
            BigDecimal transactionPrice = new BigDecimal( transaction.getTransactionPrice() ).setScale( 2, RoundingMode.HALF_EVEN );
            BigDecimal accountBalance = new BigDecimal( this.accountBalance ).setScale( 2, RoundingMode.HALF_EVEN );

            accountBalance = accountBalance.add( transactionPrice );
            this.accountBalance = accountBalance.setScale( 2, RoundingMode.HALF_EVEN ).toPlainString();

        }

        public List<Transaction> getSortedTransactions()
        { // Sort the transactions so that they are stored in chronological date order

            if( transactions == null || transactions.isEmpty() )
            { // If no transactions present, don't attempt to sort just return empty list
                return new LinkedList<>();
            }

            LinkedList<String> dates = new LinkedList<>();
            Map<String,Transaction> dateToObj = new HashMap<>();

            for( Transaction t : transactions )
            {
                dates.add( t.getTransactionDate() );
                dateToObj.put( t.getTransactionDate(), t );
            }

            // Sort by date
            Collections.sort( dates, new StringDateComparator() );

            // Store the transactions in date order to be returned
            List<Transaction> sortedTransactions = new LinkedList<>();
            for( String date : dates )
            {
                sortedTransactions.add( dateToObj.get( date ) );
            }

            // Return the now sorted transactions
            return sortedTransactions;
        }

        // Account balance get and setters
        public String getAccountBalance()
        {
            return accountBalance;
        }

        public void setAccountBalance( String accountBalance )
        {
            this.accountBalance = accountBalance;
        }

        // Get and set methods for the monthly budgets
        public String getMonthlyBudget() {
            return monthlyBudget;
        }

        public void setMonthlyBudget(String monthlyBudget) {
            this.monthlyBudget = monthlyBudget;
        }

        public void setName(String name) {
            this.name = name;
        }

        // Get and set methods for the userId
        public String getUserId() {

            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
