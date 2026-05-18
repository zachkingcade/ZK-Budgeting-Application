package zachkingcade.dev.ledger.application.accountingperiod;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.ArchivedJournalEntryEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.ArchivedJournalLineEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.ClosedAccountingPeriodEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.JournalEntryEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.jpa.JournalLineEntity;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.ArchivedJournalEntryJpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.ClosedAccountingPeriodJpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.JournalEntryJpaRepository;
import zachkingcade.dev.ledger.adapter.out.persistence.repository.PendingTransactionJpaRepository;
import zachkingcade.dev.ledger.application.account.AccountBalanceService;
import zachkingcade.dev.ledger.application.accountingperiod.AccountingPeriodViews.ArchivedJournalEntryView;
import zachkingcade.dev.ledger.application.accountingperiod.AccountingPeriodViews.ClosedPeriodAccountBalanceView;
import zachkingcade.dev.ledger.application.accountingperiod.AccountingPeriodViews.ClosedPeriodListItem;
import zachkingcade.dev.ledger.application.accountingperiod.AccountingPeriodViews.NextToCloseView;
import zachkingcade.dev.ledger.application.exception.ApplicationException;
import zachkingcade.dev.ledger.application.exception.NotFoundException;
import zachkingcade.dev.ledger.application.port.in.accountingperiod.AccountingPeriodUseCase;
import zachkingcade.dev.ledger.application.port.out.account.AccountRepositoryPort;
import zachkingcade.dev.ledger.application.port.out.accountingperiod.ClosedPeriodAccountBalanceRepositoryPort;
import zachkingcade.dev.ledger.application.port.out.accounttype.AccountClassificationRepositoryPort;
import zachkingcade.dev.ledger.application.port.out.accounttype.AccountTypeRepositoryPort;
import zachkingcade.dev.ledger.domain.account.Account;
import zachkingcade.dev.ledger.domain.account.AccountClassification;
import zachkingcade.dev.ledger.domain.account.AccountType;
import zachkingcade.dev.ledger.domain.accountingperiod.AccountingPeriodConfig;
import zachkingcade.dev.ledger.domain.accountingperiod.ClosedPeriodAccountBalance;
import zachkingcade.dev.ledger.domain.accountingperiod.PeriodWindow;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountingPeriodService implements AccountingPeriodUseCase {

    private final ClosedAccountingPeriodJpaRepository closedPeriodRepository;
    private final ArchivedJournalEntryJpaRepository archivedJournalEntryRepository;
    private final JournalEntryJpaRepository journalEntryRepository;
    private final PendingTransactionJpaRepository pendingTransactionRepository;
    private final AccountingPeriodSettingsResolver settingsResolver;
    private final AccountingPeriodBoundsService boundsService;
    private final AccountBalanceService accountBalanceService;
    private final AccountRepositoryPort accountRepository;
    private final AccountTypeRepositoryPort accountTypeRepository;
    private final AccountClassificationRepositoryPort accountClassificationRepository;
    private final ClosedPeriodAccountBalanceRepositoryPort closedPeriodAccountBalanceRepository;

    public AccountingPeriodService(
            ClosedAccountingPeriodJpaRepository closedPeriodRepository,
            ArchivedJournalEntryJpaRepository archivedJournalEntryRepository,
            JournalEntryJpaRepository journalEntryRepository,
            PendingTransactionJpaRepository pendingTransactionRepository,
            AccountingPeriodSettingsResolver settingsResolver,
            AccountingPeriodBoundsService boundsService,
            AccountBalanceService accountBalanceService,
            AccountRepositoryPort accountRepository,
            AccountTypeRepositoryPort accountTypeRepository,
            AccountClassificationRepositoryPort accountClassificationRepository,
            ClosedPeriodAccountBalanceRepositoryPort closedPeriodAccountBalanceRepository) {
        this.closedPeriodRepository = closedPeriodRepository;
        this.archivedJournalEntryRepository = archivedJournalEntryRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.pendingTransactionRepository = pendingTransactionRepository;
        this.settingsResolver = settingsResolver;
        this.boundsService = boundsService;
        this.accountBalanceService = accountBalanceService;
        this.accountRepository = accountRepository;
        this.accountTypeRepository = accountTypeRepository;
        this.accountClassificationRepository = accountClassificationRepository;
        this.closedPeriodAccountBalanceRepository = closedPeriodAccountBalanceRepository;
    }

    @Override
    public List<ClosedPeriodListItem> listClosed(Long userId) {
        return closedPeriodRepository.findAllByUserIdOrderByEndDateDesc(userId).stream()
                .map(this::toListItem)
                .toList();
    }

    @Override
    public List<ArchivedJournalEntryView> listArchivedJournalEntries(Long userId, Long closedPeriodId) {
        closedPeriodRepository.findByClosedAccountingPeriodIdAndUserId(closedPeriodId, userId)
                .orElseThrow(() -> new NotFoundException("Closed accounting period not found"));

        return archivedJournalEntryRepository
                .findAllByClosedAccountingPeriodIdAndUserIdOrderByEntryDateAscJournalEntryIdAsc(closedPeriodId, userId)
                .stream()
                .map(e -> new ArchivedJournalEntryView(
                        e.getJournalEntryId(),
                        e.getEntryDate().toLocalDate(),
                        e.getDescription(),
                        e.getNotes()))
                .toList();
    }

    @Override
    public List<ClosedPeriodAccountBalanceView> listAccountBalancesForClosedPeriod(Long userId, Long closedPeriodId) {
        closedPeriodRepository.findByClosedAccountingPeriodIdAndUserId(closedPeriodId, userId)
                .orElseThrow(() -> new NotFoundException("Closed accounting period not found"));

        Map<Long, Long> balances =
                closedPeriodAccountBalanceRepository.findBalancesByClosedPeriodIdAndUserId(closedPeriodId, userId);
        return balances.entrySet().stream()
                .map(e -> new ClosedPeriodAccountBalanceView(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(a.accountId(), b.accountId()))
                .toList();
    }

    @Override
    public NextToCloseView getNextToClose(Long userId) {
        AccountingPeriodConfig config = settingsResolver.resolve(userId)
                .orElseThrow(() -> new ApplicationException("Accounting period settings are not configured"));
        PeriodWindow window = boundsService.oldestUnclosedPeriod(userId, config);
        LocalDate today = LocalDate.now();
        boolean closable = !window.endDate().isAfter(today);
        return new NextToCloseView(window.startDate(), window.endDate(), closable, window.endDate());
    }

    @Override
    @Transactional
    public ClosedPeriodListItem closePeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        AccountingPeriodConfig config = settingsResolver.resolve(userId)
                .orElseThrow(() -> new ApplicationException("Accounting period settings are not configured"));

        PeriodWindow expected = boundsService.oldestUnclosedPeriod(userId, config);
        if (!expected.startDate().equals(startDate) || !expected.endDate().equals(endDate)) {
            throw new ApplicationException(
                    "Accounting periods must be closed in chronological order; expected "
                            + expected.startDate() + " to " + expected.endDate());
        }

        if (endDate.isAfter(LocalDate.now())) {
            throw new ApplicationException("Accounting period cannot be closed before it has ended");
        }

        Date sqlStart = Date.valueOf(startDate);
        Date sqlEnd = Date.valueOf(endDate);

        if (pendingTransactionRepository.existsByUserIdAndTransactionDateBetween(userId, sqlStart, sqlEnd)) {
            throw new ApplicationException(
                    "This period still has pending transactions. Resolve or remove them on the Pending Journal Entries page before closing.");
        }

        List<JournalEntryEntity> entries = journalEntryRepository.findAllByUserIdAndEntryDateBetween(userId, sqlStart, sqlEnd);

        Optional<Long> priorClosedPeriodId = accountBalanceService.findPriorClosedPeriodId(userId);

        ClosedAccountingPeriodEntity period = new ClosedAccountingPeriodEntity();
        period.setUserId(userId);
        period.setStartDate(sqlStart);
        period.setEndDate(sqlEnd);
        period.setTransactionCount((long) entries.size());
        ClosedAccountingPeriodEntity savedPeriod = closedPeriodRepository.save(period);

        persistAccountBalanceSnapshots(userId, startDate, endDate, priorClosedPeriodId, savedPeriod.getClosedAccountingPeriodId());

        List<ArchivedJournalEntryEntity> archivedEntries = new ArrayList<>();
        for (JournalEntryEntity entry : entries) {
            ArchivedJournalEntryEntity archived = new ArchivedJournalEntryEntity();
            archived.setJournalEntryId(entry.getId());
            archived.setEntryDate(entry.getEntryDate());
            archived.setDescription(entry.getDescription());
            archived.setNotes(entry.getNotes());
            archived.setUserId(entry.getUserId());
            archived.setClosedAccountingPeriodId(savedPeriod.getClosedAccountingPeriodId());

            List<ArchivedJournalLineEntity> archivedLines = new ArrayList<>();
            for (JournalLineEntity line : entry.getJournalLines()) {
                ArchivedJournalLineEntity archivedLine = new ArchivedJournalLineEntity();
                archivedLine.setJournalLineId(line.getId());
                archivedLine.setJournalEntry(archived);
                archivedLine.setAmount(line.getAmount());
                archivedLine.setAccountId(line.getAccount().getId());
                archivedLine.setDirection(line.getDirection());
                archivedLine.setNotes(line.getNotes());
                archivedLines.add(archivedLine);
            }
            archived.setJournalLines(archivedLines);
            archivedEntries.add(archived);
        }

        archivedJournalEntryRepository.saveAll(archivedEntries);
        journalEntryRepository.deleteAll(entries);

        return toListItem(savedPeriod);
    }

    private void persistAccountBalanceSnapshots(
            Long userId,
            LocalDate periodStart,
            LocalDate periodEnd,
            Optional<Long> priorClosedPeriodId,
            Long newClosedPeriodId) {
        Map<Long, AccountType> typeById = accountTypeRepository.findAllVisibleToUser(userId).stream()
                .collect(Collectors.toMap(AccountType::id, t -> t));
        Map<Long, AccountClassification> classById = accountClassificationRepository.findAll().stream()
                .collect(Collectors.toMap(AccountClassification::id, c -> c));

        List<ClosedPeriodAccountBalance> snapshots = new ArrayList<>();
        for (Account account : accountRepository.findAll(userId)) {
            AccountType type = typeById.get(account.typeId());
            if (type == null) {
                throw new ApplicationException("Account type not found for account " + account.id());
            }
            AccountClassification classification = classById.get(type.classificationId());
            if (classification == null) {
                throw new ApplicationException("Classification not found for account " + account.id());
            }
            long balance = accountBalanceService.balanceThroughPeriodClose(
                    userId,
                    account.id(),
                    classification,
                    periodStart,
                    periodEnd,
                    priorClosedPeriodId);
            snapshots.add(new ClosedPeriodAccountBalance(
                    null,
                    newClosedPeriodId,
                    userId,
                    account.id(),
                    balance));
        }
        closedPeriodAccountBalanceRepository.saveAll(snapshots);
    }

    private ClosedPeriodListItem toListItem(ClosedAccountingPeriodEntity entity) {
        return new ClosedPeriodListItem(
                entity.getClosedAccountingPeriodId(),
                entity.getStartDate().toLocalDate(),
                entity.getEndDate().toLocalDate(),
                entity.getTransactionCount());
    }
}
