import { Component, Input } from '@angular/core';
import { HelpBlock } from '../../../help/models/help-content.model';

@Component({
  selector: 'app-help-body',
  standalone: true,
  templateUrl: './help-body.component.html',
  styleUrl: './help-body.component.scss',
})
export class HelpBodyComponent {
  @Input({ required: true }) paragraphs!: HelpBlock[];
}
