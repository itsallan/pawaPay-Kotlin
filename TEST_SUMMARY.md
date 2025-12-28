# PawaPay Kotlin SDK - Comprehensive Test Suite

## Quick Start

```bash
# Run all tests
./gradlew test

# Run tests for shared module only
./gradlew :shared:test

# Run with verbose output
./gradlew test --info
```

## Test Suite Overview

✅ **94 comprehensive unit tests** covering all SDK functionality  
✅ **1,960+ lines** of test code  
✅ **5 test files** organized by component  
✅ **Zero external dependencies** - all tests use mocks  
✅ **Fast execution** - no network calls or I/O

## Test Coverage

### Core Components

| Component | Test File | Tests | Coverage |
|-----------|-----------|-------|----------|
| DTOs & Serialization | `SharedCommonTest.kt` | 38 | Complete |
| Repository Logic | `PawaPayRepositoryImplTest.kt` | 31 | Complete |
| Network API | `PawaPayApiTest.kt` | 13 | Complete |
| Configuration | `PawaPayConfigTest.kt` | 3 | Complete |
| Domain Models | `TransactionTypeTest.kt` | 9 | Complete |

### Feature Coverage

✅ **Deposits (Collections)**
- Request creation and validation
- Response parsing
- Success and failure scenarios
- Multiple mobile money providers
- UUID generation

✅ **Payouts (Disbursements)**
- Payout initiation
- Recipient handling
- Multiple currencies
- Error handling

✅ **Refunds**
- Full and partial refunds
- Automatic ID generation
- Custom refund IDs
- Failure scenarios

✅ **Transaction Status**
- Status queries for all transaction types
- Polling with timeout
- Status transition handling
- NOT_FOUND scenarios
- Failure reason parsing

✅ **Edge Cases**
- Empty values
- Large amounts (999999999999)
- Decimal amounts (1000.50)
- Special characters (@#$%^&*)
- Unicode text (支付 оплата)
- Concurrent operations
- Network failures

## Test Structure