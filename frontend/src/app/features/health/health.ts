import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HealthService } from '../../core/services/health.service';


@Component({
  selector: 'app-health',
  imports: [CommonModule],
  templateUrl: './health.html',
  styleUrl: './health.css'
})
export class Health {
   status = signal<'UP' | 'DOWN'>('DOWN');
  loading = signal(false);

  constructor(private health: HealthService) {
    this.refresh();
  }

  refresh() {
    this.loading.set(true);
    this.health.ping$().subscribe({
      next: s => this.status.set(s),
      error: () => this.status.set('DOWN'),
      complete: () => this.loading.set(false)
    });
  }
}
