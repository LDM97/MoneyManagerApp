
    /* Class to represent a user, and any budgets they have on their account */

    package mortimer.l.moneymanager;

    import java.math.BigDecimal;
    import java.math.RoundingMode;
    import java.util.LinkedList;
    import java.util.List;

    public class User
    {
        // Store the user data
        private String userId;
        private String name;
        private String accountBalance;
        private List<Budget> budgets;
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

        // Account balance get and setters
        public String getAccountBalance()
        {
            return accountBalance;
        }

        public void setAccountBalance( String accountBalance )
        {
            this.accountBalance = accountBalance;
        }

        // Get and set methods for the budgets
        public List<Budget> getBudgets() {
            // If no budgets made yet (is null) then return empty list.
            return ( budgets == null ) ? new LinkedList<Budget>() : this.budgets;
        }

        public void setBudgets(List<Budget> budgets) {
            this.budgets = budgets;
        }

        // Get and set methods for the name
        public String getName() {

            return name;
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
