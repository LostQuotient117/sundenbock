import { TRANSLOCO_CONFIG, translocoConfig, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { EnvironmentProviders, makeEnvironmentProviders } from '@angular/core';
import { TranslocoHttpLoader } from './transloco-loader';

export function provideTransloco(): EnvironmentProviders {
  return makeEnvironmentProviders([
    {
      provide: TRANSLOCO_CONFIG,
      useValue: translocoConfig({
        availableLangs: ['en', 'de'],
        defaultLang: 'en',
        reRenderOnLangChange: true,
        prodMode: false // auf true in prod
      })
    },
    { provide: TRANSLOCO_LOADER, useClass: TranslocoHttpLoader }
  ]);
}
