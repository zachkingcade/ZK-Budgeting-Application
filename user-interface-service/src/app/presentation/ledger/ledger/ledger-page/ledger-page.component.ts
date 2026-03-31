import { Component } from '@angular/core';
import { PageCage } from "../../../page-cage/page-cage.component";
import { LedgerTable } from "../ledger-table/ledger-table.component";
import { LedgerSortAndFilterBar } from "../ledger-sort-and-filter-bar/ledger-sort-and-filter-bar.component";

@Component({
  selector: 'app-ledger-page',
  imports: [PageCage, LedgerTable, LedgerSortAndFilterBar],
  templateUrl: './ledger-page.component.html',
  styleUrl: './ledger-page.component.scss',
})
export class LedgerPage {

}
