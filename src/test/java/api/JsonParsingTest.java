package api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JsonParsingTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    void testParseAccount() throws Exception {
        String json = """
            {
                "id": "acc-123",
                "name": "Checking Account",
                "currency": "USD",
                "balance": 1500.50,
                "institution": "Bank",
                "updatedAt": "2023-10-01T12:00:00",
                "extraField": "ignore me"
            }
            """;
        
        Account account = objectMapper.readValue(json, Account.class);
        
        assertEquals("acc-123", account.id());
        assertEquals("Checking Account", account.name());
        assertEquals("USD", account.currency());
        assertEquals(new BigDecimal("1500.50"), account.balance());
        assertEquals("Bank", account.institution());
        assertEquals(LocalDateTime.of(2023, 10, 1, 12, 0), account.updatedAt());
    }

    @Test
    void testParseBudget() throws Exception {
        String json = """
            {
                "id": "bud-1",
                "categoryId": "cat-2",
                "limitAmount": 500.00,
                "period": "MONTHLY"
            }
            """;

        Budget budget = objectMapper.readValue(json, Budget.class);

        assertEquals("bud-1", budget.id());
        assertEquals("cat-2", budget.categoryId());
        assertEquals(new BigDecimal("500.00"), budget.limitAmount());
        assertEquals("MONTHLY", budget.period());
    }

    @Test
    void testParseCategory() throws Exception {
        String json = """
            {
                "id": "cat-1",
                "name": "Groceries",
                "parentId": "cat-0",
                "icon": "shopping-cart"
            }
            """;

        Category category = objectMapper.readValue(json, Category.class);

        assertEquals("cat-1", category.id());
        assertEquals("Groceries", category.name());
        assertEquals("cat-0", category.parentId());
        assertEquals("shopping-cart", category.icon());
    }

    @Test
    void testParseDailySnapshot() throws Exception {
        String json = """
            {
                "snapshotDate": "2023-10-01",
                "netWorth": 10000.00,
                "cash": 2000.00,
                "investments": 8000.00,
                "debt": 0.00,
                "monthlyIncome": 5000.00,
                "monthlyExpenses": 3000.00
            }
            """;

        DailySnapshot snapshot = objectMapper.readValue(json, DailySnapshot.class);

        assertEquals(LocalDate.of(2023, 10, 1), snapshot.snapshotDate());
        assertEquals(new BigDecimal("10000.00"), snapshot.netWorth());
        assertEquals(new BigDecimal("2000.00"), snapshot.cash());
        assertEquals(new BigDecimal("8000.00"), snapshot.investments());
        assertEquals(new BigDecimal("0.00"), snapshot.debt());
        assertEquals(new BigDecimal("5000.00"), snapshot.monthlyIncome());
        assertEquals(new BigDecimal("3000.00"), snapshot.monthlyExpenses());
    }

    @Test
    void testParseTransaction() throws Exception {
        String json = """
            {
                "id": "txn-1",
                "accountId": "acc-1",
                "categoryId": "cat-1",
                "amount": -55.50,
                "currency": "USD",
                "transactionDate": "2023-10-01",
                "description": "Lunch",
                "paymentMethod": "CARD",
                "isTransfer": false,
                "createdAt": "2023-10-01T14:30:00"
            }
            """;

        Transaction transaction = objectMapper.readValue(json, Transaction.class);

        assertEquals("txn-1", transaction.id());
        assertEquals("acc-1", transaction.accountId());
        assertEquals("cat-1", transaction.categoryId());
        assertEquals(new BigDecimal("-55.50"), transaction.amount());
        assertEquals("USD", transaction.currency());
        assertEquals(LocalDate.of(2023, 10, 1), transaction.transactionDate());
        assertEquals("Lunch", transaction.description());
        assertEquals("CARD", transaction.paymentMethod());
        assertFalse(transaction.isTransfer());
        assertEquals(LocalDateTime.of(2023, 10, 1, 14, 30), transaction.createdAt());
    }
}
