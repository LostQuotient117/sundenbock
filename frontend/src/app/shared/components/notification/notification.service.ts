import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  message = signal<string | null>(null);

   show(msg: string, durationMs = 3000) {
    this.message.set(msg);
    setTimeout(() => this.message.set(null), durationMs);
  }
}