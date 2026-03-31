import { Component } from '@angular/core';
import { PageCage } from "../../../page-cage/page-cage";
import { LedgerTable } from "../ledger-table/ledger-table";
import { LedgerSortAndFilterBar } from "../ledger-sort-and-filter-bar/ledger-sort-and-filter-bar";

@Component({
  selector: 'app-ledger-page',
  imports: [PageCage, LedgerTable, LedgerSortAndFilterBar],
  templateUrl: './ledger-page.html',
  styleUrl: './ledger-page.scss',
})
export class LedgerPage {

}
