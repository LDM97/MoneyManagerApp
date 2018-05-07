
    /* Class to represent a user, and any budgets they have on their account */

    package mortimer.l.moneymanager;

    import java.util.LinkedList;
    import java.util.List;

    public class User
    {
        // Store the user data
        private String userId;
        private String name;
        private List<Budget> budgets;

        User()
        {
            // Blank constructor, used by Firebase for retrieving objects
        }

        User( String userId, String name )
        {
            this.userId = userId;
            this.name = name;
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
