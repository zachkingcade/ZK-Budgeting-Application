import { HelpPageContent } from '../models/help-content.model';
import hp_accounting_periods_closing from './hp/accounting-periods-closing.json';
import hp_budget_planning from './hp/budget-planning.json';
import hp_chart_of_accounts from './hp/chart-of-accounts.json';
import hp_daily_workflow from './hp/daily-workflow.json';
import hp_getting_started from './hp/getting-started.json';
import hp_journal_entries_double_entry from './hp/journal-entries-double-entry.json';
import hp_notifications from './hp/notifications.json';
import hp_pending_import_workflow from './hp/pending-import-workflow.json';
import hp_replayable_templates from './hp/replayable-templates.json';
import hp_reports from './hp/reports.json';
import hp_settings_defaults from './hp/settings-defaults.json';
import hp_system_account_types from './hp/system-account-types.json';

export const HP_BY_SLUG: Record<string, HelpPageContent> = {
  'accounting-periods-closing': hp_accounting_periods_closing as HelpPageContent,
  'budget-planning': hp_budget_planning as HelpPageContent,
  'chart-of-accounts': hp_chart_of_accounts as HelpPageContent,
  'daily-workflow': hp_daily_workflow as HelpPageContent,
  'getting-started': hp_getting_started as HelpPageContent,
  'journal-entries-double-entry': hp_journal_entries_double_entry as HelpPageContent,
  'notifications': hp_notifications as HelpPageContent,
  'pending-import-workflow': hp_pending_import_workflow as HelpPageContent,
  'replayable-templates': hp_replayable_templates as HelpPageContent,
  'reports': hp_reports as HelpPageContent,
  'settings-defaults': hp_settings_defaults as HelpPageContent,
  'system-account-types': hp_system_account_types as HelpPageContent,
};
