import { Component, inject } from '@angular/core';
import { NotificationService } from './notification.service';

@Component({
  selector: 'app-notifications',
  imports: [],
  templateUrl: './notification.html',
  styleUrl: './notification.css'
})
export class NotificationComponent {
  notify = inject(NotificationService);
}