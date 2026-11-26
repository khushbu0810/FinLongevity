import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  imports: [],
  standalone: true,
  selector: 'app-placeholder',
  styleUrl: './placeholder.css',
  templateUrl: './placeholder.html',
})
export class Placeholder {
  private readonly route = inject(ActivatedRoute);
  get title(): string { return this.route.snapshot.data['title'] as string || 'Workspace'; }
  get description(): string { return this.route.snapshot.data['description'] as string || ''; }
}