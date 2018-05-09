
    /* Class to represent and individual transaction */

    package mortimer.l.moneymanager;

    public class Transaction
    {
        // Transaction data
        private String transactionName;
        private String transactionPrice;
        private String transactionDate;

        Transaction()
        {
            // Blank constructor, used by Firebase for retrieving objects
        }

        Transaction( String transactionName, String transactionPrice, String date )
        {
            this.transactionName = transactionName;
            this.transactionPrice = transactionPrice;
            this.transactionDate = date;
        }

        // Name get and setters
        public String getTransactionName() {
            return transactionName;
        }

        public void setTransactionName(String transactionName) {
            this.transactionName = transactionName;
        }

        // Date get and setters
        public String getTransactionDate() {
            return transactionDate;
        }

        public void setTransactionDate(String transactionDate) {
            this.transactionDate = transactionDate;
        }

        // Price get and setters
        public String getTransactionPrice() {
            return transactionPrice;
        }

        public void setTransactionPrice(String transactionPrice) {
            this.transactionPrice = transactionPrice;
        }
    }
