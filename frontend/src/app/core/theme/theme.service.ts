import { Injectable, effect, signal } from '@angular/core';

type Theme = 'light' | 'dark';
const STORAGE_KEY = 'theme';

function getSystemPref(): Theme {
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly theme = signal<Theme>((localStorage.getItem(STORAGE_KEY) as Theme) || getSystemPref());

  constructor() {
    // wendet Theme + Favicon global an
    effect(() => {
      const t = this.theme();
      const root = document.documentElement;

      root.classList.toggle('dark', t === 'dark');  // Tailwind dark classes
      root.setAttribute('data-theme', t);           // DaisyUI Theme Hook
      localStorage.setItem(STORAGE_KEY, t);

      // Favicon synchronisieren
      const icon = document.getElementById('app-favicon') as HTMLLinkElement | null;
      if (icon) icon.href = t === 'dark' ? '/favicon-dark.ico' : '/favicon-light.ico';
    });

    // Auf OS-Theme-Livewechsel reagieren — aber nur, wenn User nicht manuell überschrieben hat
    const mql = window.matchMedia('(prefers-color-scheme: dark)');
    const onChange = (e: MediaQueryListEvent) => {
      if (!localStorage.getItem(STORAGE_KEY)) {
        this.theme.set(e.matches ? 'dark' : 'light');
      }
    };
    mql.addEventListener('change', onChange);
  }

  toggle() { this.theme.update(t => t === 'dark' ? 'light' : 'dark'); }
  set(t: Theme) { this.theme.set(t); }
  isDark() { return this.theme() === 'dark'; }
}
