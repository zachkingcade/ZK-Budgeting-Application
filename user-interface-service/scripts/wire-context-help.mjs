import fs from 'fs';
import path from 'path';

const base = path.resolve('src/app');

function relImport(tsPath) {
  const fromDir = path.dirname(tsPath);
  const target = path.join(base, 'help/components/context-help-button/context-help-button.component.ts');
  let rel = path.relative(fromDir, target).replace(/\\/g, '/').replace(/\.ts$/, '');
  if (!rel.startsWith('.')) rel = './' + rel;
  return `import { ContextHelpButtonComponent } from '${rel}';`;
}

function addImport(tsPath) {
  let t = fs.readFileSync(tsPath, 'utf8');
  if (t.includes('ContextHelpButtonComponent')) return;
  const imp = relImport(tsPath);
  const idx = t.indexOf('@Component');
  t = t.slice(0, idx) + imp + '\n' + t.slice(idx);
  t = t.replace(/imports: \[\n/, 'imports: [\n    ContextHelpButtonComponent,\n');
  fs.writeFileSync(tsPath, t);
}

function patchHtml(htmlPath, find, replace) {
  let h = fs.readFileSync(htmlPath, 'utf8');
  if (h.includes(replace.slice(0, 30)) || (replace.includes('helpId') && h.includes(replace.match(/helpId="[^"]+"/)?.[0] || '___'))) return false;
  if (!h.includes(find)) {
    console.warn('MISSING', htmlPath, JSON.stringify(find));
    return false;
  }
  fs.writeFileSync(htmlPath, h.replace(find, replace));
  return true;
}

const WIRE = [
  ['presentation/ledger/ledger/ledger-sort-and-filter-bar/ledger-sort-and-filter-bar.component.ts', 'presentation/ledger/ledger/ledger-sort-and-filter-bar/ledger-sort-and-filter-bar.component.html', '<div class="filterAndSortBar">', '<motion.div class="filterAndSortBar help-toolbar-row"><app-context-help-button helpId="cp-ledger-filters" /></motion.div>\n<div class="filterAndSortBar">'],
  ['presentation/ledger/pending/pending-journal-entries-page/pending-journal-entries-page.component.ts', 'presentation/ledger/pending/pending-journal-entries-page/pending-journal-entries-page.component.html', '  <div class="cageContents">', '  <div class="cageContents">\n    <div class="help-toolbar-row"><app-context-help-button helpId="cp-pending-page" /></div>'],
  ['presentation/ledger/pending/pending-import-bar/pending-import-bar.component.ts', 'presentation/ledger/pending/pending-import-bar/pending-import-bar.component.html', '<div class="importBar">', '<div class="importBar help-toolbar-row"><app-context-help-button helpId="cp-pending-import" />'],
  ['presentation/ledger/pending/pending-journal-entries-page/pending-journal-entries-page.component.ts', 'presentation/ledger/pending/pending-journal-entries-page/pending-journal-entries-page.component.html', '    <app-pending-transactions-table', '    <div class="help-toolbar-row"><app-context-help-button helpId="cp-pending-table" /></div>\n    <app-pending-transactions-table'],
  ['presentation/ledger/pending/pending-journal-entries-page/pending-journal-entries-page.component.ts', 'presentation/ledger/pending/pending-journal-entries-page/pending-journal-entries-page.component.html', '    <div class="applyBar">', '    <div class="applyBar help-toolbar-row"><app-context-help-button helpId="cp-pending-apply" />'],
  ['presentation/ledger/accounting-periods-page/accounting-periods-page.component.ts', 'presentation/ledger/accounting-periods-page/accounting-periods-page.component.html', '<div class="accountingPeriodsPage">', '<div class="accountingPeriodsPage">\n    <div class="help-toolbar-row"><app-context-help-button helpId="cp-accounting-periods-page" /></div>'],
  ['presentation/ledger/accounts/accounts-page/accounts-page.component.ts', 'presentation/ledger/accounts/accounts-page/accounts-page.component.html', '  <motion.div class="cageContents">', '  <div class="cageContents">\n    <div class="help-toolbar-row"><app-context-help-button helpId="cp-accounts-page" /></div>'],
  ['presentation/ledger/accounts/accounts-sort-and-filter-bar/accounts-sort-and-filter-bar.component.ts', 'presentation/ledger/accounts/accounts-sort-and-filter-bar/accounts-sort-and-filter-bar.component.html', '<div class="filterAndSortBar">', '<div class="filterAndSortBar help-toolbar-row"><app-context-help-button helpId="cp-accounts-filters" />'],
  ['presentation/ledger/accounts/modals/add-account-modal/add-account-modal.component.ts', 'presentation/ledger/accounts/modals/add-account-modal/add-account-modal.component.html', '<h2 class="modalTitle">Add account</h2>', '<h2 class="modalTitle help-modal-title">Add account <app-context-help-button helpId="cp-add-account" /></h2>'],
  ['presentation/ledger/accounts/modals/edit-account-modal/edit-account-modal.component.ts', 'presentation/ledger/accounts/modals/edit-account-modal/edit-account-modal.component.html', '<h2 class="modalTitle">Edit account</h2>', '<h2 class="modalTitle help-modal-title">Edit account <app-context-help-button helpId="cp-edit-account" /></h2>'],
  ['presentation/ledger/accounts/account-types-page/account-types-page.component.ts', 'presentation/ledger/accounts/account-types-page/account-types-page.component.html', '  <div class="cageContents">', '  <div class="cageContents">\n    <div class="help-toolbar-row"><app-context-help-button helpId="cp-account-types-page" /></motion.div>'],
  ['presentation/ledger/accounts/account-types-sort-and-filter-bar/account-types-sort-and-filter-bar.component.ts', 'presentation/ledger/accounts/account-types-sort-and-filter-bar/account-types-sort-and-filter-bar.component.html', '<div class="filterAndSortBar">', '<div class="filterAndSortBar help-toolbar-row"><app-context-help-button helpId="cp-account-types-filters" />'],
  ['presentation/ledger/accounts/modals/add-account-type-modal/add-account-type-modal.component.ts', 'presentation/ledger/accounts/modals/add-account-type-modal/add-account-type-modal.component.html', '<h2 class="modalTitle">Add account type</h2>', '<h2 class="modalTitle help-modal-title">Add account type <app-context-help-button helpId="cp-add-account-type" /></h2>'],
  ['presentation/ledger/accounts/modals/edit-account-type-modal/edit-account-type-modal.component.ts', 'presentation/ledger/accounts/modals/edit-account-type-modal/edit-account-type-modal.component.html', '<h2 class="modalTitle">Edit account type</h2>', '<h2 class="modalTitle help-modal-title">Edit account type <app-context-help-button helpId="cp-edit-account-type" /></h2>'],
  ['presentation/planning/replayable-journal-entries-page/replayable-journal-entries-page.component.ts', 'presentation/planning/replayable-journal-entries-page/replayable-journal-entries-page.component.html', '<div class="rje__toolbar">', '<div class="rje__toolbar help-toolbar-row"><app-context-help-button helpId="cp-replayable-page" />'],
  ['presentation/reports/reports-page/reports-page.component.ts', 'presentation/reports/reports-page/reports-page.component.html', '<div class="reports-toolbar">', '<div class="reports-toolbar help-toolbar-row"><app-context-help-button helpId="cp-reports-page" />'],
  ['presentation/user-account/settings-page/settings-page.component.ts', 'presentation/user-account/settings-page/settings-page.component.html', '<div class="settingsPage">', '<div class="settingsPage">\n    <div class="help-toolbar-row"><app-context-help-button helpId="cp-settings-page" /></div>'],
  ['presentation/user-account/notifications-page/notifications-page.component.ts', 'presentation/user-account/notifications-page/notifications-page.component.html', '<div class="notifications-page">', '<div class="notifications-page">\n    <div class="help-toolbar-row"><app-context-help-button helpId="cp-notifications-page" /></div>'],
];

let n = 0;
for (const [tsRel, htmlRel, find, replace] of WIRE) {
  const ts = path.join('src/app', tsRel);
  const html = path.join('src/app', htmlRel);
  const r = replace.replace(/<\/?motion\.div/g, (m) => m.replace('motion.', ''));
  const f = find;
  addImport(ts);
  if (patchHtml(html, f, r)) n++;
}
console.log('patched', n);
