import { defineMapper, mapBySpec } from '@shared/utils/mapping/mapping.dsl';
import { ProjectDto } from '../data/project.dto';
import { Project } from './project.model';

const projectSpec = defineMapper<ProjectDto, Project>()({
  id:              { kind: 'keep' },
  title:           { kind: 'keep' },
  description:     { kind: 'keep' },
  abbreviation:    { kind: 'keep' },

  // Strings -> Date
  createdDate:     { kind: 'map', map: (s: string) => new Date(s) },
  lastModifiedDate:{ kind: 'map', map: (s: string) => new Date(s) },

  // nested: Struktur ist identisch (reine Typüberführung)
  createdBy:       { kind: 'keep' },
  lastModifiedBy:  { kind: 'keep' },
} as const);

export function mapProject(dto: ProjectDto): Project {
  return mapBySpec(dto, projectSpec);
}
