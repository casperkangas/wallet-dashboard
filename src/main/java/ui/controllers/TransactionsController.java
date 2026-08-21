package ui.controllers;

import database.ConnectionFactory;
import database.DatabaseConfiguration;
import database.repositories.AccountRepository;
import database.repositories.CategoryRepository;
import database.repositories.TransactionRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Account;
import models.Category;
import models.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionsController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbAccount;
    @FXML private ComboBox<String> cmbType;
    @FXML private TableView<TransactionViewModel> tblTransactions;
    @FXML private TableColumn<TransactionViewModel, LocalDate> colDate;
    @FXML private TableColumn<TransactionViewModel, String> colAccount;
    @FXML private TableColumn<TransactionViewModel, String> colCategory;
    @FXML private TableColumn<TransactionViewModel, String> colDescription;
    @FXML private TableColumn<TransactionViewModel, String> colAmount;

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    private ObservableList<TransactionViewModel> masterData = FXCollections.observableArrayList();

    public TransactionsController() {
        ConnectionFactory cf = new ConnectionFactory(new DatabaseConfiguration());
        this.transactionRepository = new TransactionRepository(cf);
        this.accountRepository = new AccountRepository(cf);
        this.categoryRepository = new CategoryRepository(cf);
    }

    @FXML
    public void initialize() {
        cmbType.setItems(FXCollections.observableArrayList("All Types", "Income", "Expense"));
        cmbType.getSelectionModel().selectFirst();
        
        loadData();
    }

    @FXML
    public void loadData() {
        new Thread(() -> {
            try {
                List<Transaction> transactions = transactionRepository.findAll();
                List<Account> accounts = accountRepository.findAll();
                List<Category> categories = categoryRepository.findAll();

                Map<String, String> accountMap = accounts.stream().collect(Collectors.toMap(Account::id, Account::name));
                Map<String, String> categoryMap = categories.stream().collect(Collectors.toMap(Category::id, Category::name));

                List<TransactionViewModel> viewModels = transactions.stream()
                        .map(t -> new TransactionViewModel(
                                t,
                                accountMap.getOrDefault(t.accountId(), "Unknown Account"),
                                categoryMap.getOrDefault(t.categoryId(), "Unknown Category")
                        ))
                        .sorted((t1, t2) -> {
                            if (t1.getTransactionDate() == null) return 1;
                            if (t2.getTransactionDate() == null) return -1;
                            return t2.getTransactionDate().compareTo(t1.getTransactionDate());
                        })
                        .collect(Collectors.toList());

                Platform.runLater(() -> {
                    masterData.setAll(viewModels);
                    
                    List<String> accountNames = accounts.stream().map(Account::name).collect(Collectors.toList());
                    accountNames.add(0, "All Accounts");
                    cmbAccount.setItems(FXCollections.observableArrayList(accountNames));
                    cmbAccount.getSelectionModel().selectFirst();
                    
                    setupFiltersAndSorting();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupFiltersAndSorting() {
        FilteredList<TransactionViewModel> filteredData = new FilteredList<>(masterData, p -> true);

        // Add listeners
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> updateFilter(filteredData));
        cmbAccount.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter(filteredData));
        cmbType.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter(filteredData));

        SortedList<TransactionViewModel> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tblTransactions.comparatorProperty());
        tblTransactions.setItems(sortedData);
    }

    private void updateFilter(FilteredList<TransactionViewModel> filteredData) {
        filteredData.setPredicate(tvm -> {
            // Search text
            String search = txtSearch.getText();
            if (search != null && !search.isEmpty()) {
                String lowerCaseFilter = search.toLowerCase();
                boolean matchesDescription = tvm.getDescription() != null && tvm.getDescription().toLowerCase().contains(lowerCaseFilter);
                boolean matchesCategory = tvm.getCategoryName() != null && tvm.getCategoryName().toLowerCase().contains(lowerCaseFilter);
                if (!matchesDescription && !matchesCategory) {
                    return false;
                }
            }
            
            // Account filter
            String account = cmbAccount.getValue();
            if (account != null && !"All Accounts".equals(account)) {
                if (!account.equals(tvm.getAccountName())) {
                    return false;
                }
            }
            
            // Type filter
            String type = cmbType.getValue();
            if (type != null && !"All Types".equals(type)) {
                if ("Income".equals(type) && tvm.getTransaction().amount().signum() <= 0) {
                    return false;
                }
                if ("Expense".equals(type) && tvm.getTransaction().amount().signum() > 0) {
                    return false;
                }
            }

            return true;
        });
    }

    // Inner class for TableView binding
    public static class TransactionViewModel {
        private final Transaction transaction;
        private final String accountName;
        private final String categoryName;

        public TransactionViewModel(Transaction transaction, String accountName, String categoryName) {
            this.transaction = transaction;
            this.accountName = accountName;
            this.categoryName = categoryName;
        }

        public LocalDate getTransactionDate() { return transaction.transactionDate(); }
        public String getAccountId() { return accountName; }
        public String getAccountName() { return accountName; }
        public String getCategoryId() { return categoryName; }
        public String getCategoryName() { return categoryName; }
        public String getDescription() { return transaction.description(); }
        public String getAmount() { 
            return java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US).format(transaction.amount());
        }
        public Transaction getTransaction() { return transaction; }
    }
}
