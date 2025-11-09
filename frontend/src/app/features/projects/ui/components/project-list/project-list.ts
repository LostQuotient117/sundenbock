import { Component, Input } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Project } from '@features/projects/domain/project.model';

@Component({
  standalone: true,
  selector: 'project-list',
  imports: [CommonModule, RouterLink, DatePipe],
  templateUrl: './project-list.html',
})
export class ProjectListComponent {
  @Input({ required: true }) items: Project[] = [];
}
